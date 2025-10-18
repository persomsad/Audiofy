package com.audiofy.app.service

import com.audiofy.app.data.AppConfig
import kotlinx.coroutines.flow.Flow

/**
 * 流式TTS服务接口
 * 支持边生成边播放，大幅减少首次播放等待时间
 * 
 * 参考：Qwen3 TTS官方Gradio Demo流式实现
 */
interface StreamingTTSService {
    
    /**
     * 流式合成语音
     * 
     * @param text 要转换的文本
     * @param config API配置（包含API Key、音色、语言类型）
     * @return Flow<StreamingAudioChunk> 音频数据流
     */
    fun synthesizeSpeechStreaming(
        text: String,
        config: AppConfig
    ): Flow<StreamingAudioChunk>
}

/**
 * 流式音频数据块
 */
sealed class StreamingAudioChunk {
    /**
     * 音频数据块
     * @param data PCM音频数据 (16-bit, 24kHz采样率)
     * @param progress 当前进度 (0.0 - 1.0)
     */
    data class AudioData(
        val data: ByteArray,
        val progress: Float
    ) : StreamingAudioChunk() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as AudioData
            if (!data.contentEquals(other.data)) return false
            if (progress != other.progress) return false
            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + progress.hashCode()
            return result
        }
    }
    
    /**
     * 流结束标记
     */
    data object Complete : StreamingAudioChunk()
    
    /**
     * 错误
     */
    data class Error(val exception: Throwable) : StreamingAudioChunk()
}
