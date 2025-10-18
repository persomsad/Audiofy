package com.audiofy.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.audiofy.app.data.VoiceOption
import com.audiofy.app.data.VoiceOptions
import com.audiofy.app.ui.theme.AudiofyRadius
import com.audiofy.app.ui.theme.AudiofySpacing

/**
 * 音色选择对话框
 * 支持音色预览和选择
 */
@Composable
fun VoiceSelectionDialog(
    currentVoiceId: String,
    onVoiceSelected: (VoiceOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = "选择音色",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(AudiofySpacing.Space2)
            ) {
                items(VoiceOptions.ALL) { voice ->
                    VoiceOptionItem(
                        voice = voice,
                        isSelected = voice.id == currentVoiceId,
                        onSelect = { onVoiceSelected(voice) },
                        onPreview = {
                            // TODO: Implement voice preview
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        shape = RoundedCornerShape(AudiofyRadius.ExtraLarge)
    )
}

/**
 * 单个音色选项项
 */
@Composable
private fun VoiceOptionItem(
    voice: VoiceOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(AudiofyRadius.Medium),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AudiofySpacing.Space4),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：音色信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AudiofySpacing.Space1)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space2),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = voice.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    // 性别标签
                    Surface(
                        shape = RoundedCornerShape(AudiofyRadius.Small),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = voice.gender,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    // 风格标签
                    Surface(
                        shape = RoundedCornerShape(AudiofyRadius.Small),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = voice.style,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Text(
                    text = voice.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 右侧：操作按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 预览按钮
                IconButton(
                    onClick = onPreview,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "试听",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // 选中标记
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "已选中",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
