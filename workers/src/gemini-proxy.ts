/**
 * Gemini API 代理服务
 * 功能：保护 Gemini API Key，为移动端提供安全的翻译接口
 */

interface Env {
  GEMINI_API_KEY: string;
  APP_SECRET: string;
}

interface TranslateRequest {
  text: string;
}

interface TranslateResponse {
  translatedText: string;
}

interface GeminiResponse {
  candidates: Array<{
    content: {
      parts: Array<{
        text: string;
      }>;
    };
  }>;
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
      // 1. 验证请求来源（Bearer Token 认证）
      const authHeader = request.headers.get('Authorization');
      if (!authHeader || authHeader !== `Bearer ${env.APP_SECRET}`) {
        return jsonResponse({ error: 'Unauthorized' }, 401);
      }

      // 2. 解析请求体
      const body = await request.json() as TranslateRequest;
      const { text } = body;

      if (!text || typeof text !== 'string') {
        return jsonResponse({ error: 'Invalid request: text is required' }, 400);
      }

      // 检查文本长度（限制 10,000 字符，避免超额费用）
      if (text.length > 10000) {
        return jsonResponse({ error: 'Text too long (max 10,000 characters)' }, 400);
      }

      // 3. 调用 Gemini API
      const geminiResponse = await fetch(
        'https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'x-goog-api-key': env.GEMINI_API_KEY,
          },
          body: JSON.stringify({
            contents: [
              {
                parts: [
                  {
                    text: `请将以下英文翻译成中文，保持专业术语准确，适合口语播报，不要添加任何额外解释或标题：\n\n${text}`,
                  },
                ],
              },
            ],
            generationConfig: {
              temperature: 0.3, // 降低随机性，提高翻译一致性
              maxOutputTokens: 2048,
            },
          }),
        }
      );

      if (!geminiResponse.ok) {
        const errorText = await geminiResponse.text();
        console.error('Gemini API error:', errorText);

        // 处理常见错误
        if (geminiResponse.status === 429) {
          return jsonResponse({ error: 'API rate limit exceeded, please try again later' }, 429);
        }

        return jsonResponse(
          { error: `Gemini API error: ${geminiResponse.status}` },
          geminiResponse.status
        );
      }

      // 4. 解析并返回翻译结果
      const data = await geminiResponse.json() as GeminiResponse;

      if (!data.candidates || data.candidates.length === 0) {
        return jsonResponse({ error: 'No translation generated' }, 500);
      }

      const translatedText = data.candidates[0].content.parts[0].text;

      const response: TranslateResponse = {
        translatedText: translatedText.trim(),
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
      'Access-Control-Max-Age': '86400', // 24小时
    },
  });
}
