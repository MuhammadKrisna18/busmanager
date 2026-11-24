# Roadmap & Rencana Fase Pengerjaan TransNusantara

Dokumen ini merangkum rencana tahapan pengerjaan untuk 6 fitur dan penyempurnaan utama hasil diskusi:
1. **Jadwal Bolak-Balik (A-B & B-A)** dengan minimal jeda istirahat/putar balik 1 jam dari waktu tiba.
2. **Pemisahan Layar Trayek & Tracker**: Menu trayek murni untuk membuat/mengatur rute (tracker hanya via tombol Play Simulasi).
3. **Aktivasi Menu Garasi Armada**: Menampilkan informasi lengkap bus, sopir yang bertugas, dan rute aktif.
4. **Sistem Keuangan & Pembukuan Kas**: Pemasukan tiket penumpang, pengeluaran gaji sopir, serta filter per bus dan per rute.
5. **Panel Interaktif SimulationMap**: Tombol Bus (kursi, sisa kursi, posisi, tarif, tombol Track Bus) dan Tombol Trayek (info rute & bus pengisi trayek).
6. **Kontrol Waktu Dinamis**: Play/Jeda dan pilihan kecepatan 1x, 3x, 5x dengan pemicu jadwal real-time.

---

## 📋 Pembagian Fase Pengerjaan

### 🟢 Fase 1 [Easy]: Navigasi & Perbaikan Menu Trayek + Layar Garasi Armada
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

### 🟡 Fase 2 [Medium]: Penjadwalan Pulang-Pergi (A-B & B-A) & Validasi Jeda 1 Jam
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

### 🟠 Fase 3 [Medium]: Kontrol Waktu Dinamis (Play/Jeda, Speed 1x, 3x, 5x)
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

### 🔴 Fase 4 [Hard]: Sistem Keuangan Dinamis & Layar Buku Kas (Finance Screen)
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

### 🟣 Fase 5 [Hard]: Panel Interaktif Bus & Trayek di SimulationMap + Tracking Kamera & Rilis
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
