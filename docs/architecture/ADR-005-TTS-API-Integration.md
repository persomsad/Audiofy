# ADR-005: TTS API 集成方案

## 状态

已接受 (Accepted) - 2025-01-16

## 背景 (Context)

项目需要文本转语音（TTS）功能，用于将 AI 生成的脚本转换为音频。最初选择了豆包 TTS API，但在实际集成中发现阿里云 DashScope Qwen3-TTS 具有更好的性能和成本优势。

### 技术要求

1. **多语音角色支持**：至少支持中英文多种语音
2. **高质量音频**：清晰自然，适合有声书场景
3. **快速响应**：单次请求延迟 < 10 秒
4. **可靠性**：API 稳定性 > 99%
5. **成本可控**：适合个人/小团队项目

## 决策 (Decision)

采用**阿里云 DashScope Qwen3-TTS API** 作为 TTS 服务提供商，通过 **Cloudflare Workers 代理**保护 API 密钥。

### 核心方案

```
客户端 → Cloudflare Workers (代理 + 认证) → DashScope Qwen3-TTS API → OSS 音频 URL
```

## 技术实现细节

### 1. API Endpoint

```
POST https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation
```

**⚠️ 重要提示**：不是 `/text2speech/synthesis`，而是 `/multimodal-generation/generation`

### 2. 请求格式

#### HTTP Headers

```http
Content-Type: application/json
Authorization: Bearer <DASHSCOPE_API_KEY>
```

#### Request Body

```json
{
  "model": "qwen3-tts-flash",
  "input": {
    "text": "要合成的文本内容",
    "voice": "Cherry",
    "language_type": "Chinese"
  }
}
```

**关键点**：

- ✅ `voice` 和 `language_type` 在 `input` 对象内部
- ❌ 不是在 `parameters` 对象中（常见错误）
- ❌ 不需要 `format` 和 `sample_rate` 参数

#### 支持的语音角色

| 语音名称 | 性别 | 语言   | 特点             |
| -------- | ---- | ------ | ---------------- |
| Cherry   | 女   | 中文   | 温柔女声（默认） |
| Ethan    | 男   | 英文   | 沉稳男声         |
| Nofish   | 女   | 中文   | 清新女声         |
| Jennifer | 女   | 英文   | -                |
| Ryan     | 男   | 英文   | -                |
| Katerina | 女   | 多语言 | -                |
| Elias    | 男   | 多语言 | -                |
| Jada     | 女   | 英文   | -                |
| Dylan    | 男   | 英文   | -                |
| Sunny    | 女   | 中文   | -                |
| Li       | -    | 中文   | -                |
| Marcus   | 男   | 英文   | -                |
| Roy      | 男   | 英文   | -                |
| Peter    | 男   | 英文   | -                |
| Rocky    | 男   | 英文   | -                |
| Kiki     | 女   | 中文   | -                |
| Eric     | 男   | 英文   | -                |

### 3. 响应格式

#### 成功响应 (HTTP 200)

```json
{
  "output": {
    "audio": {
      "url": "http://dashscope-result-bj.oss-cn-beijing.aliyuncs.com/.../audio.wav",
      "expires_at": 1760696716,
      "id": "audio_xxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    },
    "finish_reason": "stop"
  },
  "usage": {
    "characters": 8
  },
  "request_id": "xxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

**关键点**：

- ✅ 音频 URL 在 `output.audio.url`
- ❌ 不是在 `output.url`（常见错误）
- ⚠️ 音频 URL **有效期 24 小时**，需要及时下载
- ⚠️ 音频格式为 **WAV**，不是 MP3

#### 错误响应

```json
{
  "code": "InvalidParameter",
  "message": "url error, please check url！",
  "request_id": "xxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

### 4. Cloudflare Workers 代理实现

#### 环境变量

```bash
APP_SECRET=yY2P4lQICBEZhHbNlDhm5NCWvOVJXXRVfAG9GWylcc4=
DASHSCOPE_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

配置方式：

```bash
npx wrangler secret put APP_SECRET
npx wrangler secret put DASHSCOPE_API_KEY
```

#### 代理 API 接口

**Endpoint**: `https://audiofy-tts-proxy.luhuizhx.workers.dev`

**请求格式**：

```bash
curl -X POST https://audiofy-tts-proxy.luhuizhx.workers.dev \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer yY2P4lQICBEZhHbNlDhm5NCWvOVJXXRVfAG9GWylcc4=" \
  -d '{
    "text": "你好世界",
    "voice": "Cherry"
  }'
```

**响应格式**：

```json
{
  "audioUrl": "http://dashscope-result-bj.oss-cn-beijing.aliyuncs.com/.../audio.wav",
  "duration": 1
}
```

**错误处理**：

| HTTP 状态码 | 错误场景           | 响应示例                                                             |
| ----------- | ------------------ | -------------------------------------------------------------------- |
| 401         | APP_SECRET 无效    | `{"error": "Unauthorized"}`                                          |
| 400         | 空文本             | `{"error": "Invalid request: text is required"}`                     |
| 400         | 无效语音角色       | `{"error": "Invalid voice: XXX. Valid options: Cherry, Ethan, ..."}` |
| 500         | DashScope API 错误 | `{"error": "TTS synthesis failed", "code": "InvalidParameter"}`      |

### 5. 客户端集成示例

#### TypeScript/JavaScript

```typescript
async function synthesizeSpeech(text: string, voice: string = 'Cherry'): Promise<string> {
  const response = await fetch('https://audiofy-tts-proxy.luhuizhx.workers.dev', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: 'Bearer yY2P4lQICBEZhHbNlDhm5NCWvOVJXXRVfAG9GWylcc4=',
    },
    body: JSON.stringify({ text, voice }),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(`TTS synthesis failed: ${error.error}`)
  }

  const { audioUrl } = await response.json()
  return audioUrl // 24小时有效期
}
```

#### Python

```python
import requests

def synthesize_speech(text: str, voice: str = 'Cherry') -> str:
    response = requests.post(
        'https://audiofy-tts-proxy.luhuizhx.workers.dev',
        headers={
            'Content-Type': 'application/json',
            'Authorization': 'Bearer yY2P4lQICBEZhHbNlDhm5NCWvOVJXXRVfAG9GWylcc4='
        },
        json={'text': text, 'voice': voice}
    )

    response.raise_for_status()
    return response.json()['audioUrl']  # 24小时有效期
```

## 备选方案 (Alternatives Considered)

### 备选方案 A: 豆包 TTS API

- **描述**: 字节跳动旗下的 TTS 服务
- **优势**:
  - 国内访问速度快
  - 语音质量优秀
  - 文档较完善
- **为何未选择**:
  - 定价较高（相比 Qwen3-TTS）
  - API 集成复杂度高（需要 4 个环境变量）
  - 社区资源较少

### 备选方案 B: Azure Text-to-Speech

- **描述**: 微软 Azure 云的 TTS 服务
- **优势**:
  - 语音质量顶级
  - 全球 CDN 加速
  - 企业级稳定性
- **为何未选择**:
  - 成本高昂（不适合个人/小团队）
  - 需要国际信用卡
  - 中文语音选择较少

### 备选方案 C: Google Cloud Text-to-Speech

- **描述**: Google Cloud 的 TTS 服务
- **优势**:
  - 语音质量优秀
  - 支持 SSML 高级控制
  - WaveNet 神经网络模型
- **为何未选择**:
  - 国内访问不稳定
  - 需要国际信用卡
  - 中文语音支持一般

### 备选方案 D: 本地部署 TTS 模型

- **描述**: 使用 Coqui TTS、FastSpeech 2 等开源模型
- **优势**:
  - 零 API 调用成本
  - 数据隐私完全可控
  - 无 API 配额限制
- **为何未选择**:
  - 需要 GPU 服务器（成本更高）
  - 运维复杂度高
  - 语音质量不如商业服务

## 后果 (Consequences)

### 正面影响

1. **成本优化**: Qwen3-TTS 定价合理，适合个人/小团队项目
2. **快速响应**: qwen3-tts-flash 模型延迟低（2-6 秒），适合实时应用
3. **多语音支持**: 17 种语音角色，覆盖中英文主流场景
4. **密钥保护**: 通过 Cloudflare Workers 代理，API 密钥不暴露给客户端
5. **免费额度**: Cloudflare Workers 每天 10 万次请求免费

### 负面影响与缓解措施

1. **音频 URL 有效期限制（24 小时）**
   - **影响**: 需要及时下载音频文件
   - **缓解**: 客户端收到 audioUrl 后立即下载并存储到本地

2. **音频格式为 WAV（文件较大）**
   - **影响**: 下载时间较长，存储空间占用大
   - **缓解**: 客户端下载后转换为 MP3 格式（使用 ffmpeg）

3. **依赖阿里云服务可用性**
   - **影响**: 如果 DashScope API 故障，TTS 功能不可用
   - **缓解**:
     - 实现重试机制（exponential backoff）
     - 监控 API 可用性
     - 未来可考虑多 TTS 提供商备份方案

4. **国际访问延迟**
   - **影响**: 海外用户访问阿里云 OSS 可能较慢
   - **缓解**: 考虑使用 CDN 加速（如 Cloudflare R2 + CDN）

### 所需资源

- **开发时间**: 已完成（Issue #17, PR #19, PR #20）
- **运维成本**:
  - 阿里云 DashScope API 费用（按字符数计费）
  - Cloudflare Workers 免费额度（每天 10 万次请求）
- **培训成本**: 无（API 调用简单，文档完善）

## 部署验证结果

已于 2025-01-16 完成部署并通过全部测试：

| 测试项                    | 状态    | 响应时间 |
| ------------------------- | ------- | -------- |
| 基本连通性（Cherry 语音） | ✅ 通过 | 2-3 秒   |
| 认证失败测试（401）       | ✅ 通过 | < 1 秒   |
| 空文本验证（400）         | ✅ 通过 | < 1 秒   |
| 不同语音角色（Ethan）     | ✅ 通过 | 2-3 秒   |
| 无效语音角色（400）       | ✅ 通过 | < 1 秒   |
| 长文本处理（~100 字符）   | ✅ 通过 | 5-6 秒   |

**通过率: 100% (6/6)**

## 相关资源

- [DashScope Qwen3-TTS 官方文档](https://bailian.console.aliyun.com/?tab=doc#/doc/?type=model&url=2879134)
- [Cloudflare Workers 文档](https://developers.cloudflare.com/workers/)
- [Workers 代码仓库](../../workers/tts-proxy/)
- [部署检查清单](../../workers/tts-proxy/DEPLOYMENT_CHECKLIST.md)
- [Issue #17: 部署 Qwen3-TTS Workers](https://github.com/persomsad/Audiofy/issues/17)
- [PR #19: 实现 Qwen3-TTS 集成](https://github.com/persomsad/Audiofy/pull/19)
- [PR #20: 修复 API 集成问题](https://github.com/persomsad/Audiofy/pull/20)

## 更新历史

- 2025-01-16: 初始版本，记录 Qwen3-TTS API 集成方案
