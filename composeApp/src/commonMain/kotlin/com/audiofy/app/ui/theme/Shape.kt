package com.audiofy.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Audiofy 圆角系统
 * 基于设计文档 docs/design/design-system.md
 *
 * 定义圆角，为组件增加现代感
 * 效果应保持微妙，以符合"清爽"的设计调性
 */

val AudiofyShapes = Shapes(
    // Small - 用于小组件（Chips, Tags）
    small = RoundedCornerShape(4.dp),

    // Medium - 默认圆角，用于按钮、输入框、卡片
    medium = RoundedCornerShape(8.dp),

    // Large - 用于大卡片、对话框
    large = RoundedCornerShape(16.dp),

    // Extra Large - 用于特殊场景
    extraLarge = RoundedCornerShape(24.dp),
)

/**
 * 圆形形状（用于头像、圆形按钮等）
 */
val CircleShape = RoundedCornerShape(50) // 50% = 完全圆形
