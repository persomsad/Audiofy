package com.audiofy.app.repository

import com.audiofy.app.data.AudioVersion
import com.audiofy.app.data.Podcast
import com.audiofy.app.data.PodcastFilter
import com.audiofy.app.data.PodcastSortOrder
import kotlinx.coroutines.flow.Flow

/**
 * 播客数据仓库接口
 * 负责播客元数据的CRUD操作
 */
interface PodcastRepository {
    /**
     * 获取所有播客（响应式）
     */
    fun getAllPodcasts(): Flow<List<Podcast>>
    
    /**
     * 获取筛选后的播客列表
     */
    suspend fun getPodcastsFiltered(
        filter: PodcastFilter = PodcastFilter.ALL,
        sortOrder: PodcastSortOrder = PodcastSortOrder.CREATED_DESC
    ): List<Podcast>
    
    /**
     * 根据ID获取播客
     */
    suspend fun getPodcastById(id: String): Podcast?
    
    /**
     * 保存新播客
     */
    suspend fun savePodcast(podcast: Podcast)
    
    /**
     * 更新播客
     */
    suspend fun updatePodcast(podcast: Podcast)
    
    /**
     * 删除播客
     */
    suspend fun deletePodcast(id: String)
    
    /**
     * 为播客添加新音色版本
     */
    suspend fun addAudioVersion(podcastId: String, audioVersion: AudioVersion)
    
    /**
     * 切换当前音色版本
     */
    suspend fun setCurrentVersion(podcastId: String, versionId: String)
    
    /**
     * 更新播放进度
     */
    suspend fun updatePlayProgress(podcastId: String, progress: Float)
    
    /**
     * 更新最后播放时间
     */
    suspend fun updateLastPlayedAt(podcastId: String, timestamp: Long)
    
    /**
     * 切换收藏状态
     */
    suspend fun toggleFavorite(podcastId: String)
    
    /**
     * 搜索播客
     */
    suspend fun searchPodcasts(query: String): List<Podcast>
}

