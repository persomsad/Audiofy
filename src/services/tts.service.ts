/**
 * TTS (Text-to-Speech) 服务
 * 调用 Cloudflare Workers 豆包 TTS API 代理
 */

import { $fetch } from 'ofetch'
import { API_CONFIG } from './config'

export interface TTSRequest {
  text: string
}

export interface TTSResponse {
  audioData: string // Base64 编码的音频数据
  duration: number // 音频时长（秒）
}

export class TTSService {
  /**
   * 将文本转换为语音
   * @param text 中文文本
   * @returns Base64 编码的音频数据和时长
   */
  static async textToSpeech(text: string): Promise<TTSResponse> {
    if (!text || text.trim().length === 0) {
      throw new Error('Text cannot be empty')
    }

    if (text.length > API_CONFIG.MAX_TEXT_LENGTH) {
      throw new Error(`Text too long (max ${API_CONFIG.MAX_TEXT_LENGTH} characters)`)
    }

    try {
      const response = await $fetch<TTSResponse>(API_CONFIG.TTS_PROXY_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${API_CONFIG.APP_SECRET}`,
        },
        body: {
          text: text.trim(),
        },
        timeout: API_CONFIG.TIMEOUT,
      })

      if (!response.audioData || !response.duration) {
        throw new Error('Invalid API response: missing audioData or duration')
      }

      return response
    } catch (error: any) {
      // 处理常见错误
      if (error.status === 401) {
        throw new Error('Authentication failed: invalid APP_SECRET')
      } else if (error.status === 429) {
        throw new Error('API rate limit exceeded, please try again later')
      } else if (error.status >= 500) {
        throw new Error('Server error, please try again later')
      } else if (error.message?.includes('timeout')) {
        throw new Error('Request timeout, please check your network connection')
      } else {
        throw new Error(`TTS generation failed: ${error.message || 'Unknown error'}`)
      }
    }
  }
}
