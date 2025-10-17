package com.audiofy.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Audiofy主题系统
 * 基于设计系统文档：docs/design/design-system.md
 * 参考原型：docs/prototype/ui.html
 */

/**
 * 亮色主题配色
 */
private val LightColorScheme = lightColorScheme(
    // Primary色系
    primary = AudiofyColors.Primary500,
    onPrimary = AudiofyColors.Neutral100,
    primaryContainer = AudiofyColors.Primary100,
    onPrimaryContainer = AudiofyColors.Primary900,
    
    // Secondary色系（使用Accent色）
    secondary = AudiofyColors.Accent500,
    onSecondary = AudiofyColors.Neutral100,
    secondaryContainer = AudiofyColors.Accent100,
    onSecondaryContainer = AudiofyColors.Accent500,
    
    // Tertiary色系
    tertiary = AudiofyColors.Primary700,
    onTertiary = AudiofyColors.Neutral100,
    
    // Background
    background = AudiofyColors.BgWarm,
    onBackground = AudiofyColors.Neutral800,
    
    // Surface
    surface = AudiofyColors.Neutral100,
    onSurface = AudiofyColors.Neutral800,
    surfaceVariant = AudiofyColors.Neutral200,
    onSurfaceVariant = AudiofyColors.Neutral600,
    
    // 其他
    outline = AudiofyColors.Neutral400,
    outlineVariant = AudiofyColors.Neutral300,
    error = AudiofyColors.Error,
    onError = AudiofyColors.Neutral100
)

/**
 * 暗色主题配色（可选，暂时使用亮色方案）
 */
private val DarkColorScheme = darkColorScheme(
    primary = AudiofyColors.Primary400,
    onPrimary = AudiofyColors.Neutral900,
    primaryContainer = AudiofyColors.Primary800,
    onPrimaryContainer = AudiofyColors.Primary100,
    
    background = AudiofyColors.Neutral900,
    onBackground = AudiofyColors.Neutral100,
    
    surface = AudiofyColors.Neutral800,
    onSurface = AudiofyColors.Neutral100,
    
    outline = AudiofyColors.Neutral600,
    error = AudiofyColors.Error
)

/**
 * Audiofy主题组件
 * 
 * 用法：
 * ```kotlin
 * AudiofyTheme {
 *     // 您的UI代码
 * }
 * ```
 */
@Composable
fun AudiofyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AudiofyTypography,
        shapes = AudiofyShapes,
        content = content
    )
}
