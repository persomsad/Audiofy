#!/usr/bin/env python3
"""
简单的Qwen3 TTS API测试
验证API是否可用以及字符限制
"""

import requests
import json

API_KEY = "sk-724a0524ffd94522a70c6869d11e002c"
API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation"


def test_qwen3_tts(text: str, voice: str = "Cherry"):
    """测试Qwen3 TTS API调用"""
    
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
    
    print(f"\n{'='*60}")
    print(f"测试文本: {text[:50]}{'...' if len(text) > 50 else ''}")
    print(f"文本长度: {len(text)} 字符")
    print(f"{'='*60}")
    
    try:
        response = requests.post(API_URL, headers=headers, json=payload, timeout=30)
        
        print(f"状态码: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            audio_url = result.get('output', {}).get('audio', {}).get('url', '')
            print(f"✅ 成功！")
            print(f"音频URL: {audio_url}")
            return True
        else:
            error_data = response.json()
            print(f"❌ 失败！")
            print(f"错误代码: {error_data.get('code')}")
            print(f"错误信息: {error_data.get('message')}")
            return False
            
    except Exception as e:
        print(f"❌ 异常: {str(e)}")
        return False


if __name__ == "__main__":
    print("\n🧪 Qwen3 TTS API 测试套件\n")
    
    # 测试1: 非常短的文本（应该成功）
    print("测试1: 超短文本（10字符）")
    test_qwen3_tts("你好世界，测试。")
    
    # 测试2: 中等长度文本（应该成功）
    print("\n测试2: 中等长度文本（100字符）")
    test_qwen3_tts("这是一个测试文本。" * 10)
    
    # 测试3: 接近限制的文本（应该成功）
    print("\n测试3: 接近600字符限制（550字符）")
    test_text_550 = "这是一个测试句子。" * 55
    test_qwen3_tts(test_text_550)
    
    # 测试4: 恰好600字符（应该成功）
    print("\n测试4: 恰好600字符")
    test_text_600 = "字" * 600
    test_qwen3_tts(test_text_600)
    
    # 测试5: 超过600字符（应该失败）
    print("\n测试5: 超过600字符（650字符）- 预期失败")
    test_text_650 = "字" * 650
    test_qwen3_tts(test_text_650)
    
    print("\n" + "="*60)
    print("📊 测试完成")
    print("="*60)

