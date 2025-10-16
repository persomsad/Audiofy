/**
 * 音频播放器 Composable
 * 负责音频播放、暂停、进度控制
 */

import { ref, onUnmounted } from 'vue'
import { TNSPlayer } from 'nativescript-audio'
import { useStorage } from './useStorage'
import type { Article } from '../types'

/**
 * 播放器状态
 */
export type PlayerStatus = 'idle' | 'loading' | 'playing' | 'paused' | 'stopped' | 'error'

/**
 * 播放器配置
 */
export interface PlayerOptions {
  /** 是否启用进度回调（每秒触发） */
  enableProgress?: boolean
  /** 是否在播放完成后自动停止 */
  autoStopOnComplete?: boolean
}

/**
 * 播放器错误代码
 */
type PlayerErrorCode = 'LOAD_ERROR' | 'PLAY_ERROR' | 'SEEK_ERROR' | 'NOT_LOADED' | 'FILE_NOT_FOUND'

/**
 * 创建播放器错误对象
 */
function createPlayerError(code: PlayerErrorCode, message: string): Error {
  const error = new Error(message) as any
  error.code = code
  error.name = 'PlayerError'
  return error
}

/**
 * 使用音频播放器
 */
export function usePlayer(options: PlayerOptions = {}) {
  const { enableProgress = true, autoStopOnComplete = true } = options

  // 响应式状态
  const status = ref<PlayerStatus>('idle')
  const currentArticle = ref<Article | null>(null)
  const currentTime = ref(0)
  const duration = ref(0)
  const error = ref<string | null>(null)

  // 播放器实例
  let player: TNSPlayer | null = null
  let progressInterval: any = null

  // 获取存储服务
  const storage = useStorage()

  /**
   * 加载音频
   * @param articleId 文章ID
   */
  const loadAudio = async (articleId: string): Promise<void> => {
    try {
      status.value = 'loading'
      error.value = null

      // 1. 从 StorageService 获取文章元数据
      const article = await storage.getArticleById(articleId)

      if (!article) {
        throw createPlayerError('FILE_NOT_FOUND', `Article not found: ${articleId}`)
      }

      // 2. 释放旧的播放器资源
      if (player) {
        await dispose()
      }

      // 3. 初始化播放器
      player = new TNSPlayer()

      // 4. 加载音频文件
      await player.initFromFile({
        audioFile: article.audioPath,
        loop: false,
        completeCallback: () => {
          if (autoStopOnComplete) {
            stop()
          }
          console.log('[PlayerService] Playback completed')
        },
        errorCallback: (err: any) => {
          const message = err?.msg || String(err) || 'Unknown playback error'
          error.value = message
          status.value = 'error'
          console.error('[PlayerService] Playback error:', message)
        },
      })

      // 5. 获取音频时长
      const audioDuration = await player.getAudioTrackDuration()
      duration.value = Math.floor(Number(audioDuration) / 1000) // 毫秒转秒

      // 6. 保存当前文章
      currentArticle.value = article
      currentTime.value = 0
      status.value = 'stopped'

      console.log('[PlayerService] Audio loaded:', article.id, 'duration:', duration.value)
    } catch (err: any) {
      const message = err.message || String(err)
      error.value = message
      status.value = 'error'
      throw createPlayerError('LOAD_ERROR', `Failed to load audio: ${message}`)
    }
  }

  /**
   * 播放音频
   */
  const play = async (): Promise<void> => {
    try {
      if (!player) {
        throw createPlayerError('NOT_LOADED', 'No audio loaded. Call loadAudio() first.')
      }

      error.value = null
      await player.play()
      status.value = 'playing'

      // 启动进度回调
      if (enableProgress && !progressInterval) {
        progressInterval = setInterval(async () => {
          if (player && status.value === 'playing') {
            const current = await player.currentTime
            currentTime.value = Math.floor(current / 1000) // 毫秒转秒
          }
        }, 1000)
      }

      console.log('[PlayerService] Playing')
    } catch (err: any) {
      const message = err.message || String(err)
      error.value = message
      status.value = 'error'
      throw createPlayerError('PLAY_ERROR', `Failed to play audio: ${message}`)
    }
  }

  /**
   * 暂停播放
   */
  const pause = async (): Promise<void> => {
    try {
      if (!player) {
        throw createPlayerError('NOT_LOADED', 'No audio loaded')
      }

      error.value = null
      await player.pause()
      status.value = 'paused'

      // 停止进度回调
      if (progressInterval) {
        clearInterval(progressInterval)
        progressInterval = null
      }

      console.log('[PlayerService] Paused at:', currentTime.value)
    } catch (err: any) {
      const message = err.message || String(err)
      error.value = message
      throw createPlayerError('PLAY_ERROR', `Failed to pause audio: ${message}`)
    }
  }

  /**
   * 停止播放
   * 重置播放位置到 0
   */
  const stop = async (): Promise<void> => {
    try {
      if (!player) {
        return
      }

      error.value = null

      // 停止进度回调
      if (progressInterval) {
        clearInterval(progressInterval)
        progressInterval = null
      }

      await player.pause()
      await player.seekTo(0)
      currentTime.value = 0
      status.value = 'stopped'

      console.log('[PlayerService] Stopped')
    } catch (err: any) {
      const message = err.message || String(err)
      error.value = message
      console.error('[PlayerService] Stop error:', message)
    }
  }

  /**
   * 跳转到指定位置
   * @param seconds 目标位置（秒）
   */
  const seekTo = async (seconds: number): Promise<void> => {
    try {
      if (!player) {
        throw createPlayerError('NOT_LOADED', 'No audio loaded')
      }

      if (seconds < 0 || seconds > duration.value) {
        throw createPlayerError('SEEK_ERROR', `Invalid seek position: ${seconds}`)
      }

      error.value = null
      await player.seekTo(seconds * 1000) // 秒转毫秒
      currentTime.value = seconds

      console.log('[PlayerService] Seeked to:', seconds)
    } catch (err: any) {
      const message = err.message || String(err)
      error.value = message
      throw createPlayerError('SEEK_ERROR', `Failed to seek: ${message}`)
    }
  }

  /**
   * 释放播放器资源
   */
  const dispose = async (): Promise<void> => {
    try {
      // 停止进度回调
      if (progressInterval) {
        clearInterval(progressInterval)
        progressInterval = null
      }

      // 释放播放器
      if (player) {
        await player.dispose()
        player = null
      }

      // 重置状态
      status.value = 'idle'
      currentArticle.value = null
      currentTime.value = 0
      duration.value = 0
      error.value = null

      console.log('[PlayerService] Disposed')
    } catch (err: any) {
      console.error('[PlayerService] Dispose error:', err)
    }
  }

  // 页面销毁时自动释放资源
  onUnmounted(() => {
    dispose()
  })

  return {
    // 状态
    status,
    currentArticle,
    currentTime,
    duration,
    error,

    // 方法
    loadAudio,
    play,
    pause,
    stop,
    seekTo,
    dispose,
  }
}
