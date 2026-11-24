# Roadmap Pengerjaan Season 8: Game Feel, Visual Overhaul & Audio Immersion

Dokumen ini memuat detail perencanaan perbaikan antarmuka game, efek pergerakan armada di peta, sistem audio efek (SFX), dan elemen visual agar **TransNusantara (Bus Manager)** memiliki atmosfer permainan tycoon yang hidup, imersif, dan menyenangkan:

---

## 📌 Rangkuman Rencana Peningkatan "Game Feel" & Visual

1. **Rotasi Sudut Hadap Armada di Peta (Heading Bearing Rotation)**
   - **Masalah Saat Ini:** Penanda bus di peta berupa ikon lingkaran statis yang tidak berbelok mengikuti lekukan jalur jalan.
   - **Solusi:** Menghitung sudut arah (*bearing angle* dalam derajat) dari segmen jalan yang sedang dilalui bus, lalu memutar sprite bus secara halus (*heading rotation*) sehingga bus tampak berbelok dan menghadap ke arah laju jalan raya.

2. **Detail Visual Jalur Peta & Siklus Waktu Siang/Malam (Day & Night Atmosphere)**
   - **Kebutuhan:** Peta simulasi tampak lebih realistis dan mencerminkan waktu operasional 24 jam.
   - **Solusi:**
     - Menambahkan garis marka jalan putus-putus pada jalur tol dan ikon gerbang tol/halte terminal yang hidup.
     - Mode Siang (06:00 – 17:59 WIB) dan Mode Malam (18:00 – 05:59 WIB): saat malam hari, kanvas peta menggelap lembut dan armada bus memancarkan sorot lampu kuning (*headlight beam*) di depannya.

3. **Sistem Efek Suara (Sound Effects / SFX Engine)**
   - **Kebutuhan:** Menghilangkan keheningan aplikasi dengan efek audio interaktif yang ringan dan memuaskan.
   - **Solusi:** Membangun `SoundManager` berbasis `SoundPool` native Android:
     - 🔔 Suara koin kasir (*kling!*) saat tiket terjual dan bus memperoleh pendapatan.
     - 🚌 Suara klakson/telolet bus saat armada diberangkatkan dari terminal.
     - 🔘 Suara klik empuk (*bubble pop*) saat menekan tombol menu navigasi.
     - 📳 Getaran *haptic feedback* lembut pada sentuhan tombol.
     - Sakelar bisu/suara (*Mute/Unmute*) di header utama atau pengaturan.

4. **Penyegaran Tipografi & Desain Antarmuka Game (Playful Game UI)**
   - **Kebutuhan:** Mengubah kesan "aplikasi kantor / dashboard fintech" menjadi "game kasual tycoon".
   - **Solusi:**
     - Mengintegrasikan custom font game kasual ramah (*Fredoka / Nunito Bold*).
     - Mengubah kartu dan tombol menjadi bergaya game (*3D bevel button* dengan bayangan bawah empuk saat ditekan).
     - Palet warna berkarakter: aksen emas berkilau untuk kas/keuangan dan badge rute yang lebih kontras.

5. **Interaksi "Juicy" & Angka Pendapatan Melayang (Floating Numbers)**
   - **Kebutuhan:** Umpan balik visual yang memuaskan saat transaksi atau keberhasilan operasional terjadi.
   - **Solusi:**
     - Teks melayang warna hijau `+Rp ...` (*floating combat/income text*) yang terbang ke atas dan memudar saat bus menurunkan penumpang dan mengantongi omzet.
     - Efek membal (*bouncy scale animation*) pada tombol saat ditekan.
     - Banner transisi pergantian hari operasional (*"Hari Baru Telah Tiba!"*) saat jam simulasi menyentuh 00:00 WIB.

---

## 📋 Pembagian Fase Pengerjaan (Season 8 Roadmap)

### 🔹 Fase 1 [Tinggi]: Visualisasi Peta Dinamis (Rotasi Hadap Bus, Marka Jalan & Siklus Siang-Malam)
- [ ] Kalkulasi sudut hadap (*bearing angle*) pergerakan bus di kanvas peta dan rotasi sprite bus mengikuti lekukan jalan.
- [ ] Detail visual jalan tol dengan marka jalan putus-putus dan gerbang tol/terminal yang lebih hidup.
- [ ] Efek atmosfer visual siang & malam mengikuti jam operasional simulasi (warna peta malam + sorot lampu bus).
- [ ] Verifikasi pergerakan di peta, unit test, dan Git Commit Fase 1.

---

### 🔹 Fase 2 [Tinggi]: Audio Immersion Engine (Sound Effects SFX & Haptic Feedback)
- [ ] Arsitektur `SoundManager` menggunakan Android `SoundPool` yang ringan dan efisien memory.
- [ ] Efek suara kasir (*kling!*) saat bus mencetak omzet tiket penumpang di terminal.
- [ ] Efek suara klakson bus saat armada mulai meluncur dari terminal asal.
- [ ] Efek klik taktil (*bubble/pop click*) dan getaran haptic saat menyentuh tombol antarmuka.
- [ ] Tombol pintas Bisu/Suara (*Audio Toggle*) di bilah atas aplikasi.
- [ ] Verifikasi audio playback, unit test, dan Git Commit Fase 2.

---

### 🔹 Fase 3 [Medium]: Tipografi Game Tycoon & Desain Antarmuka Playful
- [ ] Integrasi Custom Font bergaya game kasual (*Fredoka / Nunito*) pada seluruh komponen teks aplikasi.
- [ ] Pembaruan desain kartu dan tombol dengan gaya tebal (*3D bevel button & drop shadows*).
- [ ] Desain badge saldo emas dan status armada yang lebih kaya warna dan playful.
- [ ] Verifikasi tata letak UI, unit test, dan Git Commit Fase 3.

---

### 🔹 Fase 4 [Final]: Interaksi Juicy (Floating Revenue, Bouncy Touch), QA & Rilis v2.6.0
- [ ] Animasi angka pendapatan melayang (*floating revenue text*) di atas armada bus saat mendapatkan uang tiket.
- [ ] Animasi membal (*bouncy press animation*) pada tombol saat ditekan.
- [ ] Notifikasi banner transisi pergantian hari operasional pada jam 00:00 WIB.
- [ ] Audit zero comments (`0` komentar di seluruh file Kotlin).
- [ ] Full regression testing seluruh unit test (100% passing).
- [ ] Bump versi ke `v2.6.0` (versionCode 13), build signed release APK (`app-release.apk`), dan publikasi Git Tag Rilis `v2.6.0`.
