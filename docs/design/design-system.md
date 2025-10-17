# Audiofy 设计系统文档

> 本文档定义 Audiofy 应用的完整设计系统，包括颜色、字体、间距、组件规范等。
> 用于指导 Kotlin Multiplatform + Compose Multiplatform 的 UI 实现。

---

## 目录

- [颜色系统](#颜色系统)
- [字体系统](#字体系统)
- [间距系统](#间距系统)
- [圆角系统](#圆角系统)
- [阴影系统](#阴影系统)
- [图标系统](#图标系统)
- [组件规范](#组件规范)
- [页面布局](#页面布局)

---

<!-- 请在下方粘贴您的设计系统内容 -->

[文档开始]

# **设计系统：Audiofy (声阅)**

本文档是 Audiofy 应用的唯一真理之源，旨在为 AI 程序员提供一套清晰、完整且高度一致的前端构建规范。所有设计决策均源于产品的核心定位：一个**高效、智能、清爽、个人化**的智能文章转播客工具。

## **1. 设计令牌 (Design Tokens)**

设计令牌是构成我们用户界面的原子单位，确保了视觉元素在整个应用中的绝对一致性。

### **1.1. 颜色 (Colors)**

颜色系统以用户提供的核心色系为基础，扩展为一套完整的、具有逻辑性和可访问性的色板。主色 `Primary` 用于核心交互，中性色 `Neutral` 用于构建界面的骨架，语义色 `Semantic` 用于传达特定状态信息。

```yaml
colors:
  primary:
    # 主色阶：用于核心CTA、链接和活动状态。基准色为 500。
    # 源自用户提供的 #FF7F50 (Coral)
    - name: primary-100
      value: '#fff2ed'
    - name: primary-200
      value: '#ffdace'
    - name: primary-300
      value: '#ffbca5'
    - name: primary-400
      value: '#ff9875'
    - name: primary-500
      value: '#FF7F50'
    - name: primary-600
      value: '#f56a36'
    - name: primary-700
      value: '#dd5724'
    - name: primary-800
      value: '#c24516'
    - name: primary-900
      value: '#a3360f'
  accent:
    # 强调色阶：用于次要但需突出的元素，如标签或特殊状态。
    # 源自用户提供的 #8B4513 (SaddleBrown)
    - name: accent-100
      value: '#f6ede6'
    - name: accent-200
      value: '#e2d3c5'
    - name: accent-300
      value: '#cbb3a0'
    - name: accent-400
      value: '#b5947c'
    - name: accent-500
      value: '#8B4513'
  neutral:
    # 中性色阶：用于文本、背景、边框和UI控件。
    # 该色阶带有轻微的暖色调，以匹配 #F7EEDD 的背景。
    - name: neutral-100
      value: '#FFFFFF' # Pure White
    - name: neutral-200
      value: '#F7EEDD' # Main BG, from user
    - name: neutral-300
      value: '#ede4d3' # Secondary BG, from user
    - name: neutral-400
      value: '#c4bcab' # Borders / Disabled, from user
    - name: neutral-500
      value: '#9e9587' # Subtle text / Icons
    - name: neutral-600
      value: '#776f64' # Body text
    - name: neutral-700
      value: '#544f47' # Secondary heading
    - name: neutral-800
      value: '#2c2c2c' # Main heading, from user
    - name: neutral-900
      value: '#000000' # Pure Black, from user
  semantic:
    # 语义色：用于传达成功、危险、警告和信息状态。
    - name: success
      value: '#28a745'
    - name: error
      value: '#dc3545'
    - name: warning
      value: '#ffc107'
    - name: info
      value: '#17a2b8'

```

### **1.2. 字体 (Typography)**

字体系统旨在实现最佳的可读性和清晰的视觉层次。我们选择 `Inter` 作为英文字体，`阿里巴巴普惠体 2.0` 作为中文字体，以匹配产品现代、高效的气质。

```yaml
typography:
  fontFamily:
    # 定义字体栈
    - name: sans
      value: "Inter, 'Alibaba PuHuiTi 2.0', 'Source Han Sans SC', system-ui, sans-serif"
  styles:
    # 定义字体样式，fontSize 使用 rem 以便缩放，lineHeight 是无单位的乘数。
    - name: display
      fontFamily: sans
      fontWeight: 700 # Bold
      fontSize: '3rem' # 48px
      lineHeight: 1.2
    - name: heading-1
      fontFamily: sans
      fontWeight: 700 # Bold
      fontSize: '2.25rem' # 36px
      lineHeight: 1.25
    - name: heading-2
      fontFamily: sans
      fontWeight: 600 # SemiBold
      fontSize: '1.5rem' # 24px
      lineHeight: 1.4
    - name: heading-3
      fontFamily: sans
      fontWeight: 600 # SemiBold
      fontSize: '1.25rem' # 20px
      lineHeight: 1.5
    - name: body-large
      fontFamily: sans
      fontWeight: 400 # Regular
      fontSize: '1.125rem' # 18px
      lineHeight: 1.6
    - name: body-default
      fontFamily: sans
      fontWeight: 400 # Regular
      fontSize: '1rem' # 16px
      lineHeight: 1.7
    - name: caption
      fontFamily: sans
      fontWeight: 400 # Regular
      fontSize: '0.875rem' # 14px
      lineHeight: 1.5
    - name: overline
      fontFamily: sans
      fontWeight: 500 # Medium
      fontSize: '0.75rem' # 12px
      lineHeight: 1.3
      textTransform: uppercase
      letterSpacing: '0.05em'
```

### **1.3. 间距 (Spacing)**

基于 8px 网格系统（基础单位为 4px）的间距令牌，用于控制布局、内边距和外边距，确保界面元素的和谐统一。

```yaml
spacing:
  - name: space-1
    value: '0.25rem' # 4px
  - name: space-2
    value: '0.5rem' # 8px
  - name: space-3
    value: '0.75rem' # 12px
  - name: space-4
    value: '1rem' # 16px
  - name: space-5
    value: '1.5rem' # 24px
  - name: space-6
    value: '2rem' # 32px
  - name: space-7
    value: '3rem' # 48px
  - name: space-8
    value: '4rem' # 64px
```

### **1.4. 效果 (Effects)**

定义圆角和阴影，为组件增加深度和现代感。效果应保持微妙，以符合“清爽”的设计调性。

```yaml
effects:
  borderRadius:
    - name: sm
      value: '4px'
    - name: md
      value: '8px' # Default for buttons, inputs, cards
    - name: lg
      value: '16px'
    - name: full
      value: '9999px' # For circular elements
  boxShadow:
    # 阴影设计应微妙，提供层次感而非干扰
    - name: sm
      value: '0 1px 2px 0 rgba(0, 0, 0, 0.05)'
    - name: md
      value: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)' # Default for cards
    - name: lg
      value: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)'
```

---

## **2. 组件库 (Component Library)**

基于以上设计令牌，我们定义了一套可复用的核心组件。每个组件都应严格遵循其定义的 Props 和样式规则。

### **2.1. 组件：按钮 (Button)**

按钮是应用中最核心的交互元素。

```yaml
component:
  name: Button
  props:
    - name: variant
      type: String
      values: ['primary', 'secondary', 'tertiary']
      description: 按钮的视觉风格。
    - name: size
      type: String
      values: ['sm', 'md', 'lg']
      description: 按钮的尺寸。
    - name: disabled
      type: Boolean
      values: [true, false]
      description: 是否禁用按钮。
    - name: fullWidth
      type: Boolean
      values: [true, false]
      description: 是否让按钮宽度填充父容器。
  styles:
    base:
      fontFamily: 'typography.fontFamily.sans'
      fontWeight: 600
      borderRadius: 'effects.borderRadius.md'
      transition: 'all 0.2s ease-in-out'
    variants:
      primary:
        backgroundColor: 'colors.primary.500'
        color: 'colors.neutral.100'
        hover:
          backgroundColor: 'colors.primary.600'
        active:
          backgroundColor: 'colors.primary.700'
      secondary:
        backgroundColor: 'colors.neutral.300'
        color: 'colors.neutral.800'
        border: '1px solid colors.neutral.400'
        hover:
          backgroundColor: 'colors.neutral.400'
        active:
          backgroundColor: 'colors.neutral.500'
      tertiary:
        backgroundColor: 'transparent'
        color: 'colors.primary.600'
        hover:
          backgroundColor: 'colors.primary.100'
    sizes:
      sm:
        padding: 'spacing.space-1 spacing.space-2'
        fontSize: 'typography.styles.caption.fontSize'
      md:
        padding: 'spacing.space-2 spacing.space-4'
        fontSize: 'typography.styles.body-default.fontSize'
      lg:
        padding: 'spacing.space-3 spacing.space-5'
        fontSize: 'typography.styles.body-large.fontSize'
    states:
      disabled:
        backgroundColor: 'colors.neutral.400'
        color: 'colors.neutral.500'
        cursor: 'not-allowed'
```

### **2.2. 组件：输入框 (Input / TextArea)**

用于用户粘贴文本和输入链接的核心组件。

```yaml
component:
  name: Input
  props:
    - name: size
      type: String
      values: ['md', 'lg']
      description: 输入框的尺寸。
    - name: state
      type: String
      values: ['default', 'focused', 'error', 'disabled']
      description: 输入框的状态。
    - name: multiline
      type: Boolean
      values: [true, false]
      description: '如果为 true, 则渲染为 TextArea。'
  styles:
    base:
      fontFamily: 'typography.fontFamily.sans'
      color: 'colors.neutral.800'
      backgroundColor: 'colors.neutral.100'
      border: '1px solid colors.neutral.400'
      borderRadius: 'effects.borderRadius.md'
      transition: 'border-color 0.2s, box-shadow 0.2s'
    sizes:
      md:
        padding: 'spacing.space-2 spacing.space-3'
        fontSize: 'typography.styles.body-default.fontSize'
      lg:
        padding: 'spacing.space-3 spacing.space-4'
        fontSize: 'typography.styles.body-large.fontSize'
    states:
      default:
        borderColor: 'colors.neutral.400'
      focused:
        borderColor: 'colors.primary.500'
        boxShadow: '0 0 0 2px colors.primary.200'
      error:
        borderColor: 'colors.semantic.error'
      disabled:
        backgroundColor: 'colors.neutral.300'
        cursor: 'not-allowed'
```

### **2.3. 组件：播客卡片 (PodcastCard)**

在“我的播客”库中，用于展示每一个生成的音频文件。

```yaml
component:
  name: PodcastCard
  props:
    - name: title
      type: String
      description: '播客标题'
    - name: duration
      type: String
      description: '音频时长，格式为 MM:SS'
    - name: createdDate
      type: String
      description: '生成日期'
    - name: isActive
      type: Boolean
      values: [true, false]
      description: '是否为当前正在播放的曲目'
  styles:
    base:
      display: 'flex'
      flexDirection: 'column'
      padding: 'spacing.space-4'
      backgroundColor: 'colors.neutral.100'
      borderRadius: 'effects.borderRadius.lg'
      boxShadow: 'effects.boxShadow.md'
      border: '1px solid transparent'
      transition: 'all 0.2s ease-in-out'
    states:
      hover:
        transform: 'translateY(-2px)'
        boxShadow: 'effects.boxShadow.lg'
      active: # e.g., when it is the currently playing track
        borderColor: 'colors.primary.500'
        backgroundColor: 'colors.primary.100'
    layout:
      # 内部元素布局
      header:
        display: 'flex'
        justifyContent: 'space-between'
        alignItems: 'center'
      title:
        typography: 'typography.styles.heading-3'
        color: 'colors.neutral.800'
      footer:
        display: 'flex'
        justifyContent: 'space-between'
        alignItems: 'center'
        marginTop: 'spacing.space-3'
      metadata:
        typography: 'typography.styles.caption'
        color: 'colors.neutral.600'
```

### **2.4. 组件：播放器 (Player)**

一个最小化的音频播放控制器，通常固定在屏幕底部。

```yaml
component:
  name: Player
  props:
    - name: trackTitle
      type: String
    - name: currentTime
      type: Number # in seconds
    - name: totalDuration
      type: Number # in seconds
    - name: isPlaying
      type: Boolean
  styles:
    container:
      position: 'fixed'
      bottom: 0
      left: 0
      right: 0
      padding: 'spacing.space-3 spacing.space-4'
      backgroundColor: 'colors.neutral.800'
      color: 'colors.neutral.100'
      boxShadow: '0 -4px 10px rgba(0,0,0,0.1)'
    controls:
      display: 'flex'
      alignItems: 'center'
      justifyContent: 'center'
      gap: 'spacing.space-5'
    progressBar:
      # A slider component would be used here
      height: '4px'
      backgroundColor: 'colors.neutral.600'
      progressColor: 'colors.primary.500'
```

---

## **3. 示例页面蓝图：主转换页 (Home Screen)**

此蓝图使用上述定义的组件，描述了应用核心页面的结构，体现了“一键转换”的高效理念。

```yaml
page:
  name: HomeScreen
  layout:
    - component: Container
      maxWidth: '768px' # 限制最大内容宽度
      padding: 'spacing.space-6'
      children:
        - component: Header
          children:
            - component: Typography
              props:
                style: heading-1
                text: 'Audiofy 声阅'
            - component: Typography
              props:
                style: body-large
                text: '将任何英文文章，一键转为中文播客。'
                color: 'colors.neutral.600'
                marginTop: 'spacing.space-2'

        - component: MainContent
          props:
            marginTop: 'spacing.space-7'
          children:
            - component: Input
              props:
                multiline: true
                size: 'lg'
                placeholder: '在此处粘贴英文文本或文章链接...'
                rows: 12 # 建议的初始行数
            
            - component: VoiceSelector # 一个自定义组合组件
              props:
                label: "选择声音"
                marginTop: 'spacing.space-4'
              children:
                # Internally would use a Select/Dropdown primitive
                - option: '清澈男声 (Clarity)'
                - option: '温柔女声 (Gentle)'
            
            - component: Button
              props:
                variant: 'primary'
                size: 'lg'
                fullWidth: true
                label: '一键生成播客'
                marginTop: 'spacing.space-5'
```