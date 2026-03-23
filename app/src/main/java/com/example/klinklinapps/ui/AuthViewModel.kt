package com.example.klinklinapps.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _currentUser = mutableStateOf(auth.currentUser)
    val currentUser: State<com.google.firebase.auth.FirebaseUser?> = _currentUser

    private val _userRole = mutableStateOf<String?>(null)
    val userRole: State<String?> = _userRole

    init {
        checkUserRole()
    }

    fun checkUserRole() {
        val user = auth.currentUser
        if (user != null) {
            _isLoading.value = true
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    _userRole.value = document.getString("role") ?: "customer"
                    _isLoading.value = false
                }
                .addOnFailureListener {
                    _userRole.value = "customer"
                    _isLoading.value = false
                }
        } else {
            _userRole.value = null
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email dan password tidak boleh kosong"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        db.collection("users").document(user.uid).get()
                            .addOnSuccessListener { document ->
                                _isLoading.value = false
                                _currentUser.value = user
                                _userRole.value = document.getString("role") ?: "customer"
                                onSuccess()
                            }
                            .addOnFailureListener {
                                _isLoading.value = false
                                _errorMessage.value = "Gagal mengambil data role"
                            }
                    }
                } else {
                    _isLoading.value = false
                    _errorMessage.value = task.exception?.localizedMessage ?: "Login gagal"
                }
            }
    }

    fun register(email: String, password: String, role: String = "customer", onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email dan password tidak boleh kosong"
            return
        }
        
        if (password.length < 6) {
            _errorMessage.value = "Password minimal 6 karakter"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userData = hashMapOf(
                            "email" to email,
                            "role" to role,
                            "uid" to user.uid
                        )
                        db.collection("users").document(user.uid).set(userData)
                            .addOnSuccessListener {
                                _isLoading.value = false
                                _currentUser.value = user
                                _userRole.value = role
                                onSuccess()
                            }
                            .addOnFailureListener {
                                _isLoading.value = false
                                _errorMessage.value = "Gagal menyimpan data user"
                            }
                    }
                } else {
                    _isLoading.value = false
                    _errorMessage.value = task.exception?.localizedMessage ?: "Registrasi gagal"
                }
            }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _userRole.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
