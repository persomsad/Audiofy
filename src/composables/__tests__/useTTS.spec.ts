/**
 * useTTS Composable 单元测试
 * 覆盖：成功场景、网络错误、API错误响应、磁盘写入失败
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useTTS } from '../useTTS'
import { $fetch } from 'ofetch'
import { SecureStorage } from '@nativescript/secure-storage'
import { knownFolders, File } from '@nativescript/core'

// Mock dependencies
vi.mock('ofetch')
vi.mock('@nativescript/secure-storage')
vi.mock('@nativescript/core', () => ({
  knownFolders: {
    documents: vi.fn(),
  },
  path: {
    join: (...args: string[]) => args.join('/'),
  },
  File: {
    fromPath: vi.fn(),
  },
}))
vi.mock('uuid', () => ({
  v4: () => 'test-uuid-1234',
}))

describe('useTTS', () => {
  let mockDocumentsFolder: any
  let mockAudiofyFolder: any
  let mockAudiosFolder: any
  let mockFile: any

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

    // Mock File
    mockFile = {
      write: vi.fn().mockResolvedValue(undefined),
    }
    vi.mocked(File.fromPath).mockReturnValue(mockFile)

    // Mock SecureStorage
    const mockGet = vi.fn().mockResolvedValue('test-secret')
    vi.mocked(SecureStorage).mockImplementation(
      () =>
        ({
          get: mockGet,
        }) as any,
    )
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('成功场景', () => {
    it('应该成功合成语音并保存文件', async () => {
      // Mock API response
      vi.mocked($fetch).mockResolvedValueOnce({
        audioData: btoa('fake audio data'),
        duration: 10.5,
      })

      const { synthesize, isLoading, error } = useTTS()

      expect(isLoading.value).toBe(false)
      expect(error.value).toBeNull()

      const text = '这是一段测试文本'
      const result = await synthesize(text)

      expect(result.audioPath).toBe('/mock/documents/Audiofy/audios/test-uuid-1234.wav')
      expect(result.duration).toBe(10.5)
      expect(isLoading.value).toBe(false)
      expect(error.value).toBeNull()

      // 验证文件被写入
      expect(mockFile.write).toHaveBeenCalled()
    })

    it('应该在安全存储失败时使用默认APP_SECRET', async () => {
      // Mock API response
      vi.mocked($fetch).mockResolvedValueOnce({
        audioData: btoa('fake audio data'),
        duration: 10.5,
      })

      // Mock SecureStorage 抛出错误
      const mockGet = vi.fn().mockRejectedValue(new Error('Storage error'))
      vi.mocked(SecureStorage).mockImplementation(
        () =>
          ({
            get: mockGet,
          }) as any,
      )

      const { synthesize } = useTTS()
      const text = '测试文本'

      const result = await synthesize(text)

      expect(result.audioPath).toBe('/mock/documents/Audiofy/audios/test-uuid-1234.wav')
      expect($fetch).toHaveBeenCalled()
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
      vi.mocked($fetch).mockRejectedValueOnce({
        status: 401,
        message: 'Unauthorized',
      })

      const { synthesize, error } = useTTS({ maxRetries: 0 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Authentication failed')
      expect(error.value).toContain('Authentication failed')
    })

    it('应该在429错误时重试并最终失败', async () => {
      vi.mocked($fetch).mockRejectedValue({
        status: 429,
        message: 'Rate limit exceeded',
      })

      const { synthesize, error } = useTTS({ maxRetries: 1 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('API rate limit exceeded')
      expect(error.value).toContain('API rate limit exceeded')
      expect($fetch).toHaveBeenCalledTimes(2) // 初始请求 + 1次重试
    })

    it('应该在500错误时重试并最终失败', async () => {
      vi.mocked($fetch).mockRejectedValue({
        status: 500,
        message: 'Internal server error',
      })

      const { synthesize, error } = useTTS({ maxRetries: 1 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Server error')
      expect(error.value).toContain('Server error')
      expect($fetch).toHaveBeenCalledTimes(2) // 初始请求 + 1次重试
    })

    it('应该处理timeout错误', async () => {
      vi.mocked($fetch).mockRejectedValue({
        message: 'timeout of 60000ms exceeded',
      })

      const { synthesize, error } = useTTS({ maxRetries: 0 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Request timeout')
      expect(error.value).toContain('Request timeout')
    })

    it('应该处理无效的API响应', async () => {
      vi.mocked($fetch).mockResolvedValueOnce({
        // 缺少 audioData 字段
        duration: 10,
      })

      const { synthesize, error } = useTTS({ maxRetries: 0 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Invalid API response')
      expect(error.value).toContain('Invalid API response')
    })
  })

  describe('文件系统错误处理', () => {
    it('应该处理磁盘空间不足错误', async () => {
      // Mock API success
      vi.mocked($fetch).mockResolvedValueOnce({
        audioData: btoa('fake audio data'),
        duration: 10.5,
      })

      // Mock file write failure with ENOSPC error
      mockFile.write.mockRejectedValueOnce(new Error('ENOSPC: no space left on device'))

      const { synthesize, error } = useTTS({ maxRetries: 0 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Disk space insufficient')
      expect(error.value).toContain('Disk space insufficient')
    })

    it('应该处理文件写入失败错误', async () => {
      // Mock API success
      vi.mocked($fetch).mockResolvedValueOnce({
        audioData: btoa('fake audio data'),
        duration: 10.5,
      })

      // Mock file write failure
      mockFile.write.mockRejectedValueOnce(new Error('Permission denied'))

      const { synthesize, error } = useTTS({ maxRetries: 0 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Failed to save audio file')
      expect(error.value).toContain('Failed to save audio file')
    })
  })

  describe('重试机制', () => {
    it('应该在第二次重试时成功', async () => {
      vi.mocked($fetch)
        .mockRejectedValueOnce({
          status: 500,
          message: 'Server error',
        })
        .mockResolvedValueOnce({
          audioData: btoa('fake audio data'),
          duration: 10.5,
        })

      const { synthesize } = useTTS({ maxRetries: 1 })
      const text = '测试文本'

      const result = await synthesize(text)

      expect(result.audioPath).toBe('/mock/documents/Audiofy/audios/test-uuid-1234.wav')
      expect($fetch).toHaveBeenCalledTimes(2) // 第1次失败，第2次成功
    })

    it('应该不重试401错误', async () => {
      vi.mocked($fetch).mockRejectedValue({
        status: 401,
        message: 'Unauthorized',
      })

      const { synthesize } = useTTS({ maxRetries: 1 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Authentication failed')
      expect($fetch).toHaveBeenCalledTimes(1) // 只调用1次，不重试
    })

    it('应该不重试磁盘空间不足错误', async () => {
      // Mock API success
      vi.mocked($fetch).mockResolvedValue({
        audioData: btoa('fake audio data'),
        duration: 10.5,
      })

      // Mock file write failure
      mockFile.write.mockRejectedValue(new Error('ENOSPC: no space left'))

      const { synthesize } = useTTS({ maxRetries: 1 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Disk space insufficient')
      expect($fetch).toHaveBeenCalledTimes(1) // API只调用1次
    })

    it('应该遵守maxRetries配置', async () => {
      vi.mocked($fetch).mockRejectedValue({
        status: 500,
        message: 'Server error',
      })

      const { synthesize } = useTTS({ maxRetries: 2 })
      const text = '测试文本'

      await expect(synthesize(text)).rejects.toThrow('Server error')
      expect($fetch).toHaveBeenCalledTimes(3) // 初始请求 + 2次重试
    })
  })

  describe('响应式状态', () => {
    it('应该在加载期间更新isLoading状态', async () => {
      vi.mocked($fetch).mockImplementation(
        () =>
          new Promise((resolve) => {
            setTimeout(
              () =>
                resolve({
                  audioData: btoa('fake audio data'),
                  duration: 10.5,
                }),
              100,
            )
          }),
      )

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
      vi.mocked($fetch).mockResolvedValueOnce({
        audioData: btoa('fake audio data'),
        duration: 10.5,
      })

      const { synthesize } = useTTS()
      const text = '测试文本'

      const result = await synthesize(text)

      // 验证目录结构
      expect(mockDocumentsFolder.getFolder).toHaveBeenCalledWith('Audiofy')
      expect(mockAudiofyFolder.getFolder).toHaveBeenCalledWith('audios')

      // 验证文件路径
      expect(result.audioPath).toContain('/Audiofy/audios/')
      expect(result.audioPath).toContain('.wav')
    })
  })
})
