package com.example.travelupa.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelupa.data.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _userSession = MutableStateFlow(auth.currentUser?.email)
    val userSession: StateFlow<String?> = _userSession

    init {
        _userSession.value = auth.currentUser?.email
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                val userEmail = auth.currentUser?.email
                if (userEmail != null) {
                    _userSession.value = userEmail
                    _authState.value = AuthState.Success("Login berhasil!")
                } else {
                    _authState.value = AuthState.Error("Gagal mendapatkan info user setelah login.")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Email atau password salah."
                    else -> e.message ?: "Login gagal, terjadi error tidak diketahui."
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                val userEmail = auth.currentUser?.email
                if (userEmail != null) {
                     _userSession.value = userEmail
                    _authState.value = AuthState.Success("Registrasi berhasil!")
                } else {
                    _authState.value = AuthState.Error("Gagal mendapatkan info user setelah registrasi.")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthWeakPasswordException -> "Password terlalu lemah (minimal 6 karakter)."
                    is FirebaseAuthInvalidCredentialsException -> "Format email tidak valid."
                    is FirebaseAuthUserCollisionException -> "Email ini sudah terdaftar. Silakan login."
                    else -> e.message ?: "Registrasi gagal, terjadi error tidak diketahui."
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun logout() {
        auth.signOut()
        _userSession.value = null
        _authState.value = AuthState.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}