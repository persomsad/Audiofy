package com.audiofy.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Input Screen
 * Manages the state and validation of user input for content conversion
 */
class InputViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InputUiState())
    val uiState: StateFlow<InputUiState> = _uiState.asStateFlow()

    companion object {
        const val MIN_INPUT_LENGTH = 10
        // 提高字数限制到 20000（约 20 篇文章的长度）
        // TTS 服务会自动分段处理（每段最多 5000 字）
        const val MAX_INPUT_LENGTH = 20000
    }

    /**
     * Update input text and validate
     */
    fun updateInputText(text: String) {
        // Enforce max length constraint
        val trimmedText = if (text.length > MAX_INPUT_LENGTH) {
            text.substring(0, MAX_INPUT_LENGTH)
        } else {
            text
        }

        _uiState.update { currentState ->
            currentState.copy(
                inputText = trimmedText,
                characterCount = trimmedText.length,
                isValid = validateInput(trimmedText),
                errorMessage = getErrorMessage(trimmedText)
            )
        }
    }

    /**
     * Update URL input (reserved for v2.1)
     */
    fun updateUrlInput(url: String) {
        _uiState.update { currentState ->
            currentState.copy(urlInput = url)
        }
    }

    /**
     * Clear all inputs
     */
    fun clearInput() {
        _uiState.update {
            InputUiState() // Reset to initial state
        }
    }

    /**
     * Validate input text
     */
    private fun validateInput(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.length >= MIN_INPUT_LENGTH && trimmed.length <= MAX_INPUT_LENGTH
    }

    /**
     * Get error message based on input validation
     */
    private fun getErrorMessage(text: String): String? {
        val trimmed = text.trim()
        return when {
            trimmed.isEmpty() -> null // No error when empty
            trimmed.length < MIN_INPUT_LENGTH -> "输入至少需要 $MIN_INPUT_LENGTH 个字符"
            trimmed.length > MAX_INPUT_LENGTH -> "输入不能超过 $MAX_INPUT_LENGTH 个字符"
            else -> null
        }
    }

    /**
     * Start conversion process
     * This will be called when user clicks "开始转换" button
     * TODO: Integrate with Gemini API in Issue #30
     */
    fun startConversion() {
        viewModelScope.launch {
            if (!_uiState.value.isValid) {
                return@launch
            }

            _uiState.update { it.copy(isProcessing = true) }

            // TODO: Navigate to processing page
            // TODO: Trigger Gemini API call

            // Placeholder: Reset processing state after action
            _uiState.update { it.copy(isProcessing = false) }
        }
    }
}

/**
 * UI State for Input Screen
 */
data class InputUiState(
    val inputText: String = "",
    val urlInput: String = "",
    val characterCount: Int = 0,
    val isValid: Boolean = false,
    val errorMessage: String? = null,
    val isProcessing: Boolean = false,
)
