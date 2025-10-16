/**
 * useTTS Composable
 * Qwen3-TTS 服务的 Vue 3 Composition API 封装
 * 支持：音频文件下载保存、重试机制、超时控制、安全存储
 */

import { ref } from 'vue'
import { knownFolders, path } from '@nativescript/core'
import { v4 as uuidv4 } from 'uuid'
import { API_CONFIG } from '@/services/config'
import { TTSService, TTSResponse } from '@/services/tts.service'

// 类型定义
export interface TTSResult {
  audioPath: string // 本地音频文件路径
  duration: number // 音频时长（秒）
}

export interface UseTTSOptions {
  maxRetries?: number // 最大重试次数，默认1次
  voice?: string // 语音角色，默认 Cherry
}

/**
 * useTTS Composable
 * @param options 配置选项
 * @returns synthesize 方法和响应式状态
 */
export function useTTS(options: UseTTSOptions = {}) {
  const { maxRetries = 1, voice = 'Cherry' } = options

  // 响应式状态
  const isLoading = ref(false)
  const error = ref<string | null>(null)

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

      // 带重试的TTS逻辑
      let lastError: Error | null = null
      for (let attempt = 0; attempt <= maxRetries; attempt++) {
        try {
          // 1. 调用TTS API获取音频URL
          const response: TTSResponse = await TTSService.textToSpeech(text, voice)

          // 2. 确保音频目录存在
          const audioFolderPath = ensureAudioDirectory()

          // 3. 生成唯一文件名（Qwen3-TTS返回MP3格式）
          const fileName = `${uuidv4()}.mp3`
          const audioPath = path.join(audioFolderPath, fileName)

          // 4. 下载音频文件并保存到本地（带内置重试机制：3次）
          await TTSService.downloadAudio(response.audioUrl, audioPath, 3)

          isLoading.value = false
          return {
            audioPath,
            duration: response.duration,
          }
        } catch (err) {
          lastError = err instanceof Error ? err : new Error(String(err))

          // 不可重试的错误类型
          if (err.message?.includes('Authentication failed')) {
            throw new Error('Authentication failed: invalid APP_SECRET')
          }

          if (err.message?.includes('Disk space insufficient') || err.message?.includes('ENOSPC')) {
            throw new Error('Disk space insufficient, please free up space')
          }

          // 可重试的错误类型（429, 500, timeout, download failure）
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
      if (lastError && lastError.message?.includes('rate limit')) {
        throw new Error('API rate limit exceeded, please try again later')
      } else if (lastError && lastError.message?.includes('Server error')) {
        throw new Error('Server error, please try again later')
      } else if (lastError && lastError.message?.includes('timeout')) {
        throw new Error('Request timeout, please check your network connection')
      } else if (lastError && lastError.message?.includes('download audio')) {
        throw new Error(
          'Failed to download audio file. Please check your network connection and try again.',
        )
      } else {
        throw new Error(`TTS synthesis failed: ${lastError?.message || 'Unknown error'}`)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'TTS synthesis failed'
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
