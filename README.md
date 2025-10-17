# Audiofy

> AI-powered text-to-speech podcast generator for language learning

Audiofy is a cross-platform mobile application that converts text into natural-sounding Chinese audio podcasts, helping users create audio content for language learning and listening practice.

## Features

- 📝 **Text Input**: Paste any text content for audio generation
- 🎙️ **High-Quality TTS**: Generate natural-sounding Chinese audio using Qwen3 TTS API
- 💾 **Local Storage**: Audio files stored on device for offline playback
- 🎵 **Audio Player**: Built-in player with progress control and playback controls
- 🌍 **Cross-Platform**: Single codebase for Android, iOS, Desktop

## Recommended Workflow

For the best experience, we recommend using Gemini web interface for translation:

1. **Translate** your content at [Gemini Web](https://gemini.google.com)
2. **Copy** the translated Chinese text
3. **Paste** into Audiofy app
4. **Generate** high-quality audio with Qwen3 TTS (accessible in China)
5. **Listen** anywhere with offline playback

> **Why this workflow?** Gemini API is blocked by China's Great Firewall, making it inaccessible for users in mainland China. By using Gemini's web interface for translation (which you can access via VPN), you can prepare your content once, then use Audiofy's Qwen3 TTS (which is accessible in China) anytime, anywhere without needing a VPN.

## Tech Stack

- **Framework**: Kotlin Multiplatform (KMP)
- **UI**: Jetpack Compose Multiplatform
- **Architecture**: MVVM + Repository Pattern
- **State Management**: Kotlin Coroutines + Flow
- **HTTP Client**: Ktor Client
- **Data Persistence**: Androidx DataStore
- **Audio**: Platform-specific native players
- **API Integration**:
  - Qwen3 TTS API (Text-to-Speech, accessible in China)

## Project Structure

```
Audiofy/
├── docs/                       # Documentation
│   └── architecture/           # Architecture Decision Records (ADRs)
│       ├── ADR-001-Tech-Stack.md
│       ├── ADR-002-Architecture-Design.md
│       └── ADR-003-Quality-Guidelines.md
├── composeApp/                 # Compose Multiplatform UI
│   ├── src/commonMain/         # Shared UI code
│   ├── src/androidMain/        # Android-specific code
│   ├── src/iosMain/           # iOS-specific code (future)
│   └── src/desktopMain/       # Desktop-specific code (future)
├── shared/                     # Shared business logic
│   ├── src/commonMain/         # Platform-independent code
│   ├── src/androidMain/        # Android-specific implementations
│   └── src/iosMain/           # iOS-specific implementations (future)
├── README.md
└── CLAUDE.md                   # Development workflow guidelines
```

## Getting Started

### Prerequisites

- **For development**:
  - JDK 17+
  - Android Studio Ladybug or later
  - Kotlin 2.1.0+
  - Gradle 8.10+

- **For running the app**:
  - Android 8.0+ (API level 26+)
  - iOS 15+ (future support)

### Installation

```bash
# Clone the repository
git clone https://github.com/persomsad/Audiofy.git
cd Audiofy

# Build Android APK
./gradlew :composeApp:assembleRelease

# Install on connected device
adb install composeApp/build/outputs/apk/release/composeApp-release.apk
```

### Configuration

Before using the app, you need to configure your Qwen3 TTS API key:

1. Open the app and tap the **Settings** icon (⚙️)
2. Enter your **Qwen3 API Key** (get it from [Alibaba Cloud](https://www.alibabacloud.com/))
3. Select your preferred **Voice** (cherry, longxiaochun, etc.)
4. Choose **Language Type** (Chinese, English, etc.)
5. Tap **Save**

## Architecture

Audiofy follows **MVVM architecture** with clear separation of concerns:

```
┌─────────────────┐
│   UI Layer      │  Jetpack Compose Multiplatform
│  (View + State) │  - InputScreen, ProcessingScreen, SettingsScreen
└────────┬────────┘
         │
┌────────▼────────┐
│  ViewModel      │  State Management
│  + Repository   │  - InputViewModel, ProcessingViewModel, SettingsViewModel
└────────┬────────┘
         │
┌────────▼────────┐
│  Data Layer     │  Data Sources
│  + Services     │  - ConfigRepository (DataStore)
│                 │  - TTSService (Qwen3 API)
└─────────────────┘
```

For detailed architecture design, see [ADR-002](./docs/architecture/ADR-002-Architecture-Design.md).

## Quality Assurance

- **Code Style**: detekt (Kotlin linter)
- **Type Safety**: Kotlin strict mode
- **Testing**: Kotlin Test (unit tests)
- **CI/CD**: GitHub Actions with 5 parallel jobs
  - Build KMP
  - Build Android
  - Detekt (static analysis)
  - Unit Tests
  - Assemble APK
- **Test Coverage Target**: >70%

See [ADR-003](./docs/architecture/ADR-003-Quality-Guidelines.md) for details.

## Development Workflow

1. Create a feature branch: `git checkout -b feature/issue-id-description`
2. Make changes and commit with conventional commits: `feat:`, `fix:`, `docs:`, etc.
3. Push to GitHub and create a Pull Request
4. Wait for CI checks to pass (all 5 jobs must succeed)
5. Merge to `main` after approval

See [CLAUDE.md](./CLAUDE.md) for detailed development guidelines.

## Release History

- **v1.1** (2025-01-15): Fixed network permissions, APK signing, and increased input limit to 20,000 characters
- **v1.0** (2025-01-14): Initial release with Gemini translation + Qwen3 TTS
- **v0.1** (2024-12-XX): MVP with basic functionality

## Contributing

This is a personal project for language learning. Contributions are welcome!

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

MIT

## Acknowledgments

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) - Cross-platform development
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI toolkit
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) - Declarative UI framework
- [Qwen3 TTS](https://www.alibabacloud.com/help/en/dashscope/developer-reference/text-to-speech-api) - High-quality Chinese text-to-speech API
- [Google Gemini](https://gemini.google.com/) - Recommended for text translation (use web interface)

## FAQ

**Q: Why not integrate Gemini API directly into the app?**
A: Gemini API is blocked by China's Great Firewall, making it inaccessible for users in mainland China. By using the web interface with a VPN for translation, users can prepare content and use Audiofy's TTS feature (which uses Qwen3, accessible in China) without needing a VPN.

**Q: Can I use this app for English text-to-speech?**
A: Yes! Qwen3 TTS supports multiple languages. Go to Settings and change the Language Type to "English".

**Q: Where can I get a Qwen3 API key?**
A: You can get an API key from [Alibaba Cloud DashScope](https://www.alibabacloud.com/help/en/dashscope/developer-reference/activate-dashscope-and-create-an-api-key).

**Q: Is my API key secure?**
A: Yes. Your API key is stored locally on your device using Androidx DataStore. It never leaves your device and is only used for API calls directly from your device to Qwen3's servers.
