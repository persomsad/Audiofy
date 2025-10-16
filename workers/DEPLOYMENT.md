# Cloudflare Workers 部署指南

本文档说明如何部署 Audiofy 的 TTS API 代理层（Qwen3-TTS 代理）。

## 前置条件

1. **Cloudflare 账号**
   - 注册免费账号：https://dash.cloudflare.com/sign-up
   - 获取 Account ID（Dashboard 右侧边栏可查看）

2. **API 密钥准备**
   - 阿里云 DashScope API Key：https://bailian.console.aliyun.com/
   - 生成 APP_SECRET（用于保护代理接口）：
     ```bash
     openssl rand -base64 32
     ```

3. **安装工具**

   ```bash
   # 安装 Node.js 18+
   node --version  # 确认版本 >= 18

   # 安装依赖
   cd workers/tts-proxy
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
# 配置 TTS 代理的密钥
npx wrangler secret put DASHSCOPE_API_KEY
# 输入你的 DashScope API Key（格式如 sk-****）并回车

npx wrangler secret put APP_SECRET
# 输入你的 APP_SECRET 并回车
```

### 3. 本地测试

```bash
# 创建 .dev.vars 文件
cd workers/tts-proxy
cp .dev.vars.example .dev.vars
# 编辑 .dev.vars 填入真实密钥

# 启动本地开发服务器
npm run dev

# 在另一个终端测试
curl -X POST http://localhost:8787 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-app-secret" \
  -d '{"text":"你好世界","voice":"Cherry"}'
```

### 4. 部署到生产环境

```bash
npm run deploy
```

### 5. 获取部署 URL

部署成功后，命令行会输出 Worker URL：

```
✨ Published audiofy-tts-proxy
   https://audiofy-tts-proxy.<your-subdomain>.workers.dev
```

**将这个 URL 记录下来**，后续在 NativeScript 应用中需要配置这个地址。

---

## 测试部署结果

使用 `curl` 测试生产环境的 API：

### 测试 Qwen3-TTS 代理

```bash
curl -X POST https://audiofy-tts-proxy.<your-subdomain>.workers.dev \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_APP_SECRET" \
  -d '{"text":"你好世界","voice":"Cherry"}'
```

**预期响应**:

```json
{
  "audioUrl": "https://dashscope-result-****.oss-cn-beijing.aliyuncs.com/****.mp3",
  "duration": 2
}
```

---

## 故障排查

### 错误 1: `401 Unauthorized`

**原因**：APP_SECRET 不匹配

**解决**:

1. 确认请求头中的 `Authorization: Bearer YOUR_APP_SECRET` 正确
2. 重新配置密钥：`npx wrangler secret put APP_SECRET`

### 错误 2: `429 Too Many Requests`

**原因**：API 限流

**解决**：

- Qwen3-TTS 限制：根据你的账户等级
- 等待 1 分钟后重试，或升级 API 套餐

### 错误 3: `500 Internal Server Error`

**原因**：上游 API（DashScope）故障或密钥错误

**解决**:

1. 检查 Cloudflare Dashboard → Workers → 选择 Worker → Logs 查看详细错误
2. 验证 DashScope API Key 是否有效
3. 确认 Qwen3-TTS 服务已开通

### 查看实时日志

```bash
npx wrangler tail
```

---

## 费用估算

### Cloudflare Workers 免费额度

- 每天 100,000 次请求
- 每次请求最多 50ms CPU 时间
- 足够个人项目使用

**超出免费额度**：$0.50 / 百万次请求

### API 调用费用

- **Qwen3-TTS**：约 ¥0.8 / 10,000 字符（比豆包更经济）

---

## 安全最佳实践

1. **定期轮换 APP_SECRET**

   ```bash
   # 生成新密钥
   openssl rand -base64 32

   # 更新 Workers 密钥
   npx wrangler secret put APP_SECRET
   ```

2. **限制 CORS 域名**
   - 编辑 `workers/tts-proxy/src/index.ts`
   - 将 `'Access-Control-Allow-Origin': '*'` 改为你的应用域名

3. **监控异常流量**
   - 在 Cloudflare Dashboard 查看请求统计
   - 发现异常立即禁用 Worker 或轮换密钥

---

## 下一步

部署完成后，在 NativeScript 应用中配置以下环境变量：

```typescript
// src/services/config.ts
export const API_CONFIG = {
  TTS_PROXY_URL: 'https://audiofy-tts-proxy.<your-subdomain>.workers.dev',
  APP_SECRET: '<stored-in-secure-storage>',
  MAX_TEXT_LENGTH: 5000,
  TIMEOUT: 30000,
}
```

**注意**：`APP_SECRET` 应该存储在 `@nativescript/secure-storage` 中，不要硬编码在代码里！

---

## 参考资料

- [Cloudflare Workers 文档](https://developers.cloudflare.com/workers/)
- [Wrangler CLI 文档](https://developers.cloudflare.com/workers/wrangler/)
- [阿里云 DashScope Qwen3-TTS 文档](https://bailian.console.aliyun.com/?tab=doc#/doc/?type=model&url=2879134)
- [TTS Proxy 详细文档](./tts-proxy/README.md)
