package com.example.travelupa.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint

data class Wisata(
    val id: String = "",
    val nama: String = "",
    val lokasi: String = "",
    val deskripsiSingkat: String = "",
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val coordinates: GeoPoint = GeoPoint(0.0, 0.0),
    val userId: String = ""
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): Wisata {
            return Wisata(
                id = doc.id,
                nama = doc.getString("nama") ?: "",
                lokasi = doc.getString("lokasi") ?: "",
                deskripsiSingkat = doc.getString("deskripsiSingkat") ?: "",
                rating = doc.getDouble("rating") ?: 0.0,
                imageUrl = doc.getString("imageUrl") ?: "",
                coordinates = doc.getGeoPoint("coordinates") ?: GeoPoint(0.0, 0.0),
                userId = doc.getString("userId") ?: ""
            )
        }
    }
}

data class Coordinates(val latitude: Double, val longitude: Double)

sealed class FirestoreState {
    object Idle : FirestoreState()
    object Loading : FirestoreState()
    data class Success(val message: String? = null) : FirestoreState()
    data class Error(val message: String) : FirestoreState()
}

sealed class ScreenState(val route: String) {
    object Splash : ScreenState("splash")
    object Login : ScreenState("login")
    object Register : ScreenState("register")
    object Dashboard : ScreenState("dashboard")
    object TambahWisata : ScreenState("tambah_wisata")
}