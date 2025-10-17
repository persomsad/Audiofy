package com.audiofy.app.service

import com.audiofy.app.data.AppConfig
import com.audiofy.app.data.TTSProgress
import kotlinx.coroutines.flow.Flow

/**
 * Text-to-Speech (TTS) Service Interface
 * Converts Chinese text to natural speech audio using ElevenLabs API
 */
interface TTSService {

    /**
     * Synthesize speech from Chinese text
     *
     * @param text Chinese text to convert to speech
     * @param config User's API configuration (API key, voice ID, model ID)
     * @return Result containing audio data as ByteArray or error
     */
    suspend fun synthesizeSpeech(
        text: String,
        config: AppConfig,
    ): Result<ByteArray>

    /**
     * Synthesize speech with progress updates
     *
     * @param text Chinese text to convert to speech
     * @param config User's API configuration
     * @return Flow emitting progress updates and final audio data
     */
    fun synthesizeSpeechWithProgress(
        text: String,
        config: AppConfig,
    ): Flow<TTSProgress>
}
