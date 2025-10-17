package com.audiofy.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Audiofy 字体系统
 * 基于设计文档 docs/design/design-system.md
 *
 * 字体栈: Inter (英文), 阿里巴巴普惠体 2.0 (中文), Source Han Sans SC, system-ui, sans-serif
 *
 * 注意: Compose Multiplatform 默认使用系统字体
 * 如需自定义字体，需要将字体文件放入 composeApp/src/commonMain/composeResources/font/
 */

// Compose Material3 Typography 配置
val AudiofyTypography = Typography(
    // Display - 用于特大标题 (48px)
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 57.6.sp, // 48 * 1.2
    ),

    // Heading 1 - 主标题 (36px)
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 45.sp, // 36 * 1.25
    ),

    // Heading 2 - 次级标题 (24px)
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 33.6.sp, // 24 * 1.4
    ),

    // Heading 3 - 三级标题 (20px)
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 30.sp, // 20 * 1.5
    ),

    // Body Large - 大正文 (18px)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.8.sp, // 18 * 1.6
    ),

    // Body Default - 默认正文 (16px)
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 27.2.sp, // 16 * 1.7
    ),

    // Body Small - 小正文 (14px)
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp, // 14 * 1.5
    ),

    // Label Large - 按钮文字 (14px)
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),

    // Label Medium - 小标签 (12px)
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),

    // Label Small - 最小标签/Overline (12px, uppercase)
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 15.6.sp, // 12 * 1.3
        letterSpacing = 0.6.sp, // 0.05em ≈ 0.6sp at 12sp
    ),
)

/**
 * 设计系统原始字体样式定义（供参考）
 *
 * - display: 700, 48px, 1.2 line-height
 * - heading-1: 700, 36px, 1.25 line-height
 * - heading-2: 600, 24px, 1.4 line-height
 * - heading-3: 600, 20px, 1.5 line-height
 * - body-large: 400, 18px, 1.6 line-height
 * - body-default: 400, 16px, 1.7 line-height
 * - caption: 400, 14px, 1.5 line-height
 * - overline: 500, 12px, 1.3 line-height, uppercase, 0.05em letter-spacing
 */
