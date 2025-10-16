# Audiofy TTS Proxy - Cloudflare Workers

豆包 TTS API 的 Cloudflare Workers 代理服务，用于保护 API 密钥并提供统一的认证层。

## 功能特性

- ✅ 统一认证：使用 `APP_SECRET` 保护代理访问
- ✅ 密钥保护：豆包 API 凭证存储在 Cloudflare Workers 环境变量中，不暴露给客户端
- ✅ 错误处理：友好的错误信息和状态码
- ✅ CORS 支持：允许跨域请求
- ✅ 类型安全：完整的 TypeScript 类型定义

## 前置要求

1. **Cloudflare 账号**：注册免费账号 https://dash.cloudflare.com/sign-up
2. **豆包 API 凭证**（从火山引擎控制台获取）：
   - `DOUBAO_APPID`：应用ID
   - `DOUBAO_TOKEN`：访问令牌（Access Token）
   - `DOUBAO_CLUSTER`：集群ID
3. **Node.js**：v18+ 和 npm

## 本地开发

### 1. 安装依赖

```bash
cd workers/tts-proxy
npm install
```

### 2. 配置本地环境变量

创建 `.dev.vars` 文件（**不要提交到 Git**）：

```bash
# .dev.vars
APP_SECRET=your-local-app-secret
DOUBAO_APPID=your-doubao-appid
DOUBAO_TOKEN=your-doubao-token
DOUBAO_CLUSTER=your-doubao-cluster
```

### 3. 启动本地开发服务器

```bash
npm run dev
```

服务将运行在 `http://localhost:8787`

### 4. 测试请求

```bash
curl -X POST http://localhost:8787 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-local-app-secret" \
  -d '{"text":"你好，世界"}'
```

预期响应：

```json
{
  "audioData": "base64_encoded_audio_data...",
  "duration": 3
}
```

## 生产部署

### 方法 1: 使用 Wrangler CLI（推荐）

#### 1. 登录 Cloudflare

```bash
npx wrangler login
```

#### 2. 配置生产环境变量

使用 `wrangler secret` 命令设置敏感信息（**安全，不会存储在代码库**）：

```bash
# 设置 APP_SECRET
npx wrangler secret put APP_SECRET
# 提示输入时，输入您的应用密钥，例如：audiofy_prod_secret_2024

# 设置豆包 API 凭证
npx wrangler secret put DOUBAO_APPID
# 输入：从火山引擎控制台获取的 APPID

npx wrangler secret put DOUBAO_TOKEN
# 输入：从火山引擎控制台获取的 TOKEN

npx wrangler secret put DOUBAO_CLUSTER
# 输入：从火山引擎控制台获取的 CLUSTER
```

#### 3. 部署到 Cloudflare Workers

```bash
npm run deploy
```

部署成功后，服务将运行在：

```
https://audiofy-tts-proxy.luhuizhx.workers.dev
```

### 方法 2: 使用 Cloudflare Dashboard

#### 1. 部署 Worker

1. 访问 Cloudflare Dashboard: https://dash.cloudflare.com/
2. 进入 `Workers & Pages` → `Create Application` → `Create Worker`
3. 命名为 `audiofy-tts-proxy`
4. 将 `src/index.ts` 的代码复制粘贴到编辑器
5. 点击 `Save and Deploy`

#### 2. 配置环境变量

1. 在 Worker 详情页，点击 `Settings` → `Variables`
2. 添加以下环境变量（类型选择 `Secret`）：
   - `APP_SECRET`：您的应用密钥
   - `DOUBAO_APPID`：豆包应用ID
   - `DOUBAO_TOKEN`：豆包访问令牌
   - `DOUBAO_CLUSTER`：豆包集群ID
3. 点击 `Save and Deploy`

## API 使用

### 请求格式

```http
POST https://audiofy-tts-proxy.luhuizhx.workers.dev
Content-Type: application/json
Authorization: Bearer <APP_SECRET>

{
  "text": "要合成的中文文本"
}
```

### 响应格式

**成功响应** (HTTP 200)：

```json
{
  "audioData": "base64_encoded_audio_data...",
  "duration": 10
}
```

**错误响应**：

- **401 Unauthorized**：`APP_SECRET` 无效

  ```json
  { "error": "Unauthorized" }
  ```

- **400 Bad Request**：请求格式错误

  ```json
  { "error": "Invalid request: text is required" }
  ```

- **500 Internal Server Error**：豆包 API 错误或服务器错误
  ```json
  { "error": "TTS synthesis failed" }
  ```

## 监控和调试

### 查看实时日志

```bash
npm run tail
```

### 查看部署历史

```bash
npx wrangler deployments list
```

### 回滚到上一个版本

```bash
npx wrangler rollback
```

## 费用说明

Cloudflare Workers 免费额度（每天）：

- 请求数：100,000 次
- CPU 时间：10ms/请求

对于个人项目和小型团队，免费额度通常足够使用。

## 安全注意事项

1. **永远不要**将环境变量提交到代码库
2. `.dev.vars` 文件已添加到 `.gitignore`
3. 使用 `wrangler secret` 命令管理生产环境的敏感信息
4. 定期轮换 `APP_SECRET` 和豆包 API 凭证
5. 启用 Cloudflare 的速率限制（Rate Limiting）保护 API

## 故障排查

### 问题：本地开发时 `wrangler dev` 报错

**解决方案**：

1. 确保 `.dev.vars` 文件存在且包含所有必需的环境变量
2. 检查 Node.js 版本是否 >= 18
3. 清除缓存：`rm -rf node_modules && npm install`

### 问题：部署后返回 401 Unauthorized

**解决方案**：

1. 检查客户端请求头是否正确：`Authorization: Bearer <APP_SECRET>`
2. 确认生产环境的 `APP_SECRET` 已正确设置：
   ```bash
   npx wrangler secret list
   ```
3. 重新设置密钥：`npx wrangler secret put APP_SECRET`

### 问题：豆包 API 返回错误

**解决方案**：

1. 验证豆包凭证是否正确（APPID, TOKEN, CLUSTER）
2. 检查火山引擎控制台中的 API 配额和余额
3. 查看实时日志：`npm run tail`

## 相关文档

- [Cloudflare Workers 文档](https://developers.cloudflare.com/workers/)
- [Wrangler CLI 文档](https://developers.cloudflare.com/workers/wrangler/)
- [火山引擎语音合成 API](https://www.volcengine.com/docs/6561/1257584)
- [Audiofy 架构设计文档](../../docs/architecture/ADR-002-Architecture-Design.md)

## 许可证

MIT
