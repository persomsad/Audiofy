package com.audiofy.app.data

import kotlinx.serialization.Serializable

/**
 * ElevenLabs TTS API Request Models
 * Based on ElevenLabs REST API specification
 * https://elevenlabs.io/docs/api-reference/text-to-speech
 */

/**
 * Request body for ElevenLabs TTS API
 */
@Serializable
data class TTSRequest(
    val text: String,
    val model_id: String,
    val voice_settings: VoiceSettings = VoiceSettings(),
)

/**
 * Voice settings for TTS generation
 */
@Serializable
data class VoiceSettings(
    val stability: Float = 0.5f,           // Voice consistency (0-1)
    val similarity_boost: Float = 0.5f,    // Clarity vs similarity (0-1)
)

/**
 * TTS generation progress state
 */
data class TTSProgress(
    val stage: TTSStage,
    val progress: Float,  // 0.0 - 1.0
    val message: String,
)

/**
 * TTS generation stages
 */
enum class TTSStage {
    PREPARING,      // Preparing request
    GENERATING,     // Generating speech
    DOWNLOADING,    // Downloading audio stream
    COMPLETED,      // Completed
}
