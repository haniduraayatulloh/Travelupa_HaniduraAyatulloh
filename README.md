# 🗺️ TRAVELUPA - Aplikasi Rekomendasi Wisata Berbasis Lokasi (Mobile App)

**Proyek Tugas Akhir Individu**

Aplikasi Travelupa adalah aplikasi mobile yang dibangun menggunakan Kotlin dan Jetpack Compose. Tujuan utamanya adalah menyediakan platform bagi pengguna untuk berbagi, mencari, dan menemukan tempat wisata baru, dengan memanfaatkan data lokasi (Geolocation) dan Firebase Firestore.

---

## 👨‍💻 IDENTITAS PENGEMBANG

| Kategori | Detail |
| :--- | :--- |
| **Nama Lengkap** | **HANIDURA AYATULLOH** |
| **NIM / NRP** | **225150207111005** |
| **Program Studi** | **Teknik Informatika** |
| **Universitas** | [NAMA UNIVERSITAS, SILAKAN GANTI] |
| **Email Kontak** | [ALAMAT EMAIL AKTIF, SILAKAN GANTI] |

---

## 📈 STATUS BAB PENYELESAIAN PROYEK

Proyek ini terbagi dalam 9 Bab penyelesaian. Status per [Tanggal Pengumpulan/Deadline] adalah sebagai berikut:

### ✅ Bab Terselesaikan (1 - 8)

| Bab | Deskripsi | Status | Catatan |
| :--- | :--- | :--- | :--- |
| **Bab 1** | Pendahuluan / Latar Belakang | Selesai | |
| **Bab 2** | Tinjauan Pustaka | Selesai | |
| **Bab 3** | Metodologi Penelitian | Selesai | |
| **Bab 4** | Desain Sistem | Selesai | |
| **Bab 5** | Implementasi Authentication | Selesai | |
| **Bab 6** | Implementasi Data dan Fitur CRUD | Selesai | |
| **Bab 7** | Implementasi Geolocation & Database | Selesai | |
| **Bab 8** | Pengujian Sistem (Testing) | Selesai | **Selesai 1 Menit Sebelum Deadline Pengumpulan.** |

### ❌ Bab Belum Terselesaikan (Bab 9)

| Bab | Deskripsi | Status | Catatan |
| :--- | :--- | :--- | :--- |
| **Bab 9** | Analisis dan Hasil Akhir | Belum Selesai | Membutuhkan waktu tambahan untuk menyusun analisis data hasil pengujian secara komprehensif. |

---

## ⚠️ KENDALA DAN HAMBATAN PROSES PENGEMBANGAN

Proses pengembangan proyek mengalami kendala signifikan yang mayoritas terkait dengan konfigurasi lingkungan *build* (Gradle).

### 1. Inkompatibilitas Gradle Toolchain (KSP/KAPT Error)

Kendala utama adalah *error* yang berulang dan sulit diatasi yang melibatkan plugin *Annotation Processing* di Gradle. 

* **Plugin Terkena:** **KSP** (Kotlin Symbol Processing), dan **KAPT** (Kotlin Annotation Processing Tool).
* **Penyebab Inti:** Ketidakcocokan versi kritis antara **Plugin Kotlin** (saat menggunakan versi 2.0.x), **Plugin KSP** (misalnya `2.0.0-1.0.21`), dan versi **Android Gradle Plugin (AGP)**.
* **Dampak:** Menghasilkan *runtime error* seperti `Unable to find method '... KspTaskJvm.getChangedFiles(...)'`, yang memerlukan penyesuaian manual dan pembersihan *cache* yang berulang.

### 2. Isu Kompatibilitas Library dan Target API

* **Penyebab:** *Library* Compose dan AndroidX yang modern (seperti Compose BOM 2024.x) mewajibkan proyek dikompilasi terhadap **Android API 36 (Android 12)**.
* **Dampak:** Memicu 19 *issues* AAR *metadata* yang mengharuskan peningkatan `compileSdk` secara menyeluruh agar *build* dapat diselesaikan.

### 3. Masalah Type Safety ROOM/Firebase

* **Penyebab:** Konflik saat menyimpan tipe data spesifik Firebase, yaitu **`GeoPoint`**, ke dalam Room Database, karena ROOM tidak dapat mengenalinya secara *native*.
* **Dampak:** Memerlukan implementasi **`TypeConverter`** dan penyesuaian pada Entity (`Wisata.kt`) dengan `@PrimaryKey` untuk memenuhi persyaratan ROOM/KSP.
