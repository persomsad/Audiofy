package com.audiofy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.audiofy.app.ui.theme.AudiofyColors
import com.audiofy.app.ui.theme.AudiofySpacing

/**
 * 阅读页面
 * 参考: docs/prototype/ui.html - SCREEN 5
 * Issue #61
 */
@Composable
fun ReadingScreen(
    podcastId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(AudiofySpacing.Space6), Arrangement.SpaceBetween) {
            Surface(Modifier.size(40.dp), CircleShape, MaterialTheme.colorScheme.surface) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "返回") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space2)) {
                Surface(Modifier.size(40.dp), CircleShape, MaterialTheme.colorScheme.surface) {
                    IconButton(onClick = {}) { Icon(Icons.Default.Search, "搜索") }
                }
                Surface(Modifier.size(40.dp), CircleShape, MaterialTheme.colorScheme.surface) {
                    IconButton(onClick = {}) { Icon(Icons.Default.TextFields, "字体") }
                }
            }
        }
        
        LazyColumn(Modifier.weight(1f).padding(AudiofySpacing.Space8)) {
            item {
                Text("习惯的记分卡", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(AudiofySpacing.Space5))
            }
            item {
                Text(
                    "日本铁路系统被誉为世界上最好的系统之一。如果你在东京坐火车，你会注意到列车员有一个奇特的习惯...",
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp
                )
            }
        }
        
        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
            Row(Modifier.padding(AudiofySpacing.Space5), Arrangement.Center, Alignment.CenterVertically) {
                Surface(Modifier.size(48.dp), CircleShape, AudiofyColors.BgPeach) {
                    IconButton(onClick = { onNavigateToPlayer(podcastId) }) {
                        Icon(Icons.Default.Headset, "收听", tint = AudiofyColors.Primary500)
                    }
                }
                Spacer(Modifier.width(AudiofySpacing.Space4))
                Surface(Modifier.size(48.dp), CircleShape, AudiofyColors.BgPeach) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MenuBook, "阅读", tint = AudiofyColors.Primary500)
                    }
                }
            }
        }
    }
}

