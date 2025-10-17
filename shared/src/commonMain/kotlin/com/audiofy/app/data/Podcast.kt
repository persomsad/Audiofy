package com.audiofy.app.data

import kotlinx.serialization.Serializable

/**
 * 播客数据模型
 * 支持多音色版本存储（Issue #73）
 */
@Serializable
data class Podcast(
    val id: String,  // UUID
    val title: String,
    val author: String? = null,
    val textContent: String,  // ⚠️ 重要：保存原始文本，用于生成新音色版本
    val coverUrl: String? = null,
    val audioVersions: List<AudioVersion>,  // 多个音色版本
    val currentVersionId: String,  // 当前播放的版本ID
    val createdAt: Long,  // 创建时间戳
    val lastPlayedAt: Long? = null,  // 最后播放时间
    val playProgress: Float = 0f,  // 播放进度 0.0-1.0
    val isFavorite: Boolean = false  // 是否收藏
)

/**
 * 音频版本（不同音色）
 */
@Serializable
data class AudioVersion(
    val versionId: String,  // UUID
    val voice: String,  // Cherry, Ethan, Nofish, etc.
    val languageType: String,  // Chinese, English
    val audioPath: String,  // 本地文件路径: audios/{podcastId}/{versionId}.wav
    val duration: Int,  // 音频时长（秒）
    val fileSize: Long,  // 文件大小（字节）
    val createdAt: Long  // 创建时间戳
)

/**
 * 播客列表筛选器
 */
enum class PodcastFilter {
    ALL,           // 全部
    RECENT,        // 最近添加
    UNFINISHED,    // 未完成（playProgress < 0.95）
    FAVORITE       // 已收藏
}

/**
 * 播客排序方式
 */
enum class PodcastSortOrder {
    CREATED_DESC,      // 创建时间降序（最新在前）
    CREATED_ASC,       // 创建时间升序
    LAST_PLAYED_DESC,  // 最后播放时间降序
    TITLE_ASC          // 标题字母序
}

