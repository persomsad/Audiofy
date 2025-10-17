package com.audiofy.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.audiofy.app.data.Podcast
import com.audiofy.app.data.PodcastFilter
import com.audiofy.app.ui.theme.AudiofyColors
import com.audiofy.app.ui.theme.AudiofyRadius
import com.audiofy.app.ui.theme.AudiofySpacing

/**
 * 书架/播客库页面
 * 参考设计: docs/prototype/ui.html - SCREEN 4: 书架/播客库
 * 
 * 功能：播客列表展示 + 筛选 + 搜索
 */
@Composable
fun LibraryScreen(
    onNavigateToPodcastDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(PodcastFilter.ALL) }
    
    // TODO: 从Repository获取真实数据
    val podcasts = remember { emptyList<Podcast>() }
    
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // 顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AudiofySpacing.Space6),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "我的书架",
                style = MaterialTheme.typography.headlineLarge
            )
            
            // 搜索按钮
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                IconButton(onClick = { /* TODO: 搜索功能 */ }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索"
                    )
                }
            }
        }
        
        // 筛选标签
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AudiofySpacing.Space6),
            horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space2)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == PodcastFilter.ALL,
                    onClick = { selectedFilter = PodcastFilter.ALL },
                    label = { Text("全部") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == PodcastFilter.RECENT,
                    onClick = { selectedFilter = PodcastFilter.RECENT },
                    label = { Text("最近添加") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == PodcastFilter.UNFINISHED,
                    onClick = { selectedFilter = PodcastFilter.UNFINISHED },
                    label = { Text("未完成") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == PodcastFilter.FAVORITE,
                    onClick = { selectedFilter = PodcastFilter.FAVORITE },
                    label = { Text("已收藏") }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(AudiofySpacing.Space4))
        
        // 播客列表或空状态
        if (podcasts.isEmpty()) {
            EmptyLibraryState()
        } else {
            PodcastList(
                podcasts = podcasts,
                onPodcastClick = onNavigateToPodcastDetail,
                onPlayClick = { /* TODO: 直接播放 */ }
            )
        }
    }
}

/**
 * 空状态
 */
@Composable
private fun EmptyLibraryState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AudiofySpacing.Space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // TODO: SVG插图（Issue #66）
        
        Text(
            text = "还没有播客",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(AudiofySpacing.Space3))
        
        Text(
            text = "创建您的第一个播客\n开始智能听读之旅",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(AudiofySpacing.Space6))
        
        Button(
            onClick = { /* TODO: 导航到创建页面 */ },
            shape = RoundedCornerShape(AudiofyRadius.Full)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(AudiofySpacing.Space2))
            Text("创建第一个播客")
        }
    }
}

/**
 * 播客列表
 */
@Composable
private fun PodcastList(
    podcasts: List<Podcast>,
    onPodcastClick: (String) -> Unit,
    onPlayClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AudiofySpacing.Space6),
        verticalArrangement = Arrangement.spacedBy(AudiofySpacing.Space4)
    ) {
        items(podcasts) { podcast ->
            PodcastListItem(
                podcast = podcast,
                onClick = { onPodcastClick(podcast.id) },
                onPlayClick = { onPlayClick(podcast.id) }
            )
        }
    }
}

/**
 * 播客列表项
 */
@Composable
private fun PodcastListItem(
    podcast: Podcast,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AudiofyRadius.Large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(AudiofySpacing.Space4),
            horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 封面
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(AudiofyRadius.Medium)),
                shadowElevation = 2.dp
            ) {
                AsyncImage(
                    model = podcast.coverUrl ?: "https://source.unsplash.com/random/200x200?book",
                    contentDescription = podcast.title,
                    contentScale = ContentScale.Crop
                )
            }
            
            // 信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = podcast.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    maxLines = 1
                )
                
                if (podcast.author != null) {
                    Spacer(modifier = Modifier.height(AudiofySpacing.Space1))
                    Text(
                        text = podcast.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = Modifier.height(AudiofySpacing.Space2))
                
                // 元数据
                val currentVersion = podcast.audioVersions.find { it.versionId == podcast.currentVersionId }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space3)
                ) {
                    if (currentVersion != null) {
                        Text(
                            text = "${currentVersion.duration / 60}分钟",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = formatDate(podcast.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 播放按钮
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = AudiofyColors.Primary100
            ) {
                IconButton(onClick = onPlayClick) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "播放",
                        tint = AudiofyColors.Primary500
                    )
                }
            }
        }
    }
}

/**
 * 格式化日期
 */
private fun formatDate(timestamp: Long): String {
    // TODO: 使用kotlinx-datetime格式化
    // 暂时返回简单格式
    return "10月${(timestamp % 30) + 1}日"
}


