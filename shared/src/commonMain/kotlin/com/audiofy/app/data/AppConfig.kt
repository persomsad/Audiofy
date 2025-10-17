package com.audiofy.app.data

import kotlinx.serialization.Serializable

/**
 * 应用配置数据模型
 * 存储用户自定义的 Qwen3 TTS API 配置
 *
 * 注意：应用只负责 TTS 功能，文本翻译由用户在 Gemini 网页版自行完成
 */
@Serializable
data class AppConfig(
    // Qwen3 TTS API 配置
    val qwen3ApiKey: String = "",
    val qwen3Voice: String = "Cherry",  // 官方文档使用首字母大写
    val qwen3LanguageType: String = "Chinese",
) {
    /**
     * 检查配置是否完整（Qwen3 API Key 必填）
     */
    fun isComplete(): Boolean =
        qwen3ApiKey.isNotBlank()

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
