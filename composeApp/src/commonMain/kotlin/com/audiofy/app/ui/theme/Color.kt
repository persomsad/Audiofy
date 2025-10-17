package com.audiofy.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Audiofy 颜色系统
 * 基于设计文档 docs/design/design-system.md
 */

// ==================== Primary Colors (主色) ====================
// 主色阶：用于核心CTA、链接和活动状态。基准色为 primary-500 (#FF7F50 Coral)
val Primary100 = Color(0xFFFFF2ED)
val Primary200 = Color(0xFFFFDACE)
val Primary300 = Color(0xFFFFBCA5)
val Primary400 = Color(0xFFFF9875)
val Primary500 = Color(0xFFFF7F50) // Base color
val Primary600 = Color(0xFFF56A36)
val Primary700 = Color(0xFFDD5724)
val Primary800 = Color(0xFFC24516)
val Primary900 = Color(0xFFA3360F)

// ==================== Accent Colors (强调色) ====================
// 强调色阶：用于次要但需突出的元素，如标签或特殊状态
// 基准色为 accent-500 (#8B4513 SaddleBrown)
val Accent100 = Color(0xFFF6EDE6)
val Accent200 = Color(0xFFE2D3C5)
val Accent300 = Color(0xFFCBB3A0)
val Accent400 = Color(0xFFB5947C)
val Accent500 = Color(0xFF8B4513) // Base color

// ==================== Neutral Colors (中性色) ====================
// 中性色阶：用于文本、背景、边框和UI控件
// 该色阶带有轻微的暖色调，以匹配 #F7EEDD 的背景
val Neutral100 = Color(0xFFFFFFFF) // Pure White
val Neutral200 = Color(0xFFF7EEDD) // Main BG (from user)
val Neutral300 = Color(0xFFEDE4D3) // Secondary BG (from user)
val Neutral400 = Color(0xFFC4BCAB) // Borders / Disabled (from user)
val Neutral500 = Color(0xFF9E9587) // Subtle text / Icons
val Neutral600 = Color(0xFF776F64) // Body text
val Neutral700 = Color(0xFF544F47) // Secondary heading
val Neutral800 = Color(0xFF2C2C2C) // Main heading (from user)
val Neutral900 = Color(0xFF000000) // Pure Black (from user)

// ==================== Semantic Colors (语义色) ====================
// 语义色：用于传达成功、危险、警告和信息状态
val Success = Color(0xFF28A745)
val Error = Color(0xFFDC3545)
val Warning = Color(0xFFFFC107)
val Info = Color(0xFF17A2B8)
