# Roadmap Pengerjaan Season 4: Pengetatan Business Rules, Sinkronisasi Data & Simulasi Operasional

Dokumen ini memuat detail perencanaan perbaikan, pengetatan business rules, dan penyelarasan operasional aplikasi **TransNusantara (Bus Manager)** berdasarkan hasil evaluasi 10 poin diskusi operasional lapangan:

---

## 📌 Rangkuman 10 Poin Diskusi & Solusi Arsitektural

1. **Diskusi 1: Pengetatan Aturan Pemilihan Bus & Driver di Wizard Trayek (Langkah 4)**
   - **Masalah:** Bus yang sudah memiliki trayek/driver (seperti `MK01`) dan driver yang sudah berdinas masih dapat dipilih di Langkah 4 Wizard. Tata letak kode armada wrap berantakan.
   - **Solusi:**
     - Bus ber-trayek/ber-driver dinonaktifkan (`enabled = false`) dengan badge `Sudah Bertugas di [Trayek / Sopir]`.
     - Driver yang sudah bertugas dinonaktifkan (`enabled = false`) dengan badge `Bertugas di [BusCode]`.
     - Perbaiki layout row nama bus dengan `Column` dan `Modifier.weight(1f)` agar kode armada dan nama sasis-bodi rapi tanpa wrap.

2. **Diskusi 2: Penyederhanaan Tombol Trayek di Layar Play (`RoutesOverviewBottomSheet`)**
   - **Masalah:** BottomSheet menampilkan semua trayek statis kosong tanpa armada sehingga membingungkan pemain.
   - **Solusi:** Hapus trayek non-aktif dari BottomSheet Play. **Hanya tampilkan trayek yang SUDAH AKTIF** (*Definisi: ada bus, ada sopir, dan bus melayani rute tersebut*) lengkap dengan info armada, sopir, tarif, dan progres dinas.

3. **Diskusi 3: Presistensi Detail Armada saat Beli di Showroom vs Masuk Garasi**
   - **Masalah:** Di Showroom membeli Scania K360IB + Tentrem Avante H8 kelas VIP (34 Kursi) `MK03`, namun saat masuk Garasi Armada berubah menjadi Eksekutif (32 Kursi).
   - **Akar Masalah:** `OwnedBusEntity` hanya menyimpan `chassisId` dan `bodyId`. Saat dibaca kembali, bodi diambil dari katalog statis yang ber-preset Eksekutif 32 Kursi.
   - **Solusi:** Tambahkan kolom `busClass: String` dan `passengerCapacity: Int` pada `OwnedBusEntity`, serta perbarui `BusMapper` untuk memetakan kustomisasi pemain.

4. **Diskusi 4: Penyelarasan Tanggal, Filter Terminal Aktif & Fitur Riwayat Laporan**
   - **Masalah:** Tanggal laporan terminal bisa diubah manual bebas tidak selaras dengan jam simulasi Play, dan terminal menampilkan seluruh terminal se-Jatim.
   - **Solusi:** Kunci tanggal laporan utama mengikuti tanggal simulasi aktif (`GameCalendar`), filter pilihan terminal hanya yang aktif dilalui trayek, dan tambahkan fitur "Riwayat / History Laporan".

5. **Diskusi 5: Waktu Keberangkatan Bus (Armada Tidak Boleh Bergerak Sebelum Waktunya)**
   - **Masalah:** Bus bergerak terus menerus di peta meskipun belum memasuki jam keberangkatan jadwal dinasnya, dan langsung putar balik tanpa jeda saat sampai tujuan.
   - **Solusi:** Bus berstatus `STANDBY` di terminal asal hingga waktu jadwal keberangkatan tiba. Saat tiba di terminal akhir (`progress >= 1.0f`), bus **BERHENTI / STANDBY** di terminal tujuan hingga jadwal kepulangan (*return schedule*) tiba.

6. **Diskusi 6: Tombol Informasi Trayek (Aktif vs Terbuka/Belum Aktif) di HomeScreen**
   - **Kebutuhan:** Menu di HomeScreen untuk melihat daftar trayek yang aktif vs trayek yang sudah dibuka di peta namun belum ditugaskan armada/kru.
   - **Solusi:** Tambahkan tombol menu / kartu "Informasi Trayek" di HomeScreen dengan dialog pemisah kategori Trayek Aktif vs Trayek Terbuka (Standby).

7. **Diskusi 7: Tombol Reset Akun & Dialog Konfirmasi Pop-up**
   - **Kebutuhan:** Fitur bagi pemain untuk memulai permainan dari awal (New Game / Reset Game).
   - **Solusi:** Tambahkan tombol "Reset Akun" pada HomeScreen dengan dialog pop-up konfirmasi destruktif merah. Eksekusi `ResetGameUseCase` yang mengosongkan seluruh armada, kru, transaksi keuangan, laporan terminal, dan mengembalikan saldo awal kas ke Rp 50 Miliar.

8. **Diskusi 8: Mekanisme Penjualan Armada di Garasi Armada**
   - **Masalah / Pertanyaan User:** Bagaimana mekanisme jika bus yang hendak dijual masih memiliki trayek, memiliki driver, atau sedang berjalan?
   - **Solusi & Aturan Bisnis:**
     - **Jika bus sedang berjalan di jalan:** Penjualan **DITOLAK**. Tampilkan peringatan: *"Armada sedang berdinas di jalan. Harap batalkan dinas atau tunggu hingga armada tiba di terminal tujuan."*
     - **Jika bus sedang standby tetapi memiliki trayek & sopir:** Penjualan **DIIJINKAN** dengan dialog konfirmasi pelepasan otomatis: *"Armada sedang ditugaskan pada rute [X] bersama sopir [Y]. Menjual armada ini akan otomatis melepas sopir kembali ke garasi dan membatalkan jadwal rute ini. Jual armada seharga Rp [Nilai Jual]?"*
     - Saat disetujui: Driver dilepas kembali menjadi Siap Dinas, jadwal rute dicabut, armada dihapus dari garasi, dan kas bertambah sesuai harga jual (misal 70% harga beli).

9. **Diskusi 9: Pembatalan Jadwal Berikutnya & Bus Stay di Terminal Terakhir (Finish Current Leg First)**
   - **Kebutuhan & Alur Operasional:**
     - Pemain bisa membatalkan jadwal berikutnya dari bus yang sedang berdinas di layar Play.
     - **Mekanisme Perjalanan Saat Dibatalkan (Contoh Rute A - B - C):**
       - Jika bus sedang berjalan dari terminal A menuju terminal B lalu user membatalkan jadwal berikutnya:
       - Bus **TIDAK berputar balik ke A**, melainkan **tetap melanjutkan perjalanannya hingga sampai di Terminal B**.
       - Setelah tiba di Terminal B, seluruh jadwal lanjutan berikutnya (ke C atau jadwal kepulangan) resmi dibatalkan, dan bus **STAY / STANDBY di Terminal B**.
     - **Aturan Penugasan Baru (Physical Location Lock):**
       - Bus kini secara fisik berada di Terminal B (`bus.currentTerminalId = "term-B"`).
       - Pemain **wajib memilih ulang trayek baru** untuk bus ini dan titik awal keberangkatan (*Origin*) **wajib bermula dari Terminal B**.

10. **Diskusi 10: Simulasi Berjalan Terus di Latar Belakang (Offline Progression)**
    - **Kebutuhan:** Ketika simulasi dalam status PLAY, game tetap berjalan menghitung waktu dan pendapatan meskipun aplikasi ditutup.
    - **Solusi Arsitektur (Delta Time Catch-Up):**
      - Simpan timestamp epoch saat aplikasi keluar / dijeda (`lastSavedEpochMillis`), status `isPlaying`, dan kecepatan simulasi.
      - Saat aplikasi dibuka kembali, hitung selisih waktu dunia nyata (`deltaSeconds`) dan konversikan ke menit simulasi game berdasarkan kecepatan (1x, 3x, 5x).
      - Jalankan simulasi fast-forward: majukan waktu game, proses dinas yang selesai, tambahkan pendapatan tiket & komisi sopir, dan tampilkan dialog sambutan *"Ringkasan Operasional Selama Anda Tidak Aktif"*.
      - Solusi ini 100% hemat baterai, tidak memerlukan background service berat, dan kebal dari penutupan paksa oleh sistem Android.

---

## 📋 Pembagian Fase Pengerjaan (Season 4 Roadmap)

### ✅ Fase 1 [High]: Presistensi Kustomisasi Armada & Sinkronisasi Garasi (Fix Diskusi 3) - SELESAI
- Update `OwnedBusEntity` di Room Database (tambah `busClass` & `passengerCapacity`). (Selesai)
- Perbarui `BusMapper` untuk memetakan kustomisasi Showroom ke database dan mengembalikannya ke domain model. (Selesai)
- Verifikasi perakitan armada kustom (VIP 34 kursi) tampil identik di Garasi Armada. (Selesai)
- Pengujian unit test & Git Commit Fase 1. (Selesai)

---

### ✅ Fase 2 [High]: Pengetatan Validasi Bus & Driver di Wizard Trayek + Perapian UI (Fix Diskusi 1) - SELESAI
- Pengetatan aturan seleksi bus: Bus ber-trayek/ber-driver/berdinas dinonaktifkan (`enabled = false`) dengan badge merah. (Selesai)
- Pengetatan aturan seleksi driver: Driver ber-bus dinonaktifkan (`enabled = false`) dengan badge penugasan. (Selesai)
- Perbaikan layout nama bus di `Slide4BusDriverSchedule.kt` agar kode armada dan spesifikasi bus tidak wrap terpotong. (Selesai)
- Pengujian unit test & Git Commit Fase 2. (Selesai)

---

### ✅ Fase 3 [High]: Penyempurnaan Simulasi, Penjadwalan Standby & Pembatalan Jadwal (Finish Leg di Terminal) (Fix Diskusi 5 & 9) - SELESAI
- Bus standby di terminal asal hingga jam jadwal keberangkatan dinas tiba, mencocokkan terminal posisi bus saat ini. (Selesai)
- Saat tiba di terminal tujuan, bus berhenti (`isCompleted = true, progressFraction = 1.0f`) dan mencatatkan alighted passengers, serta posisi `currentTerminalId` terupdate di database Room. (Selesai)
- Fitur "Batalkan Jadwal Berikutnya": Jika dibatalkan saat bus di perjalanan A ➔ B ➔ C, bus menuntaskan perjalanan sampai di B lebih dulu (`cancelNextSchedule = true`), kemudian jadwal lanjutan dihapus dan bus STAY / STANDBY di Terminal B. (Selesai)
- Physical Location Lock: Saat bus standby di suatu terminal, Route Wizard mengunci terminal asal keberangkatan trayek baru wajib bermula dari lokasi fisik armada tersebut. (Selesai)
- Pengujian unit test & Git Commit Fase 3. (Selesai)

---

### ✅ Fase 4 [Medium]: Penjualan Armada di Garasi & Fitur Reset Akun (Fix Diskusi 7 & 8) - SELESAI
- Tombol & Dialog "Jual Bus" di Garasi Armada dengan refund 70% harga pembelian awal, proteksi armada sedang di jalan, dan auto-release driver/jadwal jika standby. (Selesai)
- Tombol & Dialog konfirmasi "Reset Akun" di HomeScreen dengan `ResetGameUseCase` untuk mengembalikan kondisi baru (Rp 50.000.000.000, 100 reputasi, reset sopir/jadwal/pricing/transaksi/data penumpang). (Selesai)
- Pengujian unit test & Git Commit Fase 4. (Selesai)

---

### ✅ Fase 5 [Medium]: Penyelarasan Laporan Terminal, BottomSheet Play & Informasi Trayek (Fix Diskusi 2, 4, 6) - SELESAI
- Kunci tanggal Laporan Terminal mengikuti simulasi aktif (read-only), filter hanya terminal yang aktif dilayani armada, dan dialog "Riwayat Laporan Lalu" untuk melihat data tanggal sebelumnya. (Selesai)
- BottomSheet Play disederhanakan hanya untuk menampilkan Trayek Aktif beserta detail bus, sopir, status dinas/standby, dan jam keberangkatan. (Selesai)
- Tombol menu "Informasi Trayek" di HomeScreen dengan modal dialog memisahkan Trayek Aktif vs Trayek Terbuka (Belum Aktif). (Selesai)
- Pengujian unit test & Git Commit Fase 5. (Selesai)

---

### ✅ Fase 6 [Medium]: Simulasi Latar Belakang (Offline Progression Engine) (Fix Diskusi 10) - SELESAI
- Persistensi status simulasi (`SavedSimulationState` via SharedPreferences) saat aplikasi ditutup/dijeda: kecepatan, waktu wall-clock, jam game, dan status running. (Selesai)
- Delta Time Catch-Up engine saat aplikasi dibuka kembali: majukan jam game, hitung rit operasional bus aktif selama aplikasi ditutup, catatkan transaksi bagi hasil sopir (30%) & laba bersih (70%), serta kreditkan saldo kas perusahaan. (Selesai)
- Dialog pop-up ringkasan pendapatan offline (`OfflineProgressionDialog`) di HomeScreen saat user membuka game kembali dengan rincian durasi offline, pendapatan kotor, bagi hasil sopir, laba bersih, dan statistik per bus. (Selesai)
- Pengujian unit test & Git Commit Fase 6. (Selesai)

---

### ✅ Fase 7 [Final]: QA Menyeluruh, Strict Zero Comments, Bump Versi ke v2.2.0 & Publikasi Rilis - SELESAI
- Audit seluruh file Kotlin: 100% bebas komentar (`//` atau `/* */`) dan pembersihan dead code template awal (`ui/main`). (Selesai)
- Full Regression Testing seluruh unit test (100% passing). (Selesai)
- Bump versi ke `v2.2.0` (versionCode 9), build signed release APK (`app-release.apk`), dan publikasi Git Tag Rilis `v2.2.0`. (Selesai)

---

## 🚀 SEASON 5: Database Persistence & Data Integrity Overhaul

### ✅ Fase 1 [Kritis]: Persistensi Trayek ke Database Room (`CustomRouteEntity` & DAO) - SELESAI
- Buat `CustomRouteEntity` & `CustomRouteDao` di database Room `bus_manager.db` (Database versi 7). (Selesai)
- Mapper `RouteMapper.kt` untuk konversi dua arah antara domain `BusRoute` dan `CustomRouteEntity`. (Selesai)
- Refaktor `RouteCatalogRepositoryImpl` berbasis database Room (sinkronisasi memori cache & DAO reaktif). (Selesai)
- Integrasi penghapusan rute custom di `ResetGameUseCase`. (Selesai)
- Pengujian unit test & Git Commit Fase 1. (Selesai)

---

### ✅ Fase 2 [Tinggi]: Perbaikan Penugasan Jadwal Pulang-Pergi (PP) & DAO Multi-Schedule - SELESAI
- Logika penugasan armada `assignBusToSchedule` disempurnakan: mendukung satu armada melayani jadwal Berangkat (*Outbound*) dan Pulang (*Return*) pada rute yang sama tanpa saling meng-unassign. (Selesai)
- Menambahkan query `getSchedulesByBusId` dan `getSchedulesByBusIdFlow` pada `RouteScheduleDao`. (Selesai)
- Proteksi re-alokasi rute: jika armada dipindahkan ke rute berbeda, jadwal lama otomatis dilepas dengan bersih. (Selesai)
- Pengujian unit test & Git Commit Fase 2. (Selesai)

---

### ✅ Fase 3 [Tinggi]: Sinkronisasi & Auto-Recovery Status Operasi Armada (State Reconciliation) - SELESAI
- Engine pemulihan armada `ReconcileBusOperatingStateUseCase`: secara otomatis mendeteksi dan memulihkan armada yang tersangkut `isOperating = true` di database Room jika aplikasi ditutup paksa / di-kill tanpa adanya active trip aktif di memori. (Selesai)
- Integrasi rekonsiliasi otomatis pada inisialisasi `HomeScreenViewModel`, `GarageViewModel`, dan proteksi `SellBusUseCase`. (Selesai)
- Penyempurnaan penyajian status di garasi: armada hanya berstatus "Berdinas" di UI jika benar-benar memiliki perjalanan aktif yang sedang berjalan di jalan raya. (Selesai)
- Pengujian unit test & Git Commit Fase 3. (Selesai)

---

### ✅ Fase 4 [Medium]: Auto-Seeding Database Terpusat & Integritas Relasi Data - SELESAI
- Auto-seeding database terpusat sejak peluncuran aplikasi pertama kali (`StartNewGameUseCase`): menjamin perusahaan terinisialisasi modal Rp 50.000.000.000 & 100 reputasi, serta 6 sopir kandidat default langsung tersedia di database Room. (Selesai)
- Peningkatan keamanan `seedCandidateDriversIfEmpty`: memeriksa dan memasukkan setiap kandidat pengemudi secara presisi tanpa menduplikasi data yang sudah ada. (Selesai)
- Integritas relasi data dan pembersihan berantai (*Cascading Cleanup*): `SellBusUseCase` kini menghapus seluruh jadwal yang terikat pada armada yang dijual via `deleteSchedulesForBus` dan `deleteSchedule`, mencegah terciptanya *orphan schedule* di database. (Selesai)
- Proteksi integritas trayek kustom: penghapusan rute kustom (`deleteRoute`) otomatis membersihkan jadwal (`deleteSchedulesByRouteSync`) dan daftar tarif (`deletePricingsByRouteSync`) terkait. (Selesai)
- Pengujian unit test & Git Commit Fase 4. (Selesai)

---

### ✅ Fase 5 [Final]: QA Menyeluruh, Strict Zero Comments, Bump Versi ke v2.3.0 & Publikasi Rilis - SELESAI
- Audit seluruh file Kotlin: 100% bebas komentar (`//` atau `/* */`) dan bebas dead code. (Selesai)
- Full Regression Testing seluruh 250+ unit test (100% passing). (Selesai)
- Bump versi ke `v2.3.0` (versionCode 10), build signed release APK (`app-release.apk`), dan publikasi Git Tag Rilis `v2.3.0`. (Selesai)

---

## 🚀 SEASON 6: UI/UX Refinement, Auto-Dispatch Engine & System Insets Overhaul

### ✅ Fase 1 [Kritis]: Penanganan System Bars & Insets Android (Fix Diskusi 6) - SELESAI
- Terapkan `statusBarsPadding()` & `navigationBarsPadding()` secara menyeluruh pada root layout navigasi dan seluruh layar (`HomeScreen`, `GarageScreen`, `ShowroomScreen`, `RoutePlannerScreen`, `RouteWizardScreen`, `FinanceScreen`, `DriverManagementScreen`, `TerminalReportScreen`, `SimulationMapScreen`, `SplashScreen`, serta bottom sheet). (Selesai)
- Header aplikasi (`CompanyHeader` dan seluruh top bar) terlindungi dari tumpukan elemen status bar Android (jam, icon notifikasi, wifi, baterai). (Selesai)
- Konten bawah (`LiveInfoBanner`, action bar showroom/wizard, dan tombol bottom sheet) aman di atas bilah navigasi 3-tombol sistem Android tanpa terpotong. (Selesai)
- Pengujian tampilan & Git Commit Fase 1. (Selesai)

---

### ✅ Fase 2 [Tinggi]: Standarisasi Format Informasi Jadwal Keberangkatan & Kepulangan (Fix Diskusi 1) - SELESAI
- Tambahkan metode domain `formatScheduleInfo(originCity, destinationCity)` dan `formatScheduleInfo(route)` pada `RouteSchedule` model. (Selesai)
- Standarisasi format teks di semua halaman menjadi: `Jam [HH:mm WIB] menuju [Kota Tujuan] dari [Kota Asal]`. (Selesai)
- Terapkan di `RoutePlannerScreen` (header section jadwal, item card jadwal, dialog penugasan armada, dan dialog tambah jadwal). (Selesai)
- Terapkan di `Slide4BusDriverSchedule` (wizard pembuatan rute: header & subteks jadwal pergi/pulang, filter chips arah rute, serta peringatan bentrok jadwal). (Selesai)
- Terapkan di `RoutesOverviewBottomSheet` (daftar jadwal armada terformat rapi per baris dengan icon `🕒`), `BusLiveTrackingCard`, `FleetOverviewBottomSheet`, dan `GarageViewModel`. (Selesai)
- Terapkan di `HomeScreen` pada kartu informasi operasional trayek aktif. (Selesai)
- Penambahan unit test domain di `RouteScheduleTest` untuk verifikasi formatting jadwal outbound, inbound, dan bus route. (Selesai)
- Pengujian unit test & Git Commit Fase 2. (Selesai)

---

### 🔹 Fase 3 [Tinggi]: Klasifikasi Status Armada di Garasi & Penggantian Istilah Standby (Fix Diskusi 3 & 4)
- Armada yang sudah memiliki penugasan trayek & pengemudi (menunggu jam keberangkatan) otomatis dikelompokkan ke tab **"Berdinas"** di Garasi.
- Tab **"Di Garasi"** hanya menampilkan armada yang belum memiliki penugasan trayek/pengemudi (menganggur).
- Ganti seluruh teks, chip, dan status `"Standby"` di seluruh aplikasi menjadi `"Menunggu Jadwal Keberangkatan"`.
- Pengujian unit test & Git Commit Fase 3.

---

### 🔹 Fase 4 [Medium]: Otomasi Keberangkatan Murni & Penghapusan Tombol Manual "Berdinas Sekarang" (Fix Diskusi 2)
- Hapus/nonaktifkan tombol manual `Berdinas Sekarang` pada BottomSheet simulasi armada.
- Tampilkan badge status kesiapan otomatisasi (`Menunggu Jam Keberangkatan`).
- Pastikan ketika user menekan Play dan waktu simulasi mencapai jadwal, engine simulasi langsung memberangkatkan bus secara otomatis.
- Pengujian unit test & Git Commit Fase 4.

---

### 🔹 Fase 5 [Medium]: Global Fade-In Transition Framework (Fix Diskusi 5)
- Buat arsitektur transisi global `AppTransitions.kt` menggunakan efek fade-in dengan durasi 1 detik (1000ms).
- Terapkan transisi global fade-in ini pada navigasi antar-halaman (`Navigation.kt`) dan animasi konten dinamis (`AnimatedContent`, `AnimatedVisibility`).
- Pengujian transisi & Git Commit Fase 5.

---

### 🔹 Fase 6 [Final]: QA Menyeluruh, Strict Zero Comments, Bump Versi ke v2.4.0 & Publikasi Rilis
- Audit zero comments (`0` komentar di seluruh file Kotlin).
- Full regression testing 100% passing.
- Bump versi ke `v2.4.0` (versionCode 11), build signed release APK, dan publikasi Git Tag Rilis `v2.4.0`.
