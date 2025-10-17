package com.audiofy.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audiofy.app.data.Podcast
import com.audiofy.app.data.PodcastFilter
import com.audiofy.app.data.PodcastSortOrder
import com.audiofy.app.repository.PodcastRepository
import com.audiofy.app.repository.createPodcastRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Library UI State
 */
data class LibraryUiState(
    val podcasts: List<Podcast> = emptyList(),
    val selectedFilter: PodcastFilter = PodcastFilter.ALL,
    val selectedSortOrder: PodcastSortOrder = PodcastSortOrder.CREATED_DESC,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Library ViewModel
 * Manages the podcast library, filtering, and sorting
 */
class LibraryViewModel(
    private val podcastRepository: PodcastRepository = createPodcastRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        // Observe podcast changes
        viewModelScope.launch {
            podcastRepository.getAllPodcasts().collect { podcasts ->
                _uiState.update { currentState ->
                    val filtered = filterAndSort(
                        podcasts,
                        currentState.selectedFilter,
                        currentState.selectedSortOrder
                    )
                    currentState.copy(podcasts = filtered, isLoading = false)
                }
            }
        }
    }

    /**
     * Change filter
     */
    fun selectFilter(filter: PodcastFilter) {
        _uiState.update { currentState ->
            val podcasts = filterAndSort(
                currentState.podcasts,
                filter,
                currentState.selectedSortOrder
            )
            currentState.copy(
                selectedFilter = filter,
                podcasts = podcasts
            )
        }
    }

    /**
     * Change sort order
     */
    fun selectSortOrder(sortOrder: PodcastSortOrder) {
        _uiState.update { currentState ->
            val podcasts = filterAndSort(
                currentState.podcasts,
                currentState.selectedFilter,
                sortOrder
            )
            currentState.copy(
                selectedSortOrder = sortOrder,
                podcasts = podcasts
            )
        }
    }

    /**
     * Search podcasts
     */
    fun searchPodcasts(query: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val results = podcastRepository.searchPodcasts(query)
                _uiState.update { it.copy(podcasts = results, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "搜索失败",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Delete podcast
     */
    fun deletePodcast(podcastId: String) {
        viewModelScope.launch {
            try {
                podcastRepository.deletePodcast(podcastId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "删除失败")
                }
            }
        }
    }

    /**
     * Toggle favorite status
     */
    fun toggleFavorite(podcastId: String) {
        viewModelScope.launch {
            try {
                podcastRepository.toggleFavorite(podcastId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "操作失败")
                }
            }
        }
    }

    /**
     * Filter and sort podcasts
     */
    private fun filterAndSort(
        podcasts: List<Podcast>,
        filter: PodcastFilter,
        sortOrder: PodcastSortOrder
    ): List<Podcast> {
        var result = when (filter) {
            PodcastFilter.ALL -> podcasts
            PodcastFilter.RECENT -> podcasts.sortedByDescending { it.createdAt }.take(10)
            PodcastFilter.UNFINISHED -> podcasts.filter { it.playProgress < 0.95f }
            PodcastFilter.FAVORITE -> podcasts.filter { it.isFavorite }
        }

        result = when (sortOrder) {
            PodcastSortOrder.CREATED_DESC -> result.sortedByDescending { it.createdAt }
            PodcastSortOrder.CREATED_ASC -> result.sortedBy { it.createdAt }
            PodcastSortOrder.LAST_PLAYED_DESC -> result.sortedByDescending { it.lastPlayedAt ?: 0 }
            PodcastSortOrder.TITLE_ASC -> result.sortedBy { it.title }
        }

        return result
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
