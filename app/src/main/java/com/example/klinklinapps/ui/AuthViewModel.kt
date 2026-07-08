package com.example.klinklinapps.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
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

    // Data profil live dari Firestore (untuk halaman kelola profil)
    private val _userName = mutableStateOf("")
    val userName: State<String> = _userName

    private val _userPhone = mutableStateOf("")
    val userPhone: State<String> = _userPhone

    private val _userAddress = mutableStateOf("")
    val userAddress: State<String> = _userAddress

    private val _isSavingProfile = mutableStateOf(false)
    val isSavingProfile: State<Boolean> = _isSavingProfile

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
                        _userName.value = snapshot.getString("name") ?: ""
                        _userPhone.value = snapshot.getString("phone") ?: ""
                        _userAddress.value = snapshot.getString("address") ?: ""

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

    /**
     * Update data profil (nama, telepon, alamat) + opsional ganti password.
     * Ganti password memakai re-authenticate (pakai password lama) agar user
     * TETAP login tanpa harus login ulang manual.
     */
    fun updateProfile(
        name: String,
        phone: String,
        address: String,
        currentPassword: String,
        newPassword: String,
        onDone: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onDone(false, "Sesi tidak ditemukan, silakan login ulang.")
            return
        }
        _isSavingProfile.value = true
        db.collection("users").document(user.uid).update(
            mapOf(
                "name" to name.trim(),
                "phone" to phone.trim(),
                "address" to address.trim()
            )
        ).addOnSuccessListener {
            if (newPassword.isBlank()) {
                _isSavingProfile.value = false
                onDone(true, null)
                return@addOnSuccessListener
            }
            val email = user.email
            if (email.isNullOrBlank() || currentPassword.isBlank()) {
                _isSavingProfile.value = false
                onDone(false, "Profil tersimpan. Untuk ganti password, isi juga 'Password Saat Ini'.")
                return@addOnSuccessListener
            }
            // Re-auth dulu agar tidak perlu login ulang manual
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            _isSavingProfile.value = false
                            onDone(true, null)
                        }
                        .addOnFailureListener { e ->
                            _isSavingProfile.value = false
                            onDone(false, "Profil tersimpan, tapi password gagal diubah: ${e.localizedMessage}")
                        }
                }
                .addOnFailureListener {
                    _isSavingProfile.value = false
                    onDone(false, "Profil tersimpan, tapi 'Password Saat Ini' salah — password tidak diubah.")
                }
        }.addOnFailureListener { e ->
            _isSavingProfile.value = false
            onDone(false, e.localizedMessage ?: "Gagal menyimpan profil")
        }
    }

    /**
     * Hapus akun permanen: data Firestore + akun Auth.
     * Berlaku untuk semua role (customer/driver/laundry).
     */
    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onError("Sesi tidak ditemukan, silakan login ulang.")
            return
        }
        val uid = user.uid
        userListener?.remove()

        db.collection("users").document(uid).delete()
            .addOnSuccessListener {
                user.delete()
                    .addOnSuccessListener {
                        _currentUser.value = null
                        _userRole.value = null
                        _balance.value = 0L
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        // Auth.delete() butuh login baru-baru ini
                        auth.signOut()
                        _currentUser.value = null
                        _userRole.value = null
                        onError(
                            "Data akun terhapus, namun sesi login perlu diperbarui: " +
                                "${e.localizedMessage ?: "silakan login ulang untuk menghapus tuntas."}"
                        )
                    }
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Gagal menghapus data akun")
            }
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
    }

    fun clearError() { _errorMessage.value = null }
}
