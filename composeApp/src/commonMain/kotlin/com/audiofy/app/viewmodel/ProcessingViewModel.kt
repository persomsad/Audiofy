package com.audiofy.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audiofy.app.data.AppConfig
import com.audiofy.app.data.AudioVersion
import com.audiofy.app.data.Podcast
import com.audiofy.app.repository.PodcastRepository
import com.audiofy.app.repository.createPodcastRepository
import com.audiofy.app.service.FileStorageService
import com.audiofy.app.service.TTSService
import com.audiofy.app.service.createFileStorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Processing Steps
 */
sealed class ProcessingStep {
    object GeneratingSpeech : ProcessingStep()
    data class Completed(val audioData: ByteArray) : ProcessingStep() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Completed) return false
            return audioData.contentEquals(other.audioData)
        }

        override fun hashCode(): Int {
            return audioData.contentHashCode()
        }
    }

    data class Error(val message: String) : ProcessingStep()
}

/**
 * Processing UI State
 */
data class ProcessingUiState(
    val currentStep: ProcessingStep = ProcessingStep.GeneratingSpeech,
    val progress: Float = 0f,
    val inputText: String = "",
    val audioData: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProcessingUiState) return false

        if (currentStep != other.currentStep) return false
        if (progress != other.progress) return false
        if (inputText != other.inputText) return false
        if (audioData != null) {
            if (other.audioData == null) return false
            if (!audioData.contentEquals(other.audioData)) return false
        } else if (other.audioData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentStep.hashCode()
        result = 31 * result + progress.hashCode()
        result = 31 * result + inputText.hashCode()
        result = 31 * result + (audioData?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * Processing ViewModel
 * 直接调用 TTS 服务将文本转为语音
 *
 * 注意：应用只负责 TTS，不进行翻译。用户应在 Gemini 网页版自行翻译文本后粘贴到应用。
 */
class ProcessingViewModel(
    private val ttsService: TTSService,
    private val config: AppConfig,
    private val podcastRepository: PodcastRepository = createPodcastRepository(),
    private val fileStorageService: FileStorageService = createFileStorageService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProcessingUiState())
    val uiState: StateFlow<ProcessingUiState> = _uiState.asStateFlow()
    
    private var currentPodcastId: String? = null

    /**
     * Start processing pipeline
     * 直接调用 TTS 生成语音
     */
    fun startProcessing(inputText: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(inputText = inputText, progress = 0f) }

                // TTS 生成语音
                _uiState.update { it.copy(currentStep = ProcessingStep.GeneratingSpeech, progress = 0.3f) }
                val ttsResult = ttsService.synthesizeSpeech(inputText, config)

                if (ttsResult.isFailure) {
                    val errorMessage = ttsResult.exceptionOrNull()?.message ?: "语音生成失败"
                    _uiState.update { it.copy(currentStep = ProcessingStep.Error(errorMessage)) }
                    return@launch
                }

                val audioData = ttsResult.getOrThrow()
                
                // 保存音频文件和播客元数据
                _uiState.update { it.copy(progress = 0.7f) }
                val podcastId = savePodcastWithAudio(inputText, audioData)
                currentPodcastId = podcastId
                
                _uiState.update {
                    it.copy(
                        currentStep = ProcessingStep.Completed(audioData),
                        audioData = audioData,
                        progress = 1.0f
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(currentStep = ProcessingStep.Error(e.message ?: "未知错误"))
                }
            }
        }
    }
    
    /**
     * Save podcast with audio file
     * Returns the podcast ID
     */
    private suspend fun savePodcastWithAudio(textContent: String, audioData: ByteArray): String {
        // Generate IDs
        val podcastId = generateUUID()
        val versionId = generateUUID()
        val timestamp = currentTimeMillis()
        
        // Save audio file
        val relativePath = "audios/$podcastId/$versionId.wav"
        fileStorageService.saveAudio(relativePath, audioData)
        
        // Calculate audio duration (estimate: ~150 bytes per character for Chinese TTS)
        val estimatedDuration = (audioData.size / 24000).coerceAtLeast(1) // seconds
        
        // Create audio version
        val audioVersion = AudioVersion(
            versionId = versionId,
            voice = config.qwen3Voice,
            languageType = config.qwen3LanguageType,
            audioPath = relativePath,
            duration = estimatedDuration,
            fileSize = audioData.size.toLong(),
            createdAt = timestamp
        )
        
        // Generate title from first 30 characters of text
        val title = if (textContent.length > 30) {
            textContent.substring(0, 30) + "..."
        } else {
            textContent
        }
        
        // Create podcast
        val podcast = Podcast(
            id = podcastId,
            title = title,
            author = null,
            textContent = textContent,
            coverUrl = null,
            audioVersions = listOf(audioVersion),
            currentVersionId = versionId,
            createdAt = timestamp,
            lastPlayedAt = null,
            playProgress = 0f,
            isFavorite = false
        )
        
        // Save to repository
        podcastRepository.savePodcast(podcast)
        
        return podcastId
    }
    
    /**
     * Get the saved podcast ID
     */
    fun getPodcastId(): String? = currentPodcastId

    /**
     * Retry processing
     */
    fun retry() {
        val inputText = _uiState.value.inputText
        if (inputText.isNotBlank()) {
            startProcessing(inputText)
        }
    }

    /**
     * Reset state
     */
    fun reset() {
        _uiState.value = ProcessingUiState()
        currentPodcastId = null
    }
    
    /**
     * Generate UUID (platform-specific implementation needed)
     */
    private fun generateUUID(): String {
        return java.util.UUID.randomUUID().toString()
    }
    
    /**
     * Get current time in milliseconds
     */
    private fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
}
