package com.audiofy.app.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WavMergerTest {

    /**
     * 创建一个最小的合法WAV文件（44字节头 + 一些PCM数据）
     */
    private fun createMinimalWavFile(pcmDataSize: Int = 100): ByteArray {
        val header = ByteArray(44)
        
        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        
        // ChunkSize (file size - 8)
        val chunkSize = 36 + pcmDataSize
        header[4] = (chunkSize and 0xFF).toByte()
        header[5] = ((chunkSize shr 8) and 0xFF).toByte()
        header[6] = ((chunkSize shr 16) and 0xFF).toByte()
        header[7] = ((chunkSize shr 24) and 0xFF).toByte()
        
        // WAVE header
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        
        // fmt subchunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        
        // Subchunk1Size (16 for PCM)
        header[16] = 16
        
        // AudioFormat (1 = PCM)
        header[20] = 1
        
        // NumChannels (1 = mono)
        header[22] = 1
        
        // SampleRate (24000 Hz)
        header[24] = (24000 and 0xFF).toByte()
        header[25] = ((24000 shr 8) and 0xFF).toByte()
        header[26] = ((24000 shr 16) and 0xFF).toByte()
        header[27] = ((24000 shr 24) and 0xFF).toByte()
        
        // ByteRate (SampleRate * NumChannels * BitsPerSample/8)
        val byteRate = 24000 * 1 * 2  // 48000
        header[28] = (byteRate and 0xFF).toByte()
        header[29] = ((byteRate shr 8) and 0xFF).toByte()
        header[30] = ((byteRate shr 16) and 0xFF).toByte()
        header[31] = ((byteRate shr 24) and 0xFF).toByte()
        
        // BlockAlign (NumChannels * BitsPerSample/8)
        header[32] = 2
        
        // BitsPerSample (16)
        header[34] = 16
        
        // data subchunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        
        // Subchunk2Size (PCM data size)
        header[40] = (pcmDataSize and 0xFF).toByte()
        header[41] = ((pcmDataSize shr 8) and 0xFF).toByte()
        header[42] = ((pcmDataSize shr 16) and 0xFF).toByte()
        header[43] = ((pcmDataSize shr 24) and 0xFF).toByte()
        
        // PCM data (简单填充0)
        val pcmData = ByteArray(pcmDataSize) { 0 }
        
        return header + pcmData
    }

    @Test
    fun testValidateWavFormat() {
        val validWav = createMinimalWavFile()
        assertTrue(WavMerger.validateWavFormat(validWav), "合法WAV文件应该通过验证")
        
        val invalidWav = ByteArray(10) { 0 }
        assertFalse(WavMerger.validateWavFormat(invalidWav), "无效WAV文件应该验证失败")
    }

    @Test
    fun testMergeSingleFile() {
        val wav = createMinimalWavFile(100)
        val merged = WavMerger.mergeWavFiles(listOf(wav))
        
        // 单个文件应该原样返回
        assertEquals(wav.size, merged.size)
        assertTrue(WavMerger.validateWavFormat(merged))
    }

    @Test
    fun testMergeTwoFiles() {
        val wav1 = createMinimalWavFile(100)
        val wav2 = createMinimalWavFile(100)
        
        val merged = WavMerger.mergeWavFiles(listOf(wav1, wav2), insertSilence = false)
        
        // 验证合并后的文件格式正确
        assertTrue(WavMerger.validateWavFormat(merged))
        
        // 验证文件大小: 44字节头 + 两个文件的PCM数据
        val expectedSize = 44 + 100 + 100
        assertEquals(expectedSize, merged.size)
    }

    @Test
    fun testMergeWithSilence() {
        val wav1 = createMinimalWavFile(100)
        val wav2 = createMinimalWavFile(100)
        
        val mergedWithSilence = WavMerger.mergeWavFiles(listOf(wav1, wav2), insertSilence = true)
        val mergedWithoutSilence = WavMerger.mergeWavFiles(listOf(wav1, wav2), insertSilence = false)
        
        // 带静音的应该比不带静音的大
        assertTrue(mergedWithSilence.size > mergedWithoutSilence.size)
        
        // 验证格式正确
        assertTrue(WavMerger.validateWavFormat(mergedWithSilence))
        assertTrue(WavMerger.validateWavFormat(mergedWithoutSilence))
    }

    @Test
    fun testMergeMultipleFiles() {
        val wavFiles = List(5) { createMinimalWavFile(100) }
        
        val merged = WavMerger.mergeWavFiles(wavFiles, insertSilence = false)
        
        // 验证格式
        assertTrue(WavMerger.validateWavFormat(merged))
        
        // 验证大小: 44字节头 + 5 * 100字节PCM数据
        val expectedSize = 44 + 5 * 100
        assertEquals(expectedSize, merged.size)
    }

    @Test
    fun testGetWavInfo() {
        val wav = createMinimalWavFile(100)
        val info = WavMerger.getWavInfo(wav)
        
        assertTrue(info.contains("WAV文件信息"))
        assertTrue(info.contains("文件大小"))
        assertTrue(info.contains("PCM数据"))
        assertTrue(info.contains("采样率: 24000"))
        assertTrue(info.contains("位深度: 16"))
    }

    @Test
    fun testInvalidWavInfo() {
        val invalidWav = ByteArray(10) { 0 }
        val info = WavMerger.getWavInfo(invalidWav)
        
        assertEquals("无效的WAV文件格式", info)
    }

    @Test
    fun testHeaderUpdate() {
        // 测试头部更新是否正确
        val wav1 = createMinimalWavFile(100)
        val wav2 = createMinimalWavFile(200)
        
        val merged = WavMerger.mergeWavFiles(listOf(wav1, wav2), insertSilence = false)
        
        // 读取ChunkSize (字节4-7)
        val chunkSize = (merged[4].toInt() and 0xFF) or
                        ((merged[5].toInt() and 0xFF) shl 8) or
                        ((merged[6].toInt() and 0xFF) shl 16) or
                        ((merged[7].toInt() and 0xFF) shl 24)
        
        val expectedChunkSize = merged.size - 8
        assertEquals(expectedChunkSize, chunkSize, "ChunkSize应该正确更新")
        
        // 读取Subchunk2Size (字节40-43)
        val subchunk2Size = (merged[40].toInt() and 0xFF) or
                            ((merged[41].toInt() and 0xFF) shl 8) or
                            ((merged[42].toInt() and 0xFF) shl 16) or
                            ((merged[43].toInt() and 0xFF) shl 24)
        
        val expectedPcmSize = 100 + 200
        assertEquals(expectedPcmSize, subchunk2Size, "Subchunk2Size应该正确更新")
    }

    @Test
    fun testRealWorldScenario() {
        // 模拟真实场景: 合并10个音频片段
        val wavFiles = List(10) { createMinimalWavFile(100) }
        
        val merged = WavMerger.mergeWavFiles(wavFiles, insertSilence = true)
        
        // 验证格式
        assertTrue(WavMerger.validateWavFormat(merged))
        
        // 验证信息可以正常读取
        val info = WavMerger.getWavInfo(merged)
        assertTrue(info.contains("WAV文件信息"))
    }

    @Test
    fun testDifferentSizePCMData() {
        // 测试不同大小的PCM数据合并
        val wav1 = createMinimalWavFile(50)
        val wav2 = createMinimalWavFile(150)
        val wav3 = createMinimalWavFile(100)
        
        val merged = WavMerger.mergeWavFiles(listOf(wav1, wav2, wav3), insertSilence = false)
        
        assertTrue(WavMerger.validateWavFormat(merged))
        
        // 验证大小: 44 + 50 + 150 + 100 = 344
        assertEquals(44 + 50 + 150 + 100, merged.size)
    }
}

