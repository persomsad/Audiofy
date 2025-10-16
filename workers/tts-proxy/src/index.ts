/**
 * 豆包 TTS Cloudflare Workers 代理
 * 基于火山引擎语音合成 API
 * 文档: https://www.volcengine.com/docs/6561/1257584
 */

// 环境变量类型定义
interface Env {
  APP_SECRET: string // Audiofy 应用密钥
  DOUBAO_APPID: string // 豆包应用ID
  DOUBAO_TOKEN: string // 豆包访问令牌
  DOUBAO_CLUSTER: string // 豆包集群ID
}

// 请求体类型
interface TTSRequest {
  text: string
}

// 豆包 API 请求体类型
interface DoubaoTTSRequest {
  app: {
    appid: string
    token: string
    cluster: string
  }
  user: {
    uid: string
  }
  audio: {
    voice_type: string
    encoding: string
    speed_ratio: number
    volume_ratio: number
    pitch_ratio: number
  }
  request: {
    reqid: string
    text: string
    text_type: string
    operation: string
  }
}

// 豆包 API 响应类型
interface DoubaoTTSResponse {
  data: string // Base64 编码的音频数据
  [key: string]: unknown
}

// 代理响应类型
interface TTSResponse {
  audioData: string // Base64 编码的音频数据
  duration: number // 音频时长（秒）
}

// 错误响应类型
interface ErrorResponse {
  error: string
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
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

      // 4. 生成唯一请求ID
      const reqid = `audiofy-${Date.now()}-${Math.random().toString(36).substring(7)}`

      // 5. 构造豆包 TTS API 请求体
      const doubaoRequest: DoubaoTTSRequest = {
        app: {
          appid: env.DOUBAO_APPID,
          token: env.DOUBAO_TOKEN,
          cluster: env.DOUBAO_CLUSTER,
        },
        user: {
          uid: 'audiofy-user',
        },
        audio: {
          voice_type: 'BV001_streaming', // 语音类型（可配置）
          encoding: 'mp3', // 音频格式: mp3, wav, ogg
          speed_ratio: 1.0, // 语速倍率 (0.5-2.0)
          volume_ratio: 1.0, // 音量倍率 (0.1-3.0)
          pitch_ratio: 1.0, // 音调倍率 (0.5-2.0)
        },
        request: {
          reqid: reqid,
          text: text,
          text_type: 'plain', // 文本类型: plain/ssml
          operation: 'submit', // 操作类型: submit(提交)/query(查询)
        },
      }

      // 6. 调用豆包 TTS API
      const doubaoResponse = await fetch('https://openspeech.bytedance.com/api/v1/tts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          // 豆包API使用特殊的Bearer格式: "Bearer; {token}"
          Authorization: `Bearer; ${env.DOUBAO_TOKEN}`,
        },
        body: JSON.stringify(doubaoRequest),
      })

      // 7. 解析豆包响应
      const doubaoData = (await doubaoResponse.json()) as DoubaoTTSResponse | ErrorResponse

      if (!doubaoResponse.ok) {
        console.error('Doubao TTS API error:', doubaoData)
        return jsonResponse(
          {
            error: 'error' in doubaoData ? doubaoData.error : 'TTS synthesis failed',
          },
          doubaoResponse.status,
        )
      }

      // 8. 提取音频数据
      if (!('data' in doubaoData) || !doubaoData.data) {
        return jsonResponse(
          {
            error: 'Invalid API response: missing audio data',
          },
          500,
        )
      }

      const audioData = doubaoData.data

      // 9. 估算音频时长（基于文本长度）
      // 注意：豆包API可能不直接返回时长，这里使用粗略估算
      // 更准确的方法是解码音频文件头获取时长
      const estimatedDuration = Math.ceil(text.length / 5) // 粗略估算：5字符/秒

      // 10. 返回音频数据
      const response: TTSResponse = {
        audioData: audioData,
        duration: estimatedDuration,
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
