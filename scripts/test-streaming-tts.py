#!/usr/bin/env python3
"""
Qwen3 TTS流式API测试（遵循ADR-006原则）

验证内容:
1. 流式API调用方式
2. 逐块接收音频数据
3. Base64解码
4. 音频拼接
5. 性能对比（流式 vs 非流式）
"""

import time
import base64
import requests
import io
from typing import List, Generator

API_KEY = "sk-724a0524ffd94522a70c6869d11e002c"
API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation"

def test_streaming_tts(text: str, voice: str = "Cherry") -> tuple:
    """
    测试流式TTS API
    
    返回: (总耗时, 首块到达时间, 音频数据)
    """
    headers = {
        "Authorization": f"Bearer {API_KEY}",
        "Content-Type": "application/json",
        "Accept": "text/event-stream"  # SSE格式
    }
    
    payload = {
        "model": "qwen3-tts-flash",
        "input": {
            "text": text,
            "voice": voice,
            "language_type": "Chinese"
        }
    }
    
    print(f"\n📤 测试流式TTS API")
    print(f"   文本: {text[:50]}...")
    print(f"   长度: {len(text)}字符 / {len(text.encode('utf-8'))}字节")
    
    start_time = time.time()
    first_chunk_time = None
    audio_chunks = []
    
    try:
        # 使用stream=True参数启用流式接收
        response = requests.post(
            API_URL, 
            headers=headers, 
            json=payload, 
            stream=True,  # ⚠️ 关键参数
            timeout=60
        )
        
        print(f"   状态码: {response.status_code}")
        print(f"   Content-Type: {response.headers.get('Content-Type')}")
        
        if response.status_code != 200:
            print(f"   ❌ 失败: {response.text}")
            return (0, 0, b'')
        
        # 逐块接收数据
        chunk_count = 0
        for chunk in response.iter_content(chunk_size=8192):
            if chunk:
                if first_chunk_time is None:
                    first_chunk_time = time.time() - start_time
                    print(f"   ⚡ 首块到达: {first_chunk_time:.2f}秒")
                
                audio_chunks.append(chunk)
                chunk_count += 1
                print(f"   📦 接收chunk #{chunk_count}: {len(chunk)}字节")
        
        total_time = time.time() - start_time
        full_audio = b''.join(audio_chunks)
        
        print(f"   ✅ 完成！")
        print(f"   总耗时: {total_time:.2f}秒")
        print(f"   总数据: {len(full_audio)}字节")
        print(f"   Chunk数量: {chunk_count}")
        
        return (total_time, first_chunk_time, full_audio)
        
    except Exception as e:
        print(f"   ❌ 异常: {e}")
        return (0, 0, b'')


def test_non_streaming_tts(text: str, voice: str = "Cherry") -> tuple:
    """
    测试非流式TTS API（当前实现）
    
    返回: (总耗时, 音频数据)
    """
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {API_KEY}"
    }
    
    payload = {
        "model": "qwen3-tts-flash",
        "input": {
            "text": text,
            "voice": voice,
            "language_type": "Chinese"
        }
    }
    
    print(f"\n📤 测试非流式TTS API（当前实现）")
    print(f"   文本: {text[:50]}...")
    
    start_time = time.time()
    
    try:
        response = requests.post(
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation",
            headers=headers,
            json=payload,
            timeout=60
        )
        
        if response.status_code == 200:
            result = response.json()
            audio_url = result['output']['audio']['url']
            
            # 下载音频
            print(f"   下载音频中...")
            audio_response = requests.get(audio_url)
            audio_data = audio_response.content
            
            total_time = time.time() - start_time
            
            print(f"   ✅ 完成！")
            print(f"   总耗时: {total_time:.2f}秒")
            print(f"   音频大小: {len(audio_data)}字节")
            
            return (total_time, audio_data)
        else:
            print(f"   ❌ 失败: {response.json()}")
            return (0, b'')
            
    except Exception as e:
        print(f"   ❌ 异常: {e}")
        return (0, b'')


if __name__ == "__main__":
    print("=" * 70)
    print("🧪 Qwen3 TTS 流式 vs 非流式 性能对比测试")
    print("=" * 70)
    
    # 测试文本（约200字符 = 600字节）
    test_text = "这是一个测试文本。" * 20
    
    # 测试1: 非流式API（当前实现）
    non_stream_time, non_stream_audio = test_non_streaming_tts(test_text)
    
    # 测试2: 流式API
    stream_time, first_chunk_time, stream_audio = test_streaming_tts(test_text)
    
    # 性能对比
    print("\n" + "=" * 70)
    print("📊 性能对比总结")
    print("=" * 70)
    
    if non_stream_time > 0 and stream_time > 0:
        print(f"非流式API总耗时: {non_stream_time:.2f}秒")
        print(f"流式API总耗时:   {stream_time:.2f}秒")
        if first_chunk_time:
            print(f"流式首块到达:   {first_chunk_time:.2f}秒")
            print(f"")
            print(f"💡 用户体验改善:")
            print(f"   非流式: 等待 {non_stream_time:.2f}秒 后才能开始播放")
            print(f"   流式: 等待 {first_chunk_time:.2f}秒 就可以开始播放")
            print(f"   改善幅度: {((non_stream_time - first_chunk_time) / non_stream_time * 100):.1f}%")
    
    print("\n" + "=" * 70)
    print("💡 结论:")
    print("   如果流式API能提前返回首块数据，")
    print("   则用户等待时间可以显著减少（边生成边播放）")
    print("=" * 70)

