package com.audiofy.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Audiofy 间距系统
 * 基于8px网格系统（基础单位4px）
 * 参考设计系统文档：docs/design/design-system.md
 */
object AudiofySpacing {
    val Space1 = 4.dp    // 0.25rem
    val Space2 = 8.dp    // 0.5rem
    val Space3 = 12.dp   // 0.75rem
    val Space4 = 16.dp   // 1rem
    val Space5 = 24.dp   // 1.5rem
    val Space6 = 32.dp   // 2rem
    val Space7 = 48.dp   // 3rem
    val Space8 = 64.dp   // 4rem
    
    // 常用别名
    val ExtraSmall = Space1
    val Small = Space2
    val Medium = Space4
    val Large = Space6
    val ExtraLarge = Space8
}

/**
 * 扩展属性，方便使用
 */
val Dp.Companion.space1: Dp get() = AudiofySpacing.Space1
val Dp.Companion.space2: Dp get() = AudiofySpacing.Space2
val Dp.Companion.space3: Dp get() = AudiofySpacing.Space3
val Dp.Companion.space4: Dp get() = AudiofySpacing.Space4
val Dp.Companion.space5: Dp get() = AudiofySpacing.Space5
val Dp.Companion.space6: Dp get() = AudiofySpacing.Space6
val Dp.Companion.space7: Dp get() = AudiofySpacing.Space7
val Dp.Companion.space8: Dp get() = AudiofySpacing.Space8
