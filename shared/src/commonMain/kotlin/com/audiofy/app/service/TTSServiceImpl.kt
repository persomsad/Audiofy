package com.audiofy.app.service

import com.audiofy.app.data.AppConfig
import com.audiofy.app.data.Qwen3TTSInput
import com.audiofy.app.data.Qwen3TTSRequest
import com.audiofy.app.data.Qwen3TTSResponse
import com.audiofy.app.data.TTSProgress
import com.audiofy.app.data.TTSStage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * Qwen3 TTS Service Implementation
 * Uses Ktor Client to call Qwen3 (DashScope) REST API
 */
class TTSServiceImpl : TTSService {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
        encodeDefaults = true  // 🔑 关键修复：确保默认值参数也会被序列化
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
            if (!config.isQwen3Configured()) {
                throw IllegalStateException("Qwen3 TTS API 配置不完整")
            }

            // 判断是否需要分片处理
            val audioData = if (text.length <= MAX_TEXT_LENGTH) {
                // 短文本：直接调用API
                callQwen3API(text, config)
            } else {
                // 长文本：分片处理并合并音频
                synthesizeLongText(text, config)
            }

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
            if (!config.isQwen3Configured()) {
                throw IllegalStateException("Qwen3 TTS API 配置不完整")
            }

            // Preparing
            emit(
                TTSProgress(
                    stage = TTSStage.PREPARING,
                    progress = 0.0f,
                    message = "准备生成语音..."
                )
            )

            // 判断是否需要分片处理
            if (text.length <= MAX_TEXT_LENGTH) {
                // 短文本：直接调用API
                emit(
                    TTSProgress(
                        stage = TTSStage.GENERATING,
                        progress = 0.2f,
                        message = "生成语音中..."
                    )
                )

                val audioData = callQwen3APIWithProgress(text, config) { progress ->
                    emit(
                        TTSProgress(
                            stage = TTSStage.DOWNLOADING,
                            progress = 0.2f + progress * 0.8f, // 20% - 100%
                            message = "下载音频... ${(progress * 100).toInt()}%"
                        )
                    )
                }
            } else {
                // 长文本：分片处理并合并音频
                synthesizeLongTextWithProgress(text, config) { progress ->
                    emit(progress)
                }
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
     * Call Qwen3 TTS API (two-step process)
     * Step 1: Call API to get audio URL
     * Step 2: Download audio from URL
     */
    private suspend fun callQwen3API(
        text: String,
        config: AppConfig,
    ): ByteArray {
        val client = createHttpClient()

        try {
            // Step 1: Call Qwen3 API to generate TTS and get audio URL
            val request = Qwen3TTSRequest(
                model = "qwen3-tts-flash",
                input = Qwen3TTSInput(
                    text = text,
                    voice = config.qwen3Voice,
                    languageType = config.qwen3LanguageType
                )
            )

            val response: HttpResponse = client.post(QWEN3_BASE_URL) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${config.qwen3ApiKey}")
                setBody(request)
            }

            if (!response.status.isSuccess()) {
                val errorBody = try {
                    response.bodyAsText()
                } catch (e: Exception) {
                    "无法读取错误响应"
                }
                throw Exception("Qwen3 API 调用失败: ${response.status} - $errorBody")
            }

            val ttsResponse: Qwen3TTSResponse = response.body()
            val audioUrl = ttsResponse.output.audio.url

            // Step 2: Download audio from URL
            val audioResponse: HttpResponse = client.get(audioUrl)

            if (!audioResponse.status.isSuccess()) {
                throw Exception("音频下载失败: ${audioResponse.status}")
            }

            return audioResponse.readRawBytes()
        } finally {
            client.close()
        }
    }

    /**
     * Call Qwen3 TTS API with progress tracking
     * Step 1: Call API to get audio URL (progress 0-0.3)
     * Step 2: Download audio from URL (progress 0.3-1.0)
     */
    private suspend fun callQwen3APIWithProgress(
        text: String,
        config: AppConfig,
        onProgress: suspend (Float) -> Unit,
    ): ByteArray {
        val client = createHttpClient()

        try {
            // Step 1: Call Qwen3 API to generate TTS and get audio URL
            val request = Qwen3TTSRequest(
                model = "qwen3-tts-flash",
                input = Qwen3TTSInput(
                    text = text,
                    voice = config.qwen3Voice,
                    languageType = config.qwen3LanguageType
                )
            )

            onProgress(0.1f) // Starting API call

            val response: HttpResponse = client.post(QWEN3_BASE_URL) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${config.qwen3ApiKey}")
                setBody(request)
            }

            if (!response.status.isSuccess()) {
                val errorBody = try {
                    response.bodyAsText()
                } catch (e: Exception) {
                    "无法读取错误响应"
                }
                throw Exception("Qwen3 API 调用失败: ${response.status} - $errorBody")
            }

            onProgress(0.3f) // API call completed, got audio URL

            val ttsResponse: Qwen3TTSResponse = response.body()
            val audioUrl = ttsResponse.output.audio.url

            // Step 2: Download audio from URL
            onProgress(0.5f) // Starting audio download

            val audioResponse: HttpResponse = client.get(audioUrl)

            if (!audioResponse.status.isSuccess()) {
                throw Exception("音频下载失败: ${audioResponse.status}")
            }

            val audioData = audioResponse.readRawBytes()
            onProgress(1.0f) // Download completed

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
            e.message?.contains("400") == true -> "请求参数错误 (400): ${e.message}\n请检查：\n1. API Key 是否正确\n2. 文本内容是否包含特殊字符\n3. 语音设置是否正确"
            e.message?.contains("401") == true -> "Qwen3 API Key 无效,请在设置中重新配置"
            e.message?.contains("403") == true -> "TTS 配额已用尽,请稍后再试"
            e.message?.contains("404") == true -> "API 端点无效,请检查配置"
            e.message?.contains("network", ignoreCase = true) == true -> "网络连接失败,请检查网络"
            e.message?.contains("timeout", ignoreCase = true) == true -> "请求超时,请重试"
            e.message?.contains("超过限制") == true -> e.message!!
            e.message?.contains("配置不完整") == true -> e.message!!
            else -> "语音生成失败: ${e.message ?: "未知错误"}"
        }

        return Exception(message, e)
    }

    /**
     * 处理长文本(>600字符)
     * 使用智能分片 + 音频拼接
     */
    private suspend fun synthesizeLongText(text: String, config: AppConfig): ByteArray {
        // 1. 智能分片
        val chunks = TextChunker.smartChunk(text, MAX_TEXT_LENGTH)
        val totalChunks = chunks.size

        println("长文本分片: $totalChunks 片")
        println(TextChunker.getChunkStats(text, MAX_TEXT_LENGTH))

        // 2. 验证所有分片长度（安全检查）
        chunks.forEachIndexed { index, chunk ->
            if (chunk.length > MAX_TEXT_LENGTH) {
                throw IllegalStateException("分片${index + 1}长度${chunk.length}超过限制${MAX_TEXT_LENGTH}字符")
            }
        }

        // 3. 逐个生成音频
        val audioChunks = mutableListOf<ByteArray>()

        for ((index, chunk) in chunks.withIndex()) {
            val chunkNumber = index + 1

            println("生成第 $chunkNumber/$totalChunks 片... (长度: ${chunk.length})")

            try {
                // 生成音频
                val audioData = callQwen3API(chunk, config)
                audioChunks.add(audioData)

                // 速率限制保护: 片段间延迟500ms
                if (chunkNumber < totalChunks) {
                    kotlinx.coroutines.delay(500)
                }
            } catch (e: Exception) {
                throw Exception("第 $chunkNumber 片生成失败: ${e.message}", e)
            }
        }

        // 3. 合并音频
        println("合并 $totalChunks 个音频片段...")
        val mergedAudio = WavMerger.mergeWavFiles(audioChunks, insertSilence = true)
        println(WavMerger.getWavInfo(mergedAudio))

        return mergedAudio
    }

    /**
     * 处理长文本(>600字符) - 带进度反馈
     */
    private suspend fun synthesizeLongTextWithProgress(
        text: String,
        config: AppConfig,
        onProgress: suspend (TTSProgress) -> Unit
    ): ByteArray {
        // 1. 智能分片
        val chunks = TextChunker.smartChunk(text, MAX_TEXT_LENGTH)
        val totalChunks = chunks.size

        onProgress(
            TTSProgress(
                stage = TTSStage.GENERATING,
                progress = 0.1f,
                message = "文本分片: $totalChunks 片"
            )
        )

        // 2. 验证所有分片长度（安全检查）
        chunks.forEachIndexed { index, chunk ->
            if (chunk.length > MAX_TEXT_LENGTH) {
                throw IllegalStateException("分片${index + 1}长度${chunk.length}超过限制${MAX_TEXT_LENGTH}字符")
            }
        }

        // 3. 逐个生成音频
        val audioChunks = mutableListOf<ByteArray>()

        for ((index, chunk) in chunks.withIndex()) {
            val chunkNumber = index + 1
            val chunkProgress = chunkNumber.toFloat() / totalChunks

            // 更新进度
            onProgress(
                TTSProgress(
                    stage = TTSStage.GENERATING,
                    progress = 0.1f + chunkProgress * 0.8f, // 10% - 90%
                    message = "生成中...(第 $chunkNumber/$totalChunks 片)"
                )
            )

            try {
                // 生成音频（不带进度，否则会干扰整体进度显示）
                val audioData = callQwen3API(chunk, config)
                audioChunks.add(audioData)

                // 速率限制保护: 片段间延迟500ms
                if (chunkNumber < totalChunks) {
                    kotlinx.coroutines.delay(500)
                }
            } catch (e: Exception) {
                throw Exception("第 $chunkNumber 片生成失败: ${e.message}", e)
            }
        }

        // 3. 合并音频
        onProgress(
            TTSProgress(
                stage = TTSStage.DOWNLOADING,
                progress = 0.95f,
                message = "合并 $totalChunks 个音频片段..."
            )
        )

        val mergedAudio = WavMerger.mergeWavFiles(audioChunks, insertSilence = true)

        return mergedAudio
    }

    companion object {
        private const val QWEN3_BASE_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation"
        private const val MAX_TEXT_LENGTH = 600  // Qwen3 TTS Flash官方限制: [0, 600]字符
    }
}
