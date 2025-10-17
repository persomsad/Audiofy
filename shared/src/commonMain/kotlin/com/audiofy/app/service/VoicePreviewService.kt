package com.audiofy.app.service

import com.audiofy.app.data.AppConfig

interface VoicePreviewService {
    suspend fun previewVoice(text: String, voice: String, config: AppConfig): Result<ByteArray>
}

class VoicePreviewServiceImpl(private val ttsService: TTSService) : VoicePreviewService {
    override suspend fun previewVoice(text: String, voice: String, config: AppConfig): Result<ByteArray> {
        val previewText = extractPreviewText(text, maxBytes = 600)
        val previewConfig = config.copy(qwen3Voice = voice)
        return ttsService.synthesizeSpeech(previewText, previewConfig)
    }
    
    private fun extractPreviewText(text: String, maxBytes: Int): String {
        var result = ""
        for (char in text) {
            val test = result + char
            if (test.encodeToByteArray().size > maxBytes) break
            result = test
        }
        return result
    }
}
