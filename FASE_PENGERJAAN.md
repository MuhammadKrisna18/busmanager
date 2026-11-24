# Roadmap Pengerjaan Season 8: Penugasan Cepat Trayek, Perbaikan Jalur PP, Navigasi Peta Bebas & Ekspansi Jalur Selatan

Dokumen ini memuat perencanaan rinci perbaikan aplikasi **TransNusantara (Bus Manager)** yang dipecah menjadi **7 fase ramping** agar pengerjaan fokus, cepat selesai, dan mudah diverifikasi:

---

## 📌 Rangkuman 4 Poin Diskusi Utama

1. **Diskusi 1:** Penugasan cepat armada ke trayek aktif langsung dari Garasi & HomeScreen dengan validasi anti-konflik jadwal (dilarang jam, asal, tujuan, dan koridor sama persis).
2. **Diskusi 2:** Perbaikan bug perjalanan bolak-balik: armada berjalan A ➔ B ➔ A ➔ B secara terus-menerus tanpa glitch posisi meloncat reset.
3. **Diskusi 3:** Fitur zoom (*pinch* & tombol `[+]`/`[-]`) dan geser bebas (*pan drag*) di peta simulasi.
4. **Diskusi 4:** Ekspansi peta & koridor rute baru ke wilayah Mojokerto, Kediri, Tulungagung, dan Blitar yang tersambung kembali ke Malang.

---

## 📋 Pembagian 7 Fase Pengerjaan Ramping (Season 8)

### 🔹 Fase 1 [Kritis]: Perbaikan Logika Perjalanan Pulang-Pergi (A ➔ B ➔ A ➔ B) [SELESAI]
- [x] Memperbaiki pergantian arah rute bolak-balik (`ORIGIN_TO_DESTINATION` ⇄ `DESTINATION_TO_ORIGIN`) saat jeda *headway* selesai di terminal tujuan.
- [x] Memastikan armada parkir di terminal yang tepat dan menyusuri polyline jalur secara terbalik tanpa glitch posisi meloncat reset.
- [x] Menerapkan jeda keberangkatan (*headway*) dan waktu berhenti ampiran (*transit dwell*) secara presisi sesuai input konfigurasi pengguna.
- [x] Pengujian unit test logika perputaran armada, verifikasi jalan bolak-balik, dan Git Commit Fase 1.

---

### 🔹 Fase 2 [Interaksi Peta]: Gestur Zoom (Pinch) & Geser Bebas (Pan) di Peta Simulasi [SELESAI]
- [x] Menambahkan gestur *Pinch-to-Zoom* dan *Drag-to-Pan* pada `EastJavaMapCanvas`.
- [x] Menambahkan kontrol tombol UI zoom `[+]`, `[-]`, tombol fokus armada `[🎯]`, dan tombol `[↺ Reset]` di layar peta.
- [x] Integrasi transisi kamera yang mulus antara navigasi bebas (pan/zoom) dan mode kuncian kamera (*Track Armada*).
- [x] Verifikasi navigasi peta, unit test, dan Git Commit Fase 2.

---

### 🔹 Fase 3 [Data Konten]: Pendaftaran 4 Terminal Baru & Koridor Jalur Selatan [SELESAI]
- [x] Menambahkan 4 Terminal Tipe A baru di `TerminalDataSource`: Terminal Kertajaya (Mojokerto), Terminal Tamanan (Kediri), Terminal Gayatri (Tulungagung), dan Terminal Patria (Blitar).
- [x] Menambahkan segmen koridor jalan tol & arteri baru di `RouteDataSource` (Surabaya – Mojokerto – Kediri – Tulungagung – Blitar – Malang).
- [x] Pengujian unit test integritas data terminal dan rute baru, lalu Git Commit Fase 3.

---

### 🔹 Fase 4 [Visual Peta]: Visualisasi Titik Kota Baru & Integrasi Wizard Rute [SELESAI]
- [x] Memperbarui kanvas peta `EastJavaMapCanvas` untuk menampilkan titik kota dan segmen jalur baru Mojokerto, Kediri, Tulungagung, dan Blitar serta penyesuaian kontur daratan barat.
- [x] Menurunkan durasi transisi layar dan animasi pergerakan kamera peta menjadi 0.5 detik (500 ms) agar cepat dan responsif.
- [x] Memastikan rute baru langsung dapat dirancang di Wizard Rute dengan estimasi jarak, durasi, dan matriks tarif otomatis beserta pengujian domain use case rute jalur selatan.
- [x] Bump versi ke `v2.6.0` (versionCode 13), build signed release APK (`app-release.apk`), dan publikasi rilis `v2.6.0`.

---

### 🔹 Fase 5 [Domain Logic]: Aturan Bisnis & Validasi Anti-Konflik Jadwal Keberangkatan [SELESAI]
- [x] Membuat domain validator / use case pengecekan jadwal armada di trayek yang sama (`ValidateScheduleConflictUseCase`).
- [x] Menerapkan aturan: menolak jika Asal, Tujuan, Koridor, dan Jam/Menit keberangkatan sama persis dengan armada lain di trayek tersebut (wajib minimal 1 berbeda).
- [x] Menulis unit test bisnis untuk seluruh skenario validasi jadwal (`ValidateScheduleConflictUseCaseTest`), lalu Git Commit Fase 5.

---

### 🔹 Fase 6 [UI & Alur]: Dialog Penugasan Cepat di Garasi & Ringkasan Trayek di HomeScreen [SELESAI]
- [x] Menambahkan tombol aksi `[⚡ Tugaskan ke Trayek]` pada kartu bus di Garasi Armada.
- [x] Membuat modal dialog penugasan cepat: memilih trayek aktif, memilih sopir siap dinas, dan mengatur jam keberangkatan dengan validasi anti-konflik real-time.
- [x] Menampilkan ringkasan status trayek aktif yang sedang beroperasi di menu HomeScreen (`ActiveRoutesSummarySection`).
- [x] Verifikasi alur UI penugasan cepat di Garasi dan HomeScreen, lalu Git Commit Fase 6.

---

### 🔹 Fase 7 [Final]: QA Menyeluruh, Strict Zero Comments, Bump Versi v2.7.0 & Rilis APK [SELESAI]
- [x] Audit zero comments (`0` komentar di seluruh file Kotlin proyek).
- [x] Full regression testing seluruh unit test (100% passing).
- [x] Bump versi ke `v2.7.0` (versionCode 14), build signed release APK (`app-release.apk`), dan publikasi Git Tag Rilis `v2.7.0`.

---

# Roadmap Pengerjaan Season 9: Decoupled Inventory, Smart Headway Buffer, Route Pricing Fix & 2.5D Isometric Map Overhaul

Dokumen ini memuat susunan pengerjaan terperinci untuk Season 9 yang dipecah menjadi **5 fase modular dan terukur**:

---

## 📌 Rangkuman 4 Poin Utama Season 9
1. **Poin 1 (Jeda Keberangkatan Semi):** Selisih waktu keberangkatan armada di trayek, koridor, dan tujuan yang sama minimal 30 menit (selisih jarak waktu >= 30 menit), dan user dapat menambah kelipatan 10 menit (30, 40, 50, dst.).
2. **Poin 2 (Decoupled Inventory):** Pembuatan trayek baru tidak mewajibkan bus dan sopir (bisa disimpan sebagai trayek standby). Pembelian bus di showroom juga mandiri (IDLE di garasi). Aktivasi operasional dilakukan saat ketiganya ditugaskan.
3. **Poin 3 (Fix Dropdown Tarif Tiket):** Memperbaiki bug dropdown daftar tarif tiket di Info Trayek yang langsung tertutup saat simulasi berjalan akibat reset state flow.
4. **Poin 4 & 5 (Perombakan Peta, Jalur & Visual Armada 2.5D Isometrik):** Garis jalur tol & non-tol lebih kontras, anti-overlap marker bus, rendering armada 2.5D isometrik dengan rotasi arah rute, serta indikator cincin warna status (Hijau = Jalan, Kuning/Oranye = Ampiran, Biru/Ungu = Terminal A/B).

---

## 📋 Pembagian Fase Pengerjaan (Season 9)

### 🔹 Fase 1 [Bugfix & Domain]: Fix Dropdown Tarif Tiket & Validasi Jeda Keberangkatan Minimal 30 Menit (Kelipatan 10) [SELESAI]
- [x] Memperbaiki `RouteInfoViewModel` agar status `isPricingsExpanded` tersimpan independen (tidak ter-reset saat ada emisi update berkala use case).
- [x] Memperbarui `ValidateScheduleConflictUseCase` untuk memvalidasi selisih waktu keberangkatan minimal 30 menit jika asal, tujuan, dan koridor sama persis.
- [x] Menambahkan opsi pemilihan jeda keberangkatan kelipatan 10 menit (30, 40, 50, 60, dst.) pada dialog penugasan armada.
- [x] Menulis/memperbarui unit test business rule jadwal, verifikasi bug dropdown tarif, lalu Git Commit Fase 1.

---

### 🔹 Fase 2 [Alur & Inventory]: Pembuatan Trayek Standby Tanpa Wajib Bus & Sopir (Decoupled Inventory) [SELESAI]
- [x] Memperbarui `RouteWizard`: membuat penugasan bus dan sopir menjadi opsional dengan opsi tombol *"Simpan Trayek Saja (Standby)"* setelah matriks tarif selesai ditentukan.
- [x] Memastikan trayek yang tersimpan tanpa bus berstatus Standby/Non-aktif secara aman di database Room dan muncul di daftar Info Trayek.
- [x] Memverifikasi alur operasional: trayek standby dapat langsung diaktifkan melalui aksi penugasan cepat dari Garasi.
- [x] Pengujian unit test use case pembuatan trayek mandiri, verifikasi alur UI, lalu Git Commit Fase 2.

---

### 🔹 Fase 3 [Visual Peta & Jalur]: Perombakan Garis Rute (Tol & Non-Tol) & Algoritma Anti-Overlap Marker [SELESAI]
- [x] Memperbarui rendering garis lintasan rute di `EastJavaMapCanvas`: Jalur Tol menggunakan warna Emas/Oranye Kontras, Jalur Arteri/Non-Tol menggunakan warna Biru Elektrik/Toska.
- [x] Mengimplementasikan algoritma penyesuaian posisi visual (anti-overlap / dynamic offset) untuk armada-armada yang sedang berada di terminal atau segmen jalan yang sama agar tidak saling menutupi.
- [x] Verifikasi tampilan garis jalur dan marker di peta simulasi, lalu Git Commit Fase 3.

---

### 🔹 Fase 4 [Armada 2.5D & Indikator Warna]: Rendering Bus 3D Isometrik & Cincin Status Perjalanan
- [ ] Mengimplementasikan rendering armada 2.5D Isometrik pada Canvas (atap ber-AC, bodi bus bersudut, kaca depan/samping, dan bayangan dinamis di aspal) yang berotasi otomatis mengikuti sudut lintasan (*heading angle*).
- [ ] Menerapkan cincin indikator warna status armada secara real-time:
  - 🟢 **Hijau Neon:** Sedang bergerak melayani rute (*In Transit*).
  - 🟡 **Kuning / Oranye:** Sedang berhenti di terminal ampiran (*Transit Dwell*).
  - 🔵 **Biru / Ungu:** Sedang berada di terminal utama A / B (*Peron / Layover*).
- [ ] Verifikasi performa rendering 60 FPS, kelancaran rotasi armada, dan Git Commit Fase 4.

---

### 🔹 Fase 5 [Final]: QA Menyeluruh, Strict Zero Comments, Bump Versi Baru & Rilis APK
- [ ] Full regression testing seluruh unit tests business rules (memastikan 100% lulus).
- [ ] Audit zero comments (`0` komentar di seluruh file Kotlin proyek).
- [ ] Bump versi aplikasi, generate signed release APK, buat Git Tag rilis baru, dan push ke GitHub Releases.
