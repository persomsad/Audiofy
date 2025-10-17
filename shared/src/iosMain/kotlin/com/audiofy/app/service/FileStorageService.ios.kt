package com.audiofy.app.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.*
import platform.posix.memcpy

/**
 * iOS平台的文件存储服务实现
 */
@OptIn(ExperimentalForeignApi::class)
actual fun createFileStorageService(): FileStorageService {
    return IosFileStorageService()
}

@OptIn(ExperimentalForeignApi::class)
class IosFileStorageService : FileStorageService {
    
    private val fileManager = NSFileManager.defaultManager
    
    override fun getDataDirectory(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        )
        val documentsPath = paths.first() as String
        return "$documentsPath/Audiofy"
    }
    
    override suspend fun saveAudio(relativePath: String, audioData: ByteArray): String {
        val fullPath = "${getDataDirectory()}/$relativePath"
        val fileURL = NSURL.fileURLWithPath(fullPath)
        
        // 创建父目录
        val parentDir = fileURL.URLByDeletingLastPathComponent
        if (parentDir != null) {
            fileManager.createDirectoryAtURL(
                parentDir,
                true,
                null,
                null
            )
        }
        
        // 写入文件
        val data = audioData.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = audioData.size.toULong())
        }
        
        val success = data.writeToURL(fileURL, true)
        if (!success) {
            throw Exception("Failed to write audio file: $fullPath")
        }
        
        return fullPath
    }
    
    override suspend fun readAudio(relativePath: String): ByteArray? {
        val fullPath = "${getDataDirectory()}/$relativePath"
        val fileURL = NSURL.fileURLWithPath(fullPath)
        
        val data = NSData.dataWithContentsOfURL(fileURL) ?: return null
        
        return ByteArray(data.length.toInt()).apply {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
    }
    
    override suspend fun deleteAudio(relativePath: String): Boolean {
        val fullPath = "${getDataDirectory()}/$relativePath"
        return try {
            fileManager.removeItemAtPath(fullPath, null)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun fileExists(relativePath: String): Boolean {
        val fullPath = "${getDataDirectory()}/$relativePath"
        return fileManager.fileExistsAtPath(fullPath)
    }
    
    override suspend fun getFileSize(relativePath: String): Long {
        val fullPath = "${getDataDirectory()}/$relativePath"
        val attributes = fileManager.attributesOfItemAtPath(fullPath, null) ?: return 0L
        return (attributes[NSFileSize] as? NSNumber)?.longValue ?: 0L
    }
    
    override suspend fun clearAllAudios() {
        val audiosPath = "${getDataDirectory()}/audios"
        try {
            fileManager.removeItemAtPath(audiosPath, null)
            fileManager.createDirectoryAtPath(audiosPath, true, null, null)
        } catch (e: Exception) {
            // Ignore if directory doesn't exist
        }
    }
    
    override suspend fun getTotalStorageUsed(): Long {
        val audiosPath = "${getDataDirectory()}/audios"
        
        // 递归计算目录大小（简化实现，避免C-interop复杂性）
        fun calculateDirectorySize(path: String): Long {
            val contents = fileManager.contentsOfDirectoryAtPath(path, null) as? List<*> ?: return 0L
            var total = 0L
            
            for (item in contents) {
                val itemPath = "$path/${item as String}"
                // 检查是否是目录（简化版本）
                val isDirectory = fileManager.fileExistsAtPath("$itemPath/")
                
                total += if (isDirectory) {
                    calculateDirectorySize(itemPath)
                } else {
                    val attrs = fileManager.attributesOfItemAtPath(itemPath, null)
                    (attrs?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L
                }
            }
            
            return total
        }
        
        return if (fileManager.fileExistsAtPath(audiosPath)) {
            calculateDirectorySize(audiosPath)
        } else {
            0L
        }
    }
}

