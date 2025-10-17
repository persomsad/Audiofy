package com.audiofy.app.service

import com.audiofy.app.data.AppConfig
import com.audiofy.app.data.Content
import com.audiofy.app.data.GeminiRequest
import com.audiofy.app.data.GeminiResponse
import com.audiofy.app.data.Part
import com.audiofy.app.data.TranslationProgress
import com.audiofy.app.data.TranslationStage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * Gemini Translation Service Implementation
 * Uses Ktor Client to call Gemini REST API
 */
class GeminiServiceImpl : GeminiService {

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
                requestTimeoutMillis = 30_000 // 30 seconds
                connectTimeoutMillis = 10_000 // 10 seconds
            }
        }
    }

    override suspend fun translateTwoStage(
        inputText: String,
        config: AppConfig,
    ): Result<String> {
        return try {
            // Stage 1: Accurate Translation
            val stage1Result = callGeminiAPI(
                text = inputText,
                prompt = STAGE_1_PROMPT_TEMPLATE,
                config = config
            )

            // Stage 2: Polish
            val stage2Result = callGeminiAPI(
                text = stage1Result,
                prompt = STAGE_2_PROMPT_TEMPLATE,
                config = config
            )

            Result.success(stage2Result)
        } catch (e: Exception) {
            Result.failure(translateException(e))
        }
    }

    override fun translateTwoStageWithProgress(
        inputText: String,
        config: AppConfig,
    ): Flow<TranslationProgress> = flow {
        try {
            // Validate config
            if (!config.isGeminiConfigured()) {
                throw IllegalStateException("Gemini API 配置不完整")
            }

            // Stage 1: Accurate Translation
            emit(
                TranslationProgress(
                    stage = TranslationStage.STAGE_1_TRANSLATE,
                    message = "翻译中..."
                )
            )

            val stage1Result = callGeminiAPI(
                text = inputText,
                prompt = STAGE_1_PROMPT_TEMPLATE,
                config = config
            )

            // Stage 2: Polish
            emit(
                TranslationProgress(
                    stage = TranslationStage.STAGE_2_POLISH,
                    message = "润色中...",
                    intermediateResult = stage1Result
                )
            )

            val stage2Result = callGeminiAPI(
                text = stage1Result,
                prompt = STAGE_2_PROMPT_TEMPLATE,
                config = config
            )

            // Completed
            emit(
                TranslationProgress(
                    stage = TranslationStage.STAGE_2_POLISH,
                    message = "翻译完成",
                    intermediateResult = stage2Result
                )
            )
        } catch (e: Exception) {
            throw translateException(e)
        }
    }

    /**
     * Call Gemini API with prompt template
     */
    private suspend fun callGeminiAPI(
        text: String,
        prompt: String,
        config: AppConfig,
    ): String {
        val client = createHttpClient()

        try {
            val fullPrompt = prompt.replace("[INPUT_TEXT]", text)

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = fullPrompt))
                    )
                )
            )

            val url = "${config.geminiBaseUrl}/models/${config.geminiModelId}:generateContent"

            val response: GeminiResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                header("x-goog-api-key", config.geminiApiKey) // Gemini uses x-goog-api-key header
                setBody(request)
            }.body()

            return extractTextFromResponse(response)
        } finally {
            client.close()
        }
    }

    /**
     * Extract generated text from Gemini response
     */
    private fun extractTextFromResponse(response: GeminiResponse): String {
        if (response.candidates.isEmpty()) {
            throw IllegalStateException("Gemini API 返回空结果")
        }

        val candidate = response.candidates.first()
        if (candidate.content.parts.isEmpty()) {
            throw IllegalStateException("Gemini API 返回无效结果")
        }

        return candidate.content.parts.first().text
    }

    /**
     * Translate exceptions to user-friendly error messages
     */
    private fun translateException(e: Exception): Exception {
        val message = when {
            e is HttpRequestTimeoutException -> "请求超时,请重试"
            e.message?.contains("401") == true -> "API Key 无效,请在设置中重新配置"
            e.message?.contains("403") == true -> "API 配额已用尽,请稍后再试"
            e.message?.contains("404") == true -> "API 地址不正确,请检查设置"
            e.message?.contains("network", ignoreCase = true) == true -> "网络连接失败,请检查网络"
            e.message?.contains("timeout", ignoreCase = true) == true -> "请求超时,请重试"
            else -> "翻译失败: ${e.message ?: "未知错误"}"
        }

        return Exception(message, e)
    }

    companion object {
        /**
         * Stage 1: Accurate Translation Prompt
         * Prioritize accuracy and completeness
         */
        private val STAGE_1_PROMPT_TEMPLATE = """
Translate the following English text to Chinese. Prioritize accuracy and completeness. Preserve the original meaning, tone, and structure.

[INPUT_TEXT]
        """.trimIndent()

        /**
         * Stage 2: Polish Prompt
         * Make translation more natural while preserving meaning
         */
        private val STAGE_2_PROMPT_TEMPLATE = """
Polish the following Chinese translation to make it more natural and fluent while preserving the original meaning. Remove translation accent, adjust sentence structure to match Chinese language logic, and improve readability.

[INPUT_TEXT]
        """.trimIndent()
    }
}
