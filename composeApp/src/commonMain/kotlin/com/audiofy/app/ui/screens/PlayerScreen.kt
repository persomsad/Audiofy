package com.audiofy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.audiofy.app.ui.theme.AudiofyColors
import com.audiofy.app.ui.theme.AudiofyRadius
import com.audiofy.app.ui.theme.AudiofySpacing

/**
 * 全屏播放器页面
 * 参考设计: docs/prototype/ui.html - SCREEN 3: 播放器
 */
@Composable
fun PlayerScreen(
    podcastId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0.35f) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AudiofySpacing.Space6),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.KeyboardArrowDown, "最小化")
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("正在播放", style = MaterialTheme.typography.labelSmall)
            }
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.MoreHoriz, "更多")
                }
            }
        }
        
        // 封面
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(320.dp).clip(RoundedCornerShape(AudiofyRadius.ExtraLarge)),
                shadowElevation = 8.dp
            ) {
                AsyncImage(
                    model = "https://source.unsplash.com/random/600x600?book",
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        // 播放信息和控制
        Column(modifier = Modifier.padding(AudiofySpacing.Space8)) {
            Text("原子习惯", style = MaterialTheme.typography.headlineSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(AudiofySpacing.Space1))
            Text("詹姆斯·克利尔", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            
            Spacer(Modifier.height(AudiofySpacing.Space5))
            
            // 进度条
            Slider(
                value = progress,
                onValueChange = { progress = it },
                colors = SliderDefaults.colors(
                    thumbColor = AudiofyColors.Primary500,
                    activeTrackColor = AudiofyColors.Primary500
                )
            )
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("12:45", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("36:20", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Spacer(Modifier.height(AudiofySpacing.Space5))
            
            // 播放控制
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.SkipPrevious, "上一首", Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(AudiofySpacing.Space6))
                Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = AudiofyColors.Primary500, shadowElevation = 8.dp) {
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            if (isPlaying) "暂停" else "播放",
                            Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(Modifier.width(AudiofySpacing.Space6))
                Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.SkipNext, "下一首", Modifier.size(24.dp))
                    }
                }
            }
            
            Spacer(Modifier.height(AudiofySpacing.Space5))
            
            // 辅助功能
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(AudiofySpacing.Space4))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                IconButton(onClick = {}) { Icon(Icons.Default.QueueMusic, "列表", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick = {}) { Icon(Icons.Default.Speed, "速度", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick = {}) { Icon(Icons.Default.Bedtime, "定时", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick = {}) { Icon(Icons.Default.Share, "分享", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

