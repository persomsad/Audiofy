package com.audiofy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.audiofy.app.ui.theme.AudiofyShapes
import com.audiofy.app.ui.theme.AudiofyTypography
import com.audiofy.app.ui.theme.Spacing
import com.audiofy.app.viewmodel.SettingsViewModel

/**
 * 设置页面
 * 允许用户配置 Gemini API 和 ElevenLabs API
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error/success messages
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", style = AudiofyTypography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.space6)
            ) {
                // Gemini API Section
                SectionTitle("Gemini API 配置")
                Spacer(Modifier.height(Spacing.space4))

                ApiKeyTextField(
                    value = uiState.geminiApiKey,
                    onValueChange = viewModel::updateGeminiApiKey,
                    label = "Gemini API Key",
                    isRequired = true
                )
                Spacer(Modifier.height(Spacing.space4))

                ConfigTextField(
                    value = uiState.geminiModelId,
                    onValueChange = viewModel::updateGeminiModelId,
                    label = "Gemini Model ID",
                    placeholder = "gemini-2.0-flash-exp"
                )
                Spacer(Modifier.height(Spacing.space4))

                ConfigTextField(
                    value = uiState.geminiBaseUrl,
                    onValueChange = viewModel::updateGeminiBaseUrl,
                    label = "Gemini Base URL",
                    placeholder = "https://generativelanguage.googleapis.com/v1beta",
                    keyboardType = KeyboardType.Uri
                )

                Spacer(Modifier.height(Spacing.space7))

                // ElevenLabs API Section
                SectionTitle("ElevenLabs TTS API 配置")
                Spacer(Modifier.height(Spacing.space4))

                ApiKeyTextField(
                    value = uiState.elevenLabsApiKey,
                    onValueChange = viewModel::updateElevenLabsApiKey,
                    label = "ElevenLabs API Key",
                    isRequired = true
                )
                Spacer(Modifier.height(Spacing.space4))

                ConfigTextField(
                    value = uiState.elevenLabsVoiceId,
                    onValueChange = viewModel::updateElevenLabsVoiceId,
                    label = "ElevenLabs Voice ID",
                    isRequired = true
                )
                Spacer(Modifier.height(Spacing.space4))

                ConfigTextField(
                    value = uiState.elevenLabsModelId,
                    onValueChange = viewModel::updateElevenLabsModelId,
                    label = "ElevenLabs Model ID",
                    placeholder = "eleven_multilingual_v2"
                )

                Spacer(Modifier.height(Spacing.space7))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.space4)
                ) {
                    // Reset Button
                    OutlinedButton(
                        onClick = viewModel::resetConfig,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = AudiofyShapes.medium
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(Spacing.space4)
                        )
                        Spacer(Modifier.width(Spacing.space2))
                        Text("重置", style = AudiofyTypography.labelLarge)
                    }

                    // Save Button
                    Button(
                        onClick = viewModel::saveConfig,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = AudiofyShapes.medium
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Spacing.space4),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = Spacing.space1
                            )
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(Spacing.space4)
                            )
                        }
                        Spacer(Modifier.width(Spacing.space2))
                        Text("保存", style = AudiofyTypography.labelLarge)
                    }
                }

                Spacer(Modifier.height(Spacing.space6))

                // Help Text
                Text(
                    text = "* 必填项",
                    style = AudiofyTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.space2)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = AudiofyTypography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ApiKeyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isRequired: Boolean = false,
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label + if (isRequired) " *" else "") },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                    imageVector = if (isPasswordVisible) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    },
                    contentDescription = if (isPasswordVisible) "隐藏" else "显示"
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        shape = AudiofyShapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun ConfigTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    isRequired: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label + if (isRequired) " *" else "") },
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = AudiofyShapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
