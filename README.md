# TransNusantara - Bus Manager (v2.1.0)

Aplikasi game simulasi manajemen Perusahaan Otobus (PO Bus) Antar Kota Dalam Provinsi (AKDP) Jawa Timur berbasis Android Native modern yang dibangun menggunakan **Kotlin** dan **Jetpack Compose**.

---

## 🎮 Inti Permainan (Core Gameplay)

Pemain berperan sebagai pemilik dan direktur operasional PO Bus. Untuk dapat menjalankan operasional dan meraup keuntungan, pemain harus memenuhi **3 Syarat Mutlak Operasi**:
1. **Memiliki Bus** (Sasis dan Bodi pilihan sesuai kelas layanan).
2. **Memiliki Sopir** (Pengemudi aktif berlisensi yang ditugaskan ke armada).
3. **Memiliki Trayek dengan Tarif** (Rute perjalanan yang telah dibuka dan matriks harga tiket antar titik transit telah ditentukan).

Armada bus akan berdinas secara otomatis mengikuti jadwal jam keberangkatan atau dapat diberangkatkan langsung melalui kontrol operasional lapangan.

---

## 🚀 Pembaruan & Penyempurnaan Season 3 (Update v2.1.0)

Rilis v2.1.0 menghadirkan perbaikan operasional, penyederhanaan UI, dan sinkronisasi data menyeluruh:
1. **Penyatuan Input Tarif Tiket Pulang-Pergi (PP)**: Matriks tarif di Wizard Langkah 3 kini menyajikan 1 baris input harga untuk rute pergi-pulang (`Kota A ⇄ Kota B [PP]`), otomatis tersimpan dua arah.
2. **Proteksi Penugasan Sopir Ganda & Upah 30%**: Sopir yang sudah berdinas di armada lain diberi badge `Bertugas di MK-XX` dan diproteksi agar tidak bisa ditugaskan ke armada lain. Keterangan upah diselaraskan menjadi `Bagi Hasil: 30% per Rit Dinas`.
3. **Sinkronisasi Trayek Armada (Garasi vs Play Peta Simulasi)**: Rute kustom tersimpan kini terbaca sempurna di kartu Play ➔ Bus dan tidak lagi tertulis `Trayek: Kosong`.
4. **Standardisasi Nama Armada**: Format seragam di seluruh layar: `[Merk Sasis] [Model Sasis] - [Karoseri] [Model Bodi]` (Contoh: `Mercedes-Benz OC 500 RF 2542 Tronton - Tentrem Avante D2 Double Decker`).
5. **Kapasitas Kursi Realistis Sasis & Bodi**: Sleeper Suites Class disesuaikan menjadi 21–24 kursi mewah (bukan 44 kursi), Eksekutif 28–36 kursi, dan Ekonomi 48–60 kursi.
6. **Pembersihan Spesifikasi Redundan & Bebas Biaya Kelas**: Menghapus info suspensi, tenaga, dan torsi sasis serta tipe dan bagasi bodi karoseri dari Showroom, menghilangkan surcharge biaya interior saat pembelian bus, dan menghapus info kondisi mesin.

---

## 🚀 Fitur Baru Season 2 (Update Major v2.0.0)

### 1. Perakitan Bus Modular 4-Langkah (Custom Bus Builder)
- **Langkah 1 (Pilih Sasis)**: Hino (AK8, RK8, RN285, RM280), Mercedes-Benz (OH 1526, OH 1626, O 500 RS 1836, OC 500 RF 2542 Tronton), dan Scania (K360IB, K410IB Opticruise) lengkap dengan horsepower, kapasitas muatan, dan harga sasis.
- **Langkah 2 (Pilih Bodi Karoseri)**: Adiputro (Jetbus 3+ & Jetbus 5), Laksana (Legacy SR2 & SR3 Suites Class/Sleeper), dan Tentrem (Avante H8 & H9 Grand Captain) yang terintegrasi secara modular.
- **Langkah 3 (Pilih Kelas Layanan & Interior)**: Ekonomi, Bisnis AC, VIP, Eksekutif, Super Eksekutif, hingga Sleeper Suite (menentukan kapasitas kursi, kenyamanan, dan batas tarif).
- **Langkah 4 (Review & Penamaan Armada)**: Menentukan plat nomor/kode armada (misal: TN-01), ringkasan harga total sasis + bodi, dan konfirmasi transaksi pemotongan kas.

### 2. Smooth Minute-by-Minute Real-Time Clock
- Jam simulasi berjalan halus menit demi menit layaknya jam normal (`12:00 -> 12:01 -> 12:02 -> ...`).
- Kecepatan simulasi fleksibel:
  - **1x**: 1 menit game per 1 detik dunia nyata.
  - **3x**: 3 menit game per 1 detik dunia nyata.
  - **5x**: 5 menit game per 1 detik dunia nyata.

### 3. Skema Bagi Hasil Sopir 30% (Anti-Boncos) & Modal Awal 50M
- Gaji sopir dihitung otomatis sebesar **30% dari omzet tiket bus** yang dihasilkannya saat berdinas (perjalanan PP / rute).
- Perusahaan mengantongi **70% laba kotor tiket**.
- Kas perusahaan tidak lagi terpotong flat saat armada menganggur di garasi, menjamin arus kas PO bus tetap sehat dan selalu menguntungkan.
- Modal kas awal perusahaan disetel sebesar **Rp 50.000.000.000 (50 Miliar)** untuk keleluasaan pengembangan rute dan ekspansi armada.

### 4. Kedinamisan Pasar Penumpang & Naik-Turun Acak di Terminal Transit
- Okupansi penumpang di terminal awal bersifat dinamis dan acak (40% - 95%) berdasarkan kelas layanan dan reputasi perusahaan.
- Saat bus singgah di terminal transit, terjadi simulasi naik-turun acak realistis: sebagian penumpang turun dan penumpang baru naik mengisi kursi yang kosong.
- Dilengkapi proteksi kapasitas mutlak: penumpang yang naik tidak akan pernah melebihi sisa kursi kosong armada (`passengerCount <= capacity`).

### 5. Peta Multi-Rute Berwarna Unik & Mode "Track Bus" ala Google Maps
- **Garis Rute Permanen Berwarna-Warni**: Setiap koridor trayek yang sudah dibuka digambar permanen di peta Jawa Timur dengan warna garis unik (misal: Surabaya-Probolinggo, Surabaya-Jember, Surabaya-Malang).
- **Mode Track Bus Realistis**:
  - Kamera peta mengunci dan menyorot posisi bus di lapangan.
  - Jalur rute spesifik yang dilewati armada menyala tebal (*highlighted polyline navigation*).
  - **Floating ETA Callout Bubble**: Balon info melayang di dekat bus menampilkan kota tujuan, tarif tiket, dan estimasi sisa waktu tempuh (ETA) persis seperti antarmuka navigasi Google Maps.

### 6. Layar Laporan Terminal Harian & Rincian Armada (`TerminalReportScreen`)
- **Tabel Log Room Database v5 (`terminal_passenger_records`)**: Menyimpan riwayat setiap aktivitas naik-turun penumpang di seluruh terminal Jawa Timur.
- **Filter Berdasarkan Terminal & Tanggal**: Pilihan terminal kota dan navigasi hari game (`Hari Sebelumnya` / `Hari Berikutnya`).
- **Kartu Ringkasan Kinerja Terminal**: Total penumpang yang naik dan total penumpang yang turun pada hari yang dipilih.
- **Modal Dialog "🔍 Rincian Naik/Turun per Armada"**: Menampilkan rincian performa setiap armada yang singgah di terminal tersebut, mencakup jumlah trip, total penumpang naik, dan total penumpang turun.
- Terintegrasi langsung di menu dashboard utama (**Laporan Terminal**).

---

## 🌟 Fitur Manajemen & Operasional Lainnya

### Garasi Armada & Inspeksi Kesiapan 3 Syarat
- Status armada lengkap: sasis, bodi, kelas layanan, sopir penugasan, dan rute dinas aktif real-time.
- Validasi visual instan 3 syarat: Trayek, Sopir, dan Tarif.

### Penjadwalan Pulang-Pergi (A-B & B-A) & Validasi Jeda 1 Jam
- Dukungan jadwal round-trip otomatis bolak-balik dalam satu klik.
- Validasi jeda istirahat armada dan kru minimal 1 jam sebelum keberangkatan kepulangan.

### Sistem Keuangan & Layar Buku Kas (`FinanceScreen`)
- Pencatatan mutasi kas masuk (tiket penumpang) dan uang keluar (komisi sopir) secara transparan.
- Filter transaksi berdasarkan tipe, armada bus, dan rute trayek.

### Wizard Pembuatan Trayek Kustom 4-Slide
- Rangkai rute antarkota Jawa Timur, titik singgah transit, seleksi jalan tol vs arteri, matriks tarif otomatis Rp 50.000 per hop, dan penugasan armada/sopir.

---

## 📋 Roadmap Pengerjaan Season 4 (Business Rules Hardening, Data Synchronization & Offline Simulation)

Rencana perbaikan operasional, validasi business rules ketat, penambahan fitur reset & jual armada, dan simulasi latar belakang berdasarkan hasil evaluasi 10 diskusi utama (merujuk pada [FASE_PENGERJAAN.md](file:///c:/Users/Muhammad%20Krisna/Documents/Project%20Portofolio/Project%20Android%20Native/busmanager/FASE_PENGERJAAN.md)):
- ✅ **Fase 1**: Presistensi Kustomisasi Armada & Sinkronisasi Garasi (Fix Kelas & Kursi saat Beli vs Masuk Garasi).
- ✅ **Fase 2**: Pengetatan Validasi Bus & Driver di Wizard Trayek + Perapian Layout Teks Kartu Bus.
- ✅ **Fase 3**: Penyempurnaan Simulasi, Penjadwalan Standby & Pembatalan Jadwal (Finish Leg di Terminal B Lalu Stay & Origin Locked).
- ✅ **Fase 4**: Fitur Jual Armada di Garasi (Proteksi Armada Sedang Dinas) & Fitur Reset Akun (Mulai Baru).
- ✅ **Fase 5**: Penyelarasan Tanggal Laporan Terminal, BottomSheet Play Hanya Trayek Aktif & Menu Informasi Trayek di HomeScreen.
- ✅ **Fase 6**: Engine Simulasi Latar Belakang (Offline Progression Delta Time Catch-Up Saat Aplikasi Ditutup).
- ⏳ **Fase 7**: QA Menyeluruh, Verifikasi Strict Zero Comments, Bump Versi ke v2.2.0, dan Rilis APK.

---

## 📱 Spesifikasi Teknis

- **Bahasa**: Kotlin 2.3+
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Komponen Arsitektur**:
  - Clean Architecture + Domain-Driven Design (DDD)
  - Single Responsibility & SOLID Principles
  - Navigation Compose & Navigation3
  - Room Database v5 (`buses`, `company`, `drivers`, `route_pricings`, `route_schedules`, `financial_transactions`, `terminal_passenger_records`)
  - Kotlin Coroutines & Reactive StateFlow UI State
- **Versi Rilis**: v2.1.0 (Build VersionCode: 8)
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
Seluruh unit tests memverifikasi:
- Custom Bus Builder modular (sasis, karoseri, kelas, penamaan).
- Smooth minute clock & simulation ticker.
- Komisi sopir bagi hasil 30% & pencatatan keuangan.
- Kedinamisan penumpang transit & kapasitas bus.
- Palet warna multi-rute & Google Maps track bus.
- Log laporan terminal & agregasi breakdown armada.
