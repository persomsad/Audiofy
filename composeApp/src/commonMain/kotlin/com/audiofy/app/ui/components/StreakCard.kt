package com.audiofy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.audiofy.app.data.ListeningStreak
import com.audiofy.app.ui.theme.AudiofyColors
import com.audiofy.app.ui.theme.AudiofyRadius
import com.audiofy.app.ui.theme.AudiofySpacing

/**
 * 打卡状态卡片
 * 显示连续收听天数和本周收听情况
 */
@Composable
fun StreakCard(
    streak: ListeningStreak,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AudiofyRadius.Large),
        colors = CardDefaults.cardColors(
            containerColor = AudiofyColors.Primary100
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AudiofySpacing.Space6)
        ) {
            // Header: 打卡标题
            Row(
                horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "连续打卡",
                    tint = AudiofyColors.Primary500,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "连续收听打卡",
                    style = MaterialTheme.typography.titleMedium,
                    color = AudiofyColors.Primary700
                )
            }

            Spacer(modifier = Modifier.height(AudiofySpacing.Space4))

            // 连续天数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${streak.currentStreak}",
                        style = MaterialTheme.typography.displayMedium,
                        color = AudiofyColors.Primary700
                    )
                    Text(
                        text = "连续天数",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AudiofyColors.Primary600
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${streak.longestStreak}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AudiofyColors.Neutral600
                    )
                    Text(
                        text = "最长记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = AudiofyColors.Neutral500
                    )
                }
            }

            Spacer(modifier = Modifier.height(AudiofySpacing.Space5))

            // 本周收听状态
            Text(
                text = "本周收听",
                style = MaterialTheme.typography.labelMedium,
                color = AudiofyColors.Primary600
            )

            Spacer(modifier = Modifier.height(AudiofySpacing.Space3))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
                weekDays.forEachIndexed { index, day ->
                    val dayNumber = index + 1
                    val isChecked = streak.thisWeekDays.contains(dayNumber)

                    DayIndicator(
                        day = day,
                        isChecked = isChecked
                    )
                }
            }
        }
    }
}

@Composable
private fun DayIndicator(
    day: String,
    isChecked: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AudiofySpacing.Space1)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isChecked) AudiofyColors.Primary500 else AudiofyColors.Neutral200
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }

        Text(
            text = day,
            style = MaterialTheme.typography.labelSmall,
            color = if (isChecked) AudiofyColors.Primary700 else AudiofyColors.Neutral500
        )
    }
}
