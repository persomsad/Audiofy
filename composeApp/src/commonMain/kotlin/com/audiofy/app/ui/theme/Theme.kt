package com.audiofy.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Audiofy Light 配色方案
 * 基于设计文档 docs/design/design-system.md
 */
private val LightColorScheme = lightColorScheme(
    // Primary - 主色 (Coral)
    primary = Primary500,
    onPrimary = Neutral100,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary900,

    // Secondary - 强调色 (SaddleBrown)
    secondary = Accent500,
    onSecondary = Neutral100,
    secondaryContainer = Accent100,
    onSecondaryContainer = Accent500,

    // Background - 背景色 (暖色调米白)
    background = Neutral200,
    onBackground = Neutral800,

    // Surface - 表面色 (卡片、对话框等)
    surface = Neutral100,
    onSurface = Neutral800,
    surfaceVariant = Neutral300,
    onSurfaceVariant = Neutral700,

    // Error - 错误色
    error = Error,
    onError = Neutral100,

    // Outline - 边框色
    outline = Neutral400,
    outlineVariant = Neutral300,
)

/**
 * Audiofy Dark 配色方案
 * 注意：设计文档未提供暗色模式，这里提供基础实现
 * 后续可根据设计需求调整
 */
private val DarkColorScheme = darkColorScheme(
    primary = Primary400,
    onPrimary = Neutral900,
    primaryContainer = Primary700,
    onPrimaryContainer = Primary100,

    secondary = Accent400,
    onSecondary = Neutral900,
    secondaryContainer = Accent500,
    onSecondaryContainer = Accent100,

    background = Neutral900,
    onBackground = Neutral200,

    surface = Neutral800,
    onSurface = Neutral200,
    surfaceVariant = Neutral700,
    onSurfaceVariant = Neutral300,

    error = Error,
    onError = Neutral900,

    outline = Neutral600,
    outlineVariant = Neutral700,
)

/**
 * Audiofy 主题
 *
 * @param darkTheme 是否使用暗色主题（默认跟随系统）
 * @param content 应用内容
 *
 * 使用示例：
 * ```kotlin
 * AudiofyTheme {
 *     // Your app content
 *     Scaffold { ... }
 * }
 * ```
 */
@Composable
fun AudiofyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AudiofyTypography,
        shapes = AudiofyShapes,
        content = content
    )
}
