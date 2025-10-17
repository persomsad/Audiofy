package com.audiofy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.audiofy.app.ui.theme.AudiofySpacing

/**
 * 我的页面
 * 参考设计: docs/prototype/ui.html - SCREEN 9: 设置页面
 * 
 * 功能：用户信息 + 设置入口
 */
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AudiofySpacing.Space6)
    ) {
        Text(
            text = "我的",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(AudiofySpacing.Space4))
        
        // TODO: 用户信息卡片
        // TODO: 统计信息（已收听X个播客）
        // TODO: 设置入口
        
        Text(
            text = "个人中心功能开发中...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(AudiofySpacing.Space4))
        
        Button(onClick = onNavigateToSettings) {
            Text("进入设置")
        }
    }
}

