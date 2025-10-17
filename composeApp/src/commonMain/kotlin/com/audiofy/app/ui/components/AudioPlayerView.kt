package com.audiofy.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.audiofy.app.player.PlayerState
import com.audiofy.app.ui.theme.AudiofyTypography
import com.audiofy.app.ui.theme.Spacing
import com.audiofy.app.viewmodel.PlayerViewModel

/**
 * Audio Player View Component
 * Displays player controls and progress
 */
@Composable
fun AudioPlayerView(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.space4),
            verticalArrangement = Arrangement.spacedBy(Spacing.space3)
        ) {
            // Title
            Text(
                text = "音频播放器",
                style = AudiofyTypography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Loading indicator
            if (uiState.state == PlayerState.LOADING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Spacing.space5),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(Spacing.space2))
                    Text(
                        text = "加载中...",
                        style = AudiofyTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Error message
            if (uiState.state == PlayerState.ERROR) {
                Text(
                    text = uiState.errorMessage ?: "播放出错",
                    style = AudiofyTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Player controls (show when READY, PLAYING, or PAUSED)
            if (uiState.state in listOf(PlayerState.READY, PlayerState.PLAYING, PlayerState.PAUSED)) {
                // Progress slider
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.space1)
                ) {
                    Slider(
                        value = uiState.currentPosition.toFloat(),
                        onValueChange = { value ->
                            viewModel.onSeekChange(value.toLong())
                        },
                        onValueChangeFinished = {
                            viewModel.onSeekEnd(uiState.currentPosition)
                        },
                        valueRange = 0f..uiState.duration.toFloat().coerceAtLeast(1f),
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    )

                    // Time display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.formatTime(uiState.currentPosition),
                            style = AudiofyTypography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = viewModel.formatTime(uiState.duration),
                            style = AudiofyTypography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stop button
                    IconButton(
                        onClick = { viewModel.stop() },
                        enabled = uiState.state == PlayerState.PLAYING || uiState.state == PlayerState.PAUSED
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "停止",
                            tint = if (uiState.state == PlayerState.PLAYING || uiState.state == PlayerState.PAUSED) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(Spacing.space4))

                    // Play/Pause button
                    FilledIconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(Spacing.space8),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (uiState.state == PlayerState.PLAYING) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                            contentDescription = if (uiState.state == PlayerState.PLAYING) {
                                "暂停"
                            } else {
                                "播放"
                            },
                            modifier = Modifier.size(Spacing.space6)
                        )
                    }
                }
            }
        }
    }
}
