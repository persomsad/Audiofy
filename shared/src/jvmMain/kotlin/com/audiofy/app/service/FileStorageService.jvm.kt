package com.audiofy.app.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * JVM平台的文件存储服务实现
 */
class JvmFileStorageService(private val dataDir: File) : FileStorageService {
    
    init {
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
    }
    
    override suspend fun saveAudio(relativePath: String, audioData: ByteArray): String = withContext(Dispatchers.IO) {
        val file = File(dataDir, relativePath)
        file.parentFile?.mkdirs()
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

actual fun createFileStorageService(): FileStorageService {
    val userHome = System.getProperty("user.home")
    val dataDir = File(userHome, ".audiofy/data")
    return JvmFileStorageService(dataDir)
}
