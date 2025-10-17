package com.audiofy.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.audiofy.app.ui.theme.AudiofyColors
import com.audiofy.app.ui.theme.AudiofyRadius
import com.audiofy.app.ui.theme.AudiofySpacing

/**
 * 成功/错误提示对话框
 * Issue #67
 */

@Composable
fun SuccessDialog(
    title: String = "播客生成成功！",
    message: String,
    onPlay: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onPlay, shape = RoundedCornerShape(AudiofyRadius.Full)) {
                Icon(Icons.Default.PlayArrow, null, Modifier.size(16.dp))
                Spacer(Modifier.width(AudiofySpacing.Space2))
                Text("立即播放")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onSave, shape = RoundedCornerShape(AudiofyRadius.Full)) {
                Icon(Icons.Default.Bookmark, null, Modifier.size(16.dp))
                Spacer(Modifier.width(AudiofySpacing.Space2))
                Text("保存到书架")
            }
        },
        icon = {
            Surface(Modifier.size(128.dp), CircleShape, AudiofyColors.Primary100) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(Modifier.size(96.dp), CircleShape, AudiofyColors.Primary500) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        },
        title = { Text(title, style = MaterialTheme.typography.headlineMedium) },
        text = { Text(message, style = MaterialTheme.typography.bodyLarge) }
    )
}

@Composable
fun ErrorDialog(
    title: String = "生成失败",
    message: String,
    errorCode: String? = null,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onRetry, shape = RoundedCornerShape(AudiofyRadius.Full)) {
                Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                Spacer(Modifier.width(AudiofySpacing.Space2))
                Text("重试")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(AudiofyRadius.Full)) {
                Text("返回")
            }
        },
        icon = {
            Surface(Modifier.size(128.dp), CircleShape, color = AudiofyColors.Error.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(Modifier.size(96.dp), CircleShape, AudiofyColors.Error) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Warning, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        },
        title = { Text(title, style = MaterialTheme.typography.headlineMedium) },
        text = { 
            Column {
                Text(message)
                if (errorCode != null) {
                    Spacer(Modifier.height(AudiofySpacing.Space2))
                    Text("错误代码: $errorCode", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}

