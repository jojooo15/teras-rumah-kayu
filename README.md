# Teras Rumah Kayu

Aplikasi pemesanan makanan berbasis Android untuk rumah makan Teras Rumah Kayu, lengkap dengan REST API-nya.

Dibuat sebagai proyek Penulisan Ilmiah di Universitas Gunadarma (2025): *Perancangan dan Pengembangan Aplikasi Pesan Makan Daring pada Teras Rumah Kayu Berbasis Android*. Pembimbing: Iwan, S.Kom., M.T.

## Fitur

- Splash, registrasi, dan login
- Beranda dengan kategori dan katalog menu
- Detail menu, keranjang, dan checkout
- Manajemen alamat (tambah, ubah, hapus, alamat utama)
- Ulasan dan penilaian menu, riwayat ulasan, dan poin ganjaran
- Profil pengguna

## Teknologi

**Aplikasi Android:** Kotlin, Jetpack Compose, MVVM, Navigation Compose, Retrofit + Gson + OkHttp, Coil, DataStore

**Back-end:** Node.js, Express.js, Mongoose, MongoDB, Helmet, CORS, Morgan, dotenv

## Struktur

```
app/            Aplikasi Android (Kotlin + Compose)
mongodb-api/    REST API (Express + MongoDB)
  models/       Skema Category, FoodItem, Review, User
  routes/       Endpoint API
  scripts/      Seed data dan utilitas database
```

## Menjalankan

### 1. API

Butuh Node.js 16+ dan MongoDB (lokal atau Atlas).

```bash
cd mongodb-api
npm install
cp .env.example .env     # sesuaikan MONGODB_URI
npm run test             # cek koneksi database
npm run seed             # isi data menu dan kategori
npm start                # http://localhost:3000
```

Di Windows, `setup.bat` dan `start-api-server.bat` menjalankan langkah yang sama.

### 2. Aplikasi Android

1. Buka folder ini di Android Studio.
2. Jalankan di emulator. Alamat API default `http://10.0.2.2:3000/` ada di `app/src/main/java/com/jojo/terasrumah/data/api/ApiClient.kt`.
3. Untuk perangkat fisik, ganti alamat itu dengan IP komputer yang menjalankan API.

## Endpoint utama

| Metode | Path | Fungsi |
| --- | --- | --- |
| GET | `/health` | Status server |
| GET | `/foodItems` | Daftar menu (filter `?category=`) |
| GET | `/foodItems/:id` | Detail menu |
| GET | `/categories` | Daftar kategori |
| GET | `/reviews/food/:foodItemId` | Ulasan sebuah menu |
| POST | `/reviews` | Tambah ulasan |
| GET | `/reviews/stats/:foodItemId` | Statistik rating |
| GET | `/users/:userId` | Data pengguna |
| GET | `/users/:userId/points` | Poin ganjaran |
| GET/POST | `/users/:userId/addresses` | Daftar dan tambah alamat |

Daftar lengkapnya ada di folder `mongodb-api/routes`.

## Hasil pengujian

Dari pengujian pada laporan Penulisan Ilmiah: waktu muat rata-rata 1,8 detik, server melayani 150 pengguna serentak, dan kepuasan pengguna 92% pada uji penerimaan.

## Data contoh

`npm run seed` mengisi 30 menu dalam 4 kategori (Paket Makanan Utama, Macam Sambal, Makanan Ringan, dan Minuman).
