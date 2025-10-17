package com.audiofy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.ui.unit.sp
import com.audiofy.app.data.Podcast
import com.audiofy.app.ui.theme.AudiofyColors
import com.audiofy.app.ui.theme.AudiofyRadius
import com.audiofy.app.ui.theme.AudiofySpacing

/**
 * 播客详情页面
 * 参考设计: docs/prototype/ui.html - SCREEN 2: 播客详情
 * 
 * 功能：展示播客完整信息，提供收听/阅读入口
 */
@Composable
fun PodcastDetailScreen(
    podcastId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit = {},
    onNavigateToReading: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // TODO: 从Repository获取播客数据
    // val podcast = viewModel.getPodcastById(podcastId)
    
    // Mock数据
    val mockPodcast = remember {
        // 暂时使用null，实际会从Repository获取
        null as Podcast?
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部操作栏
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AudiofySpacing.Space6),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 返回按钮
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space3)
                ) {
                    // 收藏按钮
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        IconButton(onClick = { /* TODO: 收藏功能 */ }) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "收藏"
                            )
                        }
                    }
                    
                    // 分享按钮
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        IconButton(onClick = { /* TODO: 分享功能 */ }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "分享"
                            )
                        }
                    }
                }
            }
        }
        
        // 封面
        item {
            Spacer(modifier = Modifier.height(AudiofySpacing.Space4))
            
            Surface(
                modifier = Modifier
                    .width(224.dp)
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(AudiofyRadius.Medium)),
                shadowElevation = 8.dp
            ) {
                AsyncImage(
                    model = mockPodcast?.coverUrl ?: "https://source.unsplash.com/random/400x600?book",
                    contentDescription = mockPodcast?.title,
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        // 标题和作者
        item {
            Spacer(modifier = Modifier.height(AudiofySpacing.Space5))
            
            Text(
                text = mockPodcast?.title ?: "原子习惯",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(AudiofySpacing.Space2))
            
            Text(
                text = mockPodcast?.author ?: "詹姆斯·克利尔",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 元数据
        item {
            Spacer(modifier = Modifier.height(AudiofySpacing.Space5))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AudiofySpacing.Space8),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetadataItem(label = "4.5 评分", icon = Icons.Default.Star)
                MetadataItem(label = "356 页", icon = Icons.Default.Description)
                MetadataItem(label = "1.2万 收听", icon = Icons.Default.Visibility)
            }
            
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AudiofySpacing.Space8, vertical = AudiofySpacing.Space5),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        
        // 内容简介
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AudiofySpacing.Space8)
            ) {
                Text(
                    text = "内容简介",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(AudiofySpacing.Space3))
                
                Text(
                    text = mockPodcast?.textContent?.take(200) ?: "每天进步一点点，微小改变带来惊人成就。世界级习惯养成专家詹姆斯·克利尔揭示了如何通过微小的习惯改变，实现卓越的人生成就。本书提供了简单实用的方法，帮助你建立好习惯、打破坏习惯...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }
        }
        
        // 行动按钮
        item {
            Spacer(modifier = Modifier.height(AudiofySpacing.Space6))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AudiofySpacing.Space8),
                horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space3)
            ) {
                // 收听按钮
                OutlinedButton(
                    onClick = { onNavigateToPlayer(podcastId) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(AudiofyRadius.Full)
                ) {
                    Icon(
                        imageVector = Icons.Default.Headset,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(AudiofySpacing.Space2))
                    Text("收听")
                }
                
                // 阅读按钮
                Button(
                    onClick = { onNavigateToReading(podcastId) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(AudiofyRadius.Full),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AudiofyColors.Primary500
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(AudiofySpacing.Space2))
                    Text("阅读")
                }
            }
            
            Spacer(modifier = Modifier.height(AudiofySpacing.Space8))
        }
    }
}

/**
 * 元数据项
 */
@Composable
private fun MetadataItem(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

