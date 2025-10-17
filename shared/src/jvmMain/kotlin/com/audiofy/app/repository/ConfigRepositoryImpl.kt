package com.audiofy.app.repository

import com.audiofy.app.data.AppConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * JVM 平台的配置存储实现
 * 使用 JSON 文件存储
 */
class ConfigRepositoryImpl(private val configDir: File) : ConfigRepository {

    private val configFile = File(configDir, "config.json")
    private val _configFlow = MutableStateFlow(loadConfig())

    private fun loadConfig(): AppConfig {
        return if (configFile.exists()) {
            try {
                val json = configFile.readText()
                Json.decodeFromString<AppConfig>(json)
            } catch (e: Exception) {
                AppConfig.DEFAULT
            }
        } else {
            AppConfig.DEFAULT
        }
    }

    override fun getConfigFlow(): Flow<AppConfig> = _configFlow.asStateFlow()

    override suspend fun getConfig(): AppConfig {
        return _configFlow.value
    }

    override suspend fun saveConfig(config: AppConfig) {
        try {
            val json = Json.encodeToString(config)
            configFile.writeText(json)
            _configFlow.value = config
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun clearConfig() {
        configFile.delete()
        _configFlow.value = AppConfig.DEFAULT
    }
}
