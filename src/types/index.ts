/**
 * Audiofy 核心类型定义
 */

/**
 * Article 数据模型
 * 代表一篇文章及其关联的音频元数据
 */
export interface Article {
  /** 唯一标识符 (UUID v4) */
  id: string

  /** 文章标题（用户输入或自动生成） */
  title: string

  /** 原始英文文本 */
  originalText: string

  /** 翻译后的中文文本 */
  translatedText: string

  /** 音频文件本地路径（相对路径，如 'audios/{uuid}.wav'） */
  audioPath: string

  /** 创建时间 */
  createdAt: Date

  /** 音频时长（秒） */
  duration: number

  /** 处理状态 */
  status: 'pending' | 'processing' | 'completed' | 'failed'

  /** 错误信息（如果状态为 failed） */
  errorMessage?: string
}

/**
 * 创建 Article 时的输入参数
 * 省略自动生成的字段 (id, createdAt)
 */
export type CreateArticleInput = Omit<Article, 'id' | 'createdAt'>

/**
 * 更新 Article 时的输入参数
 * 所有字段都是可选的
 */
export type UpdateArticleInput = Partial<Article>

/**
 * 存储服务接口
 */
export interface StorageService {
  /** 创建新文章 */
  createArticle(input: CreateArticleInput): Promise<Article>

  /** 根据 ID 获取文章 */
  getArticleById(id: string): Promise<Article | null>

  /** 获取所有文章（按创建时间倒序） */
  getAllArticles(): Promise<Article[]>

  /** 更新文章 */
  updateArticle(id: string, updates: UpdateArticleInput): Promise<Article>

  /** 删除文章（包括音频文件） */
  deleteArticle(id: string): Promise<void>

  /** 初始化存储（创建必要的目录和文件） */
  initialize(): Promise<void>
}

/**
 * 存储错误类型
 */
export class StorageError extends Error {
  constructor(
    message: string,
    public code: 'NOT_FOUND' | 'IO_ERROR' | 'PARSE_ERROR' | 'DISK_FULL',
  ) {
    super(message)
    this.name = 'StorageError'
  }
}
