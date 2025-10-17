package com.audiofy.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audiofy.app.data.AppConfig
import com.audiofy.app.repository.ConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 设置页面 ViewModel
 * 管理 API 配置的状态和业务逻辑
 */
class SettingsViewModel(
    private val configRepository: ConfigRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    /**
     * 加载已保存的配置
     */
    private fun loadConfig() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                configRepository.getConfigFlow().collect { config ->
                    _uiState.value = SettingsUiState(
                        geminiApiKey = config.geminiApiKey,
                        geminiModelId = config.geminiModelId,
                        geminiBaseUrl = config.geminiBaseUrl,
                        elevenLabsApiKey = config.elevenLabsApiKey,
                        elevenLabsVoiceId = config.elevenLabsVoiceId,
                        elevenLabsModelId = config.elevenLabsModelId,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "加载配置失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 更新 Gemini API Key
     */
    fun updateGeminiApiKey(value: String) {
        _uiState.value = _uiState.value.copy(geminiApiKey = value)
    }

    /**
     * 更新 Gemini Model ID
     */
    fun updateGeminiModelId(value: String) {
        _uiState.value = _uiState.value.copy(geminiModelId = value)
    }

    /**
     * 更新 Gemini Base URL
     */
    fun updateGeminiBaseUrl(value: String) {
        _uiState.value = _uiState.value.copy(geminiBaseUrl = value)
    }

    /**
     * 更新 ElevenLabs API Key
     */
    fun updateElevenLabsApiKey(value: String) {
        _uiState.value = _uiState.value.copy(elevenLabsApiKey = value)
    }

    /**
     * 更新 ElevenLabs Voice ID
     */
    fun updateElevenLabsVoiceId(value: String) {
        _uiState.value = _uiState.value.copy(elevenLabsVoiceId = value)
    }

    /**
     * 更新 ElevenLabs Model ID
     */
    fun updateElevenLabsModelId(value: String) {
        _uiState.value = _uiState.value.copy(elevenLabsModelId = value)
    }

    /**
     * 保存配置
     */
    fun saveConfig() {
        viewModelScope.launch {
            try {
                val state = _uiState.value

                // 验证输入
                if (state.geminiApiKey.isBlank()) {
                    _uiState.value = state.copy(errorMessage = "Gemini API Key 不能为空")
                    return@launch
                }
                if (state.elevenLabsApiKey.isBlank()) {
                    _uiState.value = state.copy(errorMessage = "ElevenLabs API Key 不能为空")
                    return@launch
                }
                if (state.elevenLabsVoiceId.isBlank()) {
                    _uiState.value = state.copy(errorMessage = "ElevenLabs Voice ID 不能为空")
                    return@launch
                }

                _uiState.value = state.copy(isSaving = true, errorMessage = null, successMessage = null)

                val config = AppConfig(
                    geminiApiKey = state.geminiApiKey,
                    geminiModelId = state.geminiModelId,
                    geminiBaseUrl = state.geminiBaseUrl,
                    elevenLabsApiKey = state.elevenLabsApiKey,
                    elevenLabsVoiceId = state.elevenLabsVoiceId,
                    elevenLabsModelId = state.elevenLabsModelId,
                )

                configRepository.saveConfig(config)

                _uiState.value = state.copy(
                    isSaving = false,
                    successMessage = "配置保存成功"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "保存失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 重置配置到默认值
     */
    fun resetConfig() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true)
                configRepository.clearConfig()
                _uiState.value = SettingsUiState(
                    isSaving = false,
                    successMessage = "配置已重置"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "重置失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 清除错误/成功消息
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}

/**
 * 设置页面 UI 状态
 */
data class SettingsUiState(
    // Gemini API
    val geminiApiKey: String = "",
    val geminiModelId: String = "gemini-2.0-flash-exp",
    val geminiBaseUrl: String = "https://generativelanguage.googleapis.com/v1beta",

    // ElevenLabs TTS API
    val elevenLabsApiKey: String = "",
    val elevenLabsVoiceId: String = "",
    val elevenLabsModelId: String = "eleven_multilingual_v2",

    // 加载状态
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,

    // 消息
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
