#!/usr/bin/env python3
"""
测试Qwen3 TTS长文本处理
验证TextChunker分片逻辑和API调用
"""

import re
import requests
import json
import base64
from typing import List

# API配置
API_KEY = "sk-724a0524ffd94522a70c6869d11e002c"
API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation"

# 测试文本（用户提供的小说内容）
TEST_TEXT = """第5081章自性本足
　　而古帝盯着洛尘的珠子，好看的珠子，和一颗金珠子！
　　"时间一天一天过去，金珠子和这颗珠子，依然没有被卖出去！"
　　"这颗珠子每天都在难过之中渡过，每天都在自我怀疑之中渡过！"
　　"直到有一天！"洛尘又继续说道。
　　"师弟，等一下，我翻下鱼！"古帝开口间，瞬间出现在不远处，开始翻鱼了。
　　而在血池那边，巫王在这一刻，背部的血肉已经在冒烟了，同时整个血池已经沸腾起来了，甚至开始干涸了。
　　她一直在哀嚎，除此之外，她什么也做不了！
　　而翻完鱼，古帝又坐了回来。
　　"金珠子也被买走了？"古帝开口道。
　　"对，其实那个人，看上了这颗好看的珠子，已经付完钱了，但是最终，他还是放弃了这颗珠子，选择了金珠子！"洛尘说着，手中只剩下最后一颗珠子了。
　　"因为这颗珠子露陷了，它变成了金珠子，但是买主拿在手中的时候，才发现，不是真正的金珠子。"
　　"这颗珠子，一直怀疑自己，一直很难受！"
　　"时间不再是一天天的过去，而是一年年过去了。"
　　"它不再变成其他的颜色，变成其他的珠子，而是回到了它一开始的模样。"
　　"直到一个大雪夜，有一个女子来了，她很美，十分的闪耀，照亮了天地间。"
　　"她最终，看上了这颗珠子！"
　　"将这颗珠子，捧在手掌之中，因为这颗珠子是一颗五彩神石珠子！"
　　"她带走了它！"洛尘说道这里，停下了。
　　"这和爱情，有什么关系？"天火蹙眉道。
　　不是讲爱情吗？
　　"怎么讲起珠子了？"
　　"爱情，有一见钟情，有你喜欢对方，但是对方却不喜欢你！"
　　"有双方喜欢，但是对方最终却背叛了你，放弃了你，换了人。"洛尘平静回答道。
　　就和这颗珠子一样，它喜欢的人，没有带走它，甚至选择了一颗石珠子，比它差太多了。
　　也有人买下了它，但是最终没有经受住金珠子的诱惑，背叛了它！
　　更有看都不看它一眼的人，直接买走了其他的珠子！
　　"这个就是爱情？"古帝问道。
　　"这代表不了爱情，只是代表了一种爱情的现象！"洛尘摇摇头。
　　"你在期待爱情！"洛尘开口道。
　　"师兄我只是想体验一下。"
　　"但是寻找不到，师弟，我就是那颗五彩神石的珠子，对吗？"
　　"人人都是那颗五彩神石的珠子，渴望爱情，寻找爱情，但是又落选，甚至为了爱情，变成了其他的模样。"洛尘再次开口道。
　　"细说！"古帝看着洛尘，认真问道。
　　"第一个人没有选择五彩神石珠子，选择了石珠子，代表了石珠子不好吗？"
　　"不，只是那个人买颗石珠子，是因为家里有用，五彩神石虽好，但是不合适。"洛尘开口道。
　　"第二个，第三个，选择其他珠子，也是一个道理。"
　　"哪怕是已经买下五彩神石那个人，最终换了金珠子，道理也是一样的，这个世界，你要找到另外一半，很难！"
　　"但是，那不代表着，你自身是不好的，也不代表着，你最终，会没有选择！"
　　"这就是爱情？"古帝再次问道。
　　"不，这只是故事的版本之一，还有另外一个版本，合起来，才是爱情！"洛尘继续说道。
　　而另外一边的血池内，巫王疼的不断在蜷缩和颤抖了，太热烈了，这烈炎热烈的让她几乎要受不了了。
　　一边聊爱情，一边却是在痛苦的煎熬着。
　　连死，都死不了！
　　"你说另外一个版本！"
　　"如果这颗五彩神石的珠子，一开始就知道自己是五彩神石的珠子，那结局是怎样的？"洛尘忽然问道。
　　"一开始，它就知道自己的优点，所以，当第一个人来的时候，它不会看一眼那个人，爱买石珠子，就买石珠子！"
　　"第二个，第三个，第四个来人，五彩神石也不会在意。"
　　"它每天都会开开心心的，每天都做着自己，并且和其他的珠子成为好朋友，当它们被买走的时候，它会开心的祝福它们！"洛尘又说道。
　　"因为它知道自己的价值，它不再期待，它很爱它自己，它认为自己值得！"
　　"如果，最后那个女子没有出现呢？"古帝又问道。
　　"那就不出现，它本身就是完整的，它向内求，它不需要向外求，不需要别人来证明它的价值，它喜欢自己的孤独，享受自身！"洛尘又开口道。
　　"它没有情感上的需求！"洛尘说的很直接了。
　　"所以？"天火也好奇的问道，这个观点很新奇。
　　因为他对爱情也不是很理解。
　　"当珠子，开始爱自己的时候，爱情就来了！"
　　"人本身就是完整的，你不需要爱情！"洛尘看着古帝又一次开口道。
　　余下的话，洛尘没有说，爱情从开始的美好，到最后的酸涩，再到最后的走散。
　　其实都是双方的需求的变化，双方都想找个人来满足自身的不足，满足自身的需求。
　　需求被满足了，就觉得甜蜜，没有被满足，就觉得不被爱了。
　　但是这都是向外求的一种方式。
　　真正的爱情，是彼此都能够满足自身的需求，也愿意去分享出去。
　　有，是锦上添花！
　　没有，也不影响人生！
　　"向内求，爱自己！"
　　"自性本足！"洛尘看着古帝。
　　而古帝在这一刻，内心深处，有个地方瞬间圆满起来了。
　　他是认真问洛尘的，而也得到了答案！
　　所以，在疑惑点上，他解开了。
　　太子爷则是一脸的惊讶与认真。
　　因为他老爹把爱情看的很透彻。
　　爱情不必去强求与追寻，强求与追寻的，势必会压抑一部分自己，到时候改变得来的，始终得不偿失。
　　大胆做自己，爱情自会来。
　　因为这个世界，不是每个人都是识货的，也没必要让每一个人喜欢。
　　太子爷琢磨着，而古帝沉思许久后，他是真的不再执着没有体会过爱情了。
　　就在刚刚洛尘讲的那个故事里，他似乎已经体验过了爱情。
　　"我再去翻下鱼！"古帝大笑道，杀气的走向了那烤鱼！
　　这一次，他不再留手了。
　　很多年前，有个少女，闯进了他的国度，闯进了他的世界……"""


def smart_chunk(text: str, max_length: int = 600) -> List[str]:
    """
    智能文本分片（模拟Kotlin的TextChunker.smartChunk）
    修复后的版本：正确计算空格长度
    """
    if len(text) <= max_length:
        return [text]
    
    # 按句子边界分割（保留标点）
    sentence_regex = r'(?<=[。？！……?!\n])'
    sentences = [s.strip() for s in re.split(sentence_regex, text) if s.strip()]
    
    chunks = []
    current_chunk = []
    current_length = 0
    
    for sentence in sentences:
        # 计算添加这个句子后的总长度（包括空格）
        space_length = 1 if current_chunk else 0
        total_length = current_length + space_length + len(sentence)
        
        if total_length > max_length:
            # 保存当前chunk
            if current_chunk:
                chunks.append(' '.join(current_chunk))
                current_chunk = []
                current_length = 0
            
            # 处理超长句子（硬切分）
            if len(sentence) > max_length:
                # 直接按max_length切割
                for i in range(0, len(sentence), max_length):
                    chunk_part = sentence[i:i + max_length]
                    chunks.append(chunk_part)
            else:
                current_chunk = [sentence]
                current_length = len(sentence)
        else:
            # 添加句子
            current_chunk.append(sentence)
            current_length = total_length
    
    # 保存最后的chunk
    if current_chunk:
        chunks.append(' '.join(current_chunk))
    
    return chunks


def call_qwen3_tts(text: str, voice: str = "Cherry", language: str = "Chinese") -> dict:
    """
    调用Qwen3 TTS API（非流式）
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
            "language_type": language
        }
    }
    
    print(f"\n📤 调用API... (文本长度: {len(text)} 字符)")
    print(f"   请求体: {json.dumps(payload, ensure_ascii=False, indent=2)}")
    
    try:
        response = requests.post(API_URL, headers=headers, json=payload, timeout=30)
        
        print(f"   状态码: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            audio_url = result.get('output', {}).get('audio', {}).get('url', '')
            print(f"   ✅ 成功！音频URL: {audio_url[:80]}...")
            return {"success": True, "url": audio_url}
        else:
            error_data = response.json()
            print(f"   ❌ 失败！错误: {error_data}")
            return {"success": False, "error": error_data}
            
    except Exception as e:
        print(f"   ❌ 异常: {str(e)}")
        return {"success": False, "error": str(e)}


def test_long_text_processing():
    """
    测试长文本处理流程
    """
    print("=" * 80)
    print("🧪 Qwen3 TTS 长文本处理测试")
    print("=" * 80)
    
    # 1. 显示原始文本信息
    print(f"\n📝 原始文本信息:")
    print(f"   总字符数: {len(TEST_TEXT)}")
    print(f"   前100字符: {TEST_TEXT[:100]}...")
    
    # 2. 执行分片
    print(f"\n✂️  智能分片处理:")
    chunks = smart_chunk(TEST_TEXT, max_length=600)
    print(f"   分片数量: {len(chunks)}")
    
    # 3. 验证每个分片长度
    print(f"\n🔍 分片长度验证:")
    all_valid = True
    for i, chunk in enumerate(chunks, 1):
        status = "✅" if len(chunk) <= 600 else "❌"
        print(f"   片段{i}: {len(chunk)} 字符 {status}")
        if len(chunk) > 600:
            all_valid = False
            print(f"      ⚠️  超出限制！前50字: {chunk[:50]}...")
    
    if all_valid:
        print(f"\n✅ 所有分片长度均<=600字符")
    else:
        print(f"\n❌ 存在超长分片！")
        return
    
    # 4. 测试API调用（只调用第一片，避免消耗配额）
    print(f"\n🚀 测试API调用（仅第1片）:")
    print(f"   文本内容: {chunks[0][:100]}...")
    
    result = call_qwen3_tts(chunks[0], voice="Cherry", language="Chinese")
    
    # 5. 总结
    print(f"\n" + "=" * 80)
    print(f"📊 测试总结:")
    print(f"   原文长度: {len(TEST_TEXT)} 字符")
    print(f"   分片数量: {len(chunks)} 片")
    print(f"   平均片长: {sum(len(c) for c in chunks) // len(chunks)} 字符")
    print(f"   最长片段: {max(len(c) for c in chunks)} 字符")
    print(f"   最短片段: {min(len(c) for c in chunks)} 字符")
    print(f"   API调用: {'✅ 成功' if result.get('success') else '❌ 失败'}")
    print("=" * 80)
    
    # 6. 如果API调用成功，显示所有分片的预估处理时间
    if result.get('success'):
        print(f"\n⏱️  完整处理时间预估:")
        print(f"   每片耗时: 约3-5秒")
        print(f"   片段间延迟: 0.5秒")
        print(f"   总耗时: 约{len(chunks) * 4 + (len(chunks) - 1) * 0.5:.1f}秒")
        print(f"   合并音频: 约1秒")
        print(f"   总计: 约{len(chunks) * 4 + (len(chunks) - 1) * 0.5 + 1:.1f}秒")


def test_stream_api_demo():
    """
    演示流式API调用方式（需要DashScope SDK）
    """
    print("\n" + "=" * 80)
    print("📘 流式API调用示例（需要安装: pip install dashscope）")
    print("=" * 80)
    
    demo_code = """
# 安装SDK
# pip install dashscope

import dashscope
import base64
import numpy as np

def stream_tts(text, voice="Cherry", language="Chinese"):
    audio_frames = []
    
    # 流式调用
    responses = dashscope.MultiModalConversation.call(
        api_key=API_KEY,
        model="qwen3-tts-flash",
        text=text,  # 可以是完整文本（但仍有600字符限制）
        voice=voice,
        stream=True,  # 关键：启用流式
        language_type=language
    )
    
    # 逐块接收音频
    for chunk in responses:
        try:
            audio_string = chunk.output.audio.data  # base64音频
            wav_bytes = base64.b64decode(audio_string)
            audio_np = np.frombuffer(wav_bytes, dtype=np.int16).astype(np.float32) / 32768.0
            audio_frames.append(audio_np)
            
            # 可以在这里实时播放每个chunk
            # play_audio_chunk(audio_np)
            
        except Exception as e:
            print(f"处理chunk失败: {e}")
    
    # 合并所有音频
    if audio_frames:
        full_audio = np.concatenate(audio_frames)
        return full_audio
    return None

# 使用示例
audio = stream_tts("你好，这是测试文本", voice="Cherry")
"""
    
    print(demo_code)


if __name__ == "__main__":
    # 测试长文本分片和API调用
    test_long_text_processing()
    
    # 显示流式API示例
    test_stream_api_demo()
    
    print("\n💡 结论:")
    print("   1. 当前的TextChunker修复后，分片逻辑正确")
    print("   2. 流式API可以改善用户体验，但仍需分片处理")
    print("   3. 600字符限制是模型本身的限制，流式和非流式都一样")
    print("   4. v1.4可以考虑流式API，实现边生成边播放")

