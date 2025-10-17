package com.audiofy.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Audiofy 间距系统
 * 基于设计文档 docs/design/design-system.md
 *
 * 基于 8px 网格系统（基础单位为 4px）
 * 用于控制布局、内边距和外边距，确保界面元素的和谐统一
 */

object Spacing {
    val space1 = 4.dp   // 0.25rem
    val space2 = 8.dp   // 0.5rem
    val space3 = 12.dp  // 0.75rem
    val space4 = 16.dp  // 1rem - 基础单位
    val space5 = 24.dp  // 1.5rem
    val space6 = 32.dp  // 2rem
    val space7 = 48.dp  // 3rem
    val space8 = 64.dp  // 4rem
}

/**
 * 语义化间距别名（可选，用于提高代码可读性）
 */
object SemanticSpacing {
    // 组件内部间距
    val extraSmall = Spacing.space1  // 4dp
    val small = Spacing.space2       // 8dp
    val medium = Spacing.space3      // 12dp
    val large = Spacing.space4       // 16dp

    // 布局间距
    val sectionPadding = Spacing.space5    // 24dp - 区块内边距
    val screenPadding = Spacing.space6     // 32dp - 屏幕边距
    val sectionGap = Spacing.space7        // 48dp - 大区块间距
    val extraLargeGap = Spacing.space8     // 64dp - 特大间距
}
