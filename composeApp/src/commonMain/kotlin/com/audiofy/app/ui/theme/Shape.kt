package com.audiofy.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Audiofy 形状系统
 * 基于设计系统文档：docs/design/design-system.md
 */

/**
 * 圆角尺寸
 */
object AudiofyRadius {
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 20.dp
    val Full = 9999.dp  // 完全圆角
}

/**
 * Material 3 Shapes配置
 */
val AudiofyShapes = Shapes(
    extraSmall = RoundedCornerShape(AudiofyRadius.Small),
    small = RoundedCornerShape(AudiofyRadius.Small),
    medium = RoundedCornerShape(AudiofyRadius.Medium),
    large = RoundedCornerShape(AudiofyRadius.Large),
    extraLarge = RoundedCornerShape(AudiofyRadius.ExtraLarge)
)
