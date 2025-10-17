package com.audiofy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.audiofy.app.ui.theme.AudiofyTypography
import com.audiofy.app.ui.theme.Spacing
import com.audiofy.app.viewmodel.ProcessingStep
import com.audiofy.app.viewmodel.ProcessingViewModel

/**
 * Processing Screen
 * Shows translation and TTS progress
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingScreen(
    viewModel: ProcessingViewModel,
    inputText: String,
    onNavigateBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Start processing when screen is first shown
    LaunchedEffect(inputText) {
        viewModel.startProcessing(inputText)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("处理中", style = AudiofyTypography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.space4),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val step = uiState.currentStep) {
                is ProcessingStep.TranslatingStage1 -> {
                    StepProgress(
                        title = "步骤 1/3: 翻译中...",
                        description = "正在进行准确翻译",
                        progress = uiState.progress
                    )
                }

                is ProcessingStep.TranslatingStage2 -> {
                    StepProgress(
                        title = "步骤 2/3: 润色中...",
                        description = "正在优化译文",
                        progress = uiState.progress
                    )
                }

                is ProcessingStep.GeneratingSpeech -> {
                    StepProgress(
                        title = "步骤 3/3: 生成语音...",
                        description = "正在生成音频",
                        progress = uiState.progress
                    )
                }

                is ProcessingStep.Completed -> {
                    CompletedView(
                        translatedText = uiState.translatedText ?: "",
                        onNavigateBack = onNavigateBack
                    )
                }

                is ProcessingStep.Error -> {
                    ErrorView(
                        errorMessage = step.message,
                        onRetry = { viewModel.retry() },
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        }
    }
}

@Composable
private fun StepProgress(
    title: String,
    description: String,
    progress: Float,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.space4)
    ) {
        Text(
            text = title,
            style = AudiofyTypography.titleLarge
        )

        Text(
            text = description,
            style = AudiofyTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.space4))

        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(Spacing.space2))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(0.7f),
        )

        Text(
            text = "${(progress * 100).toInt()}%",
            style = AudiofyTypography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompletedView(
    translatedText: String,
    onNavigateBack: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.space4)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "完成",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Spacing.space8)
        )

        Text(
            text = "处理完成!",
            style = AudiofyTypography.titleLarge
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.space4)
        ) {
            Column(
                modifier = Modifier.padding(Spacing.space4)
            ) {
                Text(
                    text = "译文:",
                    style = AudiofyTypography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(Spacing.space2))

                Text(
                    text = translatedText,
                    style = AudiofyTypography.bodyMedium
                )
            }
        }

        Text(
            text = "音频播放器功能将在后续版本中实现",
            style = AudiofyTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.space4))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("返回主页")
        }
    }
}

@Composable
private fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.space4)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "错误",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(Spacing.space8)
        )

        Text(
            text = "处理失败",
            style = AudiofyTypography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.space4),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = errorMessage,
                style = AudiofyTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(Spacing.space4),
                textAlign = TextAlign.Center
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("返回")
            }

            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            ) {
                Text("重试")
            }
        }
    }
}
