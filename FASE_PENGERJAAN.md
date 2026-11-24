# Roadmap Pengerjaan Season 8: Penugasan Cepat Trayek, Perbaikan Jalur PP, Navigasi Peta Bebas & Ekspansi Jalur Selatan

Dokumen ini memuat detail perencanaan perbaikan logika perputaran armada, penugasan armada tanpa bikin rute baru, navigasi zoom/geser peta, serta ekspansi terminal dan koridor baru ke wilayah **Mojokerto, Kediri, Tulungagung, dan Blitar** pada aplikasi **TransNusantara (Bus Manager)**:

---

## 📌 Rangkuman 4 Poin Diskusi & Solusi Arsitektural

1. **Diskusi 1: Penugasan Cepat Armada ke Trayek Aktif (Garasi & HomeScreen) dengan Validasi Anti-Konflik Jadwal**
   - **Kebutuhan:** Memudahkan pemain menugaskan bus baru/nganggur langsung ke rute yang sudah pernah dibuka tanpa harus melalui 4 langkah Wizard Rute dari awal.
   - **Solusi:**
     - Menambahkan tombol aksi `[Tugaskan ke Trayek]` pada kartu bus di Garasi Armada.
     - Membuka dialog penugasan: pemain memilih trayek aktif yang sudah ada, memilih sopir siap dinas, dan mengatur jam keberangkatan awal serta jeda *headway*.
     - **Aturan Bisnis Anti-Konflik (Schedule Conflict Validation):**
       - Sistem memeriksa jadwal armada lain pada trayek yang sama: jika ada 2 armada dengan Terminal Asal sama, Tujuan sama, Koridor sama, DAN Jam/Menit keberangkatan sama, maka sistem **MENOLAK** penugasan dan meminta pemain membedakan jam keberangkatan atau arahnya.
     - Menampilkan ringkasan trayek aktif di HomeScreen beserta tombol cepat penugasan.

2. **Diskusi 2: Perbaikan Bug Perputaran Armada Pulang-Pergi (A ➔ B ➔ A ➔ B)**
   - **Masalah:** Saat armada tiba di terminal akhir (B), pada rit keberangkatan berikutnya posisi bus mendadak meloncat reset kembali ke terminal asal (A) dan berjalan A ➔ B lagi, bukan berjalan balik menyusuri rute B ➔ A.
   - **Solusi:**
     - Memperbaiki alur pergantian arah rute (*state machine alternation*): ketika perjalanan `ORIGIN_TO_DESTINATION` (A ➔ B) selesai, armada parkir istirahat di terminal B sesuai durasi jeda (*headway*), lalu diberangkatkan dengan arah `DESTINATION_TO_ORIGIN` menyusuri titik polyline terbalik (B ➔ A).
     - Setelah tiba di A, bus beristirahat di A lalu berangkat lagi A ➔ B secara terus-menerus tanpa ada glitch posisi melompat.

3. **Diskusi 3: Fitur Zoom & Geser Bebas (Pan) pada Peta Simulasi**
   - **Kebutuhan:** Memungkinkan pemain menggeser peta ke segala arah (kanan, kiri, atas, bawah) dan memperbesar/memperkecil tampilan peta.
   - **Solusi:**
     - Mengimplementasikan gestur transformatif (*Pinch to Zoom* & *Drag to Pan*) pada kanvas peta `EastJavaMapCanvas`.
     - Menyediakan kontrol navigasi interaktif di layar peta: tombol zoom-in `[+]`, tombol zoom-out `[-]`, dan tombol `[🎯 Reset / Tengah]` untuk mengembalikan fokus kamera ke posisi tengah default.
     - Sinkronisasi halus saat fitur *Track Armada* sedang mengunci salah satu bus.

4. **Diskusi 4: Ekspansi Peta & Koridor Rute Baru (Mojokerto, Kediri, Tulungagung, dan Blitar)**
   - **Kebutuhan:** Memperluas cakupan operasional PO Bus ke wilayah barat dan selatan Jawa Timur.
   - **Solusi:**
     - Mendaftarkan 4 Terminal Tipe A baru di `TerminalDataSource`:
       - **Mojokerto:** Terminal Kertajaya
       - **Kediri:** Terminal Tamanan
       - **Tulungagung:** Terminal Gayatri
       - **Blitar:** Terminal Patria
     - Mendaftarkan segmen koridor jalan tol & arteri baru di `RouteDataSource`:
       - Surabaya ⇄ Mojokerto (Jalur Tol Sumo / Surabaya-Mojokerto & Jalur Arteri Krian)
       - Mojokerto ⇄ Kediri (Via Jombang & Kertosono)
       - Kediri ⇄ Tulungagung (Via Kras / Ngadiluwih)
       - Tulungagung ⇄ Blitar (Via Ngunut)
       - Blitar ⇄ Malang (Via Kesamben, Wlingi & Kepanjen yang menyambung kembali ke Terminal Arjosari Malang!)
     - Integrasi otomatis ke Wizard Rute, kalkulasi tarif multi-terminal, dan pencatatan statistik naik-turun penumpang di terminal baru.

---

## 📋 Pembagian Fase Pengerjaan (Season 8 Roadmap)

### 🔹 Fase 1 [Kritis - Logic]: Perbaikan Alur Perputaran Armada PP (A ➔ B ➔ A ➔ B)
- [ ] Memperbaiki pergantian arah rute bolak-balik (`ORIGIN_TO_DESTINATION` ⇄ `DESTINATION_TO_ORIGIN`) saat jeda *headway* selesai di terminal tujuan.
- [ ] Memastikan armada parkir di terminal yang tepat dan menyusuri polyline jalur secara terbalik tanpa glitch posisi meloncat reset.
- [ ] Pengujian unit test logika perputaran armada, verifikasi jalan bolak-balik, dan Git Commit Fase 1.

---

### 🔹 Fase 2 [Tinggi - Interaksi]: Fitur Zoom & Geser Bebas (Pan) di Peta Simulasi
- [ ] Menambahkan gestur *Pinch-to-Zoom* dan *Drag-to-Pan* pada `EastJavaMapCanvas`.
- [ ] Menambahkan kontrol tombol UI zoom `[+]`, `[-]`, dan tombol `[🎯 Reset]` di layar peta.
- [ ] Integrasi transisi kamera yang mulus antara navigasi bebas (pan/zoom) dan mode kuncian kamera (*Track Armada*).
- [ ] Verifikasi navigasi peta, unit test, dan Git Commit Fase 2.

---

### 🔹 Fase 3 [Tinggi - Konten]: Ekspansi Peta & Jalur Baru (Mojokerto, Kediri, Tulungagung, Blitar)
- [ ] Penambahan data terminal baru: Terminal Kertajaya (Mojokerto), Terminal Tamanan (Kediri), Terminal Gayatri (Tulungagung), dan Terminal Patria (Blitar).
- [ ] Penambahan segmen rute jalan tol & arteri penghubung (Surabaya – Mojokerto – Kediri – Tulungagung – Blitar – Malang) dengan koordinat polyline jalan raya presisi.
- [ ] Pembaruan kanvas peta `EastJavaMapCanvas` untuk menampilkan titik kota dan jalur baru.
- [ ] Verifikasi perancangan rute baru di Wizard Rute, pengujian unit test, dan Git Commit Fase 3.

---

### 🔹 Fase 4 [Tinggi - Manajemen]: Penugasan Cepat Armada ke Trayek Aktif & Validasi Anti-Konflik
- [ ] Menambahkan tombol aksi `[Tugaskan ke Trayek]` pada kartu bus di Garasi Armada.
- [ ] Dialog penugasan cepat: memilih rute aktif, memilih sopir siap dinas, dan mengatur jam & jeda keberangkatan.
- [ ] Validasi anti-konflik jadwal: melarang 2 bus berangkat dari Asal yang sama, Tujuan sama, Koridor sama pada Jam/Menit yang sama.
- [ ] Menampilkan informasi trayek aktif di menu HomeScreen.
- [ ] Pengujian unit test bisnis aturan penugasan dan jadwal, verifikasi UI, dan Git Commit Fase 4.

---

### 🔹 Fase 5 [Final]: QA Menyeluruh, Strict Zero Comments, Bump Versi ke v2.6.0 & Publikasi Rilis
- [ ] Audit zero comments (`0` komentar di seluruh file Kotlin).
- [ ] Full regression testing seluruh unit test (100% passing).
- [ ] Bump versi ke `v2.6.0` (versionCode 13), build signed release APK (`app-release.apk`), dan publikasi Git Tag Rilis `v2.6.0`.
