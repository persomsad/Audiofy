/**
 * useTranslate Composable
 * 翻译服务的 Vue 3 Composition API 封装
 * 支持：重试机制、超时控制、安全存储
 */

import { ref } from 'vue'
import { Http } from '@nativescript/core'
import { SecureStorage } from '@nativescript/secure-storage'
import { API_CONFIG } from '@/services/config'

// 初始化安全存储
const secureStorage = new SecureStorage()

// 类型定义
export interface TranslateRequest {
  text: string
}

export interface TranslateResponse {
  translatedText: string
}

export interface UseTranslateOptions {
  maxRetries?: number // 最大重试次数，默认2次
  timeout?: number // 超时时间(毫秒)，默认30000
}

/**
 * useTranslate Composable
 * @param options 配置选项
 * @returns translate 方法和响应式状态
 */
export function useTranslate(options: UseTranslateOptions = {}) {
  const { maxRetries = 2, timeout = API_CONFIG.TIMEOUT } = options

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
   * 翻译文本（单次尝试）
   * @param text 英文文本
   * @param appSecret APP密钥
   * @returns 中文翻译结果
   */
  async function translateOnce(text: string, appSecret: string): Promise<string> {
    const response = await Http.request({
      url: API_CONFIG.GEMINI_PROXY_URL,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${appSecret}`,
      },
      content: JSON.stringify({
        text: text.trim(),
      }),
      timeout,
    })

    if (response.statusCode !== 200) {
      throw new Error(`HTTP ${response.statusCode}: ${response.content?.toString()}`)
    }

    const result = response.content?.toJSON() as TranslateResponse

    if (!result || !result.translatedText) {
      throw new Error('Invalid API response: missing translatedText')
    }

    return result.translatedText
  }

  /**
   * 翻译文本（带重试机制）
   * @param text 英文文本
   * @returns 中文翻译结果
   */
  async function translate(text: string): Promise<string> {
    isLoading.value = true
    error.value = null

    try {
      // 验证输入
      if (!text || text.trim().length === 0) {
        throw new Error('Text cannot be empty')
      }

      if (text.length < API_CONFIG.MIN_TEXT_LENGTH) {
        throw new Error(`Text too short (min ${API_CONFIG.MIN_TEXT_LENGTH} characters)`)
      }

      if (text.length > API_CONFIG.MAX_TEXT_LENGTH) {
        throw new Error(`Text too long (max ${API_CONFIG.MAX_TEXT_LENGTH} characters)`)
      }

      // 获取APP_SECRET
      const appSecret = await getAppSecret()

      // 带重试的翻译逻辑
      let lastError: any
      for (let attempt = 0; attempt <= maxRetries; attempt++) {
        try {
          const result = await translateOnce(text, appSecret)
          isLoading.value = false
          return result
        } catch (err: any) {
          lastError = err

          // 不可重试的错误类型
          if (err.message?.includes('HTTP 401')) {
            throw new Error('Authentication failed: invalid APP_SECRET')
          }

          // 可重试的错误类型（429, 500, timeout）
          if (attempt < maxRetries) {
            // 指数退避：等待 2^attempt 秒
            const waitTime = Math.pow(2, attempt) * 1000
            console.log(`Translation attempt ${attempt + 1} failed, retrying in ${waitTime}ms...`)
            await new Promise((resolve) => setTimeout(resolve, waitTime))
            continue
          }

          // 最后一次尝试失败，抛出友好错误
          break
        }
      }

      // 所有重试都失败，处理最终错误
      if (lastError.message?.includes('HTTP 429')) {
        throw new Error('API rate limit exceeded, please try again later')
      } else if (lastError.message?.includes('HTTP 5')) {
        throw new Error('Server error, please try again later')
      } else if (lastError.message?.includes('timeout')) {
        throw new Error('Request timeout, please check your network connection')
      } else {
        throw new Error(`Translation failed: ${lastError.message || 'Unknown error'}`)
      }
    } catch (err: any) {
      const errorMessage = err.message || 'Translation failed'
      error.value = errorMessage
      isLoading.value = false
      throw err
    }
  }

  return {
    translate,
    isLoading,
    error,
  }
}
