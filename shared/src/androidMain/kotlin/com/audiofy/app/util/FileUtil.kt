package com.audiofy.app.util

import android.content.Context
import java.io.File
import java.io.FileOutputStream

// Context holder (initialized in MainActivity)
private lateinit var appContext: Context

fun initializeFileUtilContext(context: Context) {
    appContext = context.applicationContext
}

/**
 * Android implementation: Save audio to cache directory
 */
actual fun saveAudioToTempFile(audioData: ByteArray, fileName: String): String {
    val cacheDir = appContext.cacheDir
    val audioFile = File(cacheDir, fileName)

    FileOutputStream(audioFile).use { output ->
        output.write(audioData)
    }

    return audioFile.absolutePath
}
