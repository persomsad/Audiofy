package com.audiofy.app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.audiofy.app.data.AppConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Android 平台的配置存储实现
 * 使用 DataStore Preferences
 */
class ConfigRepositoryImpl(private val context: Context) : ConfigRepository {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "audiofy_config")

    companion object {
        private val KEY_CONFIG = stringPreferencesKey("app_config")
    }

    override fun getConfigFlow(): Flow<AppConfig> =
        context.dataStore.data.map { preferences ->
            val json = preferences[KEY_CONFIG]
            if (json != null) {
                try {
                    Json.decodeFromString<AppConfig>(json)
                } catch (e: Exception) {
                    AppConfig.DEFAULT
                }
            } else {
                AppConfig.DEFAULT
            }
        }

    override suspend fun getConfig(): AppConfig =
        getConfigFlow().first()

    override suspend fun saveConfig(config: AppConfig) {
        context.dataStore.edit { preferences ->
            val json = Json.encodeToString(config)
            preferences[KEY_CONFIG] = json
        }
    }

    override suspend fun clearConfig() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_CONFIG)
        }
    }
}
