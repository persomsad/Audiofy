/**
 * useTTS Composable
 * 豆包 TTS 服务的 Vue 3 Composition API 封装
 * 支持：音频文件保存、重试机制、超时控制、安全存储
 */

import { ref } from 'vue'
import { $fetch } from 'ofetch'
import { SecureStorage } from '@nativescript/secure-storage'
import { knownFolders, path, File } from '@nativescript/core'
import { v4 as uuidv4 } from 'uuid'
import { API_CONFIG } from '@/services/config'

// 初始化安全存储
const secureStorage = new SecureStorage()

// 类型定义
export interface TTSRequest {
  text: string
}

export interface TTSResponse {
  audioData: string // Base64 编码的音频数据
  duration: number // 音频时长（秒）
}

export interface TTSResult {
  audioPath: string // 本地音频文件路径
  duration: number // 音频时长（秒）
}

export interface UseTTSOptions {
  maxRetries?: number // 最大重试次数，默认1次
  timeout?: number // 超时时间(毫秒)，默认60000 (60秒，TTS生成较慢)
}

/**
 * useTTS Composable
 * @param options 配置选项
 * @returns synthesize 方法和响应式状态
 */
export function useTTS(options: UseTTSOptions = {}) {
  const { maxRetries = 1, timeout = 60000 } = options

  // 响应式状态
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  /**
   * 从安全存储读取 APP_SECRET
   */
  async function getAppSecret(): Promise<string> {
    try {
      const secret = await secureStorage.get({ key: 'APP_SECRET' })
      if (!secret) {
        throw new Error('APP_SECRET not found in secure storage')
      }
      return secret
    } catch (err: any) {
      // 如果安全存储中没有，使用配置文件中的默认值
      console.warn('Failed to read APP_SECRET from secure storage, using default:', err.message)
      return API_CONFIG.APP_SECRET
    }
  }

  /**
   * 确保音频目录存在
   */
  function ensureAudioDirectory(): string {
    const documentsFolder = knownFolders.documents()
    const audioFolderPath = path.join(documentsFolder.path, 'Audiofy', 'audios')

    // 检查目录是否存在，不存在则创建（调用getFolder会自动创建）
    const _audioFolder = documentsFolder.getFolder('Audiofy').getFolder('audios')

    return audioFolderPath
  }

  /**
   * Base64 解码并保存音频文件
   * @param audioData Base64 编码的音频数据
   * @returns 音频文件路径
   */
  async function saveAudioFile(audioData: string): Promise<string> {
    try {
      // 确保音频目录存在
      const audioFolderPath = ensureAudioDirectory()

      // 生成唯一文件名
      const fileName = `${uuidv4()}.wav`
      const filePath = path.join(audioFolderPath, fileName)

      // Base64 解码
      const binaryData = atob(audioData)
      const bytes = new Uint8Array(binaryData.length)
      for (let i = 0; i < binaryData.length; i++) {
        bytes[i] = binaryData.charCodeAt(i)
      }

      // 写入文件
      const file = File.fromPath(filePath)
      await file.write(bytes)

      return filePath
    } catch (err: any) {
      if (err.message?.includes('ENOSPC') || err.message?.includes('No space left')) {
        throw new Error('Disk space insufficient')
      }
      throw new Error(`Failed to save audio file: ${err.message}`)
    }
  }

  /**
   * 合成语音（单次尝试）
   * @param text 中文文本
   * @param appSecret APP密钥
   * @returns TTS响应（Base64音频数据和时长）
   */
  async function synthesizeOnce(text: string, appSecret: string): Promise<TTSResponse> {
    const response = await $fetch<TTSResponse>(API_CONFIG.TTS_PROXY_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${appSecret}`,
      },
      body: {
        text: text.trim(),
      },
      timeout,
    })

    if (!response.audioData || !response.duration) {
      throw new Error('Invalid API response: missing audioData or duration')
    }

    return response
  }

  /**
   * 合成语音（带重试机制）
   * @param text 中文文本
   * @returns 音频文件路径和时长
   */
  async function synthesize(text: string): Promise<TTSResult> {
    isLoading.value = true
    error.value = null

    try {
      // 验证输入
      if (!text || text.trim().length === 0) {
        throw new Error('Text cannot be empty')
      }

      if (text.length > API_CONFIG.MAX_TEXT_LENGTH) {
        throw new Error(`Text too long (max ${API_CONFIG.MAX_TEXT_LENGTH} characters)`)
      }

      // 获取APP_SECRET
      const appSecret = await getAppSecret()

      // 带重试的TTS逻辑
      let lastError: any
      for (let attempt = 0; attempt <= maxRetries; attempt++) {
        try {
          // 调用TTS API
          const response = await synthesizeOnce(text, appSecret)

          // 保存音频文件
          const audioPath = await saveAudioFile(response.audioData)

          isLoading.value = false
          return {
            audioPath,
            duration: response.duration,
          }
        } catch (err: any) {
          lastError = err

          // 不可重试的错误类型
          if (err.status === 401) {
            throw new Error('Authentication failed: invalid APP_SECRET')
          }

          if (err.message?.includes('Disk space insufficient')) {
            throw new Error('Disk space insufficient, please free up space')
          }

          // 可重试的错误类型（429, 500, timeout）
          if (attempt < maxRetries) {
            // 指数退避：等待 2^attempt 秒
            const waitTime = Math.pow(2, attempt) * 1000
            console.log(`TTS attempt ${attempt + 1} failed, retrying in ${waitTime}ms...`)
            await new Promise((resolve) => setTimeout(resolve, waitTime))
            continue
          }

          // 最后一次尝试失败，抛出友好错误
          break
        }
      }

      // 所有重试都失败，处理最终错误
      if (lastError.status === 429) {
        throw new Error('API rate limit exceeded, please try again later')
      } else if (lastError.status >= 500) {
        throw new Error('Server error, please try again later')
      } else if (lastError.message?.includes('timeout')) {
        throw new Error('Request timeout, please check your network connection')
      } else {
        throw new Error(`TTS synthesis failed: ${lastError.message || 'Unknown error'}`)
      }
    } catch (err: any) {
      const errorMessage = err.message || 'TTS synthesis failed'
      error.value = errorMessage
      isLoading.value = false
      throw err
    }
  }

  return {
    synthesize,
    isLoading,
    error,
  }
}
