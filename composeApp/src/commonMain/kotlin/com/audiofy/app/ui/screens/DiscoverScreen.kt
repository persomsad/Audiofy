package com.audiofy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.audiofy.app.ui.theme.AudiofySpacing

/**
 * 发现页面 - Issue #62
 * 参考: docs/prototype/ui.html - SCREEN 10
 */
@Composable
fun DiscoverScreen(
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("精选") }
    val categories = listOf("精选", "科技", "商业", "个人成长", "历史")
    
    Column(modifier = modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(AudiofySpacing.Space6), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("发现", style = MaterialTheme.typography.headlineLarge)
            Surface(Modifier.size(40.dp), androidx.compose.foundation.shape.CircleShape, MaterialTheme.colorScheme.surface) {
                IconButton(onClick = {}) { Icon(androidx.compose.material.icons.Icons.Default.Search, "搜索") }
            }
        }
        
        androidx.compose.foundation.lazy.LazyRow(
            Modifier.fillMaxWidth().padding(horizontal = AudiofySpacing.Space6),
            horizontalArrangement = Arrangement.spacedBy(AudiofySpacing.Space2)
        ) {
            items(categories.size) { index ->
                FilterChip(
                    selected = selectedCategory == categories[index],
                    onClick = { selectedCategory = categories[index] },
                    label = { Text(categories[index]) }
                )
            }
        }
        
        Spacer(Modifier.height(AudiofySpacing.Space4))
        Text("本周热门", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = AudiofySpacing.Space6))
        Spacer(Modifier.height(AudiofySpacing.Space3))
        
        androidx.compose.foundation.lazy.LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = AudiofySpacing.Space6),
            verticalArrangement = Arrangement.spacedBy(AudiofySpacing.Space4)
        ) {
            items(2) {
                Card(
                    shape = RoundedCornerShape(AudiofyRadius.Large),
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                ) {
                    Box {
                        coil3.compose.AsyncImage(
                            "https://source.unsplash.com/random/800x400?technology",
                            null,
                            Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Black.copy(0.6f)))))
                        Column(Modifier.align(Alignment.BottomStart).padding(AudiofySpacing.Space4)) {
                            Text("AI时代的思考", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleLarge)
                            Text("探索AI如何改变生活", color = androidx.compose.ui.graphics.Color.White.copy(0.8f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

