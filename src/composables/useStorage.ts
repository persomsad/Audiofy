/**
 * 本地存储服务 Composable
 * 负责管理 Article 元数据和音频文件
 */

import { File, Folder, knownFolders, path } from '@nativescript/core/file-system'
import { v4 as uuidv4 } from 'uuid'
import type { Article, CreateArticleInput, UpdateArticleInput, StorageService } from '../types'

/**
 * 存储配置
 */
const STORAGE_CONFIG = {
  /** 基础目录名称 */
  BASE_DIR: 'Audiofy',
  /** 元数据文件名 */
  METADATA_FILE: 'metadata.json',
  /** 音频文件夹名称 */
  AUDIOS_DIR: 'audios',
} as const

/**
 * 获取存储根目录
 * ~/Documents/Audiofy/
 */
function getStorageRoot(): Folder {
  const documents = knownFolders.documents()
  return documents.getFolder(STORAGE_CONFIG.BASE_DIR)
}

/**
 * 获取元数据文件路径
 * ~/Documents/Audiofy/metadata.json
 */
function getMetadataFilePath(): string {
  const root = getStorageRoot()
  return path.join(root.path, STORAGE_CONFIG.METADATA_FILE)
}

/**
 * 获取音频文件夹
 * ~/Documents/Audiofy/audios/
 */
function getAudiosFolder(): Folder {
  const root = getStorageRoot()
  return root.getFolder(STORAGE_CONFIG.AUDIOS_DIR)
}

/**
 * 读取所有 Article 元数据
 */
async function readMetadata(): Promise<Article[]> {
  try {
    const filePath = getMetadataFilePath()
    const file = File.fromPath(filePath)

    if (!file.readSync) {
      throw createStorageError('IO_ERROR', 'File system API not available')
    }

    const content = await file.readText()

    if (!content || content.trim() === '') {
      return []
    }

    const data = JSON.parse(content)

    // 将 ISO 日期字符串转换为 Date 对象
    return data.map((article: any) => ({
      ...article,
      createdAt: new Date(article.createdAt),
    }))
  } catch (error) {
    if (error instanceof SyntaxError) {
      throw createStorageError('PARSE_ERROR', `Invalid JSON in metadata file: ${error.message}`)
    }
    throw createStorageError('IO_ERROR', `Failed to read metadata: ${error}`)
  }
}

/**
 * 写入所有 Article 元数据
 */
async function writeMetadata(articles: Article[]): Promise<void> {
  try {
    const filePath = getMetadataFilePath()
    const file = File.fromPath(filePath)

    const content = JSON.stringify(articles, null, 2)
    await file.writeText(content)
  } catch (error) {
    if (isOutOfSpaceError(error)) {
      throw createStorageError('DISK_FULL', 'Not enough disk space to save metadata')
    }
    throw createStorageError('IO_ERROR', `Failed to write metadata: ${error}`)
  }
}

/**
 * 判断是否是磁盘空间不足错误
 */
function isOutOfSpaceError(error: any): boolean {
  const errorMsg = String(error).toLowerCase()
  return (
    errorMsg.includes('no space') || errorMsg.includes('disk full') || errorMsg.includes('enospc')
  )
}

/**
 * 创建存储错误对象
 */
function createStorageError(
  code: 'NOT_FOUND' | 'IO_ERROR' | 'PARSE_ERROR' | 'DISK_FULL',
  message: string,
): Error {
  const error = new Error(message) as any
  error.code = code
  error.name = 'StorageError'
  return error
}

/**
 * 使用本地存储服务
 */
export function useStorage(): StorageService {
  /**
   * 初始化存储
   * 创建必要的目录和文件
   */
  const initialize = async (): Promise<void> => {
    try {
      // 1. 创建基础目录 ~/Documents/Audiofy/
      const documents = knownFolders.documents()
      const root = documents.getFolder(STORAGE_CONFIG.BASE_DIR)

      // 2. 创建音频文件夹 ~/Documents/Audiofy/audios/
      root.getFolder(STORAGE_CONFIG.AUDIOS_DIR)

      // 3. 创建空的 metadata.json（如果不存在）
      const metadataPath = path.join(root.path, STORAGE_CONFIG.METADATA_FILE)
      const metadataFile = File.fromPath(metadataPath)

      const exists = metadataFile.readSync ? true : false
      if (!exists) {
        await metadataFile.writeText('[]')
      }

      console.log('[StorageService] Initialized successfully')
    } catch (error) {
      throw createStorageError('IO_ERROR', `Failed to initialize storage: ${error}`)
    }
  }

  /**
   * 创建新文章
   */
  const createArticle = async (input: CreateArticleInput): Promise<Article> => {
    // 1. 生成 UUID 和时间戳
    const article: Article = {
      ...input,
      id: uuidv4(),
      createdAt: new Date(),
    }

    // 2. 读取现有数据
    const articles = await readMetadata()

    // 3. 追加新文章
    articles.push(article)

    // 4. 写入文件
    await writeMetadata(articles)

    console.log('[StorageService] Created article:', article.id)
    return article
  }

  /**
   * 根据 ID 获取文章
   */
  const getArticleById = async (id: string): Promise<Article | null> => {
    const articles = await readMetadata()
    const article = articles.find((a) => a.id === id)

    if (!article) {
      console.warn('[StorageService] Article not found:', id)
      return null
    }

    return article
  }

  /**
   * 获取所有文章
   * 按创建时间倒序排序（最新的在前）
   */
  const getAllArticles = async (): Promise<Article[]> => {
    const articles = await readMetadata()

    // 按创建时间倒序排序
    return articles.sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime())
  }

  /**
   * 更新文章
   */
  const updateArticle = async (id: string, updates: UpdateArticleInput): Promise<Article> => {
    // 1. 读取现有数据
    const articles = await readMetadata()

    // 2. 查找文章索引
    const index = articles.findIndex((a) => a.id === id)

    if (index === -1) {
      throw createStorageError('NOT_FOUND', `Article not found: ${id}`)
    }

    // 3. 更新文章（合并字段）
    const updatedArticle: Article = {
      ...articles[index],
      ...updates,
      // 确保不覆盖 id 和 createdAt
      id: articles[index].id,
      createdAt: articles[index].createdAt,
    }

    articles[index] = updatedArticle

    // 4. 写入文件
    await writeMetadata(articles)

    console.log('[StorageService] Updated article:', id)
    return updatedArticle
  }

  /**
   * 删除文章
   * 包括元数据和音频文件
   */
  const deleteArticle = async (id: string): Promise<void> => {
    // 1. 读取现有数据
    const articles = await readMetadata()

    // 2. 查找文章
    const article = articles.find((a) => a.id === id)

    if (!article) {
      throw createStorageError('NOT_FOUND', `Article not found: ${id}`)
    }

    // 3. 删除音频文件（如果存在）
    try {
      const audiosFolder = getAudiosFolder()
      const audioFile = audiosFolder.getFile(path.basename(article.audioPath))
      await audioFile.remove()
      console.log('[StorageService] Deleted audio file:', article.audioPath)
    } catch (error) {
      console.warn('[StorageService] Failed to delete audio file:', error)
      // 即使音频文件删除失败，也继续删除元数据
    }

    // 4. 从元数据中移除
    const updatedArticles = articles.filter((a) => a.id !== id)
    await writeMetadata(updatedArticles)

    console.log('[StorageService] Deleted article:', id)
  }

  return {
    initialize,
    createArticle,
    getArticleById,
    getAllArticles,
    updateArticle,
    deleteArticle,
  }
}
