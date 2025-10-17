/**
 * useTTS Composable 单元测试
 * 覆盖：成功场景、网络错误、API错误响应、磁盘写入失败
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useTTS } from '../useTTS'
import { TTSService } from '@/services/tts.service'
import { knownFolders } from '@nativescript/core'

// Mock dependencies
vi.mock('@/services/tts.service')
vi.mock('@nativescript/core', () => ({
  knownFolders: {
    documents: vi.fn(),
  },
  path: {
    join: (...args: string[]) => args.join('/'),
  },
}))
vi.mock('@/utils/uuid', () => ({
  generateUUID: () => 'test-uuid-1234',
}))

describe('useTTS', () => {
  let mockDocumentsFolder: any
  let mockAudiofyFolder: any
  let mockAudiosFolder: any

  beforeEach(() => {
    vi.clearAllMocks()

    // Mock folder structure
    mockAudiosFolder = {
      path: '/mock/documents/Audiofy/audios',
    }

    mockAudiofyFolder = {
      getFolder: vi.fn().mockReturnValue(mockAudiosFolder),
    }

    mockDocumentsFolder = {
      path: '/mock/documents',
      getFolder: vi.fn().mockReturnValue(mockAudiofyFolder),
    }

    vi.mocked(knownFolders.documents).mockReturnValue(mockDocumentsFolder)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('成功场景', () => {
    it('应该成功合成语音并保存文件', async () => {
      // Mock TTSService
      vi.mocked(TTSService.textToSpeech).mockResolvedValueOnce({
        audioUrl: 'https://example.com/audio.mp3',
        duration: 10.5,
      })
      vi.mocked(TTSService.downloadAudio).mockResolvedValueOnce(true)

      const { synthesize, isLoading, error } = useTTS()

      expect(isLoading.value).toBe(false)
      expect(error.value).toBeNull()

      const text = '这是一段测试文本'
      const result = await synthesize(text)

      expect(result.audioPath).toBe('/mock/documents/Audiofy/audios/test-uuid-1234.mp3')
      expect(result.duration).toBe(10.5)
      expect(isLoading.value).toBe(false)
      expect(error.value).toBeNull()

      // 验证 TTSService 被调用
      expect(TTSService.textToSpeech).toHaveBeenCalledWith(text, 'Cherry')
      expect(TTSService.downloadAudio).toHaveBeenCalled()
    })
  })

  describe('输入验证', () => {
    it('应该拒绝空文本', async () => {
      const { synthesize } = useTTS()

      await expect(synthesize('')).rejects.toThrow('Text cannot be empty')
      await expect(synthesize('   ')).rejects.toThrow('Text cannot be empty')
    })

    it('应该拒绝太长的文本', async () => {
      const { synthesize, error } = useTTS()

      const longText = 'a'.repeat(5001)
      await expect(synthesize(longText)).rejects.toThrow('Text too long')
      expect(error.value).toContain('Text too long')
    })
  })

  describe('API错误处理', () => {
    it('应该处理401 Unauthorized错误', async () => {
      vi.mocked(TTSService.textToSpeech).mockRejectedValueOnce(new Error('Authentication failed'))

      const { synthesize, error } = useTTS({ maxRetries: 0 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Authentication failed')
      expect(error.value).toContain('Authentication failed')
    })

    it('应该在429错误时重试并最终失败', async () => {
      vi.mocked(TTSService.textToSpeech).mockRejectedValue(new Error('rate limit exceeded'))

      const { synthesize, error } = useTTS({ maxRetries: 1 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('API rate limit exceeded')
      expect(error.value).toContain('API rate limit exceeded')
      expect(TTSService.textToSpeech).toHaveBeenCalledTimes(2) // 初始请求 + 1次重试
    })

    it('应该在500错误时重试并最终失败', async () => {
      vi.mocked(TTSService.textToSpeech).mockRejectedValue(new Error('Server error'))

      const { synthesize, error } = useTTS({ maxRetries: 1 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Server error')
      expect(error.value).toContain('Server error')
      expect(TTSService.textToSpeech).toHaveBeenCalledTimes(2) // 初始请求 + 1次重试
    })

    it('应该处理timeout错误', async () => {
      vi.mocked(TTSService.textToSpeech).mockRejectedValue(new Error('timeout of 60000ms exceeded'))

      const { synthesize, error } = useTTS({ maxRetries: 0 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Request timeout')
      expect(error.value).toContain('Request timeout')
    })

    it('应该处理下载失败错误', async () => {
      vi.mocked(TTSService.textToSpeech).mockResolvedValueOnce({
        audioUrl: 'https://example.com/audio.mp3',
        duration: 10.5,
      })
      vi.mocked(TTSService.downloadAudio).mockRejectedValueOnce(
        new Error('Failed to download audio'),
      )

      const { synthesize, error } = useTTS({ maxRetries: 0 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Failed to download audio')
      expect(error.value).toContain('Failed to download audio')
    })
  })

  describe('重试机制', () => {
    it('应该在第二次重试时成功', async () => {
      vi.mocked(TTSService.textToSpeech)
        .mockRejectedValueOnce(new Error('Server error'))
        .mockResolvedValueOnce({
          audioUrl: 'https://example.com/audio.mp3',
          duration: 10.5,
        })
      vi.mocked(TTSService.downloadAudio).mockResolvedValueOnce(true)

      const { synthesize } = useTTS({ maxRetries: 1 })
      const text = '测试文本'

      const result = await synthesize(text)

      expect(result.audioPath).toBe('/mock/documents/Audiofy/audios/test-uuid-1234.mp3')
      expect(TTSService.textToSpeech).toHaveBeenCalledTimes(2) // 第1次失败，第2次成功
    })

    it('应该不重试401错误', async () => {
      vi.mocked(TTSService.textToSpeech).mockRejectedValue(new Error('Authentication failed'))

      const { synthesize } = useTTS({ maxRetries: 1 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Authentication failed')
      expect(TTSService.textToSpeech).toHaveBeenCalledTimes(1) // 只调用1次，不重试
    })

    it('应该不重试磁盘空间不足错误', async () => {
      vi.mocked(TTSService.textToSpeech).mockResolvedValue({
        audioUrl: 'https://example.com/audio.mp3',
        duration: 10.5,
      })
      vi.mocked(TTSService.downloadAudio).mockRejectedValue(new Error('ENOSPC: no space left'))

      const { synthesize } = useTTS({ maxRetries: 1 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Disk space insufficient')
      expect(TTSService.textToSpeech).toHaveBeenCalledTimes(1) // 只调用1次
    })

    it('应该遵守maxRetries配置', async () => {
      vi.mocked(TTSService.textToSpeech).mockRejectedValue(new Error('Server error'))

      const { synthesize } = useTTS({ maxRetries: 2 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Server error')
      expect(TTSService.textToSpeech).toHaveBeenCalledTimes(3) // 初始请求 + 2次重试
    })
  })

  describe('响应式状态', () => {
    it('应该在加载期间更新isLoading状态', async () => {
      vi.mocked(TTSService.textToSpeech).mockImplementation(
        () =>
          new Promise((resolve) => {
            setTimeout(
              () =>
                resolve({
                  audioUrl: 'https://example.com/audio.mp3',
                  duration: 10.5,
                }),
              100,
            )
          }),
      )
      vi.mocked(TTSService.downloadAudio).mockResolvedValue(true)

      const { synthesize, isLoading } = useTTS()
      const text = '测试文本'

      expect(isLoading.value).toBe(false)

      const promise = synthesize(text)
      expect(isLoading.value).toBe(true)

      await promise
      expect(isLoading.value).toBe(false)
    })
  })

  describe('文件路径生成', () => {
    it('应该生成正确的文件路径结构', async () => {
      vi.mocked(TTSService.textToSpeech).mockResolvedValueOnce({
        audioUrl: 'https://example.com/audio.mp3',
        duration: 10.5,
      })
      vi.mocked(TTSService.downloadAudio).mockResolvedValueOnce(true)

      const { synthesize } = useTTS()
      const text = '测试文本'

      const result = await synthesize(text)

      // 验证目录结构
      expect(mockDocumentsFolder.getFolder).toHaveBeenCalledWith('Audiofy')
      expect(mockAudiofyFolder.getFolder).toHaveBeenCalledWith('audios')

      // 验证文件路径
      expect(result.audioPath).toContain('/Audiofy/audios/')
      expect(result.audioPath).toContain('.mp3')
    })
  })
})
