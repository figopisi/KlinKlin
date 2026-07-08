package com.example.klinklinapps.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class BankChangeRequest(
    val userId: String,
    val laundryName: String,
    val currentBank: String,
    val currentAccount: String,
    val pendingBank: String,
    val pendingAccountName: String,
    val pendingAccountNumber: String,
    val requestedAt: com.google.firebase.Timestamp?
)

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var requestsListener: ListenerRegistration? = null

    private val _bankRequests = mutableStateOf<List<BankChangeRequest>>(emptyList())
    val bankRequests: State<List<BankChangeRequest>> = _bankRequests

    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    init {
        listenToRequests()
    }

    private fun listenToRequests() {
        requestsListener?.remove()
        requestsListener = db.collection("users")
            .whereEqualTo("role", "laundry")
            .whereEqualTo("bankChangeRequestStatus", "PENDING")
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    val list = snap.documents.map { doc ->
                        BankChangeRequest(
                            userId = doc.id,
                            laundryName = doc.getString("name") ?: "Laundry",
                            currentBank = doc.getString("bankName") ?: "Belum diatur",
                            currentAccount = doc.getString("bankAccountNumber") ?: "-",
                            pendingBank = doc.getString("pendingBankName") ?: "",
                            pendingAccountName = doc.getString("pendingBankAccountName") ?: "",
                            pendingAccountNumber = doc.getString("pendingBankAccountNumber") ?: "",
                            requestedAt = doc.getTimestamp("bankChangeRequestedAt")
                        )
                    }
                    _bankRequests.value = list
                }
            }
    }

    fun approveBankChange(request: BankChangeRequest) {
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                db.collection("users").document(request.userId).update(
                    mapOf(
                        "bankName" to request.pendingBank,
                        "bankAccountName" to request.pendingAccountName,
                        "bankAccountNumber" to request.pendingAccountNumber,
                        "bankChangeRequestStatus" to "APPROVED",
                        "pendingBankName" to null,
                        "pendingBankAccountName" to null,
                        "pendingBankAccountNumber" to null
                    )
                ).await()
                _isProcessing.value = false
            } catch (e: Exception) {
                _isProcessing.value = false
            }
        }
    }

    fun rejectBankChange(request: BankChangeRequest) {
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                db.collection("users").document(request.userId).update(
                    mapOf(
                        "bankChangeRequestStatus" to "REJECTED",
                        "pendingBankName" to null,
                        "pendingBankAccountName" to null,
                        "pendingBankAccountNumber" to null
                    )
                ).await()
                _isProcessing.value = false
            } catch (e: Exception) {
                _isProcessing.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        requestsListener?.remove()
    }
}
