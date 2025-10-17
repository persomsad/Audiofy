package com.audiofy.app.repository

import com.audiofy.app.data.AppConfig
import kotlinx.coroutines.flow.Flow

/**
 * 配置存储仓库接口
 * 使用 expect/actual 模式实现跨平台存储
 */
interface ConfigRepository {
    /**
     * 获取配置的 Flow（响应式）
     */
    fun getConfigFlow(): Flow<AppConfig>

    /**
     * 获取当前配置（一次性）
     */
    suspend fun getConfig(): AppConfig

    /**
     * 保存配置
     */
    suspend fun saveConfig(config: AppConfig)

    /**
     * 清除所有配置（重置为默认值）
     */
    suspend fun clearConfig()
}
