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
| **Universitas** | **Universitas Brawijaya** |

---

## 📈 STATUS BAB PENYELESAIAN PROYEK

Proyek ini terbagi dalam 9 Bab penyelesaian. Status per [Tanggal Pengumpulan/Deadline] adalah sebagai berikut:

### ✅ Bab Terselesaikan (1 - 8)

| Bab | Judul Sub Bab Modul (Sesuai/Derivasi Fitur Modul) | Status | Catatan |
| :--- | :--- | :--- | :--- |
| **Bab 1** | **Android Studio**  | Selesai | 
| **Bab 2** | **Jetpack Compose (Bagian 1)**  | Selesai | 
| **Bab 3** | **Jetpack Compose (Bagian 2)** | Selesai | 
| **Bab 4** | **UI State**  | Selesai |
| **Bab 5** | **Google Firebase dan REST API** | Selesai | 
| **Bab 6** | **Kotlin Coroutines** | Selesai |
| **Bab 7** | **Jetpack Navigation** | Selesai |
| **Bab 9** | **Camera X**  | Selesai | **Selesai 1 Menit Sebelum Deadline Pengumpulan.** |

### ❌ Bab Belum Terselesaikan 

| Bab | Judul Sub Bab Modul (Derivasi) | Status | Catatan |
| :--- | :--- | :--- | :--- |
| **Bab 9** | **Room Database** | Belum Selesai | Terkendala Gradle level Module. |

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
