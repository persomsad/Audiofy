package com.audiofy.app.repository

import com.audiofy.app.data.AppConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

/**
 * iOS 平台的配置存储实现
 * 使用 NSUserDefaults
 */
class ConfigRepositoryImpl : ConfigRepository {

    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val _configFlow = MutableStateFlow(loadConfig())

    companion object {
        private const val KEY_CONFIG = "app_config"
    }

    private fun loadConfig(): AppConfig {
        val json = userDefaults.stringForKey(KEY_CONFIG)
        return if (json != null) {
            try {
                Json.decodeFromString<AppConfig>(json)
            } catch (e: Exception) {
                AppConfig.DEFAULT
            }
        } else {
            AppConfig.DEFAULT
        }
    }

    override fun getConfigFlow(): Flow<AppConfig> =
        _configFlow.asStateFlow()

    override suspend fun getConfig(): AppConfig =
        _configFlow.value

    override suspend fun saveConfig(config: AppConfig) {
        val json = Json.encodeToString(config)
        userDefaults.setObject(json, forKey = KEY_CONFIG)
        userDefaults.synchronize()
        _configFlow.value = config
    }

    override suspend fun clearConfig() {
        userDefaults.removeObjectForKey(KEY_CONFIG)
        userDefaults.synchronize()
        _configFlow.value = AppConfig.DEFAULT
    }
}
