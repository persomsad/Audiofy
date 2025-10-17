package com.audiofy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.audiofy.app.ui.theme.AudiofyTypography
import com.audiofy.app.ui.theme.Spacing
import com.audiofy.app.viewmodel.InputViewModel

/**
 * Input Screen - Main entry point for content input
 * Allows users to paste text for TTS conversion
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    viewModel: InputViewModel,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProcessing: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Audiofy",
                        style = AudiofyTypography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.space4)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.space4)
        ) {
            Spacer(modifier = Modifier.height(Spacing.space4))

            // Header Section
            Text(
                text = "将文本转为语音播客",
                style = AudiofyTypography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "输入文本，我们将为您生成专业的中文语音播客",
                style = AudiofyTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.space2))

            // Text Input Section
            TextInputSection(
                inputText = uiState.inputText,
                characterCount = uiState.characterCount,
                errorMessage = uiState.errorMessage,
                onTextChange = viewModel::updateInputText
            )

            Spacer(modifier = Modifier.height(Spacing.space2))

            // URL Input Section (Disabled for v2.0)
            UrlInputSection(
                urlInput = uiState.urlInput,
                enabled = false,
                onUrlChange = viewModel::updateUrlInput
            )

            Spacer(modifier = Modifier.height(Spacing.space5))

            // Action Buttons
            ActionButtons(
                isValid = uiState.isValid,
                isProcessing = uiState.isProcessing,
                onStartConversion = {
                    viewModel.startConversion()
                    onNavigateToProcessing(uiState.inputText)
                },
                onClear = viewModel::clearInput
            )

            Spacer(modifier = Modifier.height(Spacing.space4))
        }
    }
}

@Composable
private fun TextInputSection(
    inputText: String,
    characterCount: Int,
    errorMessage: String?,
    onTextChange: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.space2)
    ) {
        Text(
            text = "文本输入",
            style = AudiofyTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            placeholder = {
                Text(
                    text = "请输入文本...\n\n例如：粘贴文章、博客内容、论文摘要等",
                    style = AudiofyTypography.bodyMedium
                )
            },
            textStyle = AudiofyTypography.bodyMedium,
            isError = errorMessage != null,
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            style = AudiofyTypography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Text(
                        text = "$characterCount / ${InputViewModel.MAX_INPUT_LENGTH}",
                        style = AudiofyTypography.bodySmall,
                        color = if (errorMessage != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
    }
}

@Composable
private fun UrlInputSection(
    urlInput: String,
    enabled: Boolean,
    onUrlChange: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.space2)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2)
        ) {
            Text(
                text = "或输入文章链接",
                style = AudiofyTypography.titleMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (!enabled) {
                Badge {
                    Text(
                        text = "即将支持",
                        style = AudiofyTypography.labelSmall
                    )
                }
            }
        }

        OutlinedTextField(
            value = urlInput,
            onValueChange = onUrlChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "https://example.com/article",
                    style = AudiofyTypography.bodyMedium
                )
            },
            textStyle = AudiofyTypography.bodyMedium,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri
            ),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
private fun ActionButtons(
    isValid: Boolean,
    isProcessing: Boolean,
    onStartConversion: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.space4)
    ) {
        // Primary Action: Start Conversion
        Button(
            onClick = onStartConversion,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = isValid && !isProcessing,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(Spacing.space2))
                Text(
                    text = "处理中...",
                    style = AudiofyTypography.labelLarge
                )
            } else {
                Text(
                    text = "开始转换",
                    style = AudiofyTypography.labelLarge
                )
            }
        }

        // Secondary Action: Clear
        OutlinedButton(
            onClick = onClear,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isProcessing,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "清空",
                style = AudiofyTypography.labelLarge
            )
        }
    }
}
