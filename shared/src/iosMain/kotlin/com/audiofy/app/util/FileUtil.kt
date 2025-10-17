package com.audiofy.app.util

import kotlinx.cinterop.*
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile

/**
 * iOS implementation: Save audio to Caches directory
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun saveAudioToTempFile(audioData: ByteArray, fileName: String): String {
    val cacheDirectory = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true
    ).first() as String

    val audioPath = "$cacheDirectory/$fileName"

    // Convert ByteArray to NSData and write to file
    val nsData = memScoped {
        NSData.create(bytes = allocArrayOf(audioData), length = audioData.size.toULong())
    }
    nsData.writeToFile(audioPath, atomically = true)

    return audioPath
}
