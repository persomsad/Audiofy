package com.audiofy.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Qwen3 TTS API Request Models
 * Based on DashScope API specification
 * https://help.aliyun.com/zh/model-studio/developer-reference/text-to-speech
 */

/**
 * Request body for Qwen3 TTS API
 */
@Serializable
data class Qwen3TTSRequest(
    val model: String = "qwen3-tts-flash",
    val input: Qwen3TTSInput,
)

/**
 * Input parameters for Qwen3 TTS
 */
@Serializable
data class Qwen3TTSInput(
    val text: String,
    val voice: String, // cherry, dylan, jada, sunny, etc.
    @SerialName("language_type")
    val languageType: String, // Chinese, English
)

/**
 * Response from Qwen3 TTS API
 */
@Serializable
data class Qwen3TTSResponse(
    val output: Qwen3TTSOutput,
    @SerialName("request_id")
    val requestId: String? = null,
)

/**
 * Output data containing audio URL
 */
@Serializable
data class Qwen3TTSOutput(
    val audio: Qwen3AudioInfo,
)

/**
 * Audio information with download URL
 */
@Serializable
data class Qwen3AudioInfo(
    val url: String, // Audio file URL (valid for 24 hours)
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

/**
 * Qwen3 TTS supported voices
 */
enum class Qwen3Voice(val voiceName: String, val displayName: String) {
    CHERRY("cherry", "Cherry (樱桃)"),
    DYLAN("dylan", "Dylan (迪伦)"),
    JADA("jada", "Jada (杰达)"),
    SUNNY("sunny", "Sunny (阳光)"),
    ETHAN("ethan", "Ethan (伊桑)"),
    NOFISH("nofish", "Nofish"),
    JENNIFER("jennifer", "Jennifer (珍妮弗)"),
    RYAN("ryan", "Ryan (瑞恩)"),
    KATERINA("katerina", "Katerina (卡特琳娜)"),
    ELIAS("elias", "Elias (伊莱亚斯)"),
    LI("li", "Li (李)"),
    MARCUS("marcus", "Marcus (马库斯)"),
    ROY("roy", "Roy (罗伊)"),
    PETER("peter", "Peter (彼得)"),
    ROCKY("rocky", "Rocky (洛奇)"),
    KIKI("kiki", "Kiki (琪琪)"),
    ERIC("eric", "Eric (埃里克)");

    companion object {
        fun fromName(name: String): Qwen3Voice? {
            return entries.find { it.voiceName.equals(name, ignoreCase = true) }
        }
    }
}

/**
 * Qwen3 TTS supported language types
 */
enum class Qwen3LanguageType(val typeName: String, val displayName: String) {
    CHINESE("Chinese", "中文"),
    ENGLISH("English", "英文");

    companion object {
        fun fromName(name: String): Qwen3LanguageType? {
            return entries.find { it.typeName.equals(name, ignoreCase = true) }
        }
    }
}
