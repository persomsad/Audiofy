package com.audiofy.app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.audiofy.app.data.AudioVersion
import com.audiofy.app.data.Podcast
import com.audiofy.app.data.PodcastFilter
import com.audiofy.app.data.PodcastSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Android 平台的播客持久化存储实现
 * 使用 DataStore Preferences 存储整个播客列表为 JSON
 */
class DataStorePodcastRepository(private val context: Context) : PodcastRepository {

    private val Context.podcastDataStore: DataStore<Preferences> by preferencesDataStore(name = "audiofy_podcasts")

    companion object {
        private val KEY_PODCASTS = stringPreferencesKey("podcasts_data")
        private val json = Json { 
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    private suspend fun loadPodcasts(): List<Podcast> {
        val jsonString = context.podcastDataStore.data.first()[KEY_PODCASTS]
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

    private suspend fun savePodcasts(podcasts: List<Podcast>) {
        context.podcastDataStore.edit { preferences ->
            val jsonString = json.encodeToString(podcasts)
            preferences[KEY_PODCASTS] = jsonString
        }
    }

    override fun getAllPodcasts(): Flow<List<Podcast>> {
        return context.podcastDataStore.data.map { preferences ->
            val jsonString = preferences[KEY_PODCASTS]
            if (jsonString != null) {
                try {
                    json.decodeFromString<List<Podcast>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }

    override suspend fun getPodcastsFiltered(
        filter: PodcastFilter,
        sortOrder: PodcastSortOrder
    ): List<Podcast> {
        var result = loadPodcasts()

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
        return loadPodcasts().find { it.id == id }
    }

    override suspend fun savePodcast(podcast: Podcast) {
        val podcasts = loadPodcasts()
        savePodcasts(podcasts + podcast)
    }

    override suspend fun updatePodcast(podcast: Podcast) {
        val podcasts = loadPodcasts()
        val updated = podcasts.map { if (it.id == podcast.id) podcast else it }
        savePodcasts(updated)
    }

    override suspend fun deletePodcast(id: String) {
        val podcasts = loadPodcasts()
        savePodcasts(podcasts.filter { it.id != id })
    }

    override suspend fun addAudioVersion(podcastId: String, audioVersion: AudioVersion) {
        val podcasts = loadPodcasts()
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
        val podcasts = loadPodcasts()
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
        val podcasts = loadPodcasts()
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
        val podcasts = loadPodcasts()
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
        val podcasts = loadPodcasts()
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
        val podcasts = loadPodcasts()
        if (query.isBlank()) return podcasts

        val lowerQuery = query.lowercase()
        return podcasts.filter { podcast ->
            podcast.title.lowercase().contains(lowerQuery) ||
            podcast.author?.lowercase()?.contains(lowerQuery) == true ||
            podcast.textContent.lowercase().contains(lowerQuery)
        }
    }
}
