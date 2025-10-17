package com.audiofy.app.service

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android平台的文件存储服务实现
 */
class AndroidFileStorageService(private val context: Context) : FileStorageService {
    
    private val dataDir: File
        get() = context.filesDir
    
    override suspend fun saveAudio(relativePath: String, audioData: ByteArray): String = withContext(Dispatchers.IO) {
        val file = File(dataDir, relativePath)
        file.parentFile?.mkdirs()  // 创建父目录
        file.writeBytes(audioData)
        file.absolutePath
    }
    
    override suspend fun readAudio(relativePath: String): ByteArray? = withContext(Dispatchers.IO) {
        val file = File(dataDir, relativePath)
        if (file.exists()) {
            file.readBytes()
        } else {
            null
        }
    }
    
    override suspend fun deleteAudio(relativePath: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(dataDir, relativePath)
        file.delete()
    }
    
    override suspend fun fileExists(relativePath: String): Boolean = withContext(Dispatchers.IO) {
        File(dataDir, relativePath).exists()
    }
    
    override suspend fun getFileSize(relativePath: String): Long = withContext(Dispatchers.IO) {
        val file = File(dataDir, relativePath)
        if (file.exists()) file.length() else 0L
    }
    
    override fun getDataDirectory(): String {
        return dataDir.absolutePath
    }
    
    override suspend fun clearAllAudios() = withContext(Dispatchers.IO) {
        val audiosDir = File(dataDir, "audios")
        if (audiosDir.exists()) {
            audiosDir.deleteRecursively()
        }
    }
    
    override suspend fun getTotalStorageUsed(): Long = withContext(Dispatchers.IO) {
        val audiosDir = File(dataDir, "audios")
        if (!audiosDir.exists()) return@withContext 0L
        
        fun File.sizeRecursive(): Long {
            return if (isDirectory) {
                listFiles()?.sumOf { it.sizeRecursive() } ?: 0L
            } else {
                length()
            }
        }
        
        audiosDir.sizeRecursive()
    }
}

// Context会在Android平台初始化时提供
private lateinit var appContext: Context

fun initializeAndroidContext(context: Context) {
    appContext = context.applicationContext
}

actual fun createFileStorageService(): FileStorageService {
    return AndroidFileStorageService(appContext)
}

