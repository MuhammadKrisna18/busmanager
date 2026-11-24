# TransNusantara - Bus Manager

Aplikasi game simulasi manajemen Perusahaan Otobus (PO Bus) Antar Kota Dalam Provinsi (AKDP) Jawa Timur berbasis Android Native modern yang dibangun menggunakan **Kotlin** dan **Jetpack Compose**.

---

## 🎮 Inti Permainan (Core Gameplay)

Pemain berperan sebagai pemilik dan direktur operasional PO Bus. Untuk dapat menjalankan operasional dan meraup keuntungan, pemain harus memenuhi **3 Syarat Mutlak Operasi**:
1. **Memiliki Bus** (Sasis dan Bodi pilihan sesuai kelas layanan).
2. **Memiliki Sopir** (Pengemudi aktif berlisensi yang ditugaskan ke armada).
3. **Memiliki Trayek dengan Tarif** (Rute perjalanan yang telah dibuka dan matriks harga tiket antar titik transit telah ditentukan).

Armada bus akan berdinas secara otomatis mengikuti jadwal jam keberangkatan atau dapat diberangkatkan langsung melalui kontrol operasional lapangan.

---

## 🌟 Fitur Utama

### 1. Wizard Atur Trayek 4-Slide & Custom Route Builder (New v1.4.0)
Alur pembukaan trayek dan penugasan armada bertahap dengan model **Wizard Step-by-Step 4 Slide**:
- **Slide 1 (Pilih Asal & Tujuan)**: Pemain bebas memilih Terminal Awal (Titik A) dan Terminal Akhir (Titik B) di peta Jawa Timur.
- **Slide 2 (Rangkai Jalur & Titik Singgah Bebas)**:
  - Berbasis **Graf Jaringan Jalan Jawa Timur**: Pemain bebas memilih koridor jalan (misal Pantura vs Jalur Selatan Jember).
  - Sistem memastikan rute tersambung secara kontinu tanpa terputus (tidak bisa lompat fiktif ke luar rute jalan).
  - Checkbox penentuan halte naik-turun penumpang di kota transit.
  - Pemilihan jenis jalan per segmen: **Tol Cepat** vs **Arteri (Gratis)**.
- **Slide 3 (Matriks Tarif Tiket Dinamis)**:
  - Perhitungan tiket otomatis **Rp 50.000 per lompatan kota/terminal (Hop)** untuk kedua arah perjalanan.
  - Dialog modal edit tarif fleksibel dengan tombol quick increment (+10 rb, +25 rb, -10 rb).
- **Slide 4 (Pilih Bus, Sopir, & Jam Berangkat)**:
  - Memilih armada bus yang menganggur di garasi.
  - Memilih sopir aktif berlisensi yang siap dinas.
  - Mengatur jam keberangkatan (WIB) dengan validasi anti-bentrok jadwal.
- **Tombol Utama "Jalankan & Simpan"**: Sekali klik, sistem serentak menyimpan rute baru, menyimpan daftar harga tiket, menugaskan sopir ke bus, memasang armada ke rute, mendaftarkan jadwal keberangkatan, dan armada langsung siap berdinas mencari pemasukan!

### 2. Showroom & Perakitan Bus (Custom Bus Builder)
- **Pilihan Sasis Terpercaya**:
  - **Hino**: AK8 (Front Engine), RK8 / R260, RN285 (Air Suspension), RM280 (Space Frame).
  - **Mercedes-Benz**: OH 1526, OH 1626 (Air Suspension), O 500 RS 1836, OC 500 RF 2542 (Tronton 3-Axle).
  - **Scania**: K360IB, K410IB (Opticruise 3-Axle).
- **Varian Karoseri Legendaris**:
  - **Adiputro**: Jetbus 3+ HDD/MHD/SHD, Jetbus 5 MHD/SHD/SDDE.
  - **Laksana**: Legacy SR2 & SR3 Suites Class (Sleeper) dan Panorama.
  - **Tentrem**: Avante H8 & H9 Grand Captain.
- **Kelas Layanan**: Ekonomi, Bisnis AC, VIP, Eksekutif, Super Eksekutif, hingga Sleeper Suite dengan kapasitas kursi dan harga dasar berbeda.

### 3. Manajemen Kru & Sopir (Driver Management)
- **Bursa Rekrutmen Sopir**: Jelajahi calon pengemudi dengan lisensi SIM B1 Umum atau SIM B2 Umum, level keahlian (skill), dan kondisi fisik/kelelahan.
- **Sistem Gaji & Kontrak**: Pengemudi menerima upah harian transparan yang dipotong dari kas perusahaan.
- **Penugasan Bus**: Integrasi penugasan sopir ke armada yang tersedia secara fleksibel.
- **Manajemen Karyawan**: Fitur pelepasan tugas armada dan Pemutusan Hubungan Kerja (PHK).

### 4. Penjadwalan Keberangkatan Anti-Bentrok (Route Scheduling)
- **Penjadwalan Dua Arah**: Pengaturan jam keberangkatan dari Terminal Asal ke Terminal Tujuan (A ➔ B) serta sebaliknya (B ➔ A).
- **Validasi Anti-Bentrok**: Mencegah dua armada berangkat pada jam dan menit yang sama di terminal asal yang sama untuk rute yang sama.
- **Penugasan Armada ke Jadwal**: Setiap jadwal keberangkatan dapat dipasangkan dengan armada bus yang siap dinas.

### 5. Live Dispatch & Simulasi Operasional Lapangan
- **Pemeriksaan 3 Syarat Kesiapan Armada**:
  - Status kesiapan visual pada panel armada: Trayek, Sopir, dan Tarif.
  - Label status dinamis: *Berdinas*, *Siap Beroperasi*, atau *Belum Siap Operasi* beserta rincian syarat yang belum lengkap.
- **Simulasi Perjalanan Real-Time**:
  - Visualisasi pergerakan armada bus di atas peta Jawa Timur dengan progress bar interaktif.
  - Keberangkatan otomatis saat jam simulasi mencocoki jadwal dinas bus.
  - Tombol uji operasional langsung (*Berdinas Sekarang*) untuk armada yang telah memenuhi syarat.
- **Ekonomi Tiket & Biaya Operasional**:
  - Pendapatan tiket penumpang dihitung berdasarkan rata-rata harga tarif rute dan okupansi kursi bus.
  - Biaya operasional langsung dipotong otomatis saat bus diberangkatkan (konsumsi bahan bakar solar, tarif jalan tol, dan retribusi terminal).

### 6. Finansial & Siklus Waktu Permainan
- **Kalender Game & Jam WIB**: Jam operasional 24 jam dengan kontrol kecepatan (Pause, 1x, 2x, 5x speed).
- **Buku Kas Perusahaan**: Pencatatan saldo kas, pengeluaran modal (pembelian bus, gaji kru, operasional dinas), dan pemasukan tiket penumpang.

---

## 📱 Spesifikasi Teknis

- **Bahasa**: Kotlin 2.3+
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Komponen Arsitektur**:
  - Navigation Compose & Navigation3
  - Room Database v3 (Entity: Bus, Company, Driver, RoutePricing, RouteSchedule)
  - Kotlin Coroutines & Flow (Reactive StateFlow UI State)
- **Versi Rilis**: v1.4.0 (Build VersionCode: 5)
- **Min SDK**: API 24 (Android 7.0 Nougat)
- **Target SDK**: API 36 (Android 16)
- **Java Compatibility**: JDK 17

---

## 🛠️ Panduan Build & Pengujian

### Build APK Release (Signed)
```bash
./gradlew assembleRelease
```
Berkas APK release siap instal berada di:
`app/build/outputs/apk/release/app-release.apk`

### Menjalankan Unit Tests
```bash
./gradlew testDebugUnitTest
```
Seluruh 179+ unit test memverifikasi use case finansial, showroom bus, manajemen supir, validasi jadwal anti-bentrok, graf jaringan jalan, custom route builder, kalkulasi matriks tarif, dan dispatcher operasional.
