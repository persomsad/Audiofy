#!/bin/bash
set -e

echo "=========================================="
echo "Qwen3 TTS 完整工作流测试"
echo "=========================================="
echo ""

# 配置
API_KEY="sk-724a0524ffd94522a70c6869d11e002c"
API_URL="https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation"
TEST_TEXT="你好，这是一个测试"
VOICE="cherry"
LANGUAGE="Chinese"

echo "步骤 1: 调用 Qwen3 API 生成语音..."
echo "----------------------------------------"

# 调用API
RESPONSE=$(curl -s -X POST "$API_URL" \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d "{
    \"model\": \"qwen3-tts-flash\",
    \"input\": {
      \"text\": \"$TEST_TEXT\",
      \"voice\": \"$VOICE\",
      \"language_type\": \"$LANGUAGE\"
    }
  }")

echo "API 响应:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
echo ""

# 检查是否有错误
if echo "$RESPONSE" | grep -q '"code"'; then
  echo "❌ API 调用失败!"
  echo "$RESPONSE" | jq '.message' 2>/dev/null || echo "$RESPONSE"
  exit 1
fi

# 提取音频URL
AUDIO_URL=$(echo "$RESPONSE" | jq -r '.output.audio.url')

if [ -z "$AUDIO_URL" ] || [ "$AUDIO_URL" = "null" ]; then
  echo "❌ 无法从响应中提取音频URL"
  exit 1
fi

echo "✅ API 调用成功!"
echo "音频URL: $AUDIO_URL"
echo ""

# 检查URL协议
if [[ "$AUDIO_URL" =~ ^http:// ]]; then
  echo "⚠️  警告: 音频URL使用HTTP协议 (非HTTPS)"
  echo "   这就是Android 9+阻止下载的原因!"
else
  echo "✅ 音频URL使用HTTPS协议"
fi
echo ""

echo "步骤 2: 下载音频文件..."
echo "----------------------------------------"

# 下载音频
OUTPUT_FILE="/tmp/qwen3-test-audio-$(date +%s).wav"
HTTP_CODE=$(curl -s -w "%{http_code}" -o "$OUTPUT_FILE" "$AUDIO_URL")

if [ "$HTTP_CODE" -eq 200 ]; then
  echo "✅ 音频下载成功! HTTP状态码: $HTTP_CODE"
  echo "文件保存到: $OUTPUT_FILE"
else
  echo "❌ 音频下载失败! HTTP状态码: $HTTP_CODE"
  exit 1
fi
echo ""

echo "步骤 3: 验证音频文件..."
echo "----------------------------------------"

# 检查文件大小
FILE_SIZE=$(stat -f%z "$OUTPUT_FILE" 2>/dev/null || stat -c%s "$OUTPUT_FILE" 2>/dev/null)
echo "文件大小: $FILE_SIZE 字节"

if [ "$FILE_SIZE" -lt 1000 ]; then
  echo "❌ 文件太小,可能不是有效的音频文件"
  exit 1
fi

# 检查文件类型
FILE_TYPE=$(file -b "$OUTPUT_FILE")
echo "文件类型: $FILE_TYPE"

if echo "$FILE_TYPE" | grep -iq "audio\|wav\|riff"; then
  echo "✅ 文件是有效的音频格式"
else
  echo "⚠️  警告: 文件类型可能不是音频,请手动验证"
fi
echo ""

echo "=========================================="
echo "✅ 完整工作流测试通过!"
echo "=========================================="
echo ""
echo "测试结果:"
echo "  1. API调用: ✅ 成功"
echo "  2. 音频下载: ✅ 成功 (HTTP状态码: $HTTP_CODE)"
echo "  3. 文件验证: ✅ 通过 ($FILE_SIZE 字节)"
echo ""
echo "测试文件: $OUTPUT_FILE"
echo "你可以播放这个文件验证语音质量"
echo ""

# 检查URL协议并给出Android修复建议
if [[ "$AUDIO_URL" =~ ^http:// ]]; then
  echo "=========================================="
  echo "⚠️  Android 兼容性问题"
  echo "=========================================="
  echo ""
  echo "问题: Qwen3 返回的音频URL使用HTTP协议"
  echo "影响: Android 9+ 默认阻止HTTP流量"
  echo ""
  echo "解决方案: 添加 Network Security Config"
  echo ""
  echo "需要创建文件:"
  echo "  composeApp/src/androidMain/res/xml/network_security_config.xml"
  echo ""
  echo "需要修改:"
  echo "  composeApp/src/androidMain/AndroidManifest.xml"
  echo "  (添加 android:networkSecurityConfig 属性)"
  echo ""
fi
