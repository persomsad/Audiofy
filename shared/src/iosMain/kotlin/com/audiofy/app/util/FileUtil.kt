package com.audiofy.app.util

import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.writeToFile

/**
 * iOS implementation: Save audio to Caches directory
 */
actual fun saveAudioToTempFile(audioData: ByteArray, fileName: String): String {
    val fileManager = NSFileManager.defaultManager
    val cacheDirectory = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true
    ).first() as String

    val audioPath = "$cacheDirectory/$fileName"

    // Convert ByteArray to NSData and write
    audioData.usePinned { pinned ->
        platform.Foundation.NSData.dataWithBytes(pinned.addressOf(0), audioData.size.toULong())
            ?.writeToFile(audioPath, atomically = true)
    }

    return audioPath
}
