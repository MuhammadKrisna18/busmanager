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

### 🔹 Fase 7 [Final]: QA Menyeluruh, Strict Zero Comments, Bump Versi v2.6.0 & Rilis APK
- [ ] Audit zero comments (`0` komentar di seluruh file Kotlin).
- [ ] Full regression testing seluruh unit test (100% passing).
- [ ] Bump versi ke `v2.6.0` (versionCode 13), build signed release APK (`app-release.apk`), dan publikasi Git Tag Rilis `v2.6.0`.
