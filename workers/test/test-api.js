#!/usr/bin/env node

/**
 * Audiofy Workers API 测试脚本
 * 用于测试 Gemini 和豆包 TTS 代理的功能
 */

const COLORS = {
  green: '\x1b[32m',
  red: '\x1b[31m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  reset: '\x1b[0m',
};

// 从环境变量或命令行参数读取配置
const GEMINI_PROXY_URL = process.env.GEMINI_PROXY_URL || 'http://localhost:8787';
const TTS_PROXY_URL = process.env.TTS_PROXY_URL || 'http://localhost:8788';
const APP_SECRET = process.env.APP_SECRET || 'your-test-secret';

async function testGeminiProxy() {
  console.log(`\n${COLORS.blue}[测试 1/2] Gemini 翻译 API 代理${COLORS.reset}`);
  console.log(`URL: ${GEMINI_PROXY_URL}`);

  const testText = 'Hello world! This is a test for Audiofy translation service.';

  try {
    console.log(`\n📤 发送请求...`);
    console.log(`  原文: "${testText}"`);

    const response = await fetch(GEMINI_PROXY_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${APP_SECRET}`,
      },
      body: JSON.stringify({ text: testText }),
    });

    console.log(`  状态码: ${response.status}`);

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`API 返回错误: ${errorText}`);
    }

    const data = await response.json();
    console.log(`\n📥 响应结果:`);
    console.log(`  译文: "${data.translatedText}"`);

    if (!data.translatedText) {
      throw new Error('响应中缺少 translatedText 字段');
    }

    console.log(`\n${COLORS.green}✅ Gemini 代理测试通过${COLORS.reset}`);
    return true;
  } catch (error) {
    console.error(`\n${COLORS.red}❌ Gemini 代理测试失败${COLORS.reset}`);
    console.error(`  错误: ${error.message}`);
    return false;
  }
}

async function testTTSProxy() {
  console.log(`\n${COLORS.blue}[测试 2/2] 豆包 TTS API 代理${COLORS.reset}`);
  console.log(`URL: ${TTS_PROXY_URL}`);

  const testText = '你好世界！这是音频合成测试。';

  try {
    console.log(`\n📤 发送请求...`);
    console.log(`  文本: "${testText}"`);

    const response = await fetch(TTS_PROXY_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${APP_SECRET}`,
      },
      body: JSON.stringify({ text: testText }),
    });

    console.log(`  状态码: ${response.status}`);

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`API 返回错误: ${errorText}`);
    }

    const data = await response.json();
    console.log(`\n📥 响应结果:`);
    console.log(`  音频数据长度: ${data.audioData?.length || 0} 字节 (Base64)`);
    console.log(`  音频时长: ${data.duration} 秒`);

    if (!data.audioData || !data.duration) {
      throw new Error('响应中缺少必要字段');
    }

    console.log(`\n${COLORS.green}✅ TTS 代理测试通过${COLORS.reset}`);
    return true;
  } catch (error) {
    console.error(`\n${COLORS.red}❌ TTS 代理测试失败${COLORS.reset}`);
    console.error(`  错误: ${error.message}`);
    return false;
  }
}

async function testAuthValidation() {
  console.log(`\n${COLORS.blue}[额外测试] 认证验证${COLORS.reset}`);

  try {
    console.log(`\n📤 测试无 Authorization 请求...`);
    const response = await fetch(GEMINI_PROXY_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ text: 'test' }),
    });

    if (response.status === 401) {
      console.log(`${COLORS.green}✅ 认证验证正常（拒绝未授权请求）${COLORS.reset}`);
      return true;
    } else {
      console.log(`${COLORS.red}❌ 认证验证失败（应该返回 401）${COLORS.reset}`);
      return false;
    }
  } catch (error) {
    console.error(`${COLORS.red}❌ 认证测试失败: ${error.message}${COLORS.reset}`);
    return false;
  }
}

async function main() {
  console.log(`${COLORS.yellow}=================================`);
  console.log(`Audiofy Workers API 测试套件`);
  console.log(`=================================${COLORS.reset}`);

  const results = [];

  // 测试 Gemini 代理
  results.push(await testGeminiProxy());

  // 测试 TTS 代理
  results.push(await testTTSProxy());

  // 测试认证
  results.push(await testAuthValidation());

  // 汇总结果
  const passed = results.filter(Boolean).length;
  const total = results.length;

  console.log(`\n${COLORS.yellow}=================================`);
  console.log(`测试结果汇总: ${passed}/${total} 通过`);
  console.log(`=================================${COLORS.reset}`);

  if (passed === total) {
    console.log(`\n${COLORS.green}🎉 所有测试通过！${COLORS.reset}`);
    process.exit(0);
  } else {
    console.log(`\n${COLORS.red}⚠️  部分测试失败，请检查日志${COLORS.reset}`);
    process.exit(1);
  }
}

// 运行测试
main().catch((error) => {
  console.error(`\n${COLORS.red}测试运行失败: ${error.message}${COLORS.reset}`);
  process.exit(1);
});
