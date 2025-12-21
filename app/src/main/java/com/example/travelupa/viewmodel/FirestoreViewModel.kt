package com.example.travelupa.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelupa.data.FirestoreState
import com.example.travelupa.data.Wisata
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _wisataList = MutableStateFlow<List<Wisata>>(emptyList())
    val wisataList: StateFlow<List<Wisata>> = _wisataList.asStateFlow()

    private val _firestoreState = MutableStateFlow<FirestoreState>(FirestoreState.Idle)
    val firestoreState: StateFlow<FirestoreState> = _firestoreState.asStateFlow()

    init {
        getWisataRealtime()
    }

    private fun getWisataRealtime() {
        db.collection("wisata").orderBy("nama", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreVM", "Listen failed.", error)
                    _firestoreState.value = FirestoreState.Error("Gagal mengambil data.")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _wisataList.value = snapshot.toObjects(Wisata::class.java)
                }
            }
    }

    fun addWisata(
        nama: String,
        lokasi: String,
        deskripsi: String,
        rating: Double,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _firestoreState.value = FirestoreState.Loading
            try {
                if (imageUri == null) {
                    _firestoreState.value = FirestoreState.Error("Gambar wajib diupload.")
                    return@launch
                }

                val userId = auth.currentUser?.uid ?: run {
                    _firestoreState.value = FirestoreState.Error("User tidak terautentikasi.")
                    return@launch
                }

                val storageRef = storage.reference.child("wisata_images/${System.currentTimeMillis()}_${imageUri.lastPathSegment}")
                val uploadTask = storageRef.putFile(imageUri).await()
                val imageUrl = uploadTask.storage.downloadUrl.await().toString()

                val docRef = db.collection("wisata").document()
                val wisataBaru = Wisata(
                    id = docRef.id,
                    userId = userId,
                    nama = nama,
                    lokasi = lokasi,
                    deskripsiSingkat = deskripsi,
                    rating = rating,
                    imageUrl = imageUrl
                )

                docRef.set(wisataBaru).await()
                _firestoreState.value = FirestoreState.Success("Data berhasil ditambahkan!")

            } catch (e: Exception) {
                Log.e("FirestoreVM", "Error adding wisata: ", e)
                _firestoreState.value = FirestoreState.Error(e.message ?: "Gagal menambahkan data.")
            }
        }
    }

    fun deleteWisata(wisata: Wisata) {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null || currentUserId != wisata.userId) {
                Log.w("FirestoreVM", "Unauthorized delete attempt!")
                return@launch
            }

            _firestoreState.value = FirestoreState.Loading
            try {
                db.collection("wisata").document(wisata.id).delete().await()
                if (wisata.imageUrl.isNotBlank()) {
                    storage.getReferenceFromUrl(wisata.imageUrl).delete().await()
                }
                _firestoreState.value = FirestoreState.Success("Data berhasil dihapus.")
            } catch (e: Exception) {
                Log.e("FirestoreVM", "Error deleting wisata", e)
                _firestoreState.value = FirestoreState.Error(e.message ?: "Gagal menghapus data.")
            }
        }
    }

    fun resetState() {
        _firestoreState.value = FirestoreState.Idle
    }
}