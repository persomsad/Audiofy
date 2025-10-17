# ADR-004: 从 NativeScript 迁移到 Kotlin Multiplatform

## 状态

已接受 (Accepted) - 2025-10-17

## 背景

在 v0.1.0 Milestone 开发过程中，我们遇到了 NativeScript 框架的多个致命技术问题，这些问题严重阻碍了项目的正常开发：

### 问题 1: npm 包兼容性问题（Issue #25）

**现象**:
- 常用 npm 包（`ofetch`, `uuid`）无法在 NativeScript 环境中正常工作
- 构建时报错：`Cannot find module 'node:http'`, `Cannot find module 'node:crypto'`
- 即使构建通过，运行时仍会崩溃

**原因分析**:
- NativeScript 运行时不是完整的 Node.js 环境
- 许多现代 npm 包依赖 Node.js 核心模块（`node:http`, `node:crypto`, `node:buffer` 等）
- NativeScript 提供的 polyfill 不完整，无法覆盖所有场景

**临时解决方案的问题**:
- 需要手写兼容层（如 `src/utils/uuid.ts`）
- 每次引入新 npm 包都可能遇到同样的问题
- 技术债务持续累积，维护成本越来越高

### 问题 2: Node.js API 缺失

**现象**:
- `process.env` 在 NativeScript 中不存在
- 运行时崩溃：`TypeError: Cannot read properties of undefined (reading 'env')`（`config.ts:9:12`）

**影响范围**:
- 无法使用标准的环境变量管理方式
- 无法直接使用基于 `process.env` 的配置库（如 `dotenv`）
- 需要使用 NativeScript 特有的 `ApplicationSettings` 或 `SecureStorage`

**开发体验问题**:
- 违反 JavaScript/TypeScript 生态的标准实践
- 增加学习成本和认知负担
- 代码无法在其他 JavaScript 运行时（Node.js, Deno, Bun）中复用

### 问题 3: 构建稳定性问题

**现象**:
- Android 构建频繁失败，错误信息不明确
- 依赖冲突难以调试（混合了 npm 依赖和 Gradle 依赖）
- 构建时间长（平均 3-5 分钟）

**根本原因**:
- NativeScript 需要维护两个依赖系统：npm（JavaScript 层）+ Gradle/CocoaPods（原生层）
- JavaScript 到原生的桥接层增加了复杂度
- 社区活跃度下降，问题修复周期长

## 决策

**迁移到 Kotlin Multiplatform (KMP) + Compose Multiplatform**

### 核心理由

1. **彻底解决 npm 兼容性问题**:
   - KMP 使用 Kotlin/Java 生态，无需担心 Node.js 核心模块依赖
   - Kotlin 标准库和第三方库成熟稳定
   - Gradle 依赖管理统一（无需维护两套系统）

2. **原生性能和 API 访问**:
   - KMP 编译为原生代码，无 JavaScript 桥接开销
   - 直接访问 Android/iOS 原生 API，无需封装层
   - Compose Multiplatform 提供现代声明式 UI

3. **构建稳定性**:
   - Gradle 构建系统成熟可靠
   - 错误信息清晰，调试容易
   - 构建速度快（增量编译支持）

4. **平台扩展性**:
   - Compose Multiplatform 支持 Android, iOS, Desktop (JVM), Web (Wasm)
   - 未来可以低成本扩展到桌面端和 Web 端
   - 代码共享率更高（业务逻辑 100% 共享，UI 层 80-90% 共享）

5. **长期技术趋势**:
   - Google 官方支持（Kotlin 是 Android 首选语言）
   - JetBrains 持续投入 Compose Multiplatform
   - 社区活跃，生态持续增长

## 备选方案

### 备选方案 A: 继续使用 NativeScript + 手写兼容层

- **描述**: 为每个不兼容的 npm 包编写兼容层，使用 NativeScript 特有的 API 替代 Node.js API
- **优势**:
  - 无需迁移成本，现有代码可继续使用
  - 团队已有 NativeScript 经验
- **为何未选择**:
  - **技术债务持续累积**: 每次引入新 npm 包都可能需要重写
  - **维护成本指数增长**: 兼容层代码量可能超过业务代码
  - **无法解决构建稳定性问题**: 双依赖系统的复杂性仍然存在
  - **开发效率持续下降**: 大量时间花在解决框架问题，而非业务开发

### 备选方案 B: React Native

- **描述**: 使用 React Native 框架，基于 React 生态
- **优势**:
  - JavaScript 生态支持更好（但仍有桥接层限制）
  - 社区更活跃，npm 包生态更丰富
  - Meta 官方支持
- **为何未选择**:
  - **仍有 JavaScript 桥接性能开销**: 通过 JavaScript Bridge 调用原生 API，有序列化成本
  - **npm 包兼容性问题未彻底解决**: 仍然依赖 Node.js polyfill，部分包可能不兼容
  - **性能不如 KMP**: UI 渲染仍需通过桥接层
  - **平台扩展性有限**: Desktop/Web 支持通过第三方方案（React Native for Web），成熟度不如 Compose Multiplatform

### 备选方案 C: Flutter

- **描述**: 使用 Flutter 框架，基于 Dart 语言
- **优势**:
  - 跨平台支持好（Android, iOS, Web, Desktop）
  - UI 一致性强（Skia 渲染引擎）
  - Google 官方支持
- **为何未选择**:
  - **Dart 语言生态较小**: 第三方库数量远少于 Kotlin/Java 生态
  - **Android/iOS 原生 API 集成不如 KMP 直接**: 需要通过 Platform Channel 调用原生代码
  - **团队学习成本**: Dart 语法与主流语言（TypeScript, Kotlin, Swift）差异较大
  - **Compose Multiplatform 更符合 Android 未来方向**: Google 正在将 Android 开发从 XML 迁移到 Jetpack Compose

### 备选方案 D: 不做任何改变（保持现状）

- **描述**: 继续使用 NativeScript，接受现有技术限制
- **优势**:
  - 无需额外开发成本
  - 团队熟悉现有技术栈
- **为何未选择**:
  - **无法完成核心功能**: Issue #25 只是冰山一角，未来会遇到更多兼容性问题
  - **开发效率极低**: 大量时间浪费在框架问题调试上
  - **项目无法按时交付**: 技术风险过高，影响业务目标

## 后果

### 正面影响

1. **技术债务清零**:
   - 彻底解决 npm 包兼容性问题
   - 统一依赖管理（Gradle）
   - 消除 JavaScript 桥接层

2. **性能提升**:
   - 原生代码执行，无桥接开销
   - Compose UI 渲染性能优于基于 WebView 的方案
   - 应用启动速度更快，内存占用更低

3. **开发效率提升**:
   - 错误信息清晰，调试容易
   - IDE 支持更好（Android Studio 对 Kotlin 的支持是一流的）
   - 减少"解决框架问题"的时间，专注业务开发

4. **平台扩展性**:
   - 未来可低成本扩展至 Desktop 和 Web（共享 80-90% 代码）
   - 代码复用率高，维护成本低

5. **长期技术可持续性**:
   - Google 和 JetBrains 官方支持
   - 社区活跃，生态持续增长
   - 技术栈不会过时

### 负面影响与缓解措施

1. **迁移成本 13-20 天**
   - **影响**: 需要重写所有代码
   - **缓解措施**:
     - 项目处于早期阶段，代码量少（约 3000 行）
     - NativeScript 代码已归档至 `archive/nativescript-v0.1` 分支，可作为参考
     - 业务逻辑清晰，可以快速迁移

2. **学习成本**
   - **影响**: 团队需要学习 Kotlin 和 Compose
   - **缓解措施**:
     - Kotlin 语法与 TypeScript 非常相似（都是静态类型的现代语言）
     - Compose 声明式 UI 模式与 Vue 3 Composition API 类似
     - JetBrains 提供完善的官方文档和教程
     - Android Studio 提供强大的代码补全和重构支持

3. **丢失现有代码**
   - **影响**: NativeScript 代码无法直接复用
   - **缓解措施**:
     - NativeScript 代码已归档至 `archive/nativescript-v0.1` 分支
     - 业务逻辑和架构设计可以参考
     - UI 设计系统已提取至 `docs/prototype/ui.html`，可直接转换为 Compose Theme

### 所需资源

- **开发时间**: 13-20 天
  - Phase 1: 项目初始化（1 天）
  - Phase 2: UI 设计系统实现（3-5 天）
  - Phase 3: 业务逻辑迁移（6-8 天）
  - Phase 4: 测试和调试（3-4 天）

- **工具要求**:
  - Android Studio（免费）
  - Xcode（iOS 开发需要，Mac 环境）
  - Kotlin Multiplatform 插件（免费）

- **培训资源**:
  - Kotlin 官方文档: https://kotlinlang.org/docs/home.html
  - Compose Multiplatform 官方文档: https://www.jetbrains.com/lp/compose-multiplatform/
  - KMP 官方教程: https://www.jetbrains.com/kotlin-multiplatform/

## 实施计划

### Phase 1: 归档 NativeScript 工作 ✅ (已完成)
- ✅ 提交 Issue #25 PR (#26)
- ✅ 创建归档分支 `archive/nativescript-v0.1`
- ✅ 关闭 Milestone v0.1.0

### Phase 2: 创建 ADR-004 ✅ (当前文档)
- ✅ 记录迁移决策和理由

### Phase 3: 初始化 KMP 项目 (预计 3 小时)
- 使用 Kotlin Multiplatform Wizard 生成项目
- 配置 Android + iOS 平台
- 验证基础构建

### Phase 4: 提取 UI 设计系统 (预计 1 天)
- 从 `docs/prototype/ui.html` 提取设计令牌
- 创建 Compose Theme（颜色、间距、圆角、字体）
- 实现基础组件库

### Phase 5: 创建 Milestone v2.0.0 (预计 30 分钟)
- 定义版本目标和完成标准
- 创建 30-40 个实施 Issues

## 参考资料

- [Issue #25: Android build failure due to Node.js dependencies](https://github.com/persomsad/Audiofy/issues/25)
- [Pull Request #26: Fix Android build failure](https://github.com/persomsad/Audiofy/pull/26)
- [归档分支: archive/nativescript-v0.1](https://github.com/persomsad/Audiofy/tree/archive/nativescript-v0.1)
- [UI 设计原型: docs/prototype/ui.html](../prototype/ui.html)
- [Kotlin Multiplatform 官方文档](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform 官方文档](https://www.jetbrains.com/lp/compose-multiplatform/)

## 更新历史

- 2025-10-17: 初始版本（迁移决策）
