# ADR-002: 架构设计

## 状态

已接受 (Accepted)

## 日期

2025-01-16

## 背景 (Context)

Audiofy 需要一个清晰的架构设计来支持以下核心功能：

1. **用户输入文章**（MVP 阶段：手动复制粘贴）
2. **调用 Gemini API 翻译**（英文 → 中文）
3. **调用豆包 TTS API 生成语音**（中文文本 → WAV 音频）
4. **本地存储音频和元数据**（设备文件系统）
5. **播放音频**（支持后台播放、进度控制、锁屏控制）

### 架构挑战

- **API 密钥安全**：移动应用可以被反编译，直接在应用内存储 API Key 会暴露
- **数据持久化**：需要关联音频文件和元数据（标题、原文、译文、创建时间）
- **错误处理**：网络失败、API 限流、存储空间不足等异常情况
- **模块化**：代码需要易于维护和扩展（未来可能添加 OCR、批量处理等功能）

## 决策 (Decision)

我们将采用 **三层架构 + API 代理层** 的设计，核心原则：

1. **关注点分离**：UI、业务逻辑、数据访问分离
2. **安全第一**：API 密钥通过 Cloudflare Workers 代理层保护
3. **简洁数据模型**：MVP 阶段使用 JSON 文件存储元数据（不引入数据库复杂度）
4. **渐进式架构**：先实现核心流程，预留扩展点

### 架构图

```mermaid
flowchart TB
    subgraph MobileApp [Audiofy 移动应用]
        direction TB

        subgraph PresentationLayer [表现层 - Vue 3 Components]
            InputView[文章输入页面]
            LibraryView[音频库页面]
            PlayerView[播放器页面]
        end

        subgraph BusinessLayer [业务逻辑层 - Composables]
            TranslateService[翻译服务]
            TTSService[语音合成服务]
            StorageService[存储服务]
            PlayerService[播放器服务]
        end

        subgraph DataLayer [数据访问层]
            APIClient[API 客户端<br/>ofetch]
            FileSystem[文件系统<br/>@nativescript/core]
            LocalStorage[本地存储<br/>@nativescript/localstorage]
            AudioPlayer[音频播放器<br/>nativescript-audio]
        end

        InputView --> TranslateService
        InputView --> TTSService
        LibraryView --> StorageService
        PlayerView --> PlayerService

        TranslateService --> APIClient
        TTSService --> APIClient
        StorageService --> FileSystem
        StorageService --> LocalStorage
        PlayerService --> AudioPlayer
    end

    subgraph ProxyLayer [API 代理层 - Cloudflare Workers]
        GeminiProxy[Gemini API 代理]
        TTSProxy[豆包 TTS API 代理]
    end

    subgraph ExternalAPIs [外部 API]
        GeminiAPI[Google Gemini API]
        DoubaoAPI[豆包 TTS API]
    end

    APIClient --> GeminiProxy
    APIClient --> TTSProxy
    GeminiProxy --> GeminiAPI
    TTSProxy --> DoubaoAPI

    style ProxyLayer fill:#fff3bf,stroke:#f59f00,stroke-width:2px
    style ExternalAPIs fill:#e7f5ff,stroke:#1971c2,stroke-width:1px
```

### 数据模型

#### Article 数据模型

```typescript
interface Article {
  id: string // UUID
  title: string // 文章标题（用户输入或自动生成）
  originalText: string // 原始英文文本
  translatedText: string // 翻译后的中文文本
  audioPath: string // 音频文件本地路径（相对路径）
  createdAt: Date // 创建时间
  duration: number // 音频时长（秒）
  status: 'pending' | 'processing' | 'completed' | 'failed' // 状态
  errorMessage?: string // 错误信息（如果失败）
}
```

#### 文件存储结构

```
~/Documents/Audiofy/
├── metadata.json          # 所有文章的元数据（Article[] 的 JSON）
└── audios/
    ├── {uuid-1}.wav      # 音频文件
    ├── {uuid-2}.wav
    └── ...
```

**为什么不用数据库？**

- MVP 阶段预估文章数 < 100 篇
- JSON 文件读写性能足够（~10ms）
- 简化架构，避免引入 SQLite 的复杂度
- 未来如果需要，可以轻松迁移到数据库

### 核心流程设计

#### 流程1：创建音频播客

```mermaid
sequenceDiagram
    actor User
    participant UI as 文章输入页面
    participant TS as TranslateService
    participant TTSS as TTSService
    participant SS as StorageService
    participant Proxy as Cloudflare Workers
    participant Gemini as Gemini API
    participant Doubao as 豆包 TTS API

    User->>UI: 输入英文文章
    UI->>UI: 显示加载状态

    UI->>TS: translate(originalText)
    TS->>Proxy: POST /api/translate
    Proxy->>Gemini: 调用 Gemini API
    Gemini-->>Proxy: 返回中文翻译
    Proxy-->>TS: 返回中文翻译

    TS->>TTSS: synthesize(translatedText)
    TTSS->>Proxy: POST /api/tts
    Proxy->>Doubao: 调用豆包 TTS API
    Doubao-->>Proxy: 返回 WAV 音频（Base64）
    Proxy-->>TTSS: 返回音频数据

    TTSS->>SS: saveAudio(audioData)
    SS->>SS: 生成 UUID
    SS->>SS: 写入音频文件
    SS->>SS: 更新 metadata.json
    SS-->>UI: 返回 Article 对象

    UI->>UI: 跳转到音频库页面
    UI->>User: 显示成功提示
```

#### 流程2：播放音频

```mermaid
sequenceDiagram
    actor User
    participant Library as 音频库页面
    participant Player as 播放器页面
    participant PS as PlayerService
    participant AP as AudioPlayer

    User->>Library: 点击文章卡片
    Library->>Player: 导航并传递 articleId
    Player->>PS: loadArticle(articleId)
    PS->>PS: 从 LocalStorage 读取元数据
    PS->>AP: load(audioPath)
    AP-->>Player: 返回音频时长

    User->>Player: 点击播放按钮
    Player->>PS: play()
    PS->>AP: play()
    AP->>AP: 开始播放
    AP->>Player: 定时回调播放进度

    User->>Player: 点击暂停按钮
    Player->>PS: pause()
    PS->>AP: pause()
```

### API 代理层设计（Cloudflare Workers）

#### 为什么需要代理层？

**问题**：移动应用可以被反编译，直接存储 API Key 会暴露
**方案**：部署 Cloudflare Workers 作为代理，API Key 存储在 Workers 环境变量中

#### Gemini 翻译代理

```typescript
// workers/gemini-proxy.ts
export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    // 1. 验证请求来源（可选：添加 API Token）
    const authHeader = request.headers.get('Authorization')
    if (authHeader !== `Bearer ${env.APP_SECRET}`) {
      return new Response('Unauthorized', { status: 401 })
    }

    // 2. 解析请求体
    const { text } = await request.json()

    // 3. 调用 Gemini API
    const response = await fetch(
      'https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'x-goog-api-key': env.GEMINI_API_KEY, // 密钥安全存储
        },
        body: JSON.stringify({
          contents: [
            {
              parts: [
                {
                  text: `请将以下英文翻译成中文，保持专业术语准确，适合口语播报：\n\n${text}`,
                },
              ],
            },
          ],
        }),
      },
    )

    // 4. 返回翻译结果
    const data = await response.json()
    return new Response(
      JSON.stringify({
        translatedText: data.candidates[0].content.parts[0].text,
      }),
      {
        headers: { 'Content-Type': 'application/json' },
      },
    )
  },
}
```

#### 豆包 TTS 代理

**真实API文档**: [火山引擎语音合成API](https://www.volcengine.com/docs/6561/1257584)

```typescript
// workers/tts-proxy.ts
export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    // 1. 验证请求
    const authHeader = request.headers.get('Authorization')
    if (authHeader !== `Bearer ${env.APP_SECRET}`) {
      return new Response('Unauthorized', { status: 401 })
    }

    // 2. 解析请求体
    const { text } = await request.json()

    // 3. 生成唯一请求ID
    const reqid = `${Date.now()}-${Math.random().toString(36).substring(7)}`

    // 4. 调用豆包 TTS API (火山引擎语音合成)
    const response = await fetch('https://openspeech.bytedance.com/api/v1/tts', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        // 豆包API使用特殊的Bearer格式: "Bearer; {token}"
        Authorization: `Bearer; ${env.DOUBAO_TOKEN}`,
      },
      body: JSON.stringify({
        app: {
          appid: env.DOUBAO_APPID, // 应用ID（从火山引擎控制台获取）
          token: env.DOUBAO_TOKEN, // 访问令牌
          cluster: env.DOUBAO_CLUSTER, // 集群ID
        },
        user: {
          uid: 'audiofy-user', // 用户标识
        },
        audio: {
          voice_type: 'BV001_streaming', // 语音类型（参考官方文档）
          encoding: 'mp3', // 音频格式: mp3, wav, ogg
          speed_ratio: 1.0, // 语速倍率 (0.5-2.0)
          volume_ratio: 1.0, // 音量倍率 (0.1-3.0)
          pitch_ratio: 1.0, // 音调倍率 (0.5-2.0)
        },
        request: {
          reqid: reqid, // 请求唯一标识
          text: text, // 要合成的文本
          text_type: 'plain', // 文本类型: plain/ssml
          operation: 'submit', // 操作类型: submit(提交)/query(查询)
        },
      }),
    })

    // 5. 解析响应
    const data = await response.json()

    if (!response.ok) {
      console.error('Doubao TTS API error:', data)
      return new Response(
        JSON.stringify({
          error: data.message || 'TTS synthesis failed',
        }),
        {
          status: response.status,
          headers: { 'Content-Type': 'application/json' },
        },
      )
    }

    // 6. 提取音频数据和时长
    // 豆包API返回格式: { data: "base64_audio_string", ... }
    const audioData = data.data

    if (!audioData) {
      return new Response(
        JSON.stringify({
          error: 'Invalid API response: missing audio data',
        }),
        {
          status: 500,
          headers: { 'Content-Type': 'application/json' },
        },
      )
    }

    // 7. 估算音频时长（基于文本长度，实际应从API响应获取）
    // 注意：豆包API可能不直接返回时长，需要解码音频文件获取
    const estimatedDuration = Math.ceil(text.length / 5) // 粗略估算：5字符/秒

    // 8. 返回音频数据（Base64 编码）
    return new Response(
      JSON.stringify({
        audioData: audioData, // Base64编码的音频数据
        duration: estimatedDuration, // 音频时长（秒）
      }),
      {
        headers: { 'Content-Type': 'application/json' },
      },
    )
  },
}
```

**关键配置说明**:

1. **认证参数**（3个，从火山引擎控制台获取）:
   - `DOUBAO_APPID`: 应用ID
   - `DOUBAO_TOKEN`: 访问令牌（Access Token）
   - `DOUBAO_CLUSTER`: 集群ID

2. **语音类型** (`voice_type`): 需从[官方文档](https://www.volcengine.com/docs/6561/1257584)查询可用声音列表
   - 示例: `BV001_streaming` (流式合成), `BV700_streaming` (特定角色声音)

3. **音频格式** (`encoding`):
   - `mp3` (推荐，体积小)
   - `wav` (无损，体积大)
   - `ogg`

4. **时长获取**: 豆包API响应中可能不包含时长信息，需要：
   - 方案A: 解码音频文件头获取时长（需额外处理）
   - 方案B: 基于文本长度估算（当前实现）
   - 方案C: 在客户端播放时获取实际时长

### MVP 范围限定

**包含的功能**：

- ✅ 手动输入英文文章（复制粘贴）
- ✅ 调用 Gemini 翻译
- ✅ 调用豆包 TTS 生成音频
- ✅ 本地存储音频和元数据
- ✅ 音频库列表展示
- ✅ 基础音频播放器（播放、暂停、进度条）

**不包含的功能**（未来扩展）：

- ❌ OCR 拍照识别
- ❌ 网页/RSS 抓取
- ❌ 批量处理
- ❌ 后台播放（锁屏控制）
- ❌ 播放列表
- ❌ 分享功能
- ❌ 云端同步

## 备选方案 (Alternatives Considered)

### 备选方案 A: 直接在应用内调用 API（无代理层）

- **描述**: 将 API Key 加密后存储在应用内，直接调用 Gemini 和豆包 TTS
- **优势**:
  - 架构简单，不需要维护代理服务器
  - 减少一次网络跳转（延迟降低 ~50ms）
- **为何未选择**:
  - **安全风险极高**：即使加密，反编译工具也能提取密钥
  - API Key 泄露后，恶意用户可以无限制调用，产生高额费用
  - 业界最佳实践：绝不在客户端存储敏感密钥

### 备选方案 B: 自建 Node.js 后端 + 数据库

- **描述**: 搭建 Express.js 后端，使用 PostgreSQL 存储元数据
- **优势**:
  - 完整的后端能力（用户认证、数据分析、云端同步）
  - 数据库提供强大的查询能力
- **为何未选择**:
  - MVP 阶段过度设计，不需要用户认证和云端同步
  - 增加维护成本（服务器费用 ~$20/月 + 数据库维护）
  - Cloudflare Workers 免费额度足够 MVP 使用，且无需维护服务器

### 备选方案 C: 使用 SQLite 数据库存储元数据

- **描述**: 使用 @nativescript/sqlite 插件存储 Article 数据
- **优势**:
  - 支持复杂查询（如按时间排序、全文搜索）
  - 数据完整性约束（外键、事务）
- **为何未选择**:
  - MVP 阶段预估文章数 < 100 篇，JSON 文件性能足够
  - 引入数据库增加复杂度（Schema 迁移、查询优化）
  - 如果未来需要，可以轻松迁移（JSON → SQLite 转换脚本）

## 后果 (Consequences)

### 正面影响

1. **安全性高**：API 密钥通过 Cloudflare Workers 保护，不暴露在客户端
2. **架构清晰**：三层架构便于理解和维护，新成员上手快
3. **易于测试**：每层职责单一，可以独立编写单元测试
4. **扩展性好**：预留了扩展点（如未来添加 OCR、批量处理）
5. **成本低**：Cloudflare Workers 免费额度 100,000 请求/天，足够个人使用

### 负面影响与缓解措施

1. **网络延迟增加**
   - **影响**：增加一次代理跳转（~50-100ms）
   - **缓解**：Cloudflare Workers 部署在全球边缘节点，延迟极低；对于音频生成（~5-10 秒），50ms 可以忽略

2. **依赖外部服务**
   - **影响**：Cloudflare Workers 故障会导致应用不可用
   - **缓解**：Cloudflare 的 SLA 为 99.99%，可靠性极高；添加错误提示和重试机制

3. **JSON 文件性能限制**
   - **影响**：当文章数超过 1000 篇时，JSON 读写性能下降
   - **缓解**：MVP 阶段不会达到此规模；未来可以迁移到 SQLite（预留迁移脚本）

4. **缺少云端同步**
   - **影响**：用户更换设备后，数据无法迁移
   - **缓解**：MVP 阶段专注于核心功能；未来可以添加导出/导入功能

### 所需资源

- **Cloudflare Workers 部署**：
  - 账号注册：免费
  - 部署时间：30 分钟
  - 配置环境变量：存储 GEMINI_API_KEY、DOUBAO_API_KEY、APP_SECRET

- **开发时间估算**：
  - API 代理层开发：4 小时
  - 前端业务逻辑层：2 周
  - 数据访问层：3 天
  - 集成测试：3 天

## 相关决策

- ADR-001: 技术栈选型（NativeScript-Vue）
- ADR-003: 质量保障机制（测试策略和代码规范）

## 参考资料

- [NativeScript 文件系统 API](https://docs.nativescript.org/api-reference/modules/_file_system_.html)
- [nativescript-audio 插件文档](https://github.com/nstudio/nativescript-audio)
- [Cloudflare Workers 文档](https://developers.cloudflare.com/workers/)
- [Gemini API 文档](https://ai.google.dev/docs)
- [豆包 TTS API 文档](https://www.volcengine.com/docs/6561/97465)
