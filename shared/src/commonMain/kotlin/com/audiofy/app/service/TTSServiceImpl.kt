package com.audiofy.app.service

import com.audiofy.app.data.AppConfig
import com.audiofy.app.data.TTSProgress
import com.audiofy.app.data.TTSRequest
import com.audiofy.app.data.TTSStage
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * ElevenLabs TTS Service Implementation
 * Uses Ktor Client to call ElevenLabs REST API
 */
class TTSServiceImpl : TTSService {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    private fun createHttpClient(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000 // 60 seconds (TTS takes longer)
                connectTimeoutMillis = 10_000 // 10 seconds
            }
        }
    }

    override suspend fun synthesizeSpeech(
        text: String,
        config: AppConfig,
    ): Result<ByteArray> {
        return try {
            // Validate config
            if (!config.isElevenLabsConfigured()) {
                throw IllegalStateException("ElevenLabs API 配置不完整")
            }

            // Validate text length (ElevenLabs limit ~5000 characters)
            if (text.length > MAX_TEXT_LENGTH) {
                throw IllegalArgumentException("文本长度超过限制 ($MAX_TEXT_LENGTH 字符),请分段处理")
            }

            val audioData = callElevenLabsAPI(text, config)
            Result.success(audioData)
        } catch (e: Exception) {
            Result.failure(translateException(e))
        }
    }

    override fun synthesizeSpeechWithProgress(
        text: String,
        config: AppConfig,
    ): Flow<TTSProgress> = flow {
        try {
            // Validate config
            if (!config.isElevenLabsConfigured()) {
                throw IllegalStateException("ElevenLabs API 配置不完整")
            }

            // Validate text length
            if (text.length > MAX_TEXT_LENGTH) {
                throw IllegalArgumentException("文本长度超过限制 ($MAX_TEXT_LENGTH 字符),请分段处理")
            }

            // Preparing
            emit(
                TTSProgress(
                    stage = TTSStage.PREPARING,
                    progress = 0.0f,
                    message = "准备生成语音..."
                )
            )

            // Generating
            emit(
                TTSProgress(
                    stage = TTSStage.GENERATING,
                    progress = 0.2f,
                    message = "生成语音中..."
                )
            )

            // Call API and download audio stream
            val audioData = callElevenLabsAPIWithProgress(text, config) { progress ->
                emit(
                    TTSProgress(
                        stage = TTSStage.DOWNLOADING,
                        progress = 0.2f + progress * 0.8f, // 20% - 100%
                        message = "下载音频... ${(progress * 100).toInt()}%"
                    )
                )
            }

            // Completed
            emit(
                TTSProgress(
                    stage = TTSStage.COMPLETED,
                    progress = 1.0f,
                    message = "语音生成完成"
                )
            )
        } catch (e: Exception) {
            throw translateException(e)
        }
    }

    /**
     * Call ElevenLabs TTS API
     */
    private suspend fun callElevenLabsAPI(
        text: String,
        config: AppConfig,
    ): ByteArray {
        val client = createHttpClient()

        try {
            val request = TTSRequest(
                text = text,
                model_id = config.elevenLabsModelId
            )

            val url = "$ELEVENLABS_BASE_URL/text-to-speech/${config.elevenLabsVoiceId}"

            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                header("xi-api-key", config.elevenLabsApiKey)
                setBody(request)
            }

            return response.readBytes()
        } finally {
            client.close()
        }
    }

    /**
     * Call ElevenLabs TTS API with progress tracking
     */
    private suspend fun callElevenLabsAPIWithProgress(
        text: String,
        config: AppConfig,
        onProgress: suspend (Float) -> Unit,
    ): ByteArray {
        val client = createHttpClient()

        try {
            val request = TTSRequest(
                text = text,
                model_id = config.elevenLabsModelId
            )

            val url = "$ELEVENLABS_BASE_URL/text-to-speech/${config.elevenLabsVoiceId}"

            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                header("xi-api-key", config.elevenLabsApiKey)
                setBody(request)
            }

            // Download audio data
            // Note: Progress tracking for streaming is complex in Ktor
            // For now, we'll download the entire response at once
            onProgress(0.5f) // Halfway through (started download)
            val audioData = response.readBytes()
            onProgress(1.0f) // Completed

            return audioData
        } finally {
            client.close()
        }
    }

    /**
     * Translate exceptions to user-friendly error messages
     */
    private fun translateException(e: Exception): Exception {
        val message = when {
            e is HttpRequestTimeoutException -> "请求超时,请重试"
            e.message?.contains("401") == true -> "ElevenLabs API Key 无效,请在设置中重新配置"
            e.message?.contains("403") == true -> "TTS 配额已用尽,请稍后再试"
            e.message?.contains("404") == true -> "语音 ID 无效,请在设置中重新配置"
            e.message?.contains("network", ignoreCase = true) == true -> "网络连接失败,请检查网络"
            e.message?.contains("timeout", ignoreCase = true) == true -> "请求超时,请重试"
            e.message?.contains("超过限制") == true -> e.message!!
            e.message?.contains("配置不完整") == true -> e.message!!
            else -> "语音生成失败: ${e.message ?: "未知错误"}"
        }

        return Exception(message, e)
    }

    companion object {
        private const val ELEVENLABS_BASE_URL = "https://api.elevenlabs.io/v1"
        private const val MAX_TEXT_LENGTH = 5000
    }
}
