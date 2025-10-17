package com.audiofy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.audiofy.app.ui.theme.AudiofySpacing

/**
 * 有声书页面
 * 参考设计: docs/prototype/ui.html
 * 
 * 功能：有声书列表和播放
 */
@Composable
fun AudiobooksScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AudiofySpacing.Space6)
    ) {
        Text(
            text = "有声书",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(AudiofySpacing.Space4))
        
        // TODO: 有声书列表
        
        Text(
            text = "有声书功能开发中...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

