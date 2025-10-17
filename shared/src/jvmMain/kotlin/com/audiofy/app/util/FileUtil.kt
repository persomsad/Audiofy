package com.audiofy.app.util

import java.io.File

/**
 * JVM implementation: Save audio to temp directory
 */
actual fun saveAudioToTempFile(audioData: ByteArray, fileName: String): String {
    val tempDir = System.getProperty("java.io.tmpdir")
    val audioFile = File(tempDir, fileName)
    audioFile.writeBytes(audioData)
    return audioFile.absolutePath
}
