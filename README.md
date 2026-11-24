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

## 🌟 Fitur Utama (Update v1.5.0)

### 1. Garasi Armada & Inspeksi Kesiapan 3 Syarat (New v1.5.0)
- **Status Lengkap Armada**: Memantau daftar seluruh bus yang dimiliki lengkap dengan informasi sasis, bodi, kelas layanan, kondisi mesin, sopir yang bertugas, serta rute aktif atau status standby.
- **Pemeriksaan 3 Syarat Kesiapan Operasi**:
  - Validasi visual instan untuk **Trayek**, **Sopir**, dan **Tarif**.
  - Indikator status dinamis: *Siap Beroperasi* vs *Belum Siap Operasi*.
- **Aksi Cepat Navigasi**: Tombol cepat untuk menambah armada baru via Showroom atau membuka trayek baru via Wizard Trayek.

### 2. Penjadwalan Pulang-Pergi (A-B & B-A) & Validasi Jeda 1 Jam (New v1.5.0)
- **Dukungan Round-Trip Otomatis**: Opsi mengaktifkan jadwal dinas bolak-balik (Asal ➔ Tujuan dan Tujuan ➔ Asal) dalam satu langkah pembuatan trayek.
- **Kalkulasi Estimasi Waktu Tiba**: Sistem menghitung estimasi waktu tempuh berdasarkan jarak kilometer dan kecepatan rata-rata rute.
- **Validasi Jeda Minimum 1 Jam**: Memastikan armada dan kru memiliki jeda istirahat minimal 1 jam setelah tiba di terminal tujuan sebelum dapat diberangkatkan kembali untuk rute kepulangan (B ➔ A).

### 3. Kontrol Waktu Dinamis Lapangan (New v1.5.0)
- **Kontrol Play & Jeda (Pause)**: Pengendali jam operasional simulasi (WIB) yang responsif.
- **Pilihan Kecepatan Ticker Waktu Real-Time**:
  - **1x (Normal)**: 1 menit game per 1 detik dunia nyata.
  - **3x (Cepat)**: 3 menit game per 1 detik dunia nyata.
  - **5x (Sangat Cepat)**: 5 menit game per 1 detik dunia nyata.
- **Pemberangkatan Terjadwal Otomatis**: Armada otomatis bergerak berdinas ketika waktu simulasi menyentuh jadwal keberangkatan bus.

### 4. Sistem Keuangan Dinamis & Layar Buku Kas (FinanceScreen) (New v1.5.0)
- **Model Transaksi Keuangan (Room DB v4)**: Pencatatan mutasi kas masuk dan keluar secara mendetail.
  - **Uang Masuk**: Akumulasi tiket penumpang saat armada berdinas berdasarkan kapasitas dan tarif rute.
  - **Uang Keluar**: Gaji harian pengemudi aktif yang dipotong otomatis setiap pergantian hari game (00:00 WIB).
- **Kartu Ringkasan Finansial**: Total Kas Perusahaan, Total Pemasukan, Total Pengeluaran Gaji, dan Laba Bersih (*Net Profit*).
- **Filter Fleksibel 3 Lapis**:
  - Filter Tipe Transaksi: Semua, Pemasukan (`+Rp`), atau Pengeluaran (`-Rp`).
  - Filter per Armada Bus: Semua Armada atau spesifik unit bus.
  - Filter per Rute: Semua Rute atau spesifik koridor trayek.

### 5. Panel Interaktif Bus & Pelacakan Kamera Lapangan ("Track Bus") (New v1.5.0)
- **Panel Armada Sedang Jalan**:
  - Menampilkan bus yang aktif berdinas di jalan raya Jawa Timur.
  - Informasi mendalam: Total Kursi, Sisa Kursi Kosong, Okupansi Penumpang, Nama Trayek, Posisi Progres (%), dan Tarif Tiket per Penumpang.
- **Fitur "🎯 Track Bus"**:
  - Sekali klik, kamera lapangan langsung mengunci dan menyorot posisi bus di peta Jawa Timur.
  - Indikator visual marker bergerak dengan animasi halo sorot emas/merah dan badge pelacakan khusus.
  - Kartu pemantauan live di bagian bawah layar dengan progress bar waktu tempuh dan tombol lepas pelacakan.

### 6. Panel Interaktif Trayek & Koridor Operasi (New v1.5.0)
- **Informasi Lengkap Koridor A ➔ B**: Kota asal, kota tujuan, jenis jalan (Jalur Tol Cepat vs Arteri Non-Tol), jarak kilometer, dan estimasi durasi.
- **Tarif Tiket Rute**: Menampilkan besaran tarif tiket penumpang yang berlaku pada rute tersebut.
- **Daftar Armada Pengisi Trayek**: Memperlihatkan secara transparan bus mana saja yang ditugaskan di trayek tersebut beserta status operasionalnya (*Sedang Jalan* atau *Standby*).

### 7. Wizard Atur Trayek 4-Slide & Custom Route Builder
- **Slide 1 (Pilih Asal & Tujuan)**: Pemain bebas memilih Terminal Awal (Titik A) dan Terminal Akhir (Titik B) di peta Jawa Timur.
- **Slide 2 (Rangkai Jalur & Titik Singgah Bebas)**: Graf jaringan jalan Jawa Timur yang kontinu, halte transit, dan seleksi jenis jalan (Tol vs Non-Tol).
- **Slide 3 (Matriks Tarif Tiket Dinamis)**: Perhitungan tiket otomatis Rp 50.000 per hop dan dialog modal edit tarif fleksibel.
- **Slide 4 (Pilih Bus, Sopir, & Jam Berangkat)**: Integrasi bus, sopir, dan penjadwalan anti-bentrok.

### 8. Showroom & Perakitan Bus (Custom Bus Builder)
- **Pilihan Sasis Terpercaya**: Hino (AK8, RK8, RN285, RM280), Mercedes-Benz (OH 1526, OH 1626, O 500 RS 1836, OC 500 RF 2542 Tronton), Scania (K360IB, K410IB Opticruise).
- **Varian Karoseri Legendaris**: Adiputro (Jetbus 3+ & Jetbus 5), Laksana (Legacy SR2 & SR3 Suites Class/Sleeper), Tentrem (Avante H8 & H9 Grand Captain).
- **Kelas Layanan**: Ekonomi, Bisnis AC, VIP, Eksekutif, Super Eksekutif, hingga Sleeper Suite.

### 9. Manajemen Kru & Sopir (Driver Management)
- Rekrutmen sopir berlisensi SIM B1 / B2 Umum, level keahlian (*skill*), kondisi stamina, sistem gaji harian, dan mutasi tugas armada.

---

## 📱 Spesifikasi Teknis

- **Bahasa**: Kotlin 2.3+
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Komponen Arsitektur**:
  - Clean Architecture + Domain-Driven Design (DDD)
  - Navigation Compose & Navigation3
  - Room Database v4 (Entity: Bus, Company, Driver, RoutePricing, RouteSchedule, FinancialTransaction)
  - Kotlin Coroutines & Flow (Reactive StateFlow UI State)
- **Versi Rilis**: v1.5.0 (Build VersionCode: 6)
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
Seluruh 205+ unit test memverifikasi use case finansial, showroom bus, manajemen supir, validasi jadwal round-trip, graf jaringan jalan, custom route builder, kalkulasi matriks tarif, dan dispatcher operasional dengan live camera tracking.
