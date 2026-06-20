# DiyyMusic v0.6.3 Patch

## Perubahan

- Menghapus thumb/handle vertikal bawaan Material 3 pada progress lagu.
- Menghapus thumb/handle vertikal bawaan Material 3 pada kontrol volume.
- Mengganti keduanya dengan track bersih tanpa garis tegak.
- Tap dan geser tetap berfungsi di seluruh area slider.
- Playback, seek, volume, shuffle, repeat, queue, API, database, dan backend lain tidak diubah.
- Version code dinaikkan dari 8 ke 9.
- Version name dinaikkan dari 0.6.2 ke 0.6.3.
- Nama workflow dan artefak GitHub Actions diperbarui ke v0.6.3.

## Cara pasang

Ekstrak isi ZIP langsung ke root proyek `DiyyMusic`, lalu timpa file lama. Struktur ZIP sudah memakai path proyek dan tidak dibungkus folder tambahan.

Untuk membuat APK, jalankan workflow **Build DiyyMusic v0.6.3 APK** di tab GitHub Actions.

## Validasi

- Struktur dan pasangan kurung Kotlin diperiksa.
- Tidak ada lagi pemakaian komponen `Material3 Slider` di `PlayerScreen.kt`.
- Build Android penuh belum dijalankan di lingkungan ini karena source lengkap dan dependency Gradle tidak tersedia secara lokal.
