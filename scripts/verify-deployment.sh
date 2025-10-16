#!/bin/bash
# Qwen3-TTS Workers 部署验证脚本
# 用法: ./verify-deployment.sh <WORKER_URL> [APP_SECRET]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 默认配置
WORKER_URL="${1:-https://audiofy-tts-proxy.luhuizhx.workers.dev}"
APP_SECRET="${2:-yY2P4lQICBEZhHbNlDhm5NCWvOVJXXRVfAG9GWylcc4=}"

echo "🚀 Qwen3-TTS Workers 部署验证"
echo "================================"
echo "Worker URL: $WORKER_URL"
echo "APP_SECRET: ${APP_SECRET:0:10}..."
echo ""

# 测试计数器
TOTAL_TESTS=0
PASSED_TESTS=0

# 测试函数
run_test() {
  local test_name="$1"
  local expected_status="$2"
  local request_body="$3"
  local auth_header="$4"

  TOTAL_TESTS=$((TOTAL_TESTS + 1))
  echo -n "[$TOTAL_TESTS] $test_name ... "

  if [ -z "$auth_header" ]; then
    auth_header="Bearer $APP_SECRET"
  fi

  response=$(curl -s -w "\n%{http_code}" -X POST "$WORKER_URL" \
    -H "Content-Type: application/json" \
    -H "Authorization: $auth_header" \
    -d "$request_body")

  http_code=$(echo "$response" | tail -n1)
  body=$(echo "$response" | sed '$d')

  if [ "$http_code" -eq "$expected_status" ]; then
    echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
    PASSED_TESTS=$((PASSED_TESTS + 1))

    # 如果是成功的测试，显示部分响应
    if [ "$http_code" -eq 200 ]; then
      audio_url=$(echo "$body" | jq -r '.audioUrl // empty' 2>/dev/null)
      duration=$(echo "$body" | jq -r '.duration // empty' 2>/dev/null)
      if [ -n "$audio_url" ] && [ -n "$duration" ]; then
        echo "    Response: audioUrl=${audio_url:0:50}..., duration=${duration}s"
      fi
    fi
    return 0
  else
    echo -e "${RED}✗ FAIL${NC} (Expected HTTP $expected_status, got $http_code)"
    echo "    Response: $body"
    return 1
  fi
}

echo "📝 开始测试..."
echo ""

# 测试1: 基本连通性测试
run_test "基本连通性（Cherry语音）" 200 \
  '{"text":"你好世界","voice":"Cherry"}' || true

# 测试2: 不同语音角色测试
run_test "Ethan 语音测试" 200 \
  '{"text":"Hello world","voice":"Ethan"}' || true

run_test "Nofish 语音测试" 200 \
  '{"text":"测试 Nofish 语音","voice":"Nofish"}' || true

# 测试3: 认证失败测试
run_test "错误的 APP_SECRET（应返回401）" 401 \
  '{"text":"测试"}' \
  "Bearer wrong-secret" || true

# 测试4: 空文本验证
run_test "空文本（应返回400）" 400 \
  '{"text":""}' || true

# 测试5: 缺少文本字段
run_test "缺少text字段（应返回400）" 400 \
  '{"voice":"Cherry"}' || true

# 测试6: 无效语音角色
run_test "无效语音角色（应返回400）" 400 \
  '{"text":"测试","voice":"InvalidVoice"}' || true

# 测试7: 长文本测试
run_test "长文本测试（~100字符）" 200 \
  '{"text":"这是一段较长的测试文本，用于验证 Qwen3-TTS API 的处理能力。我们测试语音合成服务是否能够正确处理较长的中文文本输入，并返回有效的音频下载链接。这个测试对于确保生产环境的稳定性非常重要。","voice":"Cherry"}' || true

# 测试8: 默认语音（不指定voice参数）
run_test "默认语音（不指定voice）" 200 \
  '{"text":"测试默认语音角色"}' || true

# 测试9: OPTIONS 请求（CORS预检）
TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo -n "[$TOTAL_TESTS] CORS 预检请求（OPTIONS） ... "
options_response=$(curl -s -o /dev/null -w "%{http_code}" -X OPTIONS "$WORKER_URL" \
  -H "Origin: https://example.com" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type, Authorization")

if [ "$options_response" -eq 200 ] || [ "$options_response" -eq 204 ]; then
  echo -e "${GREEN}✓ PASS${NC} (HTTP $options_response)"
  PASSED_TESTS=$((PASSED_TESTS + 1))
else
  echo -e "${RED}✗ FAIL${NC} (Expected HTTP 200/204, got $options_response)"
fi

# 总结
echo ""
echo "================================"
echo "📊 测试结果总结"
echo "================================"
echo "总测试数: $TOTAL_TESTS"
echo "通过: $PASSED_TESTS"
echo "失败: $((TOTAL_TESTS - PASSED_TESTS))"

if [ $PASSED_TESTS -eq $TOTAL_TESTS ]; then
  echo -e "${GREEN}✅ 所有测试通过！Workers 部署成功！${NC}"
  exit 0
else
  echo -e "${YELLOW}⚠️  部分测试失败，请检查日志${NC}"
  exit 1
fi
