package com.audiofy.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Audiofy 颜色系统
 * 基于设计系统文档：docs/design/design-system.md
 * 参考原型：docs/prototype/ui.html
 */

/**
 * Primary色阶 - Coral主色系
 * 用于核心CTA、链接和活动状态
 */
object AudiofyColors {
    // Primary色阶
    val Primary100 = Color(0xFFFFF2ED)
    val Primary200 = Color(0xFFFFDACE)
    val Primary300 = Color(0xFFFFBCA5)
    val Primary400 = Color(0xFFFF9875)
    val Primary500 = Color(0xFFFF7F50)  // Coral - 主色
    val Primary600 = Color(0xFFF56A36)
    val Primary700 = Color(0xFFDD5724)
    val Primary800 = Color(0xFFC24516)
    val Primary900 = Color(0xFFA3360F)
    
    // Accent色阶 - SaddleBrown强调色
    val Accent100 = Color(0xFFF6EDE6)
    val Accent200 = Color(0xFFE2D3C5)
    val Accent300 = Color(0xFFCBB3A0)
    val Accent400 = Color(0xFFB5947C)
    val Accent500 = Color(0xFF8B4513)
    
    // Neutral色阶 - 暖色调灰色系
    val Neutral100 = Color(0xFFFFFFFF)  // Pure White
    val Neutral200 = Color(0xFFF7EEDD)  // 主背景
    val Neutral300 = Color(0xFFEDE4D3)  // 次级背景
    val Neutral400 = Color(0xFFC4BCAB)  // 边框/禁用
    val Neutral500 = Color(0xFF9E9587)  // 微妙文本/图标
    val Neutral600 = Color(0xFF776F64)  // 正文
    val Neutral700 = Color(0xFF544F47)  // 次级标题
    val Neutral800 = Color(0xFF2C2C2C)  // 主标题
    val Neutral900 = Color(0xFF000000)  // Pure Black
    
    // 特殊背景色
    val BgWarm = Color(0xFFFFF8F0)   // 暖色背景
    val BgPeach = Color(0xFFFFEEE5)  // 桃色背景
    
    // 语义色
    val Success = Color(0xFF28A745)
    val Error = Color(0xFFDC3545)
    val Warning = Color(0xFFFFC107)
    val Info = Color(0xFF17A2B8)
}

/**
 * 颜色别名 - 方便使用
 */
val ColorPrimary = AudiofyColors.Primary500
val ColorBackground = AudiofyColors.BgWarm
val ColorSurface = AudiofyColors.Neutral100
val ColorTextPrimary = AudiofyColors.Neutral800
val ColorTextSecondary = AudiofyColors.Neutral600
val ColorTextTertiary = AudiofyColors.Neutral500
val ColorBorder = AudiofyColors.Neutral400
