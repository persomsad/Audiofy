/**
 * 翻译服务
 * 调用 Cloudflare Workers Gemini API 代理
 */

import { Http } from '@nativescript/core'
import { API_CONFIG } from './config'

export interface TranslateRequest {
  text: string
}

export interface TranslateResponse {
  translatedText: string
}

export class TranslateService {
  /**
   * 翻译英文到中文
   * @param text 英文文本
   * @returns 中文翻译结果
   */
  static async translate(text: string): Promise<string> {
    if (!text || text.trim().length === 0) {
      throw new Error('Text cannot be empty')
    }

    if (text.length < API_CONFIG.MIN_TEXT_LENGTH) {
      throw new Error(`Text too short (min ${API_CONFIG.MIN_TEXT_LENGTH} characters)`)
    }

    if (text.length > API_CONFIG.MAX_TEXT_LENGTH) {
      throw new Error(`Text too long (max ${API_CONFIG.MAX_TEXT_LENGTH} characters)`)
    }

    try {
      const response = await Http.request({
        url: API_CONFIG.GEMINI_PROXY_URL,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${API_CONFIG.APP_SECRET}`,
        },
        content: JSON.stringify({
          text: text.trim(),
        }),
        timeout: API_CONFIG.TIMEOUT,
      })

      if (response.statusCode !== 200) {
        throw new Error(`HTTP ${response.statusCode}: ${response.content?.toString()}`)
      }

      const result = response.content?.toJSON() as TranslateResponse

      if (!result || !result.translatedText) {
        throw new Error('Invalid API response: missing translatedText')
      }

      return result.translatedText
    } catch (error: any) {
      // 处理常见错误
      if (error.message?.includes('HTTP 401')) {
        throw new Error('Authentication failed: invalid APP_SECRET')
      } else if (error.message?.includes('HTTP 429')) {
        throw new Error('API rate limit exceeded, please try again later')
      } else if (error.message?.includes('HTTP 5')) {
        throw new Error('Server error, please try again later')
      } else if (error.message?.includes('timeout')) {
        throw new Error('Request timeout, please check your network connection')
      } else {
        throw new Error(`Translation failed: ${error.message || 'Unknown error'}`)
      }
    }
  }
}
