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
                        qwen3ApiKey = config.qwen3ApiKey,
                        qwen3Voice = config.qwen3Voice,
                        qwen3LanguageType = config.qwen3LanguageType,
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
     * 更新 Qwen3 API Key
     */
    fun updateQwen3ApiKey(value: String) {
        _uiState.value = _uiState.value.copy(qwen3ApiKey = value)
    }

    /**
     * 更新 Qwen3 Voice
     */
    fun updateQwen3Voice(value: String) {
        _uiState.value = _uiState.value.copy(qwen3Voice = value)
    }

    /**
     * 更新 Qwen3 Language Type
     */
    fun updateQwen3LanguageType(value: String) {
        _uiState.value = _uiState.value.copy(qwen3LanguageType = value)
    }

    /**
     * 保存配置
     */
    fun saveConfig() {
        viewModelScope.launch {
            try {
                val state = _uiState.value

                // 验证输入
                if (state.qwen3ApiKey.isBlank()) {
                    _uiState.value = state.copy(errorMessage = "Qwen3 API Key 不能为空")
                    return@launch
                }

                _uiState.value = state.copy(isSaving = true, errorMessage = null, successMessage = null)

                val config = AppConfig(
                    qwen3ApiKey = state.qwen3ApiKey,
                    qwen3Voice = state.qwen3Voice,
                    qwen3LanguageType = state.qwen3LanguageType,
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
    // Qwen3 TTS API
    val qwen3ApiKey: String = "",
    val qwen3Voice: String = "cherry",
    val qwen3LanguageType: String = "Chinese",

    // 加载状态
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,

    // 消息
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
