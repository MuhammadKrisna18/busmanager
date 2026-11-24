# Roadmap Pengerjaan & Penyempurnaan Fitur TransNusantara

Dokumen ini memuat daftar rencana tahapan perbaikan, penyederhanaan informasi, dan penyesuaian operasional aplikasi berdasarkan evaluasi langsung:

1. **Hapus Kondisi Mesin**: Hapus atribut dan tampilan kondisi mesin (100% Mesin Sehat) di seluruh modul karena tidak ada fungsinya dalam simulasi.
2. **Penyatuan Input Tarif Tiket PP**: Penentuan tarif rute bolak-balik (misal A=Surabaya-Jember dan B=Jember-Surabaya) yang sebelumnya 2 kolom input dijadikan 1 kolom input harga untuk kedua arah.
3. **Pembersihan Spesifikasi Chassis**: Hapus informasi suspensi, tenaga kuda (HP), dan torsi pada kartu sasis di Showroom.
4. **Pembersihan Spesifikasi Bodi**: Hapus informasi tipe bus dan volume bagasi pada kartu bodi karoseri di Showroom.
5. **Hapus Biaya Tambahan Kelas saat Pembelian Bus**: Pemilihan kelas armada saat merakit bus di Showroom tidak dikenakan biaya tambahan (cukup pilih kelas saja), karena harga tiket nantinya ditentukan secara manual.
6. **Penyesuaian Kapasitas Kursi Realistis**: Kapasitas kursi penumpang disesuaikan secara realistis berdasarkan kombinasi sasis (Single Axle vs Tronton) dan bodi (Sleeper 21–24 kursi, Eksekutif 28–32 kursi, VIP 36–40 kursi, Ekonomi 44–50 kursi).
7. **Sinkronisasi Ketersediaan Sopir di Wizard**: Sopir yang sudah bertugas di armada lain diberi tanda bertugas dan tidak dapat dipilih lagi untuk bus kedua di Langkah 4 Wizard.
8. **Penyelarasan Teks Upah Sopir di Wizard**: Ubah label dari format harian (`Upah: Rp.../hari`) menjadi format bagi hasil (`Bagi Hasil: 30% per Rit Dinas`).
9. **Standardisasi Format Nama Armada**: Format nama seragam `[Merk Sasis] [Model Sasis] - [Karoseri] [Model Bodi]` di semua layar, termasuk memunculkan kembali nama bodi karoseri di kartu peta simulasi.
10. **Sinkronisasi Trayek Armada Antara Garasi & Play ➔ Bus**: Memperbaiki pembacaan rute di `SimulationMapViewModel` agar menggunakan `routeRepository.getAllRoutes()`, sehingga rute kustom yang tersimpan di bus (seperti *Situbondo - Probolinggo*) terbaca sempurna di kartu Play ➔ Bus dan tidak lagi tertulis 'Trayek: Kosong'.

---

## 📋 Pembagian Fase Pengerjaan

### ✅ Fase 1 [Medium]: Pembersihan Informasi Redundan & Sinkronisasi Trayek (Garasi vs Play->Bus) - SELESAI
- **Tujuan**: Menghapus data spesifikasi yang tidak memiliki fungsi gameplay, menyederhanakan Showroom, serta menyinkronkan rute bus antara Garasi dan Play ➔ Bus.
- **Deliverables**:
  1. **Pembersihan Spesifikasi Chassis**: Hapus informasi suspensi, tenaga (HP), dan torsi dari kartu tampilan Showroom. (Selesai)
  2. **Pembersihan Spesifikasi Bodi**: Hapus informasi tipe bodi dan volume bagasi dari kartu bodi karoseri di Showroom. (Selesai)
  3. **Hapus Biaya Tambahan Kelas**: Pemilihan kelas bus di Showroom Langkah 3 murni menentukan kelas layanan armada tanpa membebankan harga tambahan pada pembelian bus. (Selesai)
  4. **Pembersihan Kondisi Mesin**: Hapus tampilan kondisi mesin (100% Mesin Sehat) dari Showroom, Garasi, Wizard, dan BottomSheet armada. (Selesai)
  5. **Sinkronisasi Rute Kustom di Play ➔ Bus**: Menggunakan `routeRepository.getAllRoutes()` pada `SimulationMapViewModel` dan `FinanceViewModel` sehingga rute kustom terbaca di kartu armada dan tidak tertulis `Kosong`. (Selesai)
  6. **Pengujian & Git Commit Fase 1**. (Selesai)

---

### ✅ Fase 2 [Easy-Medium]: Penyatuan Input Tarif Tiket PP (1 Kolom Harga A-B & B-A) di Wizard Trayek - SELESAI
- **Tujuan**: Menyederhanakan penentuan harga tiket di Langkah 3 Wizard Matriks Tarif agar rute bolak-balik tidak membutuhkan input tarif terpisah.
- **Deliverables**:
  1. **Penyatuan Input Tarif Rute PP**: Matriks tarif menyajikan 1 baris input harga untuk pasangan pergi-pulang (`Kota A ⇄ Kota B [PP]`), mengeliminasi baris duplikat arah sebaliknya. (Selesai)
  2. **Sinkronisasi Dua Arah**: Nilai harga tiket yang diatur otomatis disimpan dan berlaku identik untuk rute pergi (A ➔ B) dan rute pulang (B ➔ A) secara konsisten di UI dan database. (Selesai)
  3. **Pengujian & Git Commit Fase 2**. (Selesai)

---

### ✅ Fase 3 [Medium]: Sinkronisasi Penugasan Sopir & Penyelarasan Skema Upah di Wizard Trayek - SELESAI
- **Tujuan**: Mencegah penugasan sopir ganda ke beberapa armada dan menyelaraskan label upah pengemudi ke skema bagi hasil 30%.
- **Deliverables**:
  1. **Proteksi Ketersediaan Sopir**:
     - Sopir yang sudah bertugas di bus lain diberi badge `Bertugas di MK-XX`, tidak dapat dipilih untuk bus lain, dan pemilihan otomatis dialihkan ke pengemudi yang siap dinas. (Selesai)
  2. **Penyelarasan Teks Skema Upah**:
     - Mengubah keterangan upah di kartu pilihan pengemudi menjadi `Bagi Hasil: 30% per Rit Dinas • Status: Siap Dinas` (atau `Sedang Bertugas`). (Selesai)
  3. **Pengujian & Git Commit Fase 3**. (Selesai)

---

### ✅ Fase 4 [Hard]: Penyesuaian Kapasitas Kursi Realistis (Chassis & Bodi) + Standardisasi Nama Armada - SELESAI
- **Tujuan**: Menghadirkan kapasitas kursi yang realistis sesuai sasis dan bodi serta menyeragamkan format penamaan bus di seluruh antarmuka aplikasi.
- **Deliverables**:
  1. **Kapasitas Kursi Realistis**:
     - Menyesuaikan kapasitas kursi penumpang berdasarkan kombinasi sasis dan model bodi:
       - `Suites / Sleeper Class` = 21–24 kursi mewah (bukan 44 kursi).
       - `Eksekutif` = 28–36 kursi.
       - `VIP / Bisnis AC` = 34–50 kursi.
       - `Ekonomi` = 48–60 kursi.
       - `Double Decker / Tronton` = Kapasitas proporsional terstandarisasi. (Selesai)
  2. **Standardisasi Format Penamaan Bus**:
     - Format seragam di seluruh layar: `[Merk Sasis] [Model Sasis] - [Karoseri] [Model Bodi]` menggunakan `bus.fullName` secara terpadu di Garasi, Peta Simulasi (`FleetOverviewBottomSheet`), Wizard Trayek, dan Manajemen Kru. (Selesai)
  3. **Pengujian & Git Commit Fase 4**. (Selesai)

---

### 🔴 Fase 5 [Medium]: QA Menyeluruh, Pembersihan Kode (Clean Code + Zero Comments) & Tag Rilis Versi Baru (v2.1.0)
- **Tujuan**: Memastikan keandalan seluruh perbaikan, verifikasi zero comments, pembersihan dead code, dan rilis versi baru.
- **Deliverables**:
  1. **Pembersihan Dead Code & Verifikasi Strict Zero Comments**.
  2. **Full Regression Testing (Seluruh Unit Test Passing)**.
  3. **Bump Versi ke v2.1.0 (VersionCode 8) & Build Signed Release APK**.
  4. **Update Dokumentasi & Publikasi Git Tag Rilis v2.1.0**.
