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

---

## 📋 Pembagian Fase Pengerjaan

### 🟢 Fase 1 [Medium]: Pembersihan Informasi Redundan & Penyederhanaan Showroom (Chassis, Bodi, & Kelas)
- **Tujuan**: Menghapus data spesifikasi yang tidak memiliki fungsi gameplay dan menyederhanakan langkah pemilihan kelas di Showroom.
- **Deliverables**:
  1. **Pembersihan Spesifikasi Chassis**: Hapus informasi suspensi, tenaga (HP), dan torsi dari model data sasis serta kartu tampilan Showroom.
  2. **Pembersihan Spesifikasi Bodi**: Hapus informasi tipe bodi dan volume bagasi dari model data bodi serta kartu tampilan Showroom.
  3. **Hapus Biaya Tambahan Kelas**: Pemilihan kelas bus di Showroom Langkah 3 murni menentukan kelas layanan armada tanpa membebankan harga tambahan pada pembelian bus.
  4. **Pembersihan Kondisi Mesin**: Hapus tampilan & atribut kondisi mesin (100% Mesin Sehat) dari Showroom, Garasi, Wizard, dan model Bus.
  5. **Pengujian & Git Commit Fase 1**.

---

### 🟡 Fase 2 [Easy-Medium]: Penyatuan Input Tarif Tiket PP (1 Kolom Harga A-B & B-A) di Wizard Trayek
- **Tujuan**: Menyederhanakan penentuan harga tiket di Langkah 3 Wizard Matriks Tarif agar rute bolak-balik tidak membutuhkan input tarif terpisah.
- **Deliverables**:
  1. **Penyatuan Input Tarif Rute PP**: Saat membuat trayek bolak-balik (Round-Trip), matriks tarif menyajikan 1 baris input harga untuk pasangan pergi-pulang (misal `Surabaya ⇄ Jember: Rp [Input]`).
  2. **Sinkronisasi Dua Arah**: Nilai harga tiket yang diisi pemain otomatis disimpan dan berlaku identik untuk rute pergi (A ➔ B) dan rute pulang (B ➔ A).
  3. **Pengujian & Git Commit Fase 2**.

---

### 🟠 Fase 3 [Medium]: Sinkronisasi Penugasan Sopir & Penyelarasan Skema Upah di Wizard Trayek
- **Tujuan**: Mencegah penugasan sopir ganda ke beberapa armada dan menyelaraskan label upah pengemudi ke skema bagi hasil 30%.
- **Deliverables**:
  1. **Proteksi Ketersediaan Sopir**:
     - Sopir yang sudah bertugas di bus lain diberi badge `Bertugas di MK-XX` dan dinonaktifkan/difilter dari daftar pengemudi yang siap dinas di Langkah 4 Wizard.
  2. **Penyelarasan Teks Skema Upah**:
     - Mengubah keterangan upah di kartu pilihan pengemudi dari `Upah: Rp.../hari` menjadi `Bagi Hasil: 30% per Rit Dinas`.
  3. **Pengujian & Git Commit Fase 3**.

---

### 🟣 Fase 4 [Hard]: Penyesuaian Kapasitas Kursi Realistis (Chassis & Bodi) + Standardisasi Nama Armada
- **Tujuan**: Menghadirkan kapasitas kursi yang realistis sesuai sasis dan bodi serta menyeragamkan format penamaan bus di seluruh antarmuka aplikasi.
- **Deliverables**:
  1. **Kapasitas Kursi Realistis**:
     - Menyesuaikan kapasitas kursi penumpang berdasarkan kombinasi sasis dan model bodi:
       - `Suites / Sleeper Class` = 21–24 kursi mewah (bukan 44 kursi).
       - `Eksekutif` = 28–32 kursi.
       - `VIP / Bisnis AC` = 36–40 kursi.
       - `Ekonomi` = 44–50 kursi.
       - `Double Decker / Tronton` = Kapasitas bertingkat proporsional.
  2. **Standardisasi Format Penamaan Bus**:
     - Format seragam di seluruh layar: `[Merk Sasis] [Model Sasis] - [Karoseri] [Model Bodi]` (Contoh: `Mercedes-Benz OC 500 RF 2542 Tronton - Tentrem Avante D2 Double Decker`).
     - Memperbaiki Bottom Sheet Peta Simulasi (`FleetOverviewBottomSheet`) agar nama bodi karoseri (Adiputro Jetbus 5, Tentrem Avante D2, Laksana Legacy SR3) tidak hilang.
  3. **Pengujian & Git Commit Fase 4**.

---

### 🔴 Fase 5 [Medium]: QA Menyeluruh, Pembersihan Kode (Clean Code + Zero Comments) & Tag Rilis Versi Baru (v2.1.0)
- **Tujuan**: Memastikan keandalan seluruh perbaikan, verifikasi zero comments, pembersihan dead code, dan rilis versi baru.
- **Deliverables**:
  1. **Pembersihan Dead Code & Verifikasi Strict Zero Comments**.
  2. **Full Regression Testing (Seluruh Unit Test Passing)**.
  3. **Bump Versi ke v2.1.0 (VersionCode 8) & Build Signed Release APK**.
  4. **Update Dokumentasi & Publikasi Git Tag Rilis v2.1.0**.
