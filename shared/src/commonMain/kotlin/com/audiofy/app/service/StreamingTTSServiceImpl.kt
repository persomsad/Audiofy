package com.audiofy.app.service

import com.audiofy.app.data.AppConfig
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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * 流式TTS服务实现
 * 使用Qwen3 TTS的流式API (stream=true)
 * 
 * API端点：https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation
 * 参数：stream=true，返回SSE格式的base64音频块
 * 
 * TODO: 完整实现需要解决的问题：
 * 1. SSE (Server-Sent Events) 解析
 * 2. Base64解码后的音频格式处理
 * 3. 音频块缓冲和平滑播放
 * 4. 错误重试机制
 * 5. 跨平台音频播放器集成
 */
class StreamingTTSServiceImpl : StreamingTTSService {
    
    private val json = Json {
        ignoreUnknownKeys = true
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
                requestTimeoutMillis = 300_000 // 5分钟（流式需要更长时间）
                connectTimeoutMillis = 10_000
            }
        }
    }
    
    @OptIn(ExperimentalEncodingApi::class)
    override fun synthesizeSpeechStreaming(
        text: String,
        config: AppConfig
    ): Flow<StreamingAudioChunk> = flow {
        val client = createHttpClient()
        
        try {
            // 验证配置
            if (!config.isQwen3Configured()) {
                emit(StreamingAudioChunk.Error(IllegalStateException("Qwen3 TTS API 配置不完整")))
                return@flow
            }
            
            // 构建请求体
            val requestBody = """
                {
                    "model": "qwen3-tts-flash",
                    "input": {
                        "text": "${text.replace("\"", "\\\"")}",
                        "voice": "${config.qwen3Voice}",
                        "language_type": "${config.qwen3LanguageType}"
                    },
                    "parameters": {
                        "format": "pcm",
                        "sample_rate": 24000,
                        "stream": true
                    }
                }
            """.trimIndent()
            
            // 发起流式请求
            client.preparePost("https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${config.qwen3ApiKey}")
                header("X-DashScope-SSE", "enable")  // 启用SSE
                header("Accept", "text/event-stream")  // SSE内容类型
                setBody(requestBody)
            }.execute { response ->
                if (response.status != HttpStatusCode.OK) {
                    emit(StreamingAudioChunk.Error(Exception("API请求失败: ${response.status}")))
                    return@execute
                }
                
                // 读取SSE流
                val channel = response.bodyAsChannel()
                var totalChunks = 0
                var buffer = ""
                
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    
                    // SSE格式：data: {...}
                    if (line.startsWith("data:")) {
                        val jsonData = line.removePrefix("data:").trim()
                        
                        // 跳过空行或完成标记
                        if (jsonData.isEmpty() || jsonData == "[DONE]") {
                            continue
                        }
                        
                        try {
                            // 解析JSON响应
                            val jsonElement = json.parseToJsonElement(jsonData)
                            val audioDataBase64 = jsonElement.jsonObject["output"]
                                ?.jsonObject?.get("audio")
                                ?.jsonObject?.get("data")
                                ?.jsonPrimitive?.content
                            
                            if (audioDataBase64 != null) {
                                // Base64解码音频数据
                                val audioBytes = Base64.decode(audioDataBase64)
                                
                                // 发送音频块
                                totalChunks++
                                val progress = totalChunks / 100f // TODO: 计算实际进度
                                emit(StreamingAudioChunk.AudioData(
                                    data = audioBytes,
                                    progress = progress.coerceAtMost(1.0f)
                                ))
                            }
                        } catch (e: Exception) {
                            // 解析错误，继续处理下一块
                            println("Warning: Failed to parse SSE chunk: ${e.message}")
                        }
                    }
                }
                
                // 流结束
                emit(StreamingAudioChunk.Complete)
            }
            
        } catch (e: Exception) {
            emit(StreamingAudioChunk.Error(e))
        } finally {
            client.close()
        }
    }
    
    companion object {
        private const val QWEN3_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation"
    }
}

/**
 * 创建流式TTS服务实例
 */
fun createStreamingTTSService(): StreamingTTSService {
    return StreamingTTSServiceImpl()
}
