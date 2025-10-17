package com.audiofy.app.service

import com.audiofy.app.data.AppConfig
import com.audiofy.app.data.TranslationProgress
import kotlinx.coroutines.flow.Flow

/**
 * Gemini Translation Service Interface
 * Provides two-stage translation: Accurate Translation → Polish
 */
interface GeminiService {

    /**
     * Translate English text to Chinese using two-stage process
     *
     * @param inputText English text to translate
     * @param config User's API configuration (API key, model ID, base URL)
     * @return Result containing final polished Chinese text or error
     */
    suspend fun translateTwoStage(
        inputText: String,
        config: AppConfig,
    ): Result<String>

    /**
     * Translate with progress updates
     *
     * @param inputText English text to translate
     * @param config User's API configuration
     * @return Flow emitting progress updates and final result
     */
    fun translateTwoStageWithProgress(
        inputText: String,
        config: AppConfig,
    ): Flow<TranslationProgress>
}
