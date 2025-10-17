# Audiofy 设计系统使用指南

本目录包含Audiofy应用的完整设计系统实现，基于：
- `docs/design/design-system.md` - 设计系统文档
- `docs/prototype/ui.html` - UI原型

## 📁 文件结构

```
theme/
├── Color.kt        # 颜色系统（Primary/Accent/Neutral色阶）
├── Type.kt         # 字体系统（Display/Headline/Body/Label）
├── Shape.kt        # 形状系统（圆角尺寸）
├── Spacing.kt      # 间距系统（8px网格）
├── Theme.kt        # 主题组件（整合所有Token）
└── README.md       # 本文档
```

## 🎨 使用示例

### 1. 应用主题

在App的根组件使用`AudiofyTheme`：

```kotlin
@Composable
fun App() {
    AudiofyTheme {
        // 您的UI代码
    }
}
```

### 2. 使用颜色

```kotlin
import com.audiofy.app.ui.theme.*

@Composable
fun MyButton() {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,  // Primary500
            contentColor = MaterialTheme.colorScheme.onPrimary   // White
        )
    ) {
        Text("按钮")
    }
}

// 或直接使用颜色对象
Box(
    modifier = Modifier.background(AudiofyColors.Primary100)
)
```

### 3. 使用字体

```kotlin
// 使用Material 3的Typography
Text(
    text = "大标题",
    style = MaterialTheme.typography.headlineLarge  // 36sp, Bold
)

Text(
    text = "正文内容",
    style = MaterialTheme.typography.bodyMedium  // 16sp, Regular, 1.7倍行高
)

Text(
    text = "小标签",
    style = MaterialTheme.typography.labelSmall  // 10sp, Medium
)
```

### 4. 使用间距

```kotlin
import com.audiofy.app.ui.theme.AudiofySpacing

Column(
    modifier = Modifier.padding(AudiofySpacing.Space4),  // 16dp
    verticalArrangement = Arrangement.spacedBy(AudiofySpacing.Space2)  // 8dp
) {
    // 内容
}

// 或使用扩展属性
import com.audiofy.app.ui.theme.space4

Box(modifier = Modifier.padding(Dp.space4))
```

### 5. 使用圆角

```kotlin
import com.audiofy.app.ui.theme.AudiofyRadius

Card(
    shape = RoundedCornerShape(AudiofyRadius.Large)  // 16dp
) {
    // 内容
}

// 或使用Material 3的shapes
Card(
    shape = MaterialTheme.shapes.large  // 16dp圆角
) {
    // 内容
}

// 完全圆角按钮
Button(
    shape = RoundedCornerShape(AudiofyRadius.Full)  // 9999dp
) {
    Text("圆角按钮")
}
```

## 🎨 颜色系统快速参考

### Primary色阶（Coral）
- `Primary100` (#FFF2ED) - 浅色背景
- `Primary200` (#FFDACE) - 高亮背景
- `Primary500` (#FF7F50) - **主色**，CTA按钮
- `Primary700` (#DD5724) - 按钮按下状态

### Neutral色阶（暖灰）
- `Neutral100` (#FFFFFF) - Surface白色
- `Neutral200` (#F7EEDD) - 主背景
- `Neutral400` (#C4BCAB) - 边框
- `Neutral600` (#776F64) - 正文
- `Neutral800` (#2C2C2C) - 标题

### 特殊色
- `BgWarm` (#FFF8F0) - 暖色背景
- `BgPeach` (#FFEEE5) - 桃色背景

## 📏 间距系统快速参考

基于8px网格：
- `Space1` = 4dp
- `Space2` = 8dp
- `Space3` = 12dp
- `Space4` = 16dp ⭐ 最常用
- `Space5` = 24dp
- `Space6` = 32dp
- `Space7` = 48dp
- `Space8` = 64dp

## 🔤 字体系统快速参考

| 用途 | 样式 | 大小 | 粗细 |
|------|------|------|------|
| 页面主标题 | headlineLarge | 36sp | Bold |
| 组件标题 | headlineMedium | 24sp | SemiBold |
| 卡片标题 | titleLarge | 18sp | SemiBold |
| 正文 | bodyMedium | 16sp | Regular |
| 辅助文本 | bodySmall | 14sp | Regular |
| 按钮文字 | labelLarge | 14sp | Medium |

## ⚙️ 圆角系统快速参考

- `Small` = 8dp - 小元素
- `Medium` = 12dp - 按钮、输入框
- `Large` = 16dp - 卡片
- `ExtraLarge` = 20dp - 大卡片
- `Full` = 9999dp - 完全圆角（按钮、图标）

## 🚀 最佳实践

### ✅ 推荐做法

```kotlin
// 使用MaterialTheme访问颜色
Text(
    text = "标题",
    color = MaterialTheme.colorScheme.primary
)

// 使用MaterialTheme访问字体
Text(
    text = "内容",
    style = MaterialTheme.typography.bodyMedium
)

// 使用AudiofySpacing对象
Column(
    modifier = Modifier.padding(AudiofySpacing.Space4)
)
```

### ❌ 避免做法

```kotlin
// ❌ 不要硬编码颜色
Text(text = "标题", color = Color(0xFFFF7F50))

// ❌ 不要硬编码字体大小
Text(text = "内容", fontSize = 16.sp)

// ❌ 不要硬编码间距
Column(modifier = Modifier.padding(16.dp))
```

## 📚 参考资料

- [设计系统文档](../../../../docs/design/design-system.md)
- [UI原型](../../../../docs/prototype/ui.html)
- [Material 3 设计指南](https://m3.material.io/)
- [Compose Multiplatform文档](https://www.jetbrains.com/lp/compose-multiplatform/)

