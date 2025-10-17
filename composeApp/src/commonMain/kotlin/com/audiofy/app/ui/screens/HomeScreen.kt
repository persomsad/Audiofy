package com.audiofy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Bolt
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
 * 主页（阅读）
 * 参考设计: docs/prototype/ui.html - SCREEN 1: 主页
 * 
 * 功能：连续收听打卡 + 推荐内容
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(AudiofySpacing.Space6)
    ) {
        // 标题区域
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "嗨，朋友！",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    
                    Spacer(modifier = Modifier.height(AudiofySpacing.Space1))
                    
                    Text(
                        text = "今天想听点什么？",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 通知图标
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    IconButton(onClick = { /* TODO: 通知功能 */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "通知",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // 连续收听打卡卡片
        item {
            Spacer(modifier = Modifier.height(AudiofySpacing.Space6))
            
            StreakCard(
                currentStreak = 120,
                weekDays = listOf(true, true, true, true, true, false, false),
                onCheckIn = { /* TODO: 打卡功能 */ }
            )
        }
        
        // 推荐内容
        item {
            Spacer(modifier = Modifier.height(AudiofySpacing.Space6))
            
            Text(
                text = "为你推荐",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(AudiofySpacing.Space4))
        }
        
        // 推荐播客网格
        item {
            RecommendedPodcastsGrid()
        }
    }
}

/**
 * 连续收听打卡卡片
 */
@Composable
private fun StreakCard(
    currentStreak: Int,
    weekDays: List<Boolean>,  // 周一到周日的打卡状态
    onCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(AudiofySpacing.Space5)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space2)
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = AudiofyColors.Primary500,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "连续收听 $currentStreak 天",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(AudiofySpacing.Space4))
            
            // 一周打卡状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dayLabels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
                weekDays.forEachIndexed { index, checked ->
                    StreakDayItem(
                        label = dayLabels[index],
                        checked = checked
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AudiofySpacing.Space5))
            
            // 打卡按钮
            Button(
                onClick = onCheckIn,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AudiofyRadius.Full),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AudiofyColors.Primary500
                )
            ) {
                Text("我今天听了")
            }
        }
    }
}

/**
 * 单个打卡日组件
 */
@Composable
private fun StreakDayItem(
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = if (checked) AudiofyColors.Primary500 else AudiofyColors.Neutral400
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Headset,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

/**
 * 推荐播客网格
 */
@Composable
private fun RecommendedPodcastsGrid(
    modifier: Modifier = Modifier
) {
    // Mock数据
    val recommendations = listOf(
        Pair("原子习惯", "詹姆斯·克利尔"),
        Pair("设计心理学", "唐纳德·诺曼")
    )
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space4)
    ) {
        recommendations.forEach { (title, author) ->
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 封面
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(AudiofyRadius.Medium)),
                    shadowElevation = 4.dp
                ) {
                    AsyncImage(
                        model = "https://source.unsplash.com/random/300x400?book",
                        contentDescription = title,
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.height(AudiofySpacing.Space3))
                
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(AudiofySpacing.Space1))
                
                // 作者
                Text(
                    text = author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


