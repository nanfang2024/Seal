# WuHen (无痕) - Pure Client-Side Video Downloader

A modern Android application for downloading videos from popular Chinese video platforms with no watermarks, built entirely with pure client-side parsing.

## 🌟 Features

- **Pure Local Parsing**: All video parsing happens on-device without external dependencies
- **Multi-Platform Support**: 
  - Douyin (抖音)
  - Kuaishou (快手)
  - Bilibili (哔哩哔哩)
  - Xiaohongshu (小红书)
  - Pipixia (皮皮虾)
  
- **High-Quality Downloads**: Multiple quality options up to 4K/UHD
- **Background Downloads**: Queue management with pause/resume support
- **Metadata Embedding**: Title, author, thumbnail embedded in downloaded files
- **Material Design 3**: Modern UI following Material You design guidelines
- **No Watermarks**: Clean downloads without platform branding

## 🏗️ Architecture

Built using modern Android development practices:

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose + Material Design 3
- **Architecture Pattern**: MVI (Model-View-Intent)
- **Async Processing**: Kotlin Coroutines & Flow
- **Data Storage**: Room Database + DataStore Preferences
- **Dependency Injection**: Lightweight DI framework

## 📦 Technical Stack

```kotlin
// Core Dependencies
implementation("androidx.compose.material3:material3")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("androidx.room:room-ktx")
implementation("androidx.datastore:datastore-preferences")
implementation("com.tencent:mmkv")
```

## 🔧 Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 21
- Android SDK 35 (Android 15)

### Build Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/wuhen.git
   cd wuhen
   ```

2. Open in Android Studio and wait for Gradle sync to complete

3. Build debug version:
   ```bash
   ./gradlew assembleDebug
   ```

4. Install on connected device/emulator:
   ```bash
   adb install app/build/outputs/apk/generic/debug/WuHen-*-debug.apk
   ```

## 🚀 Usage

1. Paste a video URL from supported platforms
2. Select desired quality/format option
3. Start download
4. Monitor progress in Download Queue tab

## 🛡️ Privacy & Ethics

This application is designed for personal use only:
- No data collection or telemetry
- All processing done locally on your device
- Respect platform ToS and copyright laws
- Use responsibly for offline viewing of content you own rights to

## 📝 Legal Disclaimer

WARNING: This software is for educational purposes only. Downloading copyrighted content without permission may violate copyright laws in your jurisdiction. Users are responsible for complying with applicable laws and platform terms of service.

## 🤝 Contributing

Contributions are welcome! Areas of interest:
- Platform parser implementations
- Bug fixes
- UI/UX improvements
- Documentation
- Internationalization (i18n)

## 📄 License

GPLv3 License - See LICENSE file for details

## 🐛 Reporting Issues

Please report bugs through the GitHub issues page. Include:
- Android version
- App version
- Steps to reproduce
- Expected vs actual behavior

## 🔮 Roadmap

- [ ] v1.0: MVP with core parsers
- [ ] v1.1: Additional platform support
- [ ] v1.2: Advanced features (video compression, batch download)
- [ ] v2.0: Plugin system for community parsers

## 📧 Contact

For questions or suggestions, please open an issue on GitHub.

---

Made with ❤️ by the WuHen team