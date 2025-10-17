package com.audiofy.app.service

/**
 * 文件存储服务接口
 * 负责音频文件的本地存储和读取
 */
interface FileStorageService {
    /**
     * 保存音频文件
     * 
     * @param relativePath 相对路径，如: audios/{podcastId}/{versionId}.wav
     * @param audioData 音频数据
     * @return 实际保存的完整路径
     */
    suspend fun saveAudio(relativePath: String, audioData: ByteArray): String
    
    /**
     * 读取音频文件
     * 
     * @param relativePath 相对路径
     * @return 音频数据，如果文件不存在返回null
     */
    suspend fun readAudio(relativePath: String): ByteArray?
    
    /**
     * 删除音频文件
     * 
     * @param relativePath 相对路径
     * @return 是否删除成功
     */
    suspend fun deleteAudio(relativePath: String): Boolean
    
    /**
     * 检查文件是否存在
     */
    suspend fun fileExists(relativePath: String): Boolean
    
    /**
     * 获取文件大小（字节）
     */
    suspend fun getFileSize(relativePath: String): Long
    
    /**
     * 获取应用数据目录路径
     */
    fun getDataDirectory(): String
    
    /**
     * 清理所有音频文件
     */
    suspend fun clearAllAudios()
    
    /**
     * 获取总存储空间占用（字节）
     */
    suspend fun getTotalStorageUsed(): Long
}

/**
 * 创建平台特定的文件存储服务
 * 实际实现在androidMain和iosMain中
 */
expect fun createFileStorageService(): FileStorageService

