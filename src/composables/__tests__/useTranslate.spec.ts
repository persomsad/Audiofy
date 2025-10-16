/**
 * useTranslate Composable 单元测试
 * 覆盖：成功场景、网络错误、API错误响应
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useTranslate } from '../useTranslate'
import { $fetch } from 'ofetch'
import { SecureStorage } from '@nativescript/secure-storage'

// Mock dependencies
vi.mock('ofetch')
vi.mock('@nativescript/secure-storage')

describe('useTranslate', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('成功场景', () => {
    it('应该成功翻译文本', async () => {
      // Mock API response
      vi.mocked($fetch).mockResolvedValueOnce({
        translatedText: '这是翻译后的中文',
      })

      // Mock SecureStorage
      const mockGet = vi.fn().mockResolvedValue('test-secret')
      vi.mocked(SecureStorage).mockImplementation(
        () =>
          ({
            get: mockGet,
          }) as any,
      )

      const { translate, isLoading, error } = useTranslate()

      expect(isLoading.value).toBe(false)
      expect(error.value).toBeNull()

      const text = '这是一个测试文本'.repeat(15) // 确保>100字符
      const result = await translate(text)

      expect(result).toBe('这是翻译后的中文')
      expect(isLoading.value).toBe(false)
      expect(error.value).toBeNull()
    })

    it('应该在安全存储失败时使用默认APP_SECRET', async () => {
      // Mock API response
      vi.mocked($fetch).mockResolvedValueOnce({
        translatedText: '翻译结果',
      })

      // Mock SecureStorage 抛出错误
      const mockGet = vi.fn().mockRejectedValue(new Error('Storage error'))
      vi.mocked(SecureStorage).mockImplementation(
        () =>
          ({
            get: mockGet,
          }) as any,
      )

      const { translate } = useTranslate()
      const text = 'Test text that is long enough to meet the minimum length requirement.'

      const result = await translate(text)

      expect(result).toBe('翻译结果')
      expect($fetch).toHaveBeenCalled()
    })
  })

  describe('输入验证', () => {
    it('应该拒绝空文本', async () => {
      const { translate } = useTranslate()

      await expect(translate('')).rejects.toThrow('Text cannot be empty')
      await expect(translate('   ')).rejects.toThrow('Text cannot be empty')
    })

    it('应该拒绝太短的文本', async () => {
      const { translate, error } = useTranslate()

      await expect(translate('Short')).rejects.toThrow('Text too short')
      expect(error.value).toContain('Text too short')
    })

    it('应该拒绝太长的文本', async () => {
      const { translate, error } = useTranslate()

      const longText = 'a'.repeat(5001)
      await expect(translate(longText)).rejects.toThrow('Text too long')
      expect(error.value).toContain('Text too long')
    })
  })

  describe('API错误处理', () => {
    beforeEach(() => {
      // Mock SecureStorage 成功
      const mockGet = vi.fn().mockResolvedValue('test-secret')
      vi.mocked(SecureStorage).mockImplementation(
        () =>
          ({
            get: mockGet,
          }) as any,
      )
    })

    it('应该处理401 Unauthorized错误', async () => {
      vi.mocked($fetch).mockRejectedValueOnce({
        status: 401,
        message: 'Unauthorized',
      })

      const { translate, error } = useTranslate({ maxRetries: 0 })
      const text = 'Test text that is long enough to meet the minimum length requirement.'

      await expect(translate(text)).rejects.toThrow('Authentication failed')
      expect(error.value).toContain('Authentication failed')
    })

    it('应该在429错误时重试并最终失败', async () => {
      vi.mocked($fetch).mockRejectedValue({
        status: 429,
        message: 'Rate limit exceeded',
      })

      const { translate, error } = useTranslate({ maxRetries: 1 })
      const text = 'Test text that is long enough to meet the minimum length requirement.'

      await expect(translate(text)).rejects.toThrow('API rate limit exceeded')
      expect(error.value).toContain('API rate limit exceeded')
      expect($fetch).toHaveBeenCalledTimes(2) // 初始请求 + 1次重试
    })

    it('应该在500错误时重试并最终失败', async () => {
      vi.mocked($fetch).mockRejectedValue({
        status: 500,
        message: 'Internal server error',
      })

      const { translate, error } = useTranslate({ maxRetries: 1 })
      const text = 'Test text that is long enough to meet the minimum length requirement.'

      await expect(translate(text)).rejects.toThrow('Server error')
      expect(error.value).toContain('Server error')
      expect($fetch).toHaveBeenCalledTimes(2) // 初始请求 + 1次重试
    })

    it('应该处理timeout错误', async () => {
      vi.mocked($fetch).mockRejectedValue({
        message: 'timeout of 30000ms exceeded',
      })

      const { translate, error } = useTranslate({ maxRetries: 0 })
      const text = 'Test text that is long enough to meet the minimum length requirement.'

      await expect(translate(text)).rejects.toThrow('Request timeout')
      expect(error.value).toContain('Request timeout')
    })

    it('应该处理无效的API响应', async () => {
      vi.mocked($fetch).mockResolvedValueOnce({
        // 缺少 translatedText 字段
      })

      const { translate, error } = useTranslate({ maxRetries: 0 })
      const text = 'Test text that is long enough to meet the minimum length requirement.'

      await expect(translate(text)).rejects.toThrow('Invalid API response')
      expect(error.value).toContain('Invalid API response')
    })
  })

  describe('重试机制', () => {
    beforeEach(() => {
      // Mock SecureStorage 成功
      const mockGet = vi.fn().mockResolvedValue('test-secret')
      vi.mocked(SecureStorage).mockImplementation(
        () =>
          ({
            get: mockGet,
          }) as any,
      )
    })

    it('应该在第二次重试时成功', async () => {
      vi.mocked($fetch)
        .mockRejectedValueOnce({
          status: 500,
          message: 'Server error',
        })
        .mockResolvedValueOnce({
          translatedText: '最终成功的翻译',
        })

      const { translate } = useTranslate({ maxRetries: 2 })
      const text = 'Test text that is long enough to meet the minimum length requirement.'

      const result = await translate(text)

      expect(result).toBe('最终成功的翻译')
      expect($fetch).toHaveBeenCalledTimes(2) // 第1次失败，第2次成功
    })

    it('应该不重试401错误', async () => {
      vi.mocked($fetch).mockRejectedValue({
        status: 401,
        message: 'Unauthorized',
      })

      const { translate } = useTranslate({ maxRetries: 2 })
      const text = 'Test text that is long enough to meet the minimum length requirement.'

      await expect(translate(text)).rejects.toThrow('Authentication failed')
      expect($fetch).toHaveBeenCalledTimes(1) // 只调用1次，不重试
    })

    it('应该遵守maxRetries配置', async () => {
      vi.mocked($fetch).mockRejectedValue({
        status: 500,
        message: 'Server error',
      })

      const { translate } = useTranslate({ maxRetries: 3 })
      const text = 'Test text that is long enough to meet the minimum length requirement.'

      await expect(translate(text)).rejects.toThrow('Server error')
      expect($fetch).toHaveBeenCalledTimes(4) // 初始请求 + 3次重试
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
                  translatedText: '翻译结果',
                }),
              100,
            )
          }),
      )

      const mockGet = vi.fn().mockResolvedValue('test-secret')
      vi.mocked(SecureStorage).mockImplementation(
        () =>
          ({
            get: mockGet,
          }) as any,
      )

      const { translate, isLoading } = useTranslate()
      const text = 'Test text that is long enough to meet the minimum length requirement.'

      expect(isLoading.value).toBe(false)

      const promise = translate(text)
      expect(isLoading.value).toBe(true)

      await promise
      expect(isLoading.value).toBe(false)
    })
  })
})
