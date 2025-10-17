package com.audiofy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.audiofy.app.theme.*
import com.audiofy.app.viewmodel.SettingsViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

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
                    containerColor = AudiofyColors.Primary,
                    titleContentColor = AudiofyColors.OnPrimary,
                    navigationIconContentColor = AudiofyColors.OnPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AudiofyColors.Background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AudiofyColors.Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(AudiofySpacing.Large)
            ) {
                // Gemini API Section
                SectionTitle("Gemini API 配置")
                Spacer(Modifier.height(AudiofySpacing.Medium))

                ApiKeyTextField(
                    value = uiState.geminiApiKey,
                    onValueChange = viewModel::updateGeminiApiKey,
                    label = "Gemini API Key",
                    isRequired = true
                )
                Spacer(Modifier.height(AudiofySpacing.Medium))

                ConfigTextField(
                    value = uiState.geminiModelId,
                    onValueChange = viewModel::updateGeminiModelId,
                    label = "Gemini Model ID",
                    placeholder = "gemini-2.0-flash-exp"
                )
                Spacer(Modifier.height(AudiofySpacing.Medium))

                ConfigTextField(
                    value = uiState.geminiBaseUrl,
                    onValueChange = viewModel::updateGeminiBaseUrl,
                    label = "Gemini Base URL",
                    placeholder = "https://generativelanguage.googleapis.com/v1beta",
                    keyboardType = KeyboardType.Uri
                )

                Spacer(Modifier.height(AudiofySpacing.ExtraLarge))

                // ElevenLabs API Section
                SectionTitle("ElevenLabs TTS API 配置")
                Spacer(Modifier.height(AudiofySpacing.Medium))

                ApiKeyTextField(
                    value = uiState.elevenLabsApiKey,
                    onValueChange = viewModel::updateElevenLabsApiKey,
                    label = "ElevenLabs API Key",
                    isRequired = true
                )
                Spacer(Modifier.height(AudiofySpacing.Medium))

                ConfigTextField(
                    value = uiState.elevenLabsVoiceId,
                    onValueChange = viewModel::updateElevenLabsVoiceId,
                    label = "ElevenLabs Voice ID",
                    isRequired = true
                )
                Spacer(Modifier.height(AudiofySpacing.Medium))

                ConfigTextField(
                    value = uiState.elevenLabsModelId,
                    onValueChange = viewModel::updateElevenLabsModelId,
                    label = "ElevenLabs Model ID",
                    placeholder = "eleven_multilingual_v2"
                )

                Spacer(Modifier.height(AudiofySpacing.ExtraLarge))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Medium)
                ) {
                    // Reset Button
                    OutlinedButton(
                        onClick = viewModel::resetConfig,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AudiofyColors.Error
                        ),
                        shape = AudiofyShapes.medium
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(AudiofySpacing.Medium)
                        )
                        Spacer(Modifier.width(AudiofySpacing.Small))
                        Text("重置", style = AudiofyTypography.labelLarge)
                    }

                    // Save Button
                    Button(
                        onClick = viewModel::saveConfig,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AudiofyColors.Primary,
                            contentColor = AudiofyColors.OnPrimary
                        ),
                        shape = AudiofyShapes.medium
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(AudiofySpacing.Medium),
                                color = AudiofyColors.OnPrimary,
                                strokeWidth = AudiofySpacing.ExtraSmall
                            )
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(AudiofySpacing.Medium)
                            )
                        }
                        Spacer(Modifier.width(AudiofySpacing.Small))
                        Text("保存", style = AudiofyTypography.labelLarge)
                    }
                }

                Spacer(Modifier.height(AudiofySpacing.Large))

                // Help Text
                Text(
                    text = "* 必填项",
                    style = AudiofyTypography.bodySmall,
                    color = AudiofyColors.OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = AudiofySpacing.Small)
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
        color = AudiofyColors.Primary
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
            focusedBorderColor = AudiofyColors.Primary,
            unfocusedBorderColor = AudiofyColors.Outline,
            focusedLabelColor = AudiofyColors.Primary,
            unfocusedLabelColor = AudiofyColors.OnSurfaceVariant
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
        placeholder = { Text(placeholder, color = AudiofyColors.OnSurfaceVariant) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = AudiofyShapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AudiofyColors.Primary,
            unfocusedBorderColor = AudiofyColors.Outline,
            focusedLabelColor = AudiofyColors.Primary,
            unfocusedLabelColor = AudiofyColors.OnSurfaceVariant
        )
    )
}
