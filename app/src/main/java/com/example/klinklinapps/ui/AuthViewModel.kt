package com.example.klinklinapps.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var userListener: ListenerRegistration? = null

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _currentUser = mutableStateOf(auth.currentUser)
    val currentUser: State<com.google.firebase.auth.FirebaseUser?> = _currentUser

    private val _userRole = mutableStateOf<String?>(null)
    val userRole: State<String?> = _userRole

    private val _balance = mutableStateOf(0L)
    val balance: State<Long> = _balance

    init {
        checkUserRole()
    }

    fun checkUserRole() {
        val user = auth.currentUser
        _currentUser.value = user
        if (user != null) {
            _isLoading.value = true
            userListener?.remove()
            
            userListener = db.collection("users").document(user.uid)
                .addSnapshotListener { snapshot, e ->
                    _isLoading.value = false
                    if (e != null) {
                        _errorMessage.value = "Gagal memuat data: ${e.localizedMessage}"
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null && snapshot.exists()) {
                        _userRole.value = snapshot.getString("role") ?: "customer"
                        
                        // Cek jika field balance tidak ada, inisialisasi ke 0 di DB
                        if (!snapshot.contains("balance")) {
                            db.collection("users").document(user.uid).set(
                                mapOf("balance" to 0L),
                                SetOptions.merge()
                            )
                        } else {
                            _balance.value = snapshot.getLong("balance") ?: 0L
                        }
                    } else if (snapshot != null && !snapshot.exists()) {
                        createDefaultUserDocument(user.uid, user.email ?: "")
                    }
                }
        } else {
            _userRole.value = null
            _balance.value = 0L
            userListener?.remove()
        }
    }

    private fun createDefaultUserDocument(uid: String, email: String) {
        val userData = hashMapOf(
            "uid" to uid,
            "name" to email.substringBefore("@"),
            "email" to email,
            "phone" to "",
            "role" to "customer",
            "balance" to 0L, // Pastikan 0 untuk user baru
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(uid).set(userData)
    }

    fun topUp(amount: Long, onSuccess: () -> Unit) {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid)
            .update("balance", FieldValue.increment(amount))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                _errorMessage.value = "Top up gagal: ${e.localizedMessage}"
            }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email dan password tidak boleh kosong"
            return
        }
        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkUserRole()
                    onSuccess()
                } else {
                    _isLoading.value = false
                    _errorMessage.value = task.exception?.localizedMessage ?: "Login gagal"
                }
            }
    }

    fun register(email: String, password: String, name: String, phone: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userData = hashMapOf(
                            "uid" to user.uid,
                            "name" to name,
                            "email" to email,
                            "phone" to phone,
                            "role" to "customer",
                            "balance" to 0L,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                        db.collection("users").document(user.uid).set(userData)
                            .addOnSuccessListener {
                                checkUserRole()
                                onSuccess()
                            }
                    }
                } else {
                    _isLoading.value = false
                    _errorMessage.value = task.exception?.localizedMessage ?: "Registrasi gagal"
                }
            }
    }

    fun logout() {
        userListener?.remove()
        auth.signOut()
        _currentUser.value = null
        _userRole.value = null
        _balance.value = 0L
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
    }

    fun clearError() { _errorMessage.value = null }
}
