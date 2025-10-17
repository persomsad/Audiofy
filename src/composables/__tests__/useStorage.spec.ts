/**
 * useStorage Composable 单元测试
 * 覆盖：CRUD操作、初始化、错误处理、边界情况
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useStorage } from '../useStorage'
import { knownFolders, File } from '@nativescript/core/file-system'
import type { Article, CreateArticleInput } from '../../types'

// Mock dependencies
vi.mock('@nativescript/core/file-system', () => ({
  knownFolders: {
    documents: vi.fn(),
  },
  path: {
    join: (...args: string[]) => args.join('/'),
    basename: (filePath: string) => filePath.split('/').pop() || '',
  },
  File: {
    fromPath: vi.fn(),
  },
  Folder: vi.fn(),
}))

vi.mock('@/utils/uuid', () => ({
  generateUUID: vi.fn(() => 'test-uuid-1234'),
}))

describe('useStorage', () => {
  let mockDocumentsFolder: any
  let mockAudiofyFolder: any
  let mockAudiosFolder: any
  let mockMetadataFile: any

  // 测试用的 Article 数据
  const testArticle1: Article = {
    id: 'test-uuid-1234',
    title: 'Test Article 1',
    originalText: 'Hello world',
    translatedText: '你好世界',
    audioPath: 'audios/test-uuid-1234.wav',
    createdAt: new Date('2025-01-01T00:00:00Z'),
    duration: 10,
    status: 'completed',
  }

  const testArticle2: Article = {
    id: 'test-uuid-5678',
    title: 'Test Article 2',
    originalText: 'Good morning',
    translatedText: '早上好',
    audioPath: 'audios/test-uuid-5678.wav',
    createdAt: new Date('2025-01-02T00:00:00Z'),
    duration: 5,
    status: 'pending',
  }

  beforeEach(() => {
    vi.clearAllMocks()

    // Mock folder structure
    mockAudiosFolder = {
      path: '/mock/documents/Audiofy/audios',
      getFile: vi.fn((_fileName: string) => ({
        remove: vi.fn().mockResolvedValue(undefined),
      })),
    }

    mockAudiofyFolder = {
      path: '/mock/documents/Audiofy',
      getFolder: vi.fn((name: string) => {
        if (name === 'audios') return mockAudiosFolder
        return mockAudiofyFolder
      }),
    }

    mockDocumentsFolder = {
      path: '/mock/documents',
      getFolder: vi.fn((name: string) => {
        if (name === 'Audiofy') return mockAudiofyFolder
        return mockDocumentsFolder
      }),
    }

    vi.mocked(knownFolders.documents).mockReturnValue(mockDocumentsFolder)

    // Mock metadata file
    mockMetadataFile = {
      readText: vi.fn().mockResolvedValue('[]'),
      writeText: vi.fn().mockResolvedValue(undefined),
    }

    vi.mocked(File.fromPath).mockReturnValue(mockMetadataFile)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('initialize', () => {
    it('应该成功初始化存储目录和文件', async () => {
      // 模拟文件不存在（readText 抛出错误）
      mockMetadataFile.readText.mockRejectedValueOnce(new Error('File not found'))

      const { initialize } = useStorage()

      await expect(initialize()).resolves.toBeUndefined()

      // 验证目录创建
      expect(mockDocumentsFolder.getFolder).toHaveBeenCalledWith('Audiofy')
      expect(mockAudiofyFolder.getFolder).toHaveBeenCalledWith('audios')

      // 验证 metadata.json 被创建
      expect(mockMetadataFile.writeText).toHaveBeenCalledWith('[]')
    })

    it('应该在 metadata.json 已存在时跳过创建', async () => {
      // 模拟文件已存在（readText 成功返回）
      mockMetadataFile.readText.mockResolvedValueOnce('[]')

      const { initialize } = useStorage()
      await expect(initialize()).resolves.toBeUndefined()

      // 不应该写入空数组
      expect(mockMetadataFile.writeText).not.toHaveBeenCalled()
    })

    it('应该处理目录创建失败', async () => {
      mockDocumentsFolder.getFolder.mockImplementation(() => {
        throw new Error('Permission denied')
      })

      const { initialize } = useStorage()

      await expect(initialize()).rejects.toThrow('Failed to initialize storage')
    })
  })

  describe('createArticle', () => {
    it('应该成功创建新文章', async () => {
      const input: CreateArticleInput = {
        title: 'Test Article',
        originalText: 'Hello world',
        translatedText: '你好世界',
        audioPath: 'audios/test-uuid-1234.wav',
        duration: 10,
        status: 'completed',
      }

      mockMetadataFile.readText.mockResolvedValueOnce('[]')

      const { createArticle } = useStorage()
      const result = await createArticle(input)

      expect(result.id).toBe('test-uuid-1234')
      expect(result.title).toBe(input.title)
      expect(result.createdAt).toBeInstanceOf(Date)

      // 验证写入了正确的数据（注意：JSON 带缩进，所以有空格）
      expect(mockMetadataFile.writeText).toHaveBeenCalledWith(
        expect.stringContaining('"id": "test-uuid-1234"'),
      )
    })

    it('应该追加到现有文章列表', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce(JSON.stringify([testArticle1], null, 2))

      const input: CreateArticleInput = {
        title: 'New Article',
        originalText: 'New text',
        translatedText: '新文本',
        audioPath: 'audios/test-uuid-1234.wav',
        duration: 5,
        status: 'pending',
      }

      const { createArticle } = useStorage()
      await createArticle(input)

      // 验证写入包含两篇文章
      const writeCall = vi.mocked(mockMetadataFile.writeText).mock.calls[0][0]
      const writtenData = JSON.parse(writeCall)
      expect(writtenData).toHaveLength(2)
      expect(writtenData[0].id).toBe(testArticle1.id)
      expect(writtenData[1].id).toBe('test-uuid-1234')
    })

    it('应该处理写入失败错误', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce('[]')
      mockMetadataFile.writeText.mockRejectedValueOnce(new Error('Permission denied'))

      const input: CreateArticleInput = {
        title: 'Test',
        originalText: 'Test',
        translatedText: 'Test',
        audioPath: 'test.wav',
        duration: 1,
        status: 'pending',
      }

      const { createArticle } = useStorage()

      await expect(createArticle(input)).rejects.toThrow('Failed to write metadata')
    })

    it('应该处理磁盘空间不足错误', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce('[]')
      mockMetadataFile.writeText.mockRejectedValueOnce(new Error('ENOSPC: no space left on device'))

      const input: CreateArticleInput = {
        title: 'Test',
        originalText: 'Test',
        translatedText: 'Test',
        audioPath: 'test.wav',
        duration: 1,
        status: 'pending',
      }

      const { createArticle } = useStorage()

      await expect(createArticle(input)).rejects.toThrow('Not enough disk space')
    })
  })

  describe('getArticleById', () => {
    it('应该成功获取指定ID的文章', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce(
        JSON.stringify([testArticle1, testArticle2], null, 2),
      )

      const { getArticleById } = useStorage()
      const result = await getArticleById(testArticle1.id)

      expect(result).not.toBeNull()
      expect(result?.id).toBe(testArticle1.id)
      expect(result?.title).toBe(testArticle1.title)
      expect(result?.createdAt).toBeInstanceOf(Date)
    })

    it('应该在文章不存在时返回null', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce('[]')

      const { getArticleById } = useStorage()
      const result = await getArticleById('non-existent-id')

      expect(result).toBeNull()
    })

    it('应该处理JSON解析错误', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce('invalid json')

      const { getArticleById } = useStorage()

      await expect(getArticleById('test-id')).rejects.toThrow('Invalid JSON')
    })

    it('应该处理文件读取失败', async () => {
      mockMetadataFile.readText.mockRejectedValueOnce(new Error('Permission denied'))

      const { getArticleById } = useStorage()

      await expect(getArticleById('test-id')).rejects.toThrow('Failed to read metadata')
    })
  })

  describe('getAllArticles', () => {
    it('应该获取所有文章并按创建时间倒序排序', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce(
        JSON.stringify([testArticle1, testArticle2], null, 2),
      )

      const { getAllArticles } = useStorage()
      const result = await getAllArticles()

      expect(result).toHaveLength(2)
      // 应该按创建时间倒序（最新的在前）
      expect(result[0].id).toBe(testArticle2.id) // 2025-01-02
      expect(result[1].id).toBe(testArticle1.id) // 2025-01-01
      expect(result[0].createdAt).toBeInstanceOf(Date)
    })

    it('应该在没有文章时返回空数组', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce('[]')

      const { getAllArticles } = useStorage()
      const result = await getAllArticles()

      expect(result).toEqual([])
    })

    it('应该处理空文件', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce('')

      const { getAllArticles } = useStorage()
      const result = await getAllArticles()

      expect(result).toEqual([])
    })
  })

  describe('updateArticle', () => {
    it('应该成功更新文章字段', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce(JSON.stringify([testArticle1], null, 2))

      const updates = {
        title: 'Updated Title',
        status: 'completed' as const,
      }

      const { updateArticle } = useStorage()
      const result = await updateArticle(testArticle1.id, updates)

      expect(result.id).toBe(testArticle1.id) // ID 不变
      expect(result.title).toBe('Updated Title') // 标题已更新
      expect(result.status).toBe('completed')
      expect(result.createdAt).toEqual(testArticle1.createdAt) // createdAt 不变

      // 验证写入了正确的数据
      const writeCall = vi.mocked(mockMetadataFile.writeText).mock.calls[0][0]
      const writtenData = JSON.parse(writeCall)
      expect(writtenData[0].title).toBe('Updated Title')
    })

    it('应该不允许覆盖id和createdAt', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce(JSON.stringify([testArticle1], null, 2))

      const updates = {
        id: 'new-id',
        createdAt: new Date('2030-01-01'),
        title: 'Updated Title',
      } as any

      const { updateArticle } = useStorage()
      const result = await updateArticle(testArticle1.id, updates)

      expect(result.id).toBe(testArticle1.id) // ID 保持不变
      expect(result.createdAt).toEqual(testArticle1.createdAt) // createdAt 保持不变
      expect(result.title).toBe('Updated Title') // 其他字段正常更新
    })

    it('应该在文章不存在时抛出错误', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce('[]')

      const { updateArticle } = useStorage()

      await expect(updateArticle('non-existent-id', { title: 'New Title' })).rejects.toThrow(
        'Article not found',
      )
    })

    it('应该处理部分字段更新', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce(JSON.stringify([testArticle1], null, 2))

      const { updateArticle } = useStorage()
      const result = await updateArticle(testArticle1.id, { duration: 15 })

      expect(result.duration).toBe(15)
      expect(result.title).toBe(testArticle1.title) // 其他字段不变
      expect(result.originalText).toBe(testArticle1.originalText)
    })
  })

  describe('deleteArticle', () => {
    it('应该成功删除文章和音频文件', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce(
        JSON.stringify([testArticle1, testArticle2], null, 2),
      )

      const mockAudioFileInstance = {
        remove: vi.fn().mockResolvedValue(undefined),
      }
      mockAudiosFolder.getFile.mockReturnValueOnce(mockAudioFileInstance)

      const { deleteArticle } = useStorage()
      await expect(deleteArticle(testArticle1.id)).resolves.toBeUndefined()

      // 验证音频文件被删除
      expect(mockAudiosFolder.getFile).toHaveBeenCalledWith('test-uuid-1234.wav')
      expect(mockAudioFileInstance.remove).toHaveBeenCalled()

      // 验证元数据中移除了该文章
      const writeCall = vi.mocked(mockMetadataFile.writeText).mock.calls[0][0]
      const writtenData = JSON.parse(writeCall)
      expect(writtenData).toHaveLength(1)
      expect(writtenData[0].id).toBe(testArticle2.id)
    })

    it('应该在文章不存在时抛出错误', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce('[]')

      const { deleteArticle } = useStorage()

      await expect(deleteArticle('non-existent-id')).rejects.toThrow('Article not found')
    })

    it('应该在音频文件删除失败时继续删除元数据', async () => {
      mockMetadataFile.readText.mockResolvedValueOnce(JSON.stringify([testArticle1], null, 2))

      const mockAudioFileInstance = {
        remove: vi.fn().mockRejectedValue(new Error('File not found')),
      }
      mockAudiosFolder.getFile.mockReturnValueOnce(mockAudioFileInstance)

      const { deleteArticle } = useStorage()
      await expect(deleteArticle(testArticle1.id)).resolves.toBeUndefined()

      // 验证元数据仍然被删除
      const writeCall = vi.mocked(mockMetadataFile.writeText).mock.calls[0][0]
      const writtenData = JSON.parse(writeCall)
      expect(writtenData).toHaveLength(0)
    })
  })

  describe('边界情况', () => {
    it('应该处理大量文章', async () => {
      const manyArticles = Array.from({ length: 100 }, (_, i) => ({
        ...testArticle1,
        id: `article-${i}`,
        createdAt: new Date(2025, 0, i + 1),
      }))

      mockMetadataFile.readText.mockResolvedValueOnce(JSON.stringify(manyArticles, null, 2))

      const { getAllArticles } = useStorage()
      const result = await getAllArticles()

      expect(result).toHaveLength(100)
      // 验证排序正确（最新的在前）
      expect(result[0].id).toBe('article-99')
      expect(result[99].id).toBe('article-0')
    })

    it('应该处理特殊字符在文本中', async () => {
      const articleWithSpecialChars: CreateArticleInput = {
        title: 'Test "quotes" & <tags>',
        originalText: 'Text with\nnewlines\tand\ttabs',
        translatedText: '包含特殊字符的文本 😀',
        audioPath: 'audios/test.wav',
        duration: 1,
        status: 'pending',
      }

      mockMetadataFile.readText.mockResolvedValueOnce('[]')

      const { createArticle } = useStorage()
      const result = await createArticle(articleWithSpecialChars)

      expect(result.title).toBe(articleWithSpecialChars.title)
      expect(result.translatedText).toContain('😀')

      // 验证 JSON 正确转义
      const writeCall = vi.mocked(mockMetadataFile.writeText).mock.calls[0][0]
      expect(writeCall).toContain('\\"quotes\\"')
      expect(writeCall).toContain('\\n')
      expect(writeCall).toContain('\\t')
    })

    it('应该处理 Date 对象序列化和反序列化', async () => {
      const now = new Date('2025-01-15T12:00:00.000Z')
      const article = { ...testArticle1, createdAt: now }

      mockMetadataFile.readText.mockResolvedValueOnce(JSON.stringify([article], null, 2))

      const { getAllArticles } = useStorage()
      const result = await getAllArticles()

      expect(result[0].createdAt).toBeInstanceOf(Date)
      expect(result[0].createdAt.toISOString()).toBe(now.toISOString())
    })
  })

  describe('错误处理完整性', () => {
    it('应该为所有错误提供有意义的错误消息', async () => {
      const { getArticleById, createArticle, updateArticle, deleteArticle } = useStorage()

      // 文件系统 IO 错误
      mockMetadataFile.readText.mockRejectedValue(new Error('Permission denied'))
      await expect(getArticleById('test')).rejects.toThrow('Failed to read metadata')

      // JSON 解析错误
      mockMetadataFile.readText.mockResolvedValue('invalid json')
      await expect(getArticleById('test')).rejects.toThrow('Invalid JSON')

      // 磁盘空间不足
      mockMetadataFile.readText.mockResolvedValue('[]')
      mockMetadataFile.writeText.mockRejectedValue(new Error('ENOSPC'))
      await expect(
        createArticle({
          title: 'Test',
          originalText: 'Test',
          translatedText: 'Test',
          audioPath: 'test.wav',
          duration: 1,
          status: 'pending',
        }),
      ).rejects.toThrow('Not enough disk space')

      // 文章不存在
      mockMetadataFile.readText.mockResolvedValue('[]')
      await expect(updateArticle('non-existent', { title: 'Test' })).rejects.toThrow(
        'Article not found',
      )
      await expect(deleteArticle('non-existent')).rejects.toThrow('Article not found')
    })
  })
})
