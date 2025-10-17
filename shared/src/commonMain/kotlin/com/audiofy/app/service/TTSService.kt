package com.audiofy.app.service

import com.audiofy.app.data.AppConfig
import com.audiofy.app.data.TTSProgress
import kotlinx.coroutines.flow.Flow

/**
 * Text-to-Speech (TTS) Service Interface
 * Converts text to natural speech audio using Qwen3 TTS API
 */
interface TTSService {

    /**
     * Synthesize speech from text
     *
     * @param text Text to convert to speech
     * @param config User's API configuration (API key, voice, language type)
     * @return Result containing audio data as ByteArray or error
     */
    suspend fun synthesizeSpeech(
        text: String,
        config: AppConfig,
    ): Result<ByteArray>

    /**
     * Synthesize speech with progress updates
     *
     * @param text Text to convert to speech
     * @param config User's API configuration
     * @return Flow emitting progress updates and final audio data
     */
    fun synthesizeSpeechWithProgress(
        text: String,
        config: AppConfig,
    ): Flow<TTSProgress>
}
