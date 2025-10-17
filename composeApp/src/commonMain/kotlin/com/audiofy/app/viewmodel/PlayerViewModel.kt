package com.audiofy.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audiofy.app.data.Podcast
import com.audiofy.app.player.AudioPlayer
import com.audiofy.app.player.PlayerState
import com.audiofy.app.repository.PodcastRepository
import com.audiofy.app.repository.createPodcastRepository
import com.audiofy.app.service.FileStorageService
import com.audiofy.app.service.createFileStorageService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Player UI State
 */
data class PlayerUiState(
    val state: PlayerState = PlayerState.IDLE,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val errorMessage: String? = null,
    val isUserSeeking: Boolean = false, // User is dragging slider
    val currentPodcast: Podcast? = null
)

/**
 * Audio Player ViewModel
 * Manages player state and provides UI-level control
 */
class PlayerViewModel(
    private val podcastRepository: PodcastRepository = createPodcastRepository(),
    private val fileStorageService: FileStorageService = createFileStorageService()
) : ViewModel() {

    private val player = AudioPlayer()

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressUpdateJob: Job? = null

    init {
        player.setOnStateChangedListener { newState ->
            _uiState.update { it.copy(state = newState) }

            // Start/stop progress updates based on state
            when (newState) {
                PlayerState.PLAYING -> startProgressUpdates()
                PlayerState.PAUSED, PlayerState.IDLE, PlayerState.READY -> stopProgressUpdates()
                PlayerState.ERROR -> {
                    stopProgressUpdates()
                    _uiState.update { it.copy(errorMessage = "播放出错") }
                }

                else -> {}
            }
        }
    }

    /**
     * Load audio file by podcast ID
     */
    fun loadPodcast(podcastId: String) {
        viewModelScope.launch {
            try {
                val podcast = podcastRepository.getPodcastById(podcastId)
                if (podcast == null) {
                    _uiState.update { it.copy(errorMessage = "播客不存在") }
                    return@launch
                }
                
                val currentVersion = podcast.audioVersions.find { it.versionId == podcast.currentVersionId }
                if (currentVersion == null) {
                    _uiState.update { it.copy(errorMessage = "音频版本不存在") }
                    return@launch
                }
                
                val audioPath = fileStorageService.getDataDirectory() + "/" + currentVersion.audioPath
                player.loadAudio(audioPath)
                updateDuration()
                
                _uiState.update { it.copy(currentPodcast = podcast) }
                
                // Update last played timestamp
                podcastRepository.updateLastPlayedAt(podcastId, System.currentTimeMillis())
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "加载失败") }
            }
        }
    }
    
    /**
     * Load audio file directly by path (deprecated, use loadPodcast)
     */
    @Deprecated("Use loadPodcast(podcastId) instead")
    fun loadAudio(filePath: String) {
        player.loadAudio(filePath)
        updateDuration()
    }

    /**
     * Play/Pause toggle
     */
    fun togglePlayPause() {
        when (player.getState()) {
            PlayerState.READY, PlayerState.PAUSED -> player.play()
            PlayerState.PLAYING -> player.pause()
            else -> {}
        }
    }

    /**
     * Stop playback
     */
    fun stop() {
        player.stop()
        _uiState.update { it.copy(currentPosition = 0L) }
    }

    /**
     * User starts seeking (dragging slider)
     */
    fun onSeekStart() {
        _uiState.update { it.copy(isUserSeeking = true) }
    }

    /**
     * User seeks to position (dragging slider)
     */
    fun onSeekChange(position: Long) {
        _uiState.update { it.copy(currentPosition = position) }
    }

    /**
     * User finishes seeking (releases slider)
     */
    fun onSeekEnd(position: Long) {
        player.seekTo(position)
        _uiState.update { it.copy(currentPosition = position, isUserSeeking = false) }
    }

    /**
     * Start real-time progress updates
     */
    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressUpdateJob = viewModelScope.launch {
            while (isActive && player.getState() == PlayerState.PLAYING) {
                if (!_uiState.value.isUserSeeking) {
                    val currentPos = player.getCurrentPosition()
                    _uiState.update { it.copy(currentPosition = currentPos) }

                    // Check if playback completed
                    val duration = player.getDuration()
                    if (duration > 0 && currentPos >= duration) {
                        player.stop()
                        _uiState.update { it.copy(currentPosition = 0L) }
                        break
                    }
                }
                delay(100) // Update every 100ms
            }
        }
    }

    /**
     * Stop progress updates
     */
    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    /**
     * Update duration
     */
    private fun updateDuration() {
        viewModelScope.launch {
            // Wait for duration to be available
            var retries = 0
            while (retries < 20) {
                val duration = player.getDuration()
                if (duration > 0) {
                    _uiState.update { it.copy(duration = duration) }
                    break
                }
                delay(100)
                retries++
            }
        }
    }

    /**
     * Format milliseconds to mm:ss
     */
    fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}"
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressUpdates()
        player.release()
    }
}
