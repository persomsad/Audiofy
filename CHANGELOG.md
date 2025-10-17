# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3] - 2025-10-17

### Added
- **智能文本分片 (TextChunker)**: 突破600字符限制，支持任意长度文本的TTS转换
  - 智能识别句子边界（句号、问号、感叹号等）
  - 单个句子超长时按次要标点切割（逗号、分号等）
  - 保留标点符号和语义完整性
- **无缝音频拼接 (WavMerger)**: 合并多个WAV音频片段
  - 支持WAV格式（24kHz, 16位, 单声道）
  - 片段间插入100ms静音避免咔哒声
  - 自动更新WAV文件头部大小字段
- **进度反馈增强**: 长文本处理时显示详细进度
  - 显示"生成中...(第X/Y片)"
  - 每个分片的状态追踪
  - 合并音频时的进度提示
- **速率限制保护**: 片段间延迟500ms避免触发API限制
- **完整单元测试**: 新增20个测试用例
  - TextChunkerTest: 10个测试（短文本、长文本、标点识别、真实场景等）
  - WavMergerTest: 10个测试（格式验证、合并、静音插入、头部更新等）

### Fixed
- 修复iOS编译错误（String.format跨平台兼容性问题）

### Changed
- TTSServiceImpl自动检测文本长度并选择合适的处理方式
  - ≤600字符: 直接调用API
  - >600字符: 自动分片处理并合并音频

### Technical Details
- 新增代码: 791行（2个核心类 + 2个测试类）
- 跨平台支持: Android + iOS
- CI/CD: 全部检查通过（Code Quality, Tests, iOS Build, Android Build）

### Related
- Issue: #51
- Pull Request: #52

---

## [1.2] - 2025-10-17

### Changed
- **架构简化**: 移除Gemini API集成，专注TTS功能
  - 删除513行代码（GeminiService相关）
  - 简化用户工作流：在Gemini网页版翻译，粘贴到应用TTS
  - 更好的国内体验：Qwen3 TTS API国内可直接访问

### Removed
- 移除GeminiService.kt, GeminiServiceImpl.kt, GeminiRequest.kt
- 移除AppConfig中的Gemini配置字段
- 简化ProcessingViewModel逻辑

### Documentation
- 完整重写README.md
- 添加推荐工作流说明

### Related
- Pull Request: #49

---

## [1.1] - 2025-10-17

### Fixed
- **网络权限缺失** (P0 - 阻塞问题)
  - 添加INTERNET和ACCESS_NETWORK_STATE权限到AndroidManifest.xml
  - 修复v1.0的"Permission denied"错误
- **APK未签名** (P1 - 重要问题)
  - 在build.gradle.kts配置debug签名
  - Release APK现在已正确签名
- **输入字数限制过低** (P2 - 改进)
  - 将MAX_INPUT_LENGTH从5000提高到20000

### Changed
- CI/CD: 所有检查通过

### Related
- Issues: #46, #47, #48, #49
- Pull Request: #46

---

## [1.0] - 2025-10-17

### Added
- **首个正式版本** 🎉
- Qwen3 TTS API集成（17种语音角色）
- 中文/英文语音合成
- Gemini AI文本处理
- 跨平台架构（Android + iOS）
- 完整CI/CD流程
- 代码质量保障（detekt）

### Technical Details
- Kotlin Multiplatform
- Compose Multiplatform UI
- Ktor Client for HTTP
- kotlinx.serialization for JSON

---

## Legend

- `Added` - 新功能
- `Changed` - 功能变更
- `Deprecated` - 即将移除的功能
- `Removed` - 已移除的功能
- `Fixed` - Bug修复
- `Security` - 安全修复

