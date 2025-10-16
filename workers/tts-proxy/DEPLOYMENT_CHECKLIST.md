# Qwen3-TTS Workers 部署检查清单

## 📋 部署前准备

### 1. 账号和凭证

- [ ] **Cloudflare 账号**
  - 已注册并登录 https://dash.cloudflare.com/
  - 确认账号状态为 Active

- [ ] **阿里云 DashScope API 密钥**
  - 已在 https://bailian.console.aliyun.com/ 开通 Qwen3-TTS 服务
  - 已获取 API Key（格式：`sk-****`）
  - 已确认 API 配额足够（推荐至少 100 元余额）

- [ ] **APP_SECRET 密钥**
  - 使用现有密钥：`yY2P4lQICBEZhHbNlDhm5NCWvOVJXXRVfAG9GWylcc4=`
  - 或生成新密钥：`openssl rand -base64 32`

### 2. 本地环境

- [ ] **Node.js 18+**
  ```bash
  node --version  # 应显示 v18.x 或更高
  ```

- [ ] **Wrangler CLI**
  ```bash
  cd workers/tts-proxy
  npm install
  npx wrangler --version  # 确认安装成功
  ```

### 3. 代码验证

- [ ] **本地测试通过**
  ```bash
  # 创建 .dev.vars 文件
  cp .dev.vars.example .dev.vars
  # 编辑 .dev.vars 填入测试密钥

  # 启动本地开发服务器
  npm run dev

  # 在另一个终端测试
  curl -X POST http://localhost:8787 \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer yY2P4lQICBEZhHbNlDhm5NCWvOVJXXRVfAG9GWylcc4=" \
    -d '{"text":"测试","voice":"Cherry"}'
  ```

---

## 🚀 部署步骤

### 步骤 1: 登录 Cloudflare

```bash
npx wrangler login
```

**验证登录成功**：
```bash
npx wrangler whoami
```

应显示你的 Cloudflare 账号信息。

---

### 步骤 2: 配置生产环境变量

```bash
# 配置 APP_SECRET
npx wrangler secret put APP_SECRET
# 输入：yY2P4lQICBEZhHbNlDhm5NCWvOVJXXRVfAG9GWylcc4=

# 配置 DASHSCOPE_API_KEY
npx wrangler secret put DASHSCOPE_API_KEY
# 输入：sk-**** (你的阿里云 API 密钥)
```

**验证环境变量已设置**：
```bash
npx wrangler secret list
```

应显示：
```
[
  {
    "name": "APP_SECRET",
    "type": "secret_text"
  },
  {
    "name": "DASHSCOPE_API_KEY",
    "type": "secret_text"
  }
]
```

---

### 步骤 3: 部署到 Cloudflare Workers

```bash
npm run deploy
```

**预期输出**：
```
Total Upload: xx.xx KiB / gzip: xx.xx KiB
Uploaded audiofy-tts-proxy (x.xx sec)
Published audiofy-tts-proxy (x.xx sec)
  https://audiofy-tts-proxy.luhuizhx.workers.dev
Current Deployment ID: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

**记录部署 URL**：`https://audiofy-tts-proxy.luhuizhx.workers.dev`

---

## ✅ 部署后验证

### 1. 基本连通性测试

```bash
curl -X POST https://audiofy-tts-proxy.luhuizhx.workers.dev \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer yY2P4lQICBEZhHbNlDhm5NCWvOVJXXRVfAG9GWylcc4=" \
  -d '{"text":"你好世界","voice":"Cherry"}'
```

**预期响应**（HTTP 200）：
```json
{
  "audioUrl": "https://dashscope-result-****.oss-cn-beijing.aliyuncs.com/****.mp3",
  "duration": 2
}
```

### 2. 认证测试

```bash
# 错误的 APP_SECRET 应返回 401
curl -X POST https://audiofy-tts-proxy.luhuizhx.workers.dev \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer wrong-secret" \
  -d '{"text":"测试"}'
```

**预期响应**（HTTP 401）：
```json
{
  "error": "Unauthorized"
}
```

### 3. 参数验证测试

```bash
# 空文本应返回 400
curl -X POST https://audiofy-tts-proxy.luhuizhx.workers.dev \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer yY2P4lQICBEZhHbNlDhm5NCWvOVJXXRVfAG9GWylcc4=" \
  -d '{"text":""}'
```

**预期响应**（HTTP 400）：
```json
{
  "error": "Invalid request: text is required"
}
```

### 4. 语音角色测试

```bash
# 测试不同语音角色
for voice in Cherry Ethan Nofish; do
  echo "Testing voice: $voice"
  curl -s -X POST https://audiofy-tts-proxy.luhuizhx.workers.dev \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer yY2P4lQICBEZhHbNlDhm5NCWvOVJXXRVfAG9GWylcc4=" \
    -d "{\"text\":\"测试${voice}语音\",\"voice\":\"${voice}\"}" | jq .
done
```

### 5. 使用验证脚本（推荐）

```bash
# 运行自动化验证脚本
./scripts/verify-deployment.sh https://audiofy-tts-proxy.luhuizhx.workers.dev
```

---

## 📊 Cloudflare Dashboard 检查

### 访问 Workers 控制台

1. 登录 https://dash.cloudflare.com/
2. 进入 **Workers & Pages**
3. 找到 **audiofy-tts-proxy**

### 检查项

- [ ] **部署状态**: 显示为 "Active"
- [ ] **环境变量**: APP_SECRET 和 DASHSCOPE_API_KEY 已设置
- [ ] **最近请求日志**: 显示成功的请求（HTTP 200）
- [ ] **错误率**: < 1%

### 可选：配置自定义域名

如果需要自定义域名（如 `tts.audiofy.app`）：

1. 在 Workers 详情页点击 **Triggers** → **Custom Domains**
2. 点击 **Add Custom Domain**
3. 输入域名并按提示配置 DNS

---

## 🔒 安全检查

- [ ] **环境变量加密**: 所有密钥通过 `wrangler secret` 设置，未出现在代码中
- [ ] **CORS 配置**: 生产环境应限制 `Access-Control-Allow-Origin`（当前为 `*`）
- [ ] **速率限制**: 考虑在 Cloudflare Dashboard 启用 Rate Limiting
- [ ] **日志监控**: 配置 Cloudflare Workers Analytics 和告警

---

## 📝 更新代码库配置

### 如果部署 URL 不同于默认值

编辑 `src/services/config.ts`:

```typescript
export const API_CONFIG = {
  TTS_PROXY_URL: process.env.TTS_PROXY_URL || 'https://your-actual-url.workers.dev',
  // ...
}
```

提交配置更新：
```bash
git add src/services/config.ts
git commit -m "chore: update TTS_PROXY_URL to production endpoint"
git push
```

---

## ❌ 常见问题排查

### 问题 1: `wrangler login` 失败

**症状**: 浏览器无法打开或认证失败

**解决方案**:
```bash
# 使用 API Token 方式
export CLOUDFLARE_API_TOKEN="your-api-token"
npx wrangler whoami
```

在 Cloudflare Dashboard → My Profile → API Tokens 创建新 Token。

---

### 问题 2: 部署后返回 401

**症状**: curl 测试返回 `{"error": "Unauthorized"}`

**解决方案**:
1. 确认 APP_SECRET 已正确设置：`npx wrangler secret list`
2. 确认请求头 Authorization 格式正确：`Bearer <APP_SECRET>`
3. 重新设置密钥：`npx wrangler secret put APP_SECRET`

---

### 问题 3: 部署后返回 500

**症状**: curl 测试返回 `{"error": "TTS synthesis failed"}`

**解决方案**:
1. 检查 DashScope API 密钥是否有效
2. 确认 Qwen3-TTS 服务已开通
3. 检查 API 余额是否充足
4. 查看 Workers 日志：`npm run tail`

---

### 问题 4: audioUrl 无法下载

**症状**: 返回的 audioUrl 访问提示 403 Forbidden

**解决方案**:
- audioUrl 有效期为 24 小时，确保及时下载
- 检查网络连接
- 验证 URL 是否正确（应为 OSS 地址）

---

## 📞 支持资源

- **Cloudflare Workers 文档**: https://developers.cloudflare.com/workers/
- **Wrangler CLI 文档**: https://developers.cloudflare.com/workers/wrangler/
- **Qwen3-TTS 文档**: https://bailian.console.aliyun.com/?tab=doc#/doc/?type=model&url=2879134
- **项目 README**: `workers/tts-proxy/README.md`

---

## ✅ 完成标记

当以下所有项都完成时，关闭 Issue #17：

- [ ] Workers 已成功部署到 Cloudflare
- [ ] 所有环境变量已正确配置
- [ ] 基本连通性测试通过
- [ ] 认证测试通过
- [ ] 参数验证测试通过
- [ ] 语音角色测试通过
- [ ] Cloudflare Dashboard 显示正常状态
- [ ] 代码库配置已更新（如需要）
- [ ] 验证脚本测试通过

**部署完成时间**: ___________
**部署人员**: ___________
**部署 URL**: ___________
