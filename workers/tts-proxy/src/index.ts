/**
 * Qwen3-TTS Cloudflare Workers 代理
 * 基于阿里云 DashScope 文本语音合成 API
 * 文档: https://bailian.console.aliyun.com/?tab=doc#/doc/?type=model&url=2879134
 */

// 环境变量类型定义
interface Env {
  APP_SECRET: string // Audiofy 应用密钥
  DASHSCOPE_API_KEY: string // 阿里云 DashScope API 密钥
}

// 请求体类型
interface TTSRequest {
  text: string
  voice?: string // 可选：语音角色（默认 Cherry）
}

// DashScope TTS API 请求体类型
interface DashScopeTTSRequest {
  model: string
  input: {
    text: string
    voice: string
    language_type: string // 语言类型：Chinese, English 等
  }
}

// DashScope API 响应类型
interface DashScopeTTSResponse {
  output: {
    audio: {
      url: string // 音频下载 URL（24小时有效）
      expires_at: number // URL 过期时间戳
    }
    finish_reason: string
  }
  usage: {
    characters: number
  }
  request_id: string
  [key: string]: unknown
}

// 代理响应类型
interface TTSResponse {
  audioUrl: string // 音频下载 URL（24小时有效）
  duration: number // 音频时长（秒）
}

// 错误响应类型
interface ErrorResponse {
  error: string
  code?: string
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    // 处理 OPTIONS 预检请求（CORS）
    if (request.method === 'OPTIONS') {
      return new Response(null, {
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Methods': 'POST, OPTIONS',
          'Access-Control-Allow-Headers': 'Content-Type, Authorization',
          'Access-Control-Max-Age': '86400',
        },
      })
    }

    // 1. 验证请求方法
    if (request.method !== 'POST') {
      return jsonResponse({ error: 'Method not allowed' }, 405)
    }

    // 2. 验证 APP_SECRET
    const authHeader = request.headers.get('Authorization')
    if (!authHeader || authHeader !== `Bearer ${env.APP_SECRET}`) {
      return jsonResponse({ error: 'Unauthorized' }, 401)
    }

    try {
      // 3. 解析请求体
      const body = (await request.json()) as TTSRequest

      if (!body.text || typeof body.text !== 'string' || body.text.trim().length === 0) {
        return jsonResponse({ error: 'Invalid request: text is required' }, 400)
      }

      const text = body.text.trim()
      const voice = body.voice || 'Cherry' // 默认使用 Cherry 语音（温柔女声）

      // 4. 验证语音角色（可选：添加白名单验证）
      const validVoices = [
        'Cherry',
        'Ethan',
        'Nofish',
        'Jennifer',
        'Ryan',
        'Katerina',
        'Elias',
        'Jada',
        'Dylan',
        'Sunny',
        'Li',
        'Marcus',
        'Roy',
        'Peter',
        'Rocky',
        'Kiki',
        'Eric',
      ]
      if (!validVoices.includes(voice)) {
        return jsonResponse(
          {
            error: `Invalid voice: ${voice}. Valid options: ${validVoices.join(', ')}`,
          },
          400,
        )
      }

      // 5. 构造 DashScope TTS API 请求体
      const dashScopeRequest: DashScopeTTSRequest = {
        model: 'qwen3-tts-flash', // 快速模型，适合实时应用
        input: {
          text: text,
          voice: voice, // 语音角色
          language_type: 'Chinese', // 语言类型（建议与文本语种一致，以获得正确发音和自然语调）
        },
      }

      // 6. 调用 DashScope Qwen3-TTS API
      const dashScopeResponse = await fetch(
        'https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${env.DASHSCOPE_API_KEY}`, // 标准 Bearer 认证
          },
          body: JSON.stringify(dashScopeRequest),
        },
      )

      // 7. 解析 DashScope 响应
      const dashScopeData = (await dashScopeResponse.json()) as DashScopeTTSResponse | ErrorResponse

      if (!dashScopeResponse.ok) {
        console.error('DashScope TTS API error:', dashScopeData)
        return jsonResponse(
          {
            error: 'error' in dashScopeData ? dashScopeData.error : 'TTS synthesis failed',
            code: 'code' in dashScopeData ? dashScopeData.code : undefined,
          },
          dashScopeResponse.status,
        )
      }

      // 8. 提取音频 URL 和元数据
      if (!('output' in dashScopeData) || !dashScopeData.output.audio?.url) {
        return jsonResponse(
          {
            error: 'Invalid API response: missing audio URL',
          },
          500,
        )
      }

      const audioUrl = dashScopeData.output.audio.url
      const duration = Math.ceil(text.length / 5) // 粗略估算音频时长（API 不返回 duration 字段）

      // 9. 返回音频 URL（客户端需要在24小时内下载）
      const response: TTSResponse = {
        audioUrl: audioUrl,
        duration: duration,
      }

      return jsonResponse(response, 200)
    } catch (error) {
      console.error('Worker error:', error)
      return jsonResponse(
        {
          error: error instanceof Error ? error.message : 'Internal server error',
        },
        500,
      )
    }
  },
}

/**
 * 辅助函数：返回 JSON 响应
 */
function jsonResponse(data: TTSResponse | ErrorResponse, status: number): Response {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*', // 允许跨域请求
      'Access-Control-Allow-Methods': 'POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    },
  })
}
