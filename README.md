# Bus Manager

Aplikasi Android Native modern untuk manajemen bus yang dibangun menggunakan **Kotlin** dan **Jetpack Compose**.

## 🚀 Fitur & Spesifikasi Teknis
- **Bahasa**: Kotlin 2.3+
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Arsitektur**: Modern Android Architecture (MVVM, Navigation Compose, Clean Architecture ready)
- **Dependency Management**: Gradle Version Catalog (`libs.versions.toml`)
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36 (Android 16)
- **JDK Target**: Java 17

## 📂 Struktur Project
```
busmanager/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/example/busmanager/
│   │   │   │   ├── data/             # Repository & data layer
│   │   │   │   ├── ui/               # Compose Screens, ViewModels, Themes
│   │   │   │   ├── MainActivity.kt   # Entry point
│   │   │   │   └── Navigation.kt     # App Navigation
│   │   │   └── res/                  # Drawable, mipmap, values
│   │   ├── test/                     # Unit tests
│   │   └── androidTest/              # Instrumented tests
│   └── build.gradle.kts              # Module build script
├── gradle/
│   ├── libs.versions.toml            # Version catalog
│   └── wrapper/
├── build.gradle.kts                  # Root build script
├── settings.gradle.kts
└── README.md
```

## 🛠️ Cara Menjalankan Project

### Prerequisites
- Android Studio Ladybug / Meerkat (atau versi terbaru)
- JDK 17
- Android SDK Platform 36

### Build via Command Line
```bash
# Build APK Debug
./gradlew assembleDebug

# Jalankan Unit Tests
./gradlew test
```
