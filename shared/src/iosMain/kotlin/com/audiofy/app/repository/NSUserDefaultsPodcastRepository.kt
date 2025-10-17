package com.audiofy.app.repository

import com.audiofy.app.data.AudioVersion
import com.audiofy.app.data.Podcast
import com.audiofy.app.data.PodcastFilter
import com.audiofy.app.data.PodcastSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

/**
 * iOS 平台的播客持久化存储实现
 * 使用 NSUserDefaults 存储整个播客列表为 JSON
 */
class NSUserDefaultsPodcastRepository : PodcastRepository {

    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val _podcastsFlow = MutableStateFlow(loadPodcasts())

    companion object {
        private const val KEY_PODCASTS = "podcasts_data"
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    private fun loadPodcasts(): List<Podcast> {
        val jsonString = userDefaults.stringForKey(KEY_PODCASTS)
        return if (jsonString != null) {
            try {
                json.decodeFromString<List<Podcast>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun savePodcasts(podcasts: List<Podcast>) {
        val jsonString = json.encodeToString(podcasts)
        userDefaults.setObject(jsonString, forKey = KEY_PODCASTS)
        userDefaults.synchronize()
        _podcastsFlow.value = podcasts
    }

    override fun getAllPodcasts(): Flow<List<Podcast>> {
        return _podcastsFlow.asStateFlow()
    }

    override suspend fun getPodcastsFiltered(
        filter: PodcastFilter,
        sortOrder: PodcastSortOrder
    ): List<Podcast> {
        var result = _podcastsFlow.value

        result = when (filter) {
            PodcastFilter.ALL -> result
            PodcastFilter.RECENT -> result.sortedByDescending { it.createdAt }.take(10)
            PodcastFilter.UNFINISHED -> result.filter { it.playProgress < 0.95f }
            PodcastFilter.FAVORITE -> result.filter { it.isFavorite }
        }

        result = when (sortOrder) {
            PodcastSortOrder.CREATED_DESC -> result.sortedByDescending { it.createdAt }
            PodcastSortOrder.CREATED_ASC -> result.sortedBy { it.createdAt }
            PodcastSortOrder.LAST_PLAYED_DESC -> result.sortedByDescending { it.lastPlayedAt ?: 0 }
            PodcastSortOrder.TITLE_ASC -> result.sortedBy { it.title }
        }

        return result
    }

    override suspend fun getPodcastById(id: String): Podcast? {
        return _podcastsFlow.value.find { it.id == id }
    }

    override suspend fun savePodcast(podcast: Podcast) {
        val podcasts = _podcastsFlow.value
        savePodcasts(podcasts + podcast)
    }

    override suspend fun updatePodcast(podcast: Podcast) {
        val podcasts = _podcastsFlow.value
        val updated = podcasts.map { if (it.id == podcast.id) podcast else it }
        savePodcasts(updated)
    }

    override suspend fun deletePodcast(id: String) {
        val podcasts = _podcastsFlow.value
        savePodcasts(podcasts.filter { it.id != id })
    }

    override suspend fun addAudioVersion(podcastId: String, audioVersion: AudioVersion) {
        val podcasts = _podcastsFlow.value
        val updated = podcasts.map { podcast ->
            if (podcast.id == podcastId) {
                podcast.copy(audioVersions = podcast.audioVersions + audioVersion)
            } else {
                podcast
            }
        }
        savePodcasts(updated)
    }

    override suspend fun setCurrentVersion(podcastId: String, versionId: String) {
        val podcasts = _podcastsFlow.value
        val updated = podcasts.map { podcast ->
            if (podcast.id == podcastId) {
                podcast.copy(currentVersionId = versionId)
            } else {
                podcast
            }
        }
        savePodcasts(updated)
    }

    override suspend fun updatePlayProgress(podcastId: String, progress: Float) {
        val podcasts = _podcastsFlow.value
        val updated = podcasts.map { podcast ->
            if (podcast.id == podcastId) {
                podcast.copy(playProgress = progress.coerceIn(0f, 1f))
            } else {
                podcast
            }
        }
        savePodcasts(updated)
    }

    override suspend fun updateLastPlayedAt(podcastId: String, timestamp: Long) {
        val podcasts = _podcastsFlow.value
        val updated = podcasts.map { podcast ->
            if (podcast.id == podcastId) {
                podcast.copy(lastPlayedAt = timestamp)
            } else {
                podcast
            }
        }
        savePodcasts(updated)
    }

    override suspend fun toggleFavorite(podcastId: String) {
        val podcasts = _podcastsFlow.value
        val updated = podcasts.map { podcast ->
            if (podcast.id == podcastId) {
                podcast.copy(isFavorite = !podcast.isFavorite)
            } else {
                podcast
            }
        }
        savePodcasts(updated)
    }

    override suspend fun searchPodcasts(query: String): List<Podcast> {
        val podcasts = _podcastsFlow.value
        if (query.isBlank()) return podcasts

        val lowerQuery = query.lowercase()
        return podcasts.filter { podcast ->
            podcast.title.lowercase().contains(lowerQuery) ||
            podcast.author?.lowercase()?.contains(lowerQuery) == true ||
            podcast.textContent.lowercase().contains(lowerQuery)
        }
    }
}
