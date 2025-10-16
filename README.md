# Audiofy

> AI-powered English-to-Chinese audio podcast generator for language learning

Audiofy is a mobile application that translates English articles into Chinese audio podcasts, helping users improve their language skills through listening.

## Features

- 📝 **Text Input**: Paste English articles for translation
- 🌐 **AI Translation**: Powered by Google Gemini API for accurate English-to-Chinese translation
- 🎙️ **High-Quality TTS**: Generate natural-sounding Chinese audio using Doubao TTS API
- 💾 **Local Storage**: Audio files stored on device for offline playback
- 🎵 **Audio Player**: Built-in player with progress control and background playback

## Tech Stack

- **Framework**: NativeScript 8 + Vue 3 + TypeScript
- **UI Components**: NativeScript Core Components
- **State Management**: Pinia
- **HTTP Client**: ofetch
- **Audio**: nativescript-audio
- **API Integration**:
  - Google Gemini API (Translation)
  - Doubao TTS API (Text-to-Speech)
  - Cloudflare Workers (API Proxy for Security)

## Project Structure

```
Audiofy/
├── docs/                      # Documentation
│   └── architecture/          # Architecture Decision Records (ADRs)
│       ├── ADR-001-Tech-Stack.md
│       ├── ADR-002-Architecture-Design.md
│       └── ADR-003-Quality-Guidelines.md
├── src/                       # Source code (will be created after NativeScript init)
├── README.md
└── CLAUDE.md                  # Development workflow guidelines
```

## Development Status

🚧 **Project in Day 0 Phase**

- [x] Technology stack decision (ADR-001)
- [x] Architecture design (ADR-002)
- [x] Quality guidelines (ADR-003)
- [ ] NativeScript project initialization
- [ ] Configure ESLint, Prettier, Git Hooks
- [ ] Implement core features

## Getting Started

### Prerequisites

- Node.js 20+
- NativeScript CLI 8+
- Xcode 15+ (for iOS development)
- Android Studio (for Android development)

### Installation

```bash
# Install NativeScript CLI globally
npm install -g @nativescript/cli

# Install dependencies (after project initialization)
npm install

# Run on iOS simulator
ns run ios

# Run on Android emulator
ns run android
```

## Architecture

Audiofy follows a **three-layer architecture**:

1. **Presentation Layer**: Vue 3 components
2. **Business Logic Layer**: Composables (services)
3. **Data Access Layer**: NativeScript APIs and third-party plugins

API keys are securely managed through Cloudflare Workers to prevent exposure in the mobile app.

For detailed architecture design, see [ADR-002](./docs/architecture/ADR-002-Architecture-Design.md).

## Quality Assurance

- **Code Style**: ESLint + Prettier
- **Type Safety**: TypeScript strict mode
- **Testing**: Vitest (unit tests), Appium (E2E tests)
- **CI/CD**: GitHub Actions
- **Test Coverage Target**: >70%

See [ADR-003](./docs/architecture/ADR-003-Quality-Guidelines.md) for details.

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

- [NativeScript](https://nativescript.org/) - Cross-platform mobile framework
- [Vue.js](https://vuejs.org/) - Progressive JavaScript framework
- [Google Gemini](https://ai.google.dev/) - AI translation API
- [Doubao TTS](https://www.volcengine.com/docs/6561/97465) - Text-to-Speech API
