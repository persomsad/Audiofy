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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.audiofy.app.ui.components.AudioPlayerView
import com.audiofy.app.ui.theme.AudiofyTypography
import com.audiofy.app.ui.theme.AudiofySpacing
import com.audiofy.app.util.saveAudioToTempFile
import com.audiofy.app.viewmodel.PlayerViewModel
import com.audiofy.app.viewmodel.ProcessingStep
import com.audiofy.app.viewmodel.ProcessingViewModel
import kotlinx.datetime.Clock

/**
 * Processing Screen
 * Shows TTS progress
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingScreen(
    viewModel: ProcessingViewModel,
    inputText: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
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
                .padding(AudiofySpacing.Space4),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val step = uiState.currentStep) {
                is ProcessingStep.GeneratingSpeech -> {
                    StepProgress(
                        title = "生成语音...",
                        description = "正在生成音频",
                        progress = uiState.progress
                    )
                }

                is ProcessingStep.Completed -> {
                    CompletedView(
                        inputText = uiState.inputText,
                        audioData = uiState.audioData ?: ByteArray(0),
                        onNavigateBack = onNavigateBack,
                        onNavigateToLibrary = onNavigateToLibrary
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
        verticalArrangement = Arrangement.spacedBy(AudiofySpacing.Space4)
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

        Spacer(modifier = Modifier.height(AudiofySpacing.Space4))

        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(AudiofySpacing.Space2))

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
    inputText: String,
    audioData: ByteArray,
    onNavigateBack: () -> Unit,
    onNavigateToLibrary: () -> Unit,
) {
    // Create PlayerViewModel instance
    val playerViewModel: PlayerViewModel = viewModel { PlayerViewModel() }

    // Load audio when audioData is available
    LaunchedEffect(audioData) {
        if (audioData.isNotEmpty()) {
            val audioPath = saveAudioToTempFile(
                audioData,
                "tts_audio_${Clock.System.now().toEpochMilliseconds()}.mp3"
            )
            playerViewModel.loadAudio(audioPath)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AudiofySpacing.Space4)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "完成",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(AudiofySpacing.Space8)
        )

        Text(
            text = "处理完成!",
            style = AudiofyTypography.titleLarge
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AudiofySpacing.Space4)
        ) {
            Column(
                modifier = Modifier.padding(AudiofySpacing.Space4)
            ) {
                Text(
                    text = "输入文本:",
                    style = AudiofyTypography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(AudiofySpacing.Space2))

                Text(
                    text = inputText,
                    style = AudiofyTypography.bodyMedium
                )
            }
        }

        // Audio Player
        AudioPlayerView(
            viewModel = playerViewModel,
            modifier = Modifier.padding(vertical = AudiofySpacing.Space2)
        )

        Spacer(modifier = Modifier.height(AudiofySpacing.Space2))

        // 操作按钮
        Column(
            modifier = Modifier.fillMaxWidth(0.7f),
            verticalArrangement = Arrangement.spacedBy(AudiofySpacing.Space2)
        ) {
            Button(
                onClick = onNavigateToLibrary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("查看书架")
            }
            
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("返回主页")
            }
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
        verticalArrangement = Arrangement.spacedBy(AudiofySpacing.Space4)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "错误",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(AudiofySpacing.Space8)
        )

        Text(
            text = "处理失败",
            style = AudiofyTypography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AudiofySpacing.Space4),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = errorMessage,
                style = AudiofyTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(AudiofySpacing.Space4),
                textAlign = TextAlign.Center
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space2),
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
