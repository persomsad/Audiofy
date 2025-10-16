# Audiofy API Proxy Layer

Cloudflare Workers 实现的 API 代理层，用于保护 Gemini 和豆包 TTS 的 API 密钥。

## 项目结构

```
workers/
├── src/
│   ├── gemini-proxy.ts       # Gemini 翻译 API 代理
│   └── tts-proxy.ts           # 豆包 TTS API 代理
├── test/
│   └── test-api.js            # API 测试脚本
├── wrangler-gemini.toml       # Gemini Worker 配置
├── wrangler-tts.toml          # TTS Worker 配置
├── package.json               # 依赖和脚本
├── DEPLOYMENT.md              # 详细部署指南
└── README.md                  # 本文件
```

## 快速开始

### 安装依赖

```bash
npm install
```

### 本地开发

```bash
# 启动 Gemini 代理（端口 8787）
npm run dev:gemini

# 启动 TTS 代理（端口 8788）
npm run dev:tts
```

### 测试

```bash
# 设置测试密钥
export APP_SECRET="your-test-secret"

# 运行测试脚本
npm test
```

### 部署

详细部署步骤请参考 [DEPLOYMENT.md](./DEPLOYMENT.md)

```bash
# 部署所有 Workers
npm run deploy:all
```

## API 接口

### 1. Gemini 翻译代理

**Endpoint**: `POST /`

**请求头**:
```
Content-Type: application/json
Authorization: Bearer {APP_SECRET}
```

**请求体**:
```json
{
  "text": "Hello world"
}
```

**响应**:
```json
{
  "translatedText": "你好世界"
}
```

### 2. 豆包 TTS 代理

**Endpoint**: `POST /`

**请求头**:
```
Content-Type: application/json
Authorization: Bearer {APP_SECRET}
```

**请求体**:
```json
{
  "text": "你好世界"
}
```

**响应**:
```json
{
  "audioData": "base64-encoded-audio-data...",
  "duration": 2
}
```

## 安全特性

- ✅ **API 密钥隔离**：密钥存储在 Cloudflare Workers 环境变量中，不暴露给客户端
- ✅ **Bearer Token 认证**：通过 APP_SECRET 验证请求来源
- ✅ **请求大小限制**：Gemini 10,000 字符，TTS 5,000 字符
- ✅ **CORS 支持**：跨域请求支持（生产环境需限制域名）
- ✅ **错误处理**：友好的错误信息和状态码

## 错误代码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 认证失败（APP_SECRET 错误） |
| 405 | 方法不允许（只支持 POST） |
| 429 | API 限流（请求过于频繁） |
| 500 | 内部错误（上游 API 故障） |

## 费用

### Cloudflare Workers 免费额度
- 每天 100,000 次请求
- 足够个人项目使用

### API 调用费用
- **Gemini API**: 免费版 1,500 次/天
- **豆包 TTS**: 约 ¥0.1-0.3 / 1000 字符

## 参考文档

- [Cloudflare Workers 文档](https://developers.cloudflare.com/workers/)
- [部署指南](./DEPLOYMENT.md)
- [ADR-002: 架构设计](../docs/architecture/ADR-002-Architecture-Design.md)
