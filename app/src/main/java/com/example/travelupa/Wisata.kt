import com.google.firebase.firestore.GeoPoint

data class Wisata(
    val id: String = "",
    val nama: String = "",
    val lokasi: String = "",
    val deskripsiSingkat: String = "",
    val rating: Double = 0.0,
    val coordinates: GeoPoint = GeoPoint(0.0, 0.0),
    val userId: String = "",
    val imageUrl: String = ""
)