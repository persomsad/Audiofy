/**
 * API 配置
 * 注意：NativeScript不支持process.env，使用默认值
 * 生产环境可以通过ApplicationSettings或SecureStorage动态修改
 */

export const API_CONFIG = {
  // Cloudflare Workers 端点
  GEMINI_PROXY_URL: 'https://audiofy-gemini-proxy.luhuizhx.workers.dev',
  TTS_PROXY_URL: 'https://audiofy-tts-proxy.luhuizhx.workers.dev',

  // 应用密钥（用于 Bearer Token 认证）
  APP_SECRET: 'yY2P4lQICBEZhHbNlDhm5NCWvOVJXXRVfAG9GWylcc4=',

  // 超时配置 (毫秒)
  TIMEOUT: 30000,

  // 文本长度限制
  MIN_TEXT_LENGTH: 100,
  MAX_TEXT_LENGTH: 5000,
}
