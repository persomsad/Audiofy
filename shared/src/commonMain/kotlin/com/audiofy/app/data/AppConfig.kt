package com.audiofy.app.data

import kotlinx.serialization.Serializable

/**
 * 应用配置数据模型
 * 存储用户自定义的 API 配置
 */
@Serializable
data class AppConfig(
    // Gemini API 配置
    val geminiApiKey: String = "",
    val geminiModelId: String = "gemini-2.0-flash-exp",
    val geminiBaseUrl: String = "https://generativelanguage.googleapis.com/v1beta",

    // Qwen3 TTS API 配置
    val qwen3ApiKey: String = "",
    val qwen3Voice: String = "cherry",
    val qwen3LanguageType: String = "Chinese",
) {
    /**
     * 检查配置是否完整（所有必填项都已填写）
     */
    fun isComplete(): Boolean =
        geminiApiKey.isNotBlank() &&
        qwen3ApiKey.isNotBlank()

    /**
     * 检查 Gemini 配置是否完整
     */
    fun isGeminiConfigured(): Boolean =
        geminiApiKey.isNotBlank()

    /**
     * 检查 Qwen3 TTS 配置是否完整
     */
    fun isQwen3Configured(): Boolean =
        qwen3ApiKey.isNotBlank()

    companion object {
        /**
         * 默认配置（用于首次启动）
         */
        val DEFAULT = AppConfig()
    }
}
