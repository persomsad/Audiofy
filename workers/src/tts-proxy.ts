/**
 * 豆包 TTS API 代理服务
 * 功能：保护豆包 API Key，为移动端提供安全的语音合成接口
 */

interface Env {
  DOUBAO_API_KEY: string;
  APP_SECRET: string;
}

interface TTSRequest {
  text: string;
}

interface TTSResponse {
  audioData: string; // Base64 编码的音频数据
  duration: number;  // 音频时长（秒）
}

interface DoubaoTTSRequest {
  app: {
    appid: string;
    token: string;
    cluster: string;
  };
  user: {
    uid: string;
  };
  audio: {
    voice_type: string;
    encoding: string;
    speed_ratio: number;
    volume_ratio: number;
    pitch_ratio: number;
  };
  request: {
    reqid: string;
    text: string;
    text_type: string;
    operation: string;
  };
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    // CORS 预检请求处理
    if (request.method === 'OPTIONS') {
      return handleCORS();
    }

    // 只接受 POST 请求
    if (request.method !== 'POST') {
      return jsonResponse({ error: 'Method not allowed' }, 405);
    }

    try {
      // 1. 验证请求来源
      const authHeader = request.headers.get('Authorization');
      if (!authHeader || authHeader !== `Bearer ${env.APP_SECRET}`) {
        return jsonResponse({ error: 'Unauthorized' }, 401);
      }

      // 2. 解析请求体
      const body = await request.json() as TTSRequest;
      const { text } = body;

      if (!text || typeof text !== 'string') {
        return jsonResponse({ error: 'Invalid request: text is required' }, 400);
      }

      // 检查文本长度（限制 5,000 字符，豆包 TTS 单次请求限制）
      if (text.length > 5000) {
        return jsonResponse({ error: 'Text too long (max 5,000 characters)' }, 400);
      }

      // 3. 调用豆包 TTS API
      // 注意：根据豆包 API 文档，实际的 API endpoint 和请求格式可能需要调整
      // 这里提供一个基于官方文档的示例实现
      const reqid = generateRequestId();

      const doubaoRequest: DoubaoTTSRequest = {
        app: {
          appid: 'audiofy', // 应用标识
          token: env.DOUBAO_API_KEY,
          cluster: 'volcano_tts', // 根据实际申请的集群填写
        },
        user: {
          uid: 'audiofy-user',
        },
        audio: {
          voice_type: 'zh_female_qingxin', // 清新女声
          encoding: 'wav',
          speed_ratio: 1.0, // 正常语速
          volume_ratio: 1.0, // 正常音量
          pitch_ratio: 1.0, // 正常音调
        },
        request: {
          reqid,
          text,
          text_type: 'plain',
          operation: 'query',
        },
      };

      // 豆包 TTS API endpoint（根据实际文档调整）
      const ttsResponse = await fetch('https://openspeech.bytedance.com/api/v1/tts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${env.DOUBAO_API_KEY}`,
        },
        body: JSON.stringify(doubaoRequest),
      });

      if (!ttsResponse.ok) {
        const errorText = await ttsResponse.text();
        console.error('Doubao TTS API error:', errorText);

        // 处理常见错误
        if (ttsResponse.status === 429) {
          return jsonResponse({ error: 'API rate limit exceeded, please try again later' }, 429);
        }

        return jsonResponse(
          { error: `Doubao TTS API error: ${ttsResponse.status}` },
          ttsResponse.status
        );
      }

      // 4. 处理音频数据
      const audioBuffer = await ttsResponse.arrayBuffer();
      const audioBase64 = arrayBufferToBase64(audioBuffer);

      // 估算音频时长（根据文本长度和语速估算，实际应该从 API 响应中获取）
      // 中文语速约 4-5 字/秒，这里按 4.5 字/秒估算
      const estimatedDuration = Math.ceil(text.length / 4.5);

      const response: TTSResponse = {
        audioData: audioBase64,
        duration: estimatedDuration,
      };

      return jsonResponse(response, 200);
    } catch (error) {
      console.error('Proxy error:', error);
      return jsonResponse(
        { error: 'Internal server error', details: (error as Error).message },
        500
      );
    }
  },
};

// 辅助函数：返回 JSON 响应
function jsonResponse(data: unknown, status: number): Response {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*', // 生产环境应该限制为特定域名
      'Access-Control-Allow-Methods': 'POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    },
  });
}

// 处理 CORS 预检请求
function handleCORS(): Response {
  return new Response(null, {
    status: 204,
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
      'Access-Control-Max-Age': '86400',
    },
  });
}

// 生成请求 ID
function generateRequestId(): string {
  return `audiofy-${Date.now()}-${Math.random().toString(36).substring(7)}`;
}

// 将 ArrayBuffer 转换为 Base64
function arrayBufferToBase64(buffer: ArrayBuffer): string {
  const bytes = new Uint8Array(buffer);
  let binary = '';
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}
