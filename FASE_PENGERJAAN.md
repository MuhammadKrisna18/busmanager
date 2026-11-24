<!--  --># Roadmap & Rencana Fase Pengerjaan TransNusantara

Dokumen ini merangkum rencana tahapan pengerjaan untuk 6 fitur dan penyempurnaan utama hasil diskusi:
1. **Jadwal Bolak-Balik (A-B & B-A)** dengan minimal jeda istirahat/putar balik 1 jam dari waktu tiba.
2. **Pemisahan Layar Trayek & Tracker**: Menu trayek murni untuk membuat/mengatur rute (tracker hanya via tombol Play Simulasi).
3. **Aktivasi Menu Garasi Armada**: Menampilkan informasi lengkap bus, sopir yang bertugas, dan rute aktif.
4. **Sistem Keuangan & Pembukuan Kas**: Pemasukan tiket penumpang, pengeluaran gaji sopir, serta filter per bus dan per rute.
5. **Panel Interaktif SimulationMap**: Tombol Bus (kursi, sisa kursi, posisi, tarif, tombol Track Bus) dan Tombol Trayek (info rute & bus pengisi trayek).
6. **Kontrol Waktu Dinamis**: Play/Jeda dan pilihan kecepatan 1x, 3x, 5x dengan pemicu jadwal real-time.

---

## 📋 Pembagian Fase Pengerjaan

### 🟢 Fase 1 [Easy]: Navigasi & Perbaikan Menu Trayek + Layar Garasi Armada [SELESAI ✅]
- **Tujuan**: Memisahkan alur navigasi menu Trayek agar tidak lagi salah me-redirect ke tracker, serta mengaktifkan layar Garasi Armada secara penuh.
- **Deliverables**:
  1. **Perbaikan Menu Trayek (Diskusi 2)**:
     - Hapus auto-redirect ke tracker peta (`SimulationMapRoute`) setelah peluncuran trayek.
     - Tampilkan dialog sukses peluncuran armada dan reset form wizard ke awal.
     - Memastikan menu "Rute & Trayek" dari HomeScreen selalu murni membuka form pembuatan/pengaturan trayek baru.
  2. **Aktivasi Layar Garasi Armada (Diskusi 3)**:
     - Tambahkan `GarageRoute` pada `NavigationKeys.kt` dan daftarkan di `Navigation.kt`.
     - Buat `GarageScreen.kt` dan `GarageViewModel.kt`:
       - Menampilkan seluruh armada yang dimiliki PO.
       - Rincian Bus: Kode bus (TN-01), Sasis, Karoseri, Kelas, Kapasitas kursi, Kondisi mesin (%).
       - Informasi Sopir: Nama sopir bertugas atau badge `Belum Ada Sopir`.
       - Informasi Rute Aktif: Nama rute yang dilayani (misal *Surabaya - Banyuwangi*) atau badge `Belum Ada Rute`.
       - Status Operasional: `Berdinas di Jalan`, `Siap Beroperasi`, atau `Belum Siap`.
     - Sambungkan tombol "Garasi Armada" pada `HomeScreen.kt`.
  3. **Pengujian & Git Commit Fase 1**.

---

### 🟡 Fase 2 [Medium]: Penjadwalan Pulang-Pergi (A-B & B-A) & Validasi Jeda 1 Jam [SELESAI ✅]
- **Tujuan**: Menerapkan aturan perputaran armada (*Turnaround Time / Layover*) yang realistis untuk jadwal bolak-balik.
- **Deliverables**:
  1. **Logika Waktu Putar Balik & Istirahat Kru (Diskusi 1)**:
     - `Jam Tiba di B = Jam Berangkat (A-B) + Durasi Perjalanan`.
     - `Jam Berangkat Balik Minimal (B-A) = Jam Tiba di B + 1 Jam (Jeda Istirahat)`.
  2. **Integrasi di Slide 4 Wizard**:
     - Pemain dapat mengatur jadwal pergi (A ➔ B) dan jadwal pulang (B ➔ A) sekaligus dalam satu penugasan armada.
     - Sistem otomatis menghitung jam tiba dan merekomendasikan jam pulang tercepat.
     - Validasi anti-bentrok & peringatan jika pemain memilih jam pulang kurang dari jeda 1 jam setelah tiba.
  3. **Pengujian & Git Commit Fase 2**.

---

### 🟠 Fase 3 [Medium]: Kontrol Waktu Dinamis (Play/Jeda, Speed 1x, 3x, 5x) [SELESAI ✅]
- **Tujuan**: Menghidupkan jam simulasi secara real-time dengan pilihan percepatan waktu yang fleksibel.
- **Deliverables**:
  1. **Penyempurnaan `SimulationTimeRepository` (Diskusi 6)**:
     - Tambahkan kontrol kecepatan waktu: `1x (Normal)`, `3x (Cepat)`, `5x (Sangat Cepat)`.
     - Interval tick dinamis yang menggerakkan jam dan menit game secara halus.
  2. **Widget Kontrol Waktu di `SimulationMapScreen`**:
     - Tombol Play / Pause (▶️ / ⏸️).
     - Selector kecepatan waktu `[1x]`, `[3x]`, `[5x]`.
  3. **Pemicu Jadwal Otomatis Real-Time**:
     - Begitu jam simulasi menyentuh jam keberangkatan bus yang siap dinas di terminal, armada otomatis meluncur jalan.
  4. **Pengujian & Git Commit Fase 3**.

---

### 🔴 Fase 4 [Hard]: Sistem Keuangan Dinamis & Layar Buku Kas (Finance Screen) [SELESAI ✅]
- **Tujuan**: Menghidupkan ekonomi permainan dengan pencatatan mutasi kas masuk/keluar dan mengaktifkan menu Keuangan.
- **Deliverables**:
  1. **Pencatatan Transaksi Keuangan (Diskusi 4)**:
     - Model & Tabel Database `FinancialTransaction`: Tanggal game, tipe (Masuk / Keluar), kategori (Tiket Penumpang / Gaji Kru / Pembelian Bus), nominal uang, `busId`, `routeId`, dan keterangan.
     - Uang Masuk: Akumulasi tiket penumpang saat armada berdinas.
     - Uang Keluar: Gaji harian sopir aktif saat pergantian hari game (hanya gaji saja sesuai ralat).
  2. **Aktivasi Menu "Keuangan" di HomeScreen (`FinanceScreen`)**:
     - Daftarkan `FinanceRoute` di navigasi dan ubah status menu di HomeScreen dari "Segera Hadir" menjadi aktif.
     - Kartu Ringkasan: Total Kas, Total Pemasukan, Total Pengeluaran Gaji, Laba/Rugi Bersih (*Net Profit*).
     - **Filter Canggih**:
       - Filter per Armada Bus (Semua / Bus Tertentu).
       - Filter per Rute (Semua / Rute Tertentu).
       - Filter Tipe Transaksi (Semua / Pemasukan / Pengeluaran).
     - Daftar riwayat transaksi rapi dengan badge hijau (+Rp) dan merah (-Rp).
  3. **Pengujian & Git Commit Fase 4**.

---

### 🟣 Fase 5 [Hard]: Panel Interaktif Bus & Trayek di SimulationMap + Tracking Kamera & Rilis [SELESAI ✅]
- **Tujuan**: Menyempurnakan panel informasi lapangan dan fitur sorot armada, pembersihan kode, dan rilis versi baru.
- **Deliverables**:
  1. **Panel Bus di Peta Simulasi (Diskusi 5)**:
     - Menampilkan bus yang sedang jalan: total kursi, sisa kursi kosong, nama rute, posisi progres (%), dan tarif tiket.
     - **Tombol "Track Bus" 🎯**: Kamera peta langsung mengunci dan menyorot posisi bus yang dipilih dengan efek visual.
  2. **Panel Trayek di Peta Simulasi (Diskusi 5)**:
     - Menampilkan daftar rute aktif: kota asal-tujuan, tarif, serta daftar armada bus yang sedang melayani rute tersebut.
  3. **Pembersihan Dead Code & Verifikasi Zero Comments**.
  4. **Pengujian Penuh (Seluruh Unit Tests Passing)**.
  5. **Bump Versi ke v1.5.0, Build Signed Release APK, dan Git Tag Rilis**.

---

# 🚀 ROADMAP SEASON 2: Operasional Realistis & Laporan Terminal

Dokumen perencanaan fitur lanjutan Season 2 mencakup personalisasi armada bertahap, simulasi pasar penumpang dinamis, pelaporan kinerja terminal harian, dan sistem kompensasi ritase sopir.

---

### 🟢 Fase 1 [Easy-Medium]: Pemilihan Bus Modular (Custom Bus Builder Flow) [SELESAI ✅]
- **Tujuan**: Mengubah alur pembelian armada di Showroom menjadi bertahap layaknya memesan bus di dunia nyata.
- **Deliverables**:
  1. **Flow Wizard Perakitan 4 Langkah**:
     - **Langkah 1 (Pilih Sasis)**: Hino, Mercedes-Benz, Scania dengan spesifikasi mesin, tenaga kuda, dan kapasitas beban.
     - **Langkah 2 (Pilih Bodi Karoseri)**: Adiputro, Laksana, Tentrem yang kompatibel dengan sasis terpilih.
     - **Langkah 3 (Pilih Kelas Layanan & Interior)**: Ekonomi, Bisnis AC, VIP, Eksekutif, Sleeper Suite (menentukan kapasitas kursi dan kenyamanan).
     - **Langkah 4 (Review & Penamaan Armada)**: Menentukan plat nomor/kode armada (misal: TN-03), ringkasan harga total, dan konfirmasi transaksi potong saldo kas.
  2. **Pengujian & Git Commit Fase 1 (Season 2)**.

---

### 🟡 Fase 2 [Easy-Medium]: Real-Time Smooth Clock (Menit-per-Menit) & Gaji Bagi Hasil 30% (Solusi Saldo Kas) [SELESAI ✅]
- **Tujuan**: Menghilangkan lonjakan waktu 10 menit agar waktu berjalan halus selayaknya jam normal, serta mengubah skema gaji sopir menjadi komisi 30% dari pendapatan tiket agar saldo kas perusahaan selalu untung dan tidak boncos.
- **Deliverables**:
  1. **Jam Simulasi Halus (Smooth Real-Time Clock) (Diskusi 3)**:
     - Mengubah ticker waktu dari lonjakan 10 menit menjadi 1 menit per detik pada speed 1x normal (`12.00 -> 12.01 -> 12.02 ...`).
     - Ticker speed 3x (3 menit/detik) dan 5x (5 menit/detik) berjalan proporsional dan presisi.
  2. **Skema Gaji Sopir Komisi Bagi Hasil 30% (Diskusi 4 & 5)**:
     - Mengganti sistem gaji harian flat yang menguras kas pemain setiap pergantian hari.
     - Gaji sopir otomatis dihitung sebesar **30% dari total pendapatan tiket bus yang dihasilkannya saat berdinas**.
     - Perusahaan (PO Bus) mengantongi **70% laba kotor tiket**.
     - Jika bus tidak berdinas / tidak ada pemasukan, tidak ada pengeluaran gaji, sehingga saldo kas tidak akan pernah terpotong misterius lagi.
     - Pencatatan mutasi pengeluaran gaji 30% dilakukan otomatis ke Buku Kas (`FinancialTransaction`).
  3. **Pengujian & Git Commit Fase 2 (Season 2)**.

---

### 🟠 Fase 3 [Medium]: Kedinamisan Pasar Penumpang & Naik-Turun Acak di Kota Transit [SELESAI ✅]
- **Tujuan**: Menghilangkan okupansi statis (85%) agar jumlah penumpang bervariasi secara dinamis dan realistis di setiap titik terminal.
- **Deliverables**:
  1. **Algoritma Permintaan Acak (*Random Demand Generator*)**:
     - Penumpang di terminal awal tidak selalu penuh (bervariasi misal 40% - 95% tergantung kelas armada dan reputasi).
  2. **Simulasi Naik-Turun di Kota Transit (Multi-Stop)**:
     - Saat bus tiba di terminal transit/antara: sebagian penumpang turun (misal 15% - 40%), dan penumpang baru naik mengisi kursi yang kosong.
     - Proteksi kapasitas mutlak: penumpang naik tidak boleh melebihi sisa kursi kosong (`passengerCount <= capacity`).
  3. **Penurunan Total di Terminal Akhir**: Seluruh sisa penumpang turun dan tiket perjalanan selesai.
  4. **Pengujian & Git Commit Fase 3 (Season 2)**.

---

### 🟣 Fase 4 [Hard]: Peta Multi-Rute Berwarna Unik & Mode "Track Bus" Realistis ala Google Maps [SELESAI ✅]
- **Tujuan**: Membuat visualisasi peta jaringan jalan Jawa Timur menjadi hidup dengan warna berbeda untuk setiap rute yang dibuka, serta menghadirkan pengalaman pelacakan armada bergaya Google Maps.
- **Deliverables**:
  1. **Garis Rute Aktif Berwarna-Warni (Diskusi 2)**:
     - Setiap rute yang sudah dibuka oleh pemain digambar permanen di peta dengan warna garis unik dan berbeda-beda (misal: Surabaya-Probolinggo warna Oranye, Surabaya-Jember warna Hijau Emerald, Surabaya-Malang warna Ungu).
     - Palet warna kontras tinggi yang jelas dan estetik di atas kanvas peta Jawa Timur.
  2. **Fitur "Track Bus" ala Google Maps (Diskusi 1)**:
     - Saat armada dilacak (*tracking mode*), jalur rute spesifik yang dilewati bus menyala tebal (*highlighted polyline navigation*).
     - Armada ditampilkan dengan visual mobil/bus dengan arah hadap yang jelas.
     - **Bubble Callout Info Lapangan** melayang di dekat bus: Menampilkan nama armada, kota tujuan, dan estimasi sisa waktu tempuh (ETA), persis seperti UI Google Maps.
  3. **Pengujian & Git Commit Fase 4 (Season 2)**.

---

### 🔴 Fase 5 [Hard]: Log & Layar Laporan Terminal Harian + Detail Breakdown per Armada [SELESAI ✅]
- **Tujuan**: Memberikan visibilitas kinerja tiap terminal per tanggal untuk melihat arus penumpang naik dan turun secara detail.
- **Deliverables**:
  1. **Model & Tabel Database `TerminalPassengerRecord` (Room DB v5)**:
     - Mencatat event terminal: tanggal game, terminal ID/nama, bus ID/kode, rute ID/nama, jumlah naik (`boardedCount`), dan jumlah turun (`alightedCount`).
  2. **Layar Laporan Terminal (`TerminalReportScreen`)**:
     - Pemilihan terminal (Dropdown / chip selector kota).
     - Filter Tanggal/Hari game dengan kontrol navigasi hari cepat (`Hari Sebelumnya` / `Hari Berikutnya`).
     - Kartu Ringkasan: Total Penumpang Naik & Total Penumpang Turun di terminal tersebut pada hari yang dipilih.
     - **Tombol & Modal Detail per Armada**: Menampilkan daftar bus yang singgah di terminal tersebut beserta rincian jumlah trip, total penumpang naik, dan total penumpang turun per armada.
  3. **Pembersihan Dead Code & Verifikasi Zero Comments**.
  4. **Pengujian Penuh (Seluruh Unit Tests Passing)**.
  5. **Bump Versi ke v2.0.0 (VersionCode 7), Integrasi Menu Laporan Terminal di Dashboard, dan Rilis Season 2**.
