#!/usr/bin/env python3
"""
测试Qwen3 TTS的字节数限制
"""

import requests
import json

API_KEY = "sk-724a0524ffd94522a70c6869d11e002c"
API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation"


def test_api(text: str, description: str):
    """测试API调用"""
    
    char_count = len(text)
    byte_count = len(text.encode('utf-8'))
    
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {API_KEY}"
    }
    
    payload = {
        "model": "qwen3-tts-flash",
        "input": {
            "text": text,
            "voice": "Cherry",
            "language_type": "Chinese"
        }
    }
    
    print(f"\n{'='*70}")
    print(f"测试: {description}")
    print(f"{'='*70}")
    print(f"字符数: {char_count}")
    print(f"字节数 (UTF-8): {byte_count}")
    print(f"文本预览: {text[:30]}{'...' if len(text) > 30 else ''}")
    
    try:
        response = requests.post(API_URL, headers=headers, json=payload, timeout=30)
        
        if response.status_code == 200:
            print(f"✅ 成功！")
            return True
        else:
            error_data = response.json()
            print(f"❌ 失败: {error_data.get('message', '')}")
            return False
            
    except Exception as e:
        print(f"❌ 异常: {str(e)}")
        return False


if __name__ == "__main__":
    print("\n🔬 测试字节数vs字符数限制\n")
    
    # 测试1: 英文（1字符=1字节）
    test_api("Hello world. This is a test. " * 20, "英文文本（600字符=600字节）")
    
    # 测试2: 中文（1字符=3字节）
    test_api("你好" * 100, "中文文本（200字符=600字节）")
    
    # 测试3: 中文（1字符=3字节）
    test_api("你好" * 150, "中文文本（300字符=900字节）")
    
    # 测试4: 中文（1字符=3字节）
    test_api("测" * 200, "中文文本（200字符=600字节）")
    
    # 测试5: 中文（1字符=3字节）
    test_api("测" * 250, "中文文本（250字符=750字节）")
    
    # 测试6: 混合文本
    test_api("Hello你好" * 50, "混合文本（500字符≈1000字节）")
    
    print("\n" + "="*70)
    print("💡 结论:")
    print("   如果200字符的中文成功，说明限制是600字节而不是600字符")
    print("   如果300字符的中文失败，说明限制确实是600字节")
    print("="*70)

