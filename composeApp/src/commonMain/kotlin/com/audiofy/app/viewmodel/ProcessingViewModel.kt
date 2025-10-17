package com.audiofy.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audiofy.app.data.AppConfig
import com.audiofy.app.service.GeminiService
import com.audiofy.app.service.TTSService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Processing Steps
 */
sealed class ProcessingStep {
    object TranslatingStage1 : ProcessingStep()
    object TranslatingStage2 : ProcessingStep()
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
    val currentStep: ProcessingStep = ProcessingStep.TranslatingStage1,
    val progress: Float = 0f,
    val inputText: String = "",
    val translatedText: String? = null,
    val audioData: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProcessingUiState) return false

        if (currentStep != other.currentStep) return false
        if (progress != other.progress) return false
        if (inputText != other.inputText) return false
        if (translatedText != other.translatedText) return false
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
        result = 31 * result + (translatedText?.hashCode() ?: 0)
        result = 31 * result + (audioData?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * Processing ViewModel
 * Orchestrates translation and TTS services
 */
class ProcessingViewModel(
    private val geminiService: GeminiService,
    private val ttsService: TTSService,
    private val config: AppConfig,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProcessingUiState())
    val uiState: StateFlow<ProcessingUiState> = _uiState.asStateFlow()

    /**
     * Start processing pipeline
     * 1. Translate (Stage 1 + Stage 2)
     * 2. Generate speech
     */
    fun startProcessing(inputText: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(inputText = inputText, progress = 0f) }

                // Step 1: Translation
                _uiState.update { it.copy(currentStep = ProcessingStep.TranslatingStage1, progress = 0.1f) }
                val translationResult = geminiService.translateTwoStage(inputText, config)

                if (translationResult.isFailure) {
                    val errorMessage = translationResult.exceptionOrNull()?.message ?: "翻译失败"
                    _uiState.update { it.copy(currentStep = ProcessingStep.Error(errorMessage)) }
                    return@launch
                }

                val translatedText = translationResult.getOrThrow()
                _uiState.update {
                    it.copy(
                        currentStep = ProcessingStep.TranslatingStage2,
                        translatedText = translatedText,
                        progress = 0.5f
                    )
                }

                // Step 2: TTS
                _uiState.update { it.copy(currentStep = ProcessingStep.GeneratingSpeech, progress = 0.7f) }
                val ttsResult = ttsService.synthesizeSpeech(translatedText, config)

                if (ttsResult.isFailure) {
                    val errorMessage = ttsResult.exceptionOrNull()?.message ?: "语音生成失败"
                    _uiState.update { it.copy(currentStep = ProcessingStep.Error(errorMessage)) }
                    return@launch
                }

                val audioData = ttsResult.getOrThrow()
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
    }
}
