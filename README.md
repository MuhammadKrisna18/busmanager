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

## 📂 Arsitektur Project (Clean Architecture + DDD)

Project ini menggunakan prinsip **Clean Architecture** dipadukan dengan **Domain-Driven Design (DDD)** yang dipecah berdasarkan *Bounded Contexts* game simulasi:

```
busmanager/
└── app/src/main/kotlin/com/example/busmanager/
    ├── core/                         # Cross-cutting concerns & utilitas umum
    │   ├── base/                     # Base classes (UiState, BaseViewModel, Result)
    │   ├── dispatcher/               # Coroutine Dispatcher providers
    │   └── util/                     # Formatters (mata uang, tanggal), Math, GameTickHelper
    │
    ├── domain/                       # Pure Kotlin (Tanpa ketergantungan Android SDK)
    │   ├── company/                  # [Bounded Context] Finansial, reputasi, level PO bus
    │   │   ├── model/
    │   │   ├── repository/
    │   │   └── usecase/
    │   ├── fleet/                    # [Bounded Context] Armada bus (beli, servis, modifikasi)
    │   │   ├── model/
    │   │   ├── repository/
    │   │   └── usecase/
    │   ├── route/                    # [Bounded Context] Jalur rute, terminal, tarif tiket
    │   │   ├── model/
    │   │   ├── repository/
    │   │   └── usecase/
    │   ├── staff/                    # [Bounded Context] Karyawan (supir, mekanik, gaji)
    │   │   ├── model/
    │   │   ├── repository/
    │   │   └── usecase/
    │   └── simulation/               # [Bounded Context] Game loop, waktu, penumpang, event acak
    │       ├── model/
    │       ├── repository/
    │       └── usecase/
    │
    ├── data/                         # Implementasi penyimpanan data & save game
    │   ├── local/
    │   │   ├── database/             # Room Database
    │   │   ├── dao/                  # Data Access Objects
    │   │   ├── entity/               # Room Entities (tabel database)
    │   │   └── datastore/            # Preferences & Settings lokal
    │   ├── repository/               # Implementasi Domain Repository
    │   └── mapper/                   # Mapper Entity <-> Domain Model
    │
    ├── presentation/                 # UI Layer (Jetpack Compose, ViewModels)
    │   ├── common/                   # Komponen UI global (StatusBar Game, Dialog, Card)
    │   ├── navigation/               # Navigasi Compose
    │   ├── dashboard/                # Layar utama overview perusahaan & kontrol waktu
    │   ├── fleet/                    # Manajemen & pembelian bus
    │   ├── route/                    # Manajemen rute & jadwal
    │   ├── staff/                    # Rekrutmen supir & mekanik
    │   ├── finance/                  # Laporan kas & pinjaman bank
    │   └── settings/                 # Pengaturan game & save/load
    │
    ├── di/                           # Dependency Injection modules (Hilt/Koin)
    ├── MainActivity.kt
    └── ui/theme/                     # Color, Typography, Material Theme
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
