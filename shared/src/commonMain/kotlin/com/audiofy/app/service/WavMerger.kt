package com.audiofy.app.service

/**
 * WAV音频文件合并工具
 * 用于将多个WAV音频片段合并成一个完整的音频文件
 *
 * WAV文件结构:
 * - RIFF头 (0-43字节, 44字节总共)
 *   - ChunkID (0-3): "RIFF"
 *   - ChunkSize (4-7): 文件大小 - 8
 *   - Format (8-11): "WAVE"
 *   - Subchunk1ID (12-15): "fmt "
 *   - Subchunk1Size (16-19): 16
 *   - AudioFormat (20-21): 1 (PCM)
 *   - NumChannels (22-23): 1 (mono)
 *   - SampleRate (24-27): 24000
 *   - ByteRate (28-31): 48000
 *   - BlockAlign (32-33): 2
 *   - BitsPerSample (34-35): 16
 *   - Subchunk2ID (36-39): "data"
 *   - Subchunk2Size (40-43): PCM数据大小
 * - PCM数据 (44字节之后)
 */
object WavMerger {
    private const val WAV_HEADER_SIZE = 44
    private const val SILENCE_DURATION_MS = 100 // 片段间静音时长(毫秒)
    private const val SAMPLE_RATE = 24000 // Qwen3 TTS返回24kHz
    private const val BITS_PER_SAMPLE = 16
    private const val NUM_CHANNELS = 1 // mono

    /**
     * 合并多个WAV文件
     *
     * @param wavFiles WAV文件数据列表
     * @param insertSilence 是否在片段间插入静音(默认true，避免咔哒声)
     * @return 合并后的完整WAV文件
     */
    fun mergeWavFiles(wavFiles: List<ByteArray>, insertSilence: Boolean = true): ByteArray {
        if (wavFiles.isEmpty()) {
            throw IllegalArgumentException("WAV文件列表不能为空")
        }

        if (wavFiles.size == 1) {
            return wavFiles[0]
        }

        // 1. 提取第一个文件的WAV头
        val header = wavFiles[0].copyOfRange(0, WAV_HEADER_SIZE)

        // 2. 提取所有文件的PCM数据
        val allPcmData = mutableListOf<Byte>()

        for ((index, wavFile) in wavFiles.withIndex()) {
            // 提取PCM数据(跳过44字节头部)
            val pcmData = wavFile.drop(WAV_HEADER_SIZE)
            allPcmData.addAll(pcmData)

            // 在片段间插入静音(最后一片不插入)
            if (insertSilence && index < wavFiles.size - 1) {
                val silenceBytes = generateSilence(SILENCE_DURATION_MS)
                allPcmData.addAll(silenceBytes.toList())
            }
        }

        val finalPcmData = allPcmData.toByteArray()

        // 3. 更新WAV头部的大小字段
        updateWavHeader(header, finalPcmData.size)

        // 4. 合并头部和PCM数据
        return header + finalPcmData
    }

    /**
     * 更新WAV头部的文件大小字段
     */
    private fun updateWavHeader(header: ByteArray, pcmDataSize: Int) {
        // 更新ChunkSize (字节4-7): 整个文件大小 - 8
        val chunkSize = WAV_HEADER_SIZE + pcmDataSize - 8
        header[4] = (chunkSize and 0xFF).toByte()
        header[5] = ((chunkSize shr 8) and 0xFF).toByte()
        header[6] = ((chunkSize shr 16) and 0xFF).toByte()
        header[7] = ((chunkSize shr 24) and 0xFF).toByte()

        // 更新Subchunk2Size (字节40-43): PCM数据大小
        header[40] = (pcmDataSize and 0xFF).toByte()
        header[41] = ((pcmDataSize shr 8) and 0xFF).toByte()
        header[42] = ((pcmDataSize shr 16) and 0xFF).toByte()
        header[43] = ((pcmDataSize shr 24) and 0xFF).toByte()
    }

    /**
     * 生成指定时长的静音PCM数据
     *
     * @param durationMs 静音时长(毫秒)
     * @return 静音的PCM数据
     */
    private fun generateSilence(durationMs: Int): ByteArray {
        // 计算需要的采样点数量
        // 采样数 = 采样率 * 时长(秒)
        val sampleCount = (SAMPLE_RATE * durationMs / 1000.0).toInt()

        // 每个采样点16位(2字节)，单声道
        val byteCount = sampleCount * (BITS_PER_SAMPLE / 8) * NUM_CHANNELS

        // 静音 = 所有采样点值为0
        return ByteArray(byteCount) { 0 }
    }

    /**
     * 验证WAV文件格式是否正确
     */
    fun validateWavFormat(wavData: ByteArray): Boolean {
        if (wavData.size < WAV_HEADER_SIZE) {
            return false
        }

        // 检查RIFF标识
        val riffId = wavData.sliceArray(0..3).decodeToString()
        if (riffId != "RIFF") {
            return false
        }

        // 检查WAVE标识
        val waveId = wavData.sliceArray(8..11).decodeToString()
        if (waveId != "WAVE") {
            return false
        }

        return true
    }

    /**
     * 获取WAV文件信息(用于调试)
     */
    fun getWavInfo(wavData: ByteArray): String {
        if (!validateWavFormat(wavData)) {
            return "无效的WAV文件格式"
        }

        val totalSize = wavData.size
        val pcmDataSize = wavData.size - WAV_HEADER_SIZE
        val durationSec = pcmDataSize.toDouble() / (SAMPLE_RATE * (BITS_PER_SAMPLE / 8) * NUM_CHANNELS)

        return """
            |WAV文件信息:
            |  文件大小: $totalSize 字节
            |  PCM数据: $pcmDataSize 字节
            |  采样率: $SAMPLE_RATE Hz
            |  位深度: $BITS_PER_SAMPLE bit
            |  声道数: $NUM_CHANNELS (mono)
            |  时长: %.2f 秒
        """.trimMargin().format(durationSec)
    }
}
