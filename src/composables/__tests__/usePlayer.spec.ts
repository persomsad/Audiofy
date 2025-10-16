/**
 * usePlayer Composable 单元测试
 * 覆盖：加载音频、播放控制、进度跳转、错误处理
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

// Mock dependencies BEFORE imports
vi.mock('nativescript-audio', () => ({
  TNSPlayer: vi.fn(),
}))
vi.mock('../useStorage', () => ({
  useStorage: vi.fn(),
}))

import { usePlayer } from '../usePlayer'
import { TNSPlayer } from 'nativescript-audio'
import { useStorage } from '../useStorage'

describe('usePlayer', () => {
  let mockPlayer: any
  let mockStorage: any

  beforeEach(() => {
    vi.clearAllMocks()

    // Mock TNSPlayer instance
    mockPlayer = {
      initFromFile: vi.fn().mockResolvedValue(undefined),
      play: vi.fn().mockResolvedValue(undefined),
      pause: vi.fn().mockResolvedValue(undefined),
      seekTo: vi.fn().mockResolvedValue(undefined),
      dispose: vi.fn().mockResolvedValue(undefined),
      getAudioTrackDuration: vi.fn().mockResolvedValue(10500), // 10.5 seconds in ms
      currentTime: 0,
    }

    // Mock TNSPlayer constructor
    vi.mocked(TNSPlayer).mockImplementation(() => mockPlayer)

    // Mock useStorage
    mockStorage = {
      getArticleById: vi.fn().mockResolvedValue({
        id: 'test-article-123',
        title: 'Test Article',
        originalText: 'Original text',
        translatedText: 'Translated text',
        audioPath: '/path/to/audio.mp3',
        duration: 10.5,
        status: 'completed',
        createdAt: new Date(),
      }),
      createArticle: vi.fn(),
      getAllArticles: vi.fn(),
      updateArticle: vi.fn(),
      deleteArticle: vi.fn(),
      initialize: vi.fn(),
    }
    vi.mocked(useStorage).mockReturnValue(mockStorage)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('loadAudio', () => {
    it('应该成功加载音频', async () => {
      const { loadAudio, status, currentArticle, duration } = usePlayer()

      await loadAudio('test-article-123')

      expect(mockStorage.getArticleById).toHaveBeenCalledWith('test-article-123')
      expect(mockPlayer.initFromFile).toHaveBeenCalledWith(
        expect.objectContaining({
          audioFile: '/path/to/audio.mp3',
          loop: false,
        }),
      )
      expect(mockPlayer.getAudioTrackDuration).toHaveBeenCalled()
      expect(status.value).toBe('stopped')
      expect(currentArticle.value?.id).toBe('test-article-123')
      expect(duration.value).toBe(10) // 10500ms / 1000 = 10s (floor)
    })

    it('应该处理文章不存在的情况', async () => {
      mockStorage.getArticleById.mockResolvedValueOnce(null)

      const { loadAudio, error } = usePlayer()

      await expect(loadAudio('non-existent')).rejects.toThrow('Article not found')
      expect(error.value).toContain('Article not found')
    })

    it('应该处理音频加载失败', async () => {
      mockPlayer.initFromFile.mockRejectedValueOnce(new Error('Invalid audio format'))

      const { loadAudio, error, status } = usePlayer()

      await expect(loadAudio('test-article-123')).rejects.toThrow('Failed to load audio')
      expect(error.value).toContain('Invalid audio format')
      expect(status.value).toBe('error')
    })

    it('应该在加载新音频前释放旧资源', async () => {
      const { loadAudio } = usePlayer()

      // 第一次加载
      await loadAudio('test-article-123')
      const firstPlayerInstance = mockPlayer

      // 重置 mock
      mockPlayer = {
        ...mockPlayer,
        initFromFile: vi.fn().mockResolvedValue(undefined),
        getAudioTrackDuration: vi.fn().mockResolvedValue(5000),
      }
      vi.mocked(TNSPlayer).mockImplementation(() => mockPlayer)

      // 第二次加载
      await loadAudio('test-article-456')

      // 验证旧实例被释放
      expect(firstPlayerInstance.dispose).toHaveBeenCalled()
    })
  })

  describe('play', () => {
    it('应该成功播放音频', async () => {
      const { loadAudio, play, status } = usePlayer()

      await loadAudio('test-article-123')
      await play()

      expect(mockPlayer.play).toHaveBeenCalled()
      expect(status.value).toBe('playing')
    })

    it('应该在未加载音频时抛出错误', async () => {
      const { play } = usePlayer()

      await expect(play()).rejects.toThrow('No audio loaded')
    })

    it('应该在播放时启动进度回调', async () => {
      vi.useFakeTimers()

      const { loadAudio, play, currentTime } = usePlayer({ enableProgress: true })

      await loadAudio('test-article-123')

      // 模拟播放进度
      let currentTimeMs = 0
      Object.defineProperty(mockPlayer, 'currentTime', {
        get: () => currentTimeMs,
        configurable: true,
      })

      await play()

      // 模拟时间流逝
      currentTimeMs = 3000 // 3 seconds
      await vi.advanceTimersByTimeAsync(1000)

      currentTimeMs = 5000 // 5 seconds
      await vi.advanceTimersByTimeAsync(1000)

      // 验证进度更新（可能有1秒延迟）
      expect(currentTime.value).toBeGreaterThanOrEqual(3)

      vi.useRealTimers()
    })
  })

  describe('pause', () => {
    it('应该成功暂停播放', async () => {
      const { loadAudio, play, pause, status } = usePlayer()

      await loadAudio('test-article-123')
      await play()
      await pause()

      expect(mockPlayer.pause).toHaveBeenCalled()
      expect(status.value).toBe('paused')
    })

    it('应该在未加载音频时抛出错误', async () => {
      const { pause } = usePlayer()

      await expect(pause()).rejects.toThrow('No audio loaded')
    })
  })

  describe('stop', () => {
    it('应该成功停止播放并重置进度', async () => {
      const { loadAudio, play, stop, status, currentTime } = usePlayer()

      await loadAudio('test-article-123')
      await play()

      // 模拟播放进度
      Object.defineProperty(mockPlayer, 'currentTime', {
        get: () => 5000, // 5 seconds
        configurable: true,
      })

      await stop()

      expect(mockPlayer.pause).toHaveBeenCalled()
      expect(mockPlayer.seekTo).toHaveBeenCalledWith(0)
      expect(status.value).toBe('stopped')
      expect(currentTime.value).toBe(0)
    })

    it('应该在播放完成时自动停止', async () => {
      const { loadAudio } = usePlayer({ autoStopOnComplete: true })

      await loadAudio('test-article-123')

      // 获取 completeCallback
      const initCallArgs = mockPlayer.initFromFile.mock.calls[0][0]
      const completeCallback = initCallArgs.completeCallback

      // 触发播放完成（completeCallback 调用 stop() 是异步的）
      completeCallback()

      // 等待异步操作完成
      await new Promise((resolve) => setTimeout(resolve, 10))

      // 验证自动停止
      expect(mockPlayer.pause).toHaveBeenCalled()
      expect(mockPlayer.seekTo).toHaveBeenCalledWith(0)
    })
  })

  describe('seekTo', () => {
    it('应该成功跳转到指定位置', async () => {
      const { loadAudio, seekTo, currentTime } = usePlayer()

      await loadAudio('test-article-123')
      await seekTo(5)

      expect(mockPlayer.seekTo).toHaveBeenCalledWith(5000) // 5s * 1000 = 5000ms
      expect(currentTime.value).toBe(5)
    })

    it('应该拒绝非法的跳转位置（负数）', async () => {
      const { loadAudio, seekTo } = usePlayer()

      await loadAudio('test-article-123')

      await expect(seekTo(-1)).rejects.toThrow('Invalid seek position')
    })

    it('应该拒绝超过音频时长的跳转位置', async () => {
      const { loadAudio, seekTo, duration } = usePlayer()

      await loadAudio('test-article-123')

      await expect(seekTo(duration.value + 1)).rejects.toThrow('Invalid seek position')
    })

    it('应该在未加载音频时抛出错误', async () => {
      const { seekTo } = usePlayer()

      await expect(seekTo(5)).rejects.toThrow('No audio loaded')
    })
  })

  describe('dispose', () => {
    it('应该释放播放器资源并重置状态', async () => {
      const { loadAudio, dispose, status, currentArticle, currentTime, duration } = usePlayer()

      await loadAudio('test-article-123')
      await dispose()

      expect(mockPlayer.dispose).toHaveBeenCalled()
      expect(status.value).toBe('idle')
      expect(currentArticle.value).toBeNull()
      expect(currentTime.value).toBe(0)
      expect(duration.value).toBe(0)
    })
  })

  describe('错误处理', () => {
    it('应该处理播放错误', async () => {
      mockPlayer.play.mockRejectedValueOnce(new Error('Playback failed'))

      const { loadAudio, play, error, status } = usePlayer()

      await loadAudio('test-article-123')
      await expect(play()).rejects.toThrow('Failed to play audio')
      expect(error.value).toContain('Playback failed')
      expect(status.value).toBe('error')
    })

    it('应该处理 initFromFile 的 errorCallback', async () => {
      const { loadAudio, status, error } = usePlayer()

      await loadAudio('test-article-123')

      // 获取 errorCallback
      const initCallArgs = mockPlayer.initFromFile.mock.calls[0][0]
      const errorCallback = initCallArgs.errorCallback

      // 触发播放错误
      errorCallback({ msg: 'Audio decoding error' })

      expect(status.value).toBe('error')
      expect(error.value).toContain('Audio decoding error')
    })
  })

  describe('响应式状态', () => {
    it('应该正确更新播放器状态', async () => {
      const { loadAudio, play, pause, stop, status } = usePlayer()

      expect(status.value).toBe('idle')

      // 加载中
      const loadPromise = loadAudio('test-article-123')
      expect(status.value).toBe('loading')
      await loadPromise
      expect(status.value).toBe('stopped')

      // 播放中
      await play()
      expect(status.value).toBe('playing')

      // 暂停
      await pause()
      expect(status.value).toBe('paused')

      // 停止
      await stop()
      expect(status.value).toBe('stopped')
    })
  })
})
