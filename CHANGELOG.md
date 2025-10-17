# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.3] - 2025-10-17

### Fixed
- **修复网络安全配置导致音频下载失败** (P0 - 阻塞问题)
  - 错误：Cleartext HTTP traffic to dashscope-result-bj.oss-cn-beijing.aliyuncs.com not permitted
  - 原因：网络安全配置只允许oss-cn-shanghai.aliyuncs.com（上海OSS）
  - 修复：改为允许aliyuncs.com（包含所有子域名和OSS区域）
  - 现在支持北京、上海、杭州等所有OSS区域的音频下载

### Technical Details
- 修复文件: composeApp/src/androidMain/res/xml/network_security_config.xml
- 代码变更: 2行新增，1行删除

### Related
- Issue: #53

---

## [1.3.2] - 2025-10-17

### Fixed
- **🔥 重大修复：发现并修复根本问题 - 限制是600字节而非600字符** (P0 - 阻塞问题)
  - 通过Python测试脚本验证：Qwen3 TTS Flash限制是600字节（UTF-8编码），不是600字符
  - 英文：1字符=1字节，最多600字符
  - 中文：1字符=3字节，最多**200字符**
  - 完全重写TextChunker：所有长度计算改为UTF-8字节数
  - 新增hardChunkByBytes()：逐字符累加字节数的精确切割
  - 更新TTSServiceImpl：MAX_TEXT_LENGTH → MAX_TEXT_BYTES
  - 更新所有测试：断言检查字节数而非字符数

### Added
- 新增3个Python测试脚本验证API限制：
  - scripts/test-simple-tts.py：基础API测试
  - scripts/test-byte-limit.py：字节数vs字符数验证
  - scripts/test-long-text-tts.py：长文本处理测试

### Changed
- 中文文本分片数量增加约3倍（从字符计数改为字节计数）
- 日志输出格式：同时显示字符数和字节数

### Technical Details
- 修复文件: TextChunker.kt, TTSServiceImpl.kt, TextChunkerTest.kt
- 新增文件: 3个Python测试脚本
- 代码变更: 623行新增，70行删除
- 测试: 所有单元测试通过 + Python脚本验证通过

### Related
- Issue: #53
- Pull Request: #54

---

## [1.3.1] - 2025-10-17

### Fixed
- **紧急修复：TextChunker分片长度计算bug** (P0 - 阻塞问题)
  - 修复smartChunk()方法在拼接句子时未计入空格的长度
  - 修复hardChunk()方法的相同问题
  - 问题导致部分片段超过600字符限制，触发Qwen3 API 400错误
  - 添加分片长度安全检查，确保所有片段严格<=600字符
  - 增强日志输出，显示每个分片的实际长度

### Technical Details
- 修复文件: TextChunker.kt, TTSServiceImpl.kt
- 代码变更: 26行新增，6行删除
- CI/CD: 全部检查通过

### Related
- Issue: #53
- Pull Request: #54

---

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

