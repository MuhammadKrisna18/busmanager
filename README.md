# TransNusantara - Bus Manager

**TransNusantara** adalah game simulasi manajemen Perusahaan Otobus (PO Bus) Antar Kota Dalam Provinsi (AKDP) di wilayah Jawa Timur. Di dalam game ini, Anda berperan sebagai pemilik sekaligus direktur operasional PO Bus yang bertanggung jawab membangun dan mengembangkan bisnis transportasi bus dari awal hingga menjadi perusahaan otobus terbesar dan terpercaya.

Kelola pembelian armada bus, rekrut pengemudi andal, rancang rute trayek antarkota, tentukan tarif tiket, dan pantau perjalanan bus Anda secara langsung di peta operasional Jawa Timur.

---

## 🎮 Cara Bermain & Syarat Beroperasi

Agar bus Anda dapat mulai melayani penumpang dan menghasilkan keuntungan, Anda harus melengkapi **3 Syarat Utama**:
1. **Memiliki Bus**: Membeli armada bus pilihan di Showroom Bus (rakit kustom atau beli instan siap pakai).
2. **Memiliki Sopir**: Merekrut dan menugaskan pengemudi aktif dari bursa pelamar ke armada bus.
3. **Memiliki Trayek & Tarif**: Membuka rute perjalanan antarkota dan menetapkan harga tiket perjalanannya.

Setelah ketiga syarat terpenuhi, bus akan langsung beroperasi menyusuri trayek selama 24 jam penuh sesuai jadwal slot keberangkatan yang Anda tentukan.

---

## 📜 Aturan Bisnis & Spesifikasi Sistem (Business Rules)

Berikut adalah dokumentasi lengkap seluruh aturan bisnis (*business rules*), formula matematis, dan mekanisme operasional yang diterapkan pada **TransNusantara v2.30.0**:

### 1. Sistem Jalur Tunggal Terpadu (Unified Road Network)
* **Penyederhanaan Jalur:** Seluruh trayek dioperasikan melalui satu sistem jalur jalan raya terintegrasi (**"Jalan"**).
* **Bebas Pembagian Tol vs Non-Tol:** Pengguna tidak lagi dibebani pemilihan koridor artifisial maupun biaya tarif tol tambahan per segmen. Seluruh perhitungan operasional disederhanakan agar fokus pada efisiensi waktu tempuh antarterminal.

---

### 2. Kompresi Durasi Rute Berbasis Boundary Value Analysis (BVA 17 Level)
Untuk menjaga ritme permainan tetap dinamis, ketat, dan realistis tanpa membuat jadwal tembus tengah malam, durasi mentah rute (*raw segment duration*) yang berkisar antara 25 hingga 360+ menit dipetakan secara matematis ke dalam **17 Level Partisi Diskrit** kelipatan 15 menit (antara **30 menit hingga 270 menit**):

$$\text{Level BVA} = f(\text{Durasi Mentah}) \implies [30, 45, 60, \dots, 270\text{ Menit}]$$

| Level | Range Durasi Asli (*Raw Input*) | **Durasi Game (Tersistem)** | Format Jam | Total Operasional 2 PP (4 Trip + Istirahat 1j 45m) | Sisa Waktu Garasi Malam (dari 24 Jam) |
| :---: | :---: | :---: | :---: | :---: | :---: |
| **1** | $\le 45\text{ mnt}$ | **30 menit** | 0j 30m | 3 jam 45 menit | 20 jam 15 menit |
| **2** | $46 - 65\text{ mnt}$ | **45 menit** | 0j 45m | 4 jam 45 menit | 19 jam 15 menit |
| **3** | $66 - 85\text{ mnt}$ | **60 menit** | 1j 00m | 5 jam 45 menit | 18 jam 15 menit |
| **4** | $86 - 105\text{ mnt}$ | **75 menit** | 1j 15m | 6 jam 45 menit | 17 jam 15 menit |
| **5** | $106 - 125\text{ mnt}$ | **90 menit** | 1j 30m | 7 jam 45 menit | 16 jam 15 menit |
| **6** | $126 - 145\text{ mnt}$ | **105 menit** | 1j 45m | 8 jam 45 menit | 15 jam 15 menit |
| **7** | $146 - 165\text{ mnt}$ | **120 menit** | 2j 00m | 9 jam 45 menit | 14 jam 15 menit |
| **8** | $166 - 185\text{ mnt}$ | **135 menit** | 2j 15m | 10 jam 45 menit | 13 jam 15 menit |
| **9** | $186 - 205\text{ mnt}$ | **150 menit** | 2j 30m | 11 jam 45 menit | 12 jam 15 menit |
| **10** | $206 - 225\text{ mnt}$ | **165 menit** | 2j 45m | 12 jam 45 menit | 11 jam 15 menit |
| **11** | $226 - 245\text{ mnt}$ | **180 menit** | 3j 00m | 13 jam 45 menit | 10 jam 15 menit |
| **12** | $246 - 265\text{ mnt}$ | **195 menit** | 3j 15m | 14 jam 45 menit | 9 jam 15 menit |
| **13** | $266 - 285\text{ mnt}$ | **210 menit** | 3j 30m | 15 jam 45 menit | 8 jam 15 menit |
| **14** | $286 - 305\text{ mnt}$ | **225 menit** | 3j 45m | 16 jam 45 menit | 7 jam 15 menit |
| **15** | $306 - 325\text{ mnt}$ | **240 menit** | 4j 00m | 17 jam 45 menit | 6 jam 15 menit |
| **16** | $326 - 345\text{ mnt}$ | **255 menit** | 4j 15m | 18 jam 45 menit | 5 jam 15 menit |
| **17** | $\ge 346\text{ mnt}$ | **270 menit** | 4j 30m | **19 jam 45 menit** | **4 jam 15 menit** $(< 5\text{ jam})$ |

* **Keteraturan Titik Batas:** Setiap transisi diuji secara ketat pada unit test (`RouteModelTest.kt`), memastikan nilai batas seperti $45 \to 30$, $46 \to 45$, $345 \to 255$, hingga $\ge 346 \to 270$ tidak mengalami celah *off-by-one*.

---

### 3. Sistem Operasional Rit Harian (2 PP / 4 Trip per Hari)
* **Standar Dinas Armada:** Setiap armada pada rute AKDP beroperasi dengan standar **2 Pulang-Pergi (2 PP = 4 Trip)** per hari kalender (00:00 – 23:59 WIB):
  1. **Rit 1 Pergi:** Terminal Asal $\rightarrow$ Terminal Tujuan
  2. **Rit 1 Pulang:** Terminal Tujuan $\rightarrow$ Terminal Asal
  3. **Rit 2 Pergi:** Terminal Asal $\rightarrow$ Terminal Tujuan
  4. **Rit 2 Pulang:** Terminal Tujuan $\rightarrow$ Terminal Asal
* **Jeda Istirahat Terjadwal (Layover):**
  * **Di Terminal Tujuan:** Tepat **30 menit** untuk bongkar muat penumpang dan putar balik armada.
  * **Di Terminal Asal / Garasi (Antar-PP):** Tepat **45 menit** untuk jeda istirahat siang/makan pengemudi dan pengecekan armada.
  * **Total Waktu Istirahat:** $(2 \times 30\text{ mnt}) + 45\text{ mnt} = \mathbf{105\text{ menit}}$ (1 jam 45 menit).
* **Jaminan Batas Waktu Malam:** Pada rute paling panjang (Level 17 / 270 menit), armada yang mulai berdinas pukul 04:00 WIB menyelesaikan seluruh 4 trip pada pukul 23:45 WIB. Waktu istirahat malam di garasi tersisa **4 jam 15 menit** (memenuhi syarat kelayakan operasional $< 5\text{ jam}$).

---

### 4. Manajemen Slot Keberangkatan Terminal (24-Hour Discrete Slot Grid)
* **Kuantisasi Waktu (15-Minute Slotting):** Setiap terminal membagi jadwal 24 jam ke dalam interval 15 menit (`:00`, `:15`, `:30`, `:45`), menghasilkan tepat **96 slot keberangkatan per hari** untuk tiap terminal.
* **Aturan Konflik Headway Terminal:** Satu terminal tidak mengizinkan dua armada berangkat ke kota tujuan yang sama pada slot waktu yang sama (atau selisih kurang dari buffer 15 menit).
* **Reservasi Bolak-Balik Terpadu (Dual-Direction Booking):**
  * Penugasan armada pada rute A $\rightarrow$ B secara otomatis mereservasi slot kepulangan di Terminal B (arah A) sesuai jam kedatangan ditambah jeda istirahat 30 menit.
  * Saat pengguna membuka penugasan armada lain dari B $\rightarrow$ A, slot yang telah dipesan oleh armada pertama otomatis berstatus **Terisi** (*Occupied*).
* **Identitas Armada pada Grid Slot:** Kotak slot terisi menampilkan kode armada nyata yang sedang berdinas (misal: `11:00 • TN-14`).
* **Pengalihan Otomatis Cerdas (*Smart Slot Redirection*):** Jika pengguna memilih slot yang telah terisi, sistem secara otomatis mengarahkan pilihan ke slot kosong terdekat berikutnya (*next available slot*) dengan notifikasi transparan, tanpa mengunci atau mematikan tombol konfirmasi penugasan.

---

### 5. Keuangan, Tarif Tiket, dan Bagi Hasil Pengemudi
* **Modal Awal Usaha:** Setiap perusahaan baru dibekali modal awal sebesar **Rp 50.000.000.000**.
* **Formula Bagi Hasil Adil:**
  * **70%** dari total omzet tiket masuk ke kas Perusahaan.
  * **30%** dari total omzet tiket dialokasikan sebagai upah bagi hasil pengemudi.
* **Perhitungan Pendapatan Bersih Perjalanan:**
  $$\text{Omzet Trip} = \text{Jumlah Penumpang} \times \text{Tarif Tiket}$$
  $$\text{Upah Sopir} = 30\% \times \text{Omzet Trip}$$
  $$\text{Laba Kas Perusahaan} = 70\% \times \text{Omzet Trip}$$
* **Proyeksi Laba Langsung (*Live Profit Projection*):** Saat mengatur harga tiket di wizard trayek, sistem menampilkan estimasi pendapatan kotor, komisi pengemudi, dan estimasi laba bersih secara *real-time*.

---

### 6. Relasi & Keterikatan Entitas (Data Consistency)
* **Armada & Pengemudi:**
  * Satu armada bus hanya dapat ditugaskan kepada **1 pengemudi aktif**.
  * Pengemudi yang sudah bertugas pada satu armada bus tidak dapat ditugaskan ke armada bus lain sebelum penugasan sebelumnya dibatalkan.
* **Armada & Rute:**
  * Armada bus yang aktif bertugas terikat pada 1 rute dan memiliki entitas jadwal dinas harian lengkap (2 atau 4 trip leg).
  * Penjualan armada atau pembongkaran penugasan otomatis menghapus seluruh slot dinas yang telah dipesan di terminal terkait.

---

### 7. Simulasi Waktu & Penghasilan Luar Jaringan (Offline Progression)
* **Sinkronisasi Waktu Nyata:** Detik simulasi pada peta operasional sinkron secara presisi dengan jam operasional perusahaan di dashboard utama.
* **Progresi Luar Jaringan (*Offline Earnings*):** Saat aplikasi ditutup, sistem mencatat waktu penutupan terakhir (*epoch minutes*). Ketika aplikasi dibuka kembali:
  * Sistem menghitung akumulasi menit offline yang telah terlewat.
  * Setiap perjalanan bus diselesaikan secara proporsional dengan batas aman durasi trip minimal 60 menit.
  * Pendapatan tiket dan komisi sopir dihitung otomatis serta disajikan melalui ringkasan laporan operasional offline.

---

## ✨ Fitur-Fitur Utama

### 1. Showroom Bus & Pembelian Instan
- **Pembelian Instan Siap Pakai**: Beli bus baru langsung pakai dengan spesifikasi asli pabrikan terkemuka (Hino RK8, Mercedes-Benz OH 1626, Scania K410IB, Volvo B11R) lengkap dengan bodi dan kapasitas kursi aslinya.
- **Perakitan Kustom**: Rancang sendiri kombinasi sasis (Hino, Mercedes-Benz, Scania) dan bodi karoseri mewah (Adiputro Jetbus, Laksana Legacy, Tentrem Avante).
- **Pembelian Borongan (Banyak Unit Sekaligus)**: Tambah armada dalam jumlah banyak dengan satu kali konfirmasi untuk mempercepat ekspansi bisnis.
- **Beragam Kelas Layanan**: Sediakan kelas perjalanan Ekonomi, Bisnis AC, VIP, Eksekutif, Super Eksekutif, hingga Sleeper Suite.

### 2. Garasi Armada & Fleksibilitas Jadwal
- **Pemantauan Status Armada**: Lihat armada yang sedang bertugas di jalan raya, armada yang sedang beristirahat menunggu jadwal di terminal, maupun armada yang menganggur di garasi.
- **Penugasan Cepat (Quick Assign)**: Pasang sopir, rute, dan pilih slot keberangkatan terminal secara instan langsung dari daftar armada di garasi.
- **Jual Armada**: Jual kembali bus lama untuk menambah kas usaha dan meremajakan armada.

### 3. Bursa Kerja & Manajemen Pengemudi
- **100 Pilihan Sopir Berpengalaman**: Bursa pelamar kerja menyediakan hingga 100 pilihan pengemudi dengan variasi lisensi dan pengalaman kerja.
- **Sistem Bagi Hasil Transparan**: Pembagian hasil upah 30% pengemudi dan 70% kas perusahaan tercatat otomatis di buku kas.
- **Filter Sopir Siap Dinas**: Hanya sopir yang belum memiliki armada yang muncul pada menu penugasan bus.

### 4. Perancangan Trayek Antarkota & Proyeksi Laba Live
- **Pemetaan Otomatis Jalur Rute**: Sistem otomatis membentuk rute perjalanan lengkap dengan titik transit dan terminal tujuan.
- **Kalkulator Proyeksi Laba Langsung**: Estimasi pendapatan harian dan bulanan langsung terkalkulasi saat tarif tiket diatur.
- **Tampilan Informasi Trayek Rapi**: Menampilkan peta visual koridor, armada yang berdinas, jadwal keberangkatan terurut rapi, dan tarif resmi.

### 5. Peta Simulasi & Pelacakan Bus Nyata (Real-Line Tracking)
- **Berjalan Presisi di Atas Jalur Jalan**: Bus bergerak halus dan tepat menyusuri lekukan jalan raya Jawa Timur.
- **Simulasi Otomatis Berjalan**: Game langsung aktif berjalan saat dibuka dengan kontrol jeda (*pause*) dan pilihan percepatan simulasi (1x, 3x, 5x).
- **Visualisasi Armada Istirahat**: Bus yang menunggu jam keberangkatan berikutnya parkir rapi di terminal keberangkatannya.
- **Pelacakan Kamera Mulus**: Fitur kunci kamera mengikuti armada di peta tanpa lonjakan posisi.

### 6. Waktu Permainan Terintegrasi & Penghasilan Luar Jaringan (Offline Earnings)
- **Waktu Terpadu**: Jam dashboard utama berdetak serasi secara langsung mengikuti jam simulasi operasional bus.
- **Penghasilan Otomatis saat Game Ditutup**: Buka kembali aplikasi untuk menerima laporan pemasukan tiket dan keuntungan bersih yang diraih selama game ditutup.

### 7. Laporan Operasional Seluruh Terminal
- **Pencatatan Menyeluruh**: Merekam aktivitas naik dan turun penumpang di terminal asal, terminal tujuan, serta seluruh terminal transit perantara yang disinggahi.
- **Riwayat Harian**: Tinjau kembali performa dan kepadatan penumpang harian di setiap terminal.

### 8. Buku Kas Keuangan & Pengelolaan Modal
- **Pencatatan Keuangan Bersih**: Pantau mutasi kas dari pemasukan tiket, pengeluaran komisi pengemudi, dan pembelian armada bus.
- **Modal Awal Rp 50.000.000.000**: Keleluasaan penuh membangun jaringan trayek bus impian Anda.
- **Fitur Mulai Baru (Reset Akun)**: Kembalikan kondisi permainan ke awal kapan saja Anda ingin mencoba strategi operasional yang baru.

---

## 🛠️ Arsitektur Teknologi & Kualitas Kode

- **Bahasa Pemrograman:** 100% Kotlin
- **Framework UI:** Jetpack Compose (Material 3)
- **Pola Arsitektur:** Clean Architecture + MVI/MVVM (Domain, Data, Presentation layers)
- **Basis Data Lokal:** Room Database (SQLite) dengan Kotlin Coroutines & Flow
- **Dependency Injection:** Container-based DI (AppContainer)
- **Pengujian Mutu (Quality Assurance):**
  - **372 Automated Unit Tests** mencakup Domain Use Cases, Data Repositories, ViewModels, dan Model BVA.
  - Pengujian Titik Batas BVA 17 Level & Simulasi 2 PP 24-Jam.
  - Verifikasi otomatis *Strictly Zero Comments* untuk menjaga kebersihan basis kode produksi.
