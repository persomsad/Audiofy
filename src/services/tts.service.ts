/**
 * TTS (Text-to-Speech) 服务
 * 调用 Cloudflare Workers Qwen3-TTS API 代理
 */

import { Http, File } from '@nativescript/core'
import { API_CONFIG } from './config'

export interface TTSRequest {
  text: string
  voice?: string // 可选：语音角色（默认 Cherry）
}

export interface TTSResponse {
  audioUrl: string // 音频下载 URL（24小时有效）
  duration: number // 音频时长（秒）
}

export class TTSService {
  /**
   * 将文本转换为语音
   * @param text 中文文本
   * @param voice 可选：语音角色（默认 Cherry）
   * @returns 音频下载 URL 和时长
   */
  static async textToSpeech(text: string, voice: string = 'Cherry'): Promise<TTSResponse> {
    if (!text || text.trim().length === 0) {
      throw new Error('Text cannot be empty')
    }

    if (text.length > API_CONFIG.MAX_TEXT_LENGTH) {
      throw new Error(`Text too long (max ${API_CONFIG.MAX_TEXT_LENGTH} characters)`)
    }

    try {
      const response = await Http.request({
        url: API_CONFIG.TTS_PROXY_URL,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${API_CONFIG.APP_SECRET}`,
        },
        content: JSON.stringify({
          text: text.trim(),
          voice: voice,
        }),
        timeout: API_CONFIG.TIMEOUT,
      })

      if (response.statusCode !== 200) {
        throw new Error(`HTTP ${response.statusCode}: ${response.content?.toString()}`)
      }

      const result = response.content?.toJSON() as TTSResponse

      if (!result || !result.audioUrl || !result.duration) {
        throw new Error('Invalid API response: missing audioUrl or duration')
      }

      return result
    } catch (error) {
      // 处理常见错误
      const err = error as { message?: string }
      if (err.message?.includes('HTTP 401')) {
        throw new Error('Authentication failed: invalid APP_SECRET')
      } else if (err.message?.includes('HTTP 429')) {
        throw new Error('API rate limit exceeded, please try again later')
      } else if (err.message?.includes('HTTP 5')) {
        throw new Error('Server error, please try again later')
      } else if (err.message?.includes('timeout')) {
        throw new Error('Request timeout, please check your network connection')
      } else {
        throw new Error(`TTS generation failed: ${err.message || 'Unknown error'}`)
      }
    }
  }

  /**
   * 下载音频文件并保存到本地（带重试机制）
   * @param audioUrl 音频下载 URL
   * @param localPath 本地保存路径
   * @param maxRetries 最大重试次数（默认3次）
   * @returns 下载成功返回 true
   */
  static async downloadAudio(
    audioUrl: string,
    localPath: string,
    maxRetries: number = 3,
  ): Promise<boolean> {
    let lastError: Error | null = null

    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        // 使用指数退避策略：第1次立即尝试，第2次等1秒，第3次等2秒
        if (attempt > 1) {
          const delay = Math.pow(2, attempt - 2) * 1000
          await new Promise((resolve) => setTimeout(resolve, delay))
        }

        // 使用 Http.getFile 下载文件
        await Http.getFile(audioUrl, localPath)

        // 验证文件完整性
        if (!File.exists(localPath)) {
          throw new Error('File save failed: file does not exist after writing')
        }

        const savedFile = File.fromPath(localPath)
        const fileSize = savedFile.size
        if (fileSize === 0) {
          throw new Error('File save failed: file size is 0')
        }

        console.log(
          `[TTS] Audio downloaded successfully (attempt ${attempt}/${maxRetries}): ${fileSize} bytes`,
        )
        return true
      } catch (error) {
        lastError = error instanceof Error ? error : new Error(String(error))
        console.error(`[TTS] Download attempt ${attempt}/${maxRetries} failed:`, lastError.message)

        // 如果是最后一次尝试，直接抛出错误
        if (attempt === maxRetries) {
          throw new Error(
            `Failed to download audio after ${maxRetries} attempts: ${lastError?.message || 'Unknown error'}`,
          )
        }
      }
    }

    throw new Error(`Failed to download audio: ${lastError?.message || 'Unknown error'}`)
  }
}
