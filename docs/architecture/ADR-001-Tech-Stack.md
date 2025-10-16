# ADR-001: 技术栈选型

## 状态

已接受 (Accepted)

## 日期

2025-01-16

## 背景 (Context)

Audiofy 是一款移动应用，旨在将英文文章翻译成中文音频播客，用于个人语言学习。项目需要选择合适的技术栈来实现以下核心功能：

1. **跨平台移动应用**：支持 iOS 和 Android
2. **AI 翻译**：调用 Gemini API 进行英译中
3. **语音合成**：调用 Qwen3-TTS API 生成高质量中文语音
4. **本地存储**：音频文件存储在设备本地文件系统
5. **音频播放**：支持后台播放、进度控制、锁屏控制

### 当前面临的技术挑战

- 需要真正的原生访问能力（文件系统、音频播放）
- 开发者熟悉 Vue.js 生态系统
- 希望获得接近原生应用的性能和用户体验
- 需要长期维护和扩展的技术选型

## 决策 (Decision)

我们将采用 **NativeScript 8 + Vue 3 + TypeScript** 作为核心技术栈，因为：

1. **真正的原生性能**：NativeScript 将 Vue 组件编译为原生 UI 组件（iOS UIKit / Android Material），性能接近 100% 原生应用
2. **完整的原生 API 访问**：通过 @nativescript/\* 插件完整访问文件系统、音频播放、通知等原生功能
3. **Vue.js 生态复用**：开发者可以使用熟悉的 Vue 3 语法（Composition API、响应式系统）
4. **TypeScript 类型安全**：提供完整的类型检查，减少运行时错误

### 完整技术栈清单

**前端框架**：

- NativeScript 8.x（移动端跨平台框架）
- Vue 3.x（UI 组件框架）
- TypeScript 5.x（类型系统）

**API 集成**：

- Google Gemini API（英译中翻译）
- 阿里云 Qwen3-TTS API（中文语音合成）
- Cloudflare Workers（API 密钥代理层，保护安全）

**原生功能插件**：

- @nativescript/core（核心模块）
- nativescript-audio（音频播放和录制）
- @nativescript/localstorage（数据持久化）
- @nativescript/secure-storage（敏感信息加密存储）

**状态管理与工具**：

- Pinia（Vue 官方状态管理）
- ofetch（现代化 HTTP 客户端）
- dayjs（日期时间处理）

**构建与开发**：

- NativeScript CLI 8.x（命令行工具）
- Vite 6.x（开发服务器和打包工具）
- npm（包管理器）

## 备选方案 (Alternatives Considered)

### 备选方案 A: Capacitor + Vue 3 + Ionic Framework

- **描述**: 使用 Web 技术（Vue 3）+ Capacitor 打包成移动应用
- **优势**:
  - 开发速度极快（2-3 周 vs 2-3 个月）
  - 完全复用 Web 开发技能
  - 调试简单（浏览器 DevTools）
  - 大量企业案例（Burger King、Southwest Airlines）
- **为何未选择**:
  - 使用 WebView 渲染，启动速度较慢（~2.5 秒 vs NativeScript 的 ~1.5 秒）
  - 虽然 90% 的场景性能足够，但追求极致的原生感
  - 用户明确表示希望使用 NativeScript-Vue

### 备选方案 B: React Native

- **描述**: 使用 React 生态系统构建跨平台移动应用
- **优势**:
  - 社区最大（GitHub 118k stars vs NativeScript 24k stars）
  - 第三方库丰富
  - Meta 官方维护
- **为何未选择**:
  - 开发者熟悉 Vue 而非 React，学习成本高
  - React Native 的"Learn once, write anywhere"理念需要处理大量平台差异
  - NativeScript 的"真正原生渲染"在性能上略优于 React Native 的 Bridge 架构

### 备选方案 C: Flutter

- **描述**: 使用 Dart 语言和 Flutter 框架构建跨平台应用
- **优势**:
  - 性能极佳（Skia 渲染引擎）
  - UI 一致性好（Material Design / Cupertino 风格）
  - Google 官方支持
- **为何未选择**:
  - 需要学习全新的语言（Dart）和框架
  - 开发者现有技能无法复用
  - 生态系统虽然成熟，但与 JavaScript/Vue 生态隔离

### 备选方案 D: 原生开发（Swift + Kotlin）

- **描述**: 使用原生语言分别开发 iOS 和 Android 应用
- **优势**:
  - 性能和体验最优（100% 原生）
  - 完整的平台功能访问
  - 官方文档和社区支持最好
- **为何未选择**:
  - 开发成本翻倍（两套代码库）
  - 需要学习两门新语言（Swift + Kotlin）
  - 维护成本高（功能需实现两次）
  - 对于个人项目来说资源投入过大

## 后果 (Consequences)

### 正面影响

1. **性能接近原生**：启动速度 ~1.5 秒，UI 渲染流畅度达到 60fps
2. **开发体验良好**：Vue 3 Composition API 提供清晰的代码组织方式
3. **完整的功能支持**：文件系统、后台音频播放、通知等功能无障碍实现
4. **类型安全**：TypeScript 提供编译时类型检查，减少低级错误
5. **代码可维护性高**：Vue 单文件组件（SFC）模式利于模块化开发

### 负面影响与缓解措施

1. **学习曲线陡峭**
   - **影响**：需要学习 NativeScript 的特殊语法（`<Label>`, `<StackLayout>` 等）和原生 API
   - **缓解**：官方文档完善，提供 [NativeScript-Vue 教程](https://nativescript-vue.org/)；社区有大量示例代码

2. **调试复杂度增加**
   - **影响**：不能直接使用浏览器 DevTools，需要学习 NativeScript 调试工具
   - **缓解**：使用 Chrome DevTools 的 NativeScript 扩展；利用 Android Studio / Xcode 的调试器

3. **开发周期较长**
   - **影响**：预估 2-3 个月开发周期（vs Capacitor 的 2-3 周）
   - **缓解**：采用 MVP 策略，先实现核心功能（翻译 + TTS + 播放），后续迭代增加高级功能

4. **第三方库选择受限**
   - **影响**：NativeScript 插件生态小于 React Native 和 Flutter
   - **缓解**：核心功能（文件系统、音频播放）官方插件已覆盖；必要时可以编写原生模块桥接

5. **热重载不稳定**
   - **影响**：开发时热重载偶尔失效，需要完全重启应用
   - **缓解**：使用 `ns preview` 命令在多个设备上同步预览；优化开发流程减少重启次数

### 所需资源

- **开发时间**：2-3 个月（1 人全职，假设每天 6-8 小时）
- **学习成本**：1-2 周学习 NativeScript 框架和原生 API
- **硬件要求**：
  - macOS 电脑（用于 iOS 开发，已具备）
  - Xcode 15+（已安装）
  - Android Studio（已安装）
- **账号费用**：
  - Apple Developer Program: $99/年（上架 App Store 必需）
  - Google Play Developer: $25 一次性（上架 Google Play 必需）
- **API 调用费用**：
  - Gemini API: 免费额度足够 MVP 阶段使用
  - Qwen3-TTS API: 0.8元/万字符，免费额度 2000 字符（90天），预估 ¥8-15/月（个人使用量）
  - Cloudflare Workers: 免费额度 100,000 请求/天（足够使用）

## 相关决策

- ADR-002: 架构设计（定义分层架构和模块划分）
- ADR-003: 质量保障机制（定义测试策略和代码规范）

## 参考资料

- [NativeScript 官方文档](https://docs.nativescript.org/)
- [NativeScript-Vue 文档](https://nativescript-vue.org/)
- [Vue 3 官方文档](https://vuejs.org/)
- [Gemini API 文档](https://ai.google.dev/docs)
- [阿里云 DashScope Qwen3-TTS 文档](https://bailian.console.aliyun.com/?tab=doc#/doc/?type=model&url=2879134)
