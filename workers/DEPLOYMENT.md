# Cloudflare Workers 部署指南

本文档说明如何部署 Audiofy 的 API 代理层（Gemini 翻译代理 + 豆包 TTS 代理）。

## 前置条件

1. **Cloudflare 账号**
   - 注册免费账号：https://dash.cloudflare.com/sign-up
   - 获取 Account ID（Dashboard 右侧边栏可查看）

2. **API 密钥准备**
   - Google Gemini API Key：https://ai.google.dev/
   - 豆包 TTS API Key：https://www.volcengine.com/docs/6561/97465
   - 生成 APP_SECRET（用于保护代理接口）：
     ```bash
     openssl rand -base64 32
     ```

3. **安装工具**
   ```bash
   # 安装 Node.js 20+
   node --version  # 确认版本 >= 20

   # 安装依赖
   cd workers
   npm install
   ```

---

## 部署步骤

### 1. 配置 Wrangler

```bash
# 登录 Cloudflare 账号
npx wrangler login

# 验证登录状态
npx wrangler whoami
```

### 2. 配置环境变量（密钥）

**重要：不要在代码或配置文件中硬编码密钥！**

```bash
# 配置 Gemini 代理的密钥
npx wrangler secret put GEMINI_API_KEY --config wrangler-gemini.toml
# 输入你的 Gemini API Key 并回车

npx wrangler secret put APP_SECRET --config wrangler-gemini.toml
# 输入你的 APP_SECRET 并回车

# 配置 TTS 代理的密钥
npx wrangler secret put DOUBAO_API_KEY --config wrangler-tts.toml
# 输入你的豆包 API Key 并回车

npx wrangler secret put APP_SECRET --config wrangler-tts.toml
# 输入相同的 APP_SECRET 并回车
```

### 3. 更新配置文件

编辑 `wrangler-gemini.toml` 和 `wrangler-tts.toml`，取消注释并填写 `account_id`：

```toml
account_id = "YOUR_ACCOUNT_ID"  # 从 Cloudflare Dashboard 获取
```

### 4. 本地测试

```bash
# 启动 Gemini 代理（端口 8787）
npm run dev:gemini

# 在另一个终端启动 TTS 代理（端口 8788）
npm run dev:tts

# 在第三个终端运行测试
export APP_SECRET="your-app-secret"
npm test
```

### 5. 部署到生产环境

```bash
# 部署所有代理
npm run deploy:all

# 或者单独部署
npm run deploy:gemini
npm run deploy:tts
```

### 6. 获取部署 URL

部署成功后，命令行会输出 Worker URL：

```
✨ Published audiofy-gemini-proxy
   https://audiofy-gemini-proxy.<your-subdomain>.workers.dev

✨ Published audiofy-tts-proxy
   https://audiofy-tts-proxy.<your-subdomain>.workers.dev
```

**将这两个 URL 记录下来**，后续在 NativeScript 应用中需要配置这些地址。

---

## 测试部署结果

使用 `curl` 测试生产环境的 API：

### 测试 Gemini 翻译代理

```bash
curl -X POST https://audiofy-gemini-proxy.<your-subdomain>.workers.dev \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_APP_SECRET" \
  -d '{"text": "Hello world"}'
```

**预期响应**：
```json
{
  "translatedText": "你好世界"
}
```

### 测试豆包 TTS 代理

```bash
curl -X POST https://audiofy-tts-proxy.<your-subdomain>.workers.dev \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_APP_SECRET" \
  -d '{"text": "你好世界"}'
```

**预期响应**：
```json
{
  "audioData": "base64-encoded-audio-data...",
  "duration": 2
}
```

---

## 故障排查

### 错误 1: `401 Unauthorized`

**原因**：APP_SECRET 不匹配

**解决**：
1. 确认请求头中的 `Authorization: Bearer YOUR_APP_SECRET` 正确
2. 重新配置密钥：`npx wrangler secret put APP_SECRET --config wrangler-xxx.toml`

### 错误 2: `429 Too Many Requests`

**原因**：API 限流

**解决**：
- Gemini API 免费版限制：15 RPM（每分钟请求数）
- 豆包 TTS 限制：根据你的账户等级
- 等待 1 分钟后重试，或升级 API 套餐

### 错误 3: `500 Internal Server Error`

**原因**：上游 API（Gemini/豆包）故障或密钥错误

**解决**：
1. 检查 Cloudflare Dashboard → Workers → 选择 Worker → Logs 查看详细错误
2. 验证 API Key 是否有效：
   ```bash
   # 测试 Gemini API Key
   curl https://generativelanguage.googleapis.com/v1beta/models \
     -H "x-goog-api-key: YOUR_GEMINI_KEY"
   ```

### 查看实时日志

```bash
# Gemini 代理日志
npx wrangler tail --config wrangler-gemini.toml

# TTS 代理日志
npx wrangler tail --config wrangler-tts.toml
```

---

## 费用估算

### Cloudflare Workers 免费额度

- 每天 100,000 次请求
- 每次请求最多 50ms CPU 时间
- 足够个人项目使用

**超出免费额度**：$0.50 / 百万次请求

### API 调用费用

- **Gemini API 免费版**：每天 1,500 次请求（足够测试）
- **豆包 TTS**：约 ¥0.1-0.3 / 1000 字符（月费约 ¥18-30）

---

## 安全最佳实践

1. **定期轮换 APP_SECRET**
   ```bash
   # 生成新密钥
   openssl rand -base64 32

   # 更新 Workers 密钥
   npx wrangler secret put APP_SECRET --config wrangler-gemini.toml
   npx wrangler secret put APP_SECRET --config wrangler-tts.toml
   ```

2. **限制 CORS 域名**
   - 编辑 `gemini-proxy.ts` 和 `tts-proxy.ts`
   - 将 `'Access-Control-Allow-Origin': '*'` 改为你的应用域名

3. **监控异常流量**
   - 在 Cloudflare Dashboard 查看请求统计
   - 发现异常立即禁用 Worker 或轮换密钥

---

## 下一步

部署完成后，在 NativeScript 应用中配置以下环境变量：

```typescript
// src/config/api.ts
export const API_CONFIG = {
  GEMINI_PROXY_URL: 'https://audiofy-gemini-proxy.<your-subdomain>.workers.dev',
  TTS_PROXY_URL: 'https://audiofy-tts-proxy.<your-subdomain>.workers.dev',
  APP_SECRET: '<stored-in-secure-storage>',
};
```

**注意**：`APP_SECRET` 应该存储在 `@nativescript/secure-storage` 中，不要硬编码在代码里！

---

## 参考资料

- [Cloudflare Workers 文档](https://developers.cloudflare.com/workers/)
- [Wrangler CLI 文档](https://developers.cloudflare.com/workers/wrangler/)
- [Gemini API 文档](https://ai.google.dev/docs)
- [豆包 TTS API 文档](https://www.volcengine.com/docs/6561/97465)
