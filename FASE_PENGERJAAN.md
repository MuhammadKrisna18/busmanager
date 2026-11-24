# Roadmap Pengerjaan Season 7: Headway 24 Jam, Real-Line Tracking, Pembelian Bus Instan & Ekosistem Operasional Lanjutan

Dokumen ini memuat detail perencanaan perbaikan arsitektural, logika simulasi, dan antarmuka pengguna aplikasi **TransNusantara (Bus Manager)** berdasarkan hasil evaluasi 16 poin diskusi operasional lapangan:

---

## 📌 Rangkuman 16 Poin Diskusi & Solusi Arsitektural

1. **Diskusi 1: Jadwal Keberangkatan 24 Jam (Jeda Keberangkatan & Waktu Ampiran/Transit)**
   - **Konsep:** Menggantikan jadwal jam statis (seperti 08:00 dan 14:00) menjadi sistem operasional berbasis jeda waktu perputaran (*Headway*) dan waktu singgah (*Dwell Time*) yang berjalan dinamis 24 jam.
   - **Solusi:**
     - **Jeda Keberangkatan (Headway / Turnaround Time):** Waktu istirahat armada di terminal tujuan sebelum berangkat kembali (PP).
     - **Waktu Ampiran (Dwell Time):** Waktu singgah bus di terminal perantara untuk pertukaran naik-turun penumpang (0 hingga 15 menit). Jika 0 menit, bus hanya naik-turun seketika dan langsung jalan.

2. **Diskusi 2: Proyeksi Pendapatan & Laba Bersih Live saat Pasang Tarif (Wizard Langkah 3)**
   - **Kebutuhan:** Pemain dapat melihat estimasi pemasukan dan keuntungan sebelum menyetujui tarif tiket.
   - **Solusi:** Menambahkan kartu proyeksi keuangan interaktif di bawah form tarif: menghitung estimasi omzet tiket kotor (asumsi kursi penuh), potongan bagi hasil sopir (30%), serta proyeksi laba bersih perusahaan (70%).

3. **Diskusi 3: Buku Kas Keuangan - Hapus Filter Berdasarkan Rute**
   - **Kebutuhan:** Tampilan filter transaksi keuangan terlalu padat dan jarang digunakan.
   - **Solusi:** Menghapus dropdown/chip filter rute di `FinanceScreen`. Fokus filter disederhanakan pada tipe transaksi (Semua, Penjualan Tiket, Komisi Sopir) atau filter armada.

4. **Diskusi 4: Pengeditan Pengaturan Jadwal (Jeda & Ampiran) di Garasi & Play ➔ Trayek**
   - **Kebutuhan:** Pemain ingin mengubah jeda keberangkatan dan waktu ampiran armada yang sudah berdinas tanpa harus membongkar rute.
   - **Solusi:** Menambahkan dialog *"Ubah Jadwal Operasional"* pada kartu bus di Garasi Armada serta pada daftar trayek di layar Play.

5. **Diskusi 5: Pembelian Bus Batch (Multi-Unit) & Fitur Pembelian Instan Siap Pakai**
   - **Kebutuhan:** Memudahkan ekspansi armada dalam jumlah besar tanpa perlu merakit manual satu per satu.
   - **Solusi:**
     - **Pembelian Batch:** Stepper counter `[-] [Jumlah: 1] [+]` di layar review perakitan bus kustom untuk membeli banyak unit sekaligus dengan spek sama dan penomoran otomatis.
     - **Pembelian Instan:** Tab katalog bus siap pakai bawaan pabrik (sasis, bodi, dan kapasitas kursi sesuai unit asli dunia nyata) yang dapat dibeli langsung dalam jumlah satuan maupun borongan.

6. **Diskusi 6: Bus Berjalan Presisi Tepat di Atas Garis Polyline Peta (Real-Line Tracking)**
   - **Masalah:** Posisi bus saat ini menggunakan garis lurus antar kota sehingga sedikit melenceng dari lengkungan garis jalan tol/arteri di peta.
   - **Solusi:** Menghitung posisi koordinat bus `(x, y)` menyusuri segmen-segmen polyline rute jalan raya yang sama persis dengan yang digambar di Canvas peta, menjamin bus berjalan 100% presisi di atas garis jalan.

7. **Diskusi 7: Penghapusan Tombol Play (Simulasi Otomatis Berjalan Terus)**
   - **Kebutuhan:** Menghilangkan keharusan menekan tombol Play saat membuka game.
   - **Solusi:** Simulasi dan waktu otomatis langsung berjalan aktif sejak awal. Kontrol utama disederhanakan menjadi tombol **Jeda (Pause)** dan pemilih kecepatan (1x, 3x, 5x).

8. **Diskusi 8: HomeScreen - Hapus Reputasi & Sinkronisasi Waktu Real-Time**
   - **Kebutuhan:** Menghilangkan info tidak relevan dan menyelaraskan waktu dashboard.
   - **Solusi:** Menghapus badge reputasi dan teks statis *"hari ke 1, 08.00"*, lalu menghubungkan header HomeScreen ke jam simulasi dinamis (`GameCalendar`) agar selalu sinkron dengan waktu di layar Play.

9. **Diskusi 9: Laporan Terminal - Catat Seluruh Terminal Transit/Ampiran**
   - **Masalah:** Laporan terminal hanya mencatat data di terminal asal dan akhir, mengabaikan terminal transit.
   - **Solusi:** Merekam transaksi naik dan turun penumpang di setiap terminal transit yang disinggahi ke dalam tabel `TerminalPassengerRecord`, menjamin seluruh terminal memiliki data statistik harian yang akurat.

10. **Diskusi 10: Pembersihan Elemen Peta Play (Legend Jalur & Badge Transit)**
    - **Kebutuhan:** Tampilan peta Play lebih bersih, luas, dan data counter valid.
    - **Solusi:** Menghapus legend bar warna jalur di atas tombol navigasi bawah, menghapus badge orange bertuliskan *"Transit"* di atas titik kota, dan memvalidasi counter tombol bawah agar hanya menghitung trayek aktif milik pemain.

11. **Diskusi 11: Visualisasi Bus Menunggu Jadwal di Terminal Peta & Info Lokasi Fisik**
    - **Kebutuhan:** Pemain ingin mengetahui di mana posisi armada saat sedang tidak berjalan.
    - **Solusi:** Menampilkan ikon bus yang terparkir di terminal keberangkatan pada peta simulasi, serta menambahkan info lokasi fisik armada pada kartu trayek (misal: *"Menunggu Keberangkatan di Terminal Purabaya, Surabaya"*).

12. **Diskusi 12: Halaman Khusus Informasi Trayek (Dedicated Screen)**
    - **Kebutuhan:** Informasi trayek yang saat ini berupa kartu pop-up diubah menjadi satu layar penuh agar lebih detail dan leluasa dibaca.
    - **Solusi:** Membuat layar baru `RouteInfoScreen` yang memuat daftar rute aktif, peta mini koridor, armada yang bertugas, jeda waktu operasional, dan matriks tarif.

13. **Diskusi 13: Penambahan 94 Sopir Baru (Total 100 Sopir di Bursa Pelamar)**
    - **Kebutuhan:** Memperluas ketersediaan tenaga kerja pengemudi untuk mendukung operasional armada besar.
    - **Solusi:** Memasukkan dataset 94 kandidat sopir baru ke database Room sehingga bursa pelamar memiliki total 100 pengemudi dengan variasi nama, usia, lisensi, dan tarif bagi hasil.

14. **Diskusi 14: Perapian Tata Letak Teks Pilihan Koridor di Langkah 2 Wizard Trayek**
    - **Masalah:** Teks nama rute, jarak, dan jenis jalur pada kartu koridor saling bertumpuk dan tidak rapi.
    - **Solusi:** Menata ulang tata letak kartu koridor dengan tipografi proporsional, badge tol/arteri yang elegan, dan jarak/estimasi waktu yang rapi tanpa teks terpotong.

15. **Diskusi 15: Sembunyikan Bus & Driver yang Sudah Bertugas saat Membuat Trayek Baru**
    - **Kebutuhan:** Saat pemain membuat trayek baru di Langkah 4 Wizard, wajib memilih bus dan sopir. Bus yang sudah memiliki trayek serta sopir yang sudah bertugas jangan ditampilkan dalam keadaan abu-abu/disabled, melainkan dihilangkan sepenuhnya dari daftar agar layar tidak penuh dan pemain hanya melihat pilihan yang benar-benar siap beroperasi.
    - **Solusi:** Memfilter daftar seleksi bus dan sopir di Slide 4 Wizard secara bersih sehingga hanya menampilkan bus yang tersedia di garasi dan sopir yang berstatus siap dinas.

16. **Diskusi 16: Perbaikan Glitch Loncat Posisi Bus saat Tracking Kamera Aktif**
    - **Masalah:** Saat mengunci/melacak armada bus di peta, posisi bus sempat meloncat dan baru normal kembali setelah dilepas.
    - **Solusi:** Memisahkan koordinat absolut bus dari transformasi offset kamera peta, sehingga pergerakan kamera fokus tidak memanipulasi posisi rendered bus di kanvas.

---

## 📋 Pembagian Fase Pengerjaan (Season 7 Roadmap)

### 🔹 Fase 1 [Kritis]: Core Simulation Engine (Headway 24 Jam, Real-Line Tracking & Auto-Running) - [SELESAI]
- [x] Mengubah model jadwal dan logika simulasi dari jam statis menjadi sistem **Headway 24 jam** & **Waktu Ampiran / Dwell Time** (Fix Diskusi 1).
- [x] Perhitungan posisi bus presisi menyusuri titik-titik **polyline rute jalan raya** (Fix Diskusi 6).
- [x] Memisahkan kamera tracking dan penjangkaran koordinat bus terpusat (0, 0) untuk menghilangkan glitch posisi meloncat saat di-track (Fix Diskusi 16).
- [x] Penghapusan tombol Play awal; simulasi otomatis aktif berjalan sejak app dibuka dengan tombol Jeda (Pause) dan seleksi kecepatan (Fix Diskusi 7).
- [x] Pengujian 261 unit test lolos 100% & Git Commit Fase 1.

---

### 🔹 Fase 2 [Tinggi]: Data Integrity Terminal & Visualisasi Peta Simulasi - [SELESAI]
- [x] Pencatatan aktivitas naik-turun penumpang di seluruh terminal singgah/ampiran ke database Room (Fix Diskusi 9).
- [x] Pembersihan antarmuka peta Play: hapus legend baris bawah, hapus badge transit oranye, dan perbaiki penghitungan counter tombol rute aktif (Fix Diskusi 10).
- [x] Menampilkan armada yang berstatus *"Menunggu Jadwal Keberangkatan"* tetap terlihat parkir di terminal posisinya pada peta, lengkap dengan teks lokasi fisik di kartu trayek (Fix Diskusi 11).
- [x] Pengujian unit test & Git Commit Fase 2.

---

### 🔹 Fase 3 [Tinggi]: Showroom Bus & Fleksibilitas Garasi Armada - [SELESAI]
- [x] Fitur pembelian bus secara batch (multi-unit stepper) di layar konfirmasi perakitan kustom (Fix Diskusi 5).
- [x] Penambahan katalog **Pembelian Instan** di Showroom (bus siap pakai dengan sasis, bodi, dan kapasitas asli pabrikan) (Fix Diskusi 5).
- [x] Modal dialog untuk mengubah jeda keberangkatan dan waktu ampiran langsung dari Garasi Armada dan layar Play ➔ Trayek (Fix Diskusi 4).
- [x] Pengujian unit test & Git Commit Fase 3.

---

### 🔹 Fase 4 [Medium]: UI Refinement, Wizard Rute & Layar Informasi Trayek - [SELESAI]
- [x] Kartu kalkulasi estimasi proyeksi omzet, komisi sopir, dan laba bersih perusahaan secara live saat input tarif di Langkah 3 Wizard (Fix Diskusi 2).
- [x] Perapian desain dan tata letak kartu koridor di Langkah 2 Wizard Trayek agar nyaman dibaca (Fix Diskusi 14).
- [x] Pembuatan layar penuh tersendiri **Halaman Informasi Trayek** (`RouteInfoScreen`) menggantikan kartu modal sebelumnya (Fix Diskusi 12).
- [x] Pengujian unit test & Git Commit Fase 4.

---

### 🔹 Fase 5 [Medium]: Manajemen Kru, Seleksi Bersih Wizard Rute & Ekosistem Keuangan
- Penambahan 94 kandidat sopir baru (total 100 pengemudi di bursa pelamar) (Fix Diskusi 13).
- Seleksi bersih saat membuat trayek (Slide 4 Wizard): hilangkan bus dan sopir yang sudah bertugas / tidak bisa dipilih, sehingga hanya menampilkan pilihan yang benar-benar siap beroperasi (Fix Diskusi 15).
- Pembersihan filter rute di Buku Kas Keuangan `FinanceScreen` (Fix Diskusi 3).
- Pembersihan HomeScreen: hapus info reputasi dan sinkronkan jam operasional langsung dengan jam simulasi Play (Fix Diskusi 8).
- Pengujian unit test & Git Commit Fase 5.

---

### 🔹 Fase 6 [Final]: QA Menyeluruh, Strict Zero Comments, Bump Versi ke v2.5.0 & Publikasi Rilis
- Audit zero comments (`0` komentar di seluruh file Kotlin).
- Full regression testing seluruh unit test (100% passing).
- Bump versi ke `v2.5.0` (versionCode 12), build signed release APK (`app-release.apk`), dan publikasi Git Tag Rilis `v2.5.0`.
