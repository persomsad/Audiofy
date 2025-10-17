package com.audiofy.app.repository

import com.audiofy.app.data.AudioVersion
import com.audiofy.app.data.Podcast
import com.audiofy.app.data.PodcastFilter
import com.audiofy.app.data.PodcastSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 内存中的播客仓库实现（MVP阶段）
 * 
 * ⚠️ 注意：
 * - 数据仅保存在内存中，应用关闭后会丢失
 * - 用于快速原型验证和UI开发
 * - v2.1将替换为真正的持久化存储（DataStore/SQLite）
 */
class InMemoryPodcastRepository : PodcastRepository {
    
    private val _podcasts = MutableStateFlow<List<Podcast>>(emptyList())
    
    override fun getAllPodcasts(): Flow<List<Podcast>> {
        return _podcasts.asStateFlow()
    }
    
    override suspend fun getPodcastsFiltered(
        filter: PodcastFilter,
        sortOrder: PodcastSortOrder
    ): List<Podcast> {
        var result = _podcasts.value
        
        // 应用筛选
        result = when (filter) {
            PodcastFilter.ALL -> result
            PodcastFilter.RECENT -> result.sortedByDescending { it.createdAt }.take(10)
            PodcastFilter.UNFINISHED -> result.filter { it.playProgress < 0.95f }
            PodcastFilter.FAVORITE -> result.filter { it.isFavorite }
        }
        
        // 应用排序
        result = when (sortOrder) {
            PodcastSortOrder.CREATED_DESC -> result.sortedByDescending { it.createdAt }
            PodcastSortOrder.CREATED_ASC -> result.sortedBy { it.createdAt }
            PodcastSortOrder.LAST_PLAYED_DESC -> result.sortedByDescending { it.lastPlayedAt ?: 0 }
            PodcastSortOrder.TITLE_ASC -> result.sortedBy { it.title }
        }
        
        return result
    }
    
    override suspend fun getPodcastById(id: String): Podcast? {
        return _podcasts.value.find { it.id == id }
    }
    
    override suspend fun savePodcast(podcast: Podcast) {
        _podcasts.value = _podcasts.value + podcast
    }
    
    override suspend fun updatePodcast(podcast: Podcast) {
        _podcasts.value = _podcasts.value.map {
            if (it.id == podcast.id) podcast else it
        }
    }
    
    override suspend fun deletePodcast(id: String) {
        _podcasts.value = _podcasts.value.filter { it.id != id }
    }
    
    override suspend fun addAudioVersion(podcastId: String, audioVersion: AudioVersion) {
        _podcasts.value = _podcasts.value.map { podcast ->
            if (podcast.id == podcastId) {
                podcast.copy(
                    audioVersions = podcast.audioVersions + audioVersion
                )
            } else {
                podcast
            }
        }
    }
    
    override suspend fun setCurrentVersion(podcastId: String, versionId: String) {
        _podcasts.value = _podcasts.value.map { podcast ->
            if (podcast.id == podcastId) {
                podcast.copy(currentVersionId = versionId)
            } else {
                podcast
            }
        }
    }
    
    override suspend fun updatePlayProgress(podcastId: String, progress: Float) {
        _podcasts.value = _podcasts.value.map { podcast ->
            if (podcast.id == podcastId) {
                podcast.copy(playProgress = progress.coerceIn(0f, 1f))
            } else {
                podcast
            }
        }
    }
    
    override suspend fun updateLastPlayedAt(podcastId: String, timestamp: Long) {
        _podcasts.value = _podcasts.value.map { podcast ->
            if (podcast.id == podcastId) {
                podcast.copy(lastPlayedAt = timestamp)
            } else {
                podcast
            }
        }
    }
    
    override suspend fun toggleFavorite(podcastId: String) {
        _podcasts.value = _podcasts.value.map { podcast ->
            if (podcast.id == podcastId) {
                podcast.copy(isFavorite = !podcast.isFavorite)
            } else {
                podcast
            }
        }
    }
    
    override suspend fun searchPodcasts(query: String): List<Podcast> {
        if (query.isBlank()) return _podcasts.value
        
        val lowerQuery = query.lowercase()
        return _podcasts.value.filter { podcast ->
            podcast.title.lowercase().contains(lowerQuery) ||
            podcast.author?.lowercase()?.contains(lowerQuery) == true ||
            podcast.textContent.lowercase().contains(lowerQuery)
        }
    }
}

/**
 * 创建Podcast Repository实例
 */
fun createPodcastRepository(): PodcastRepository {
    return InMemoryPodcastRepository()
}

