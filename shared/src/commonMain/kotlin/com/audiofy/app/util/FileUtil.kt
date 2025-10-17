package com.audiofy.app.util

/**
 * Save audio ByteArray to temporary file and return file path
 * Uses expect/actual pattern for platform-specific implementations
 */
expect fun saveAudioToTempFile(audioData: ByteArray, fileName: String = "audio.mp3"): String
