package com.example.klinklinapps.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.klinklinapps.data.Order
import com.example.klinklinapps.data.OrderRepository
import com.example.klinklinapps.data.WeighOutcome
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel Laundry Partner.
 */
class LaundryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private val repository = OrderRepository()

    private var ordersListener: ListenerRegistration? = null
    private var profileListener: ListenerRegistration? = null

    private val _incomingOrders = mutableStateOf<List<Order>>(emptyList())
    val incomingOrders: State<List<Order>> = _incomingOrders

    private val _processingOrders = mutableStateOf<List<Order>>(emptyList())
    val processingOrders: State<List<Order>> = _processingOrders

    private val _completedOrders = mutableStateOf<List<Order>>(emptyList())
    val completedOrders: State<List<Order>> = _completedOrders

    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _laundryName = mutableStateOf("Mitra Laundry")
    val laundryName: State<String> = _laundryName

    // Keuangan
    private val _balance = mutableStateOf(0L)
    val balance: State<Long> = _balance

    private val _totalRevenue = mutableStateOf(0L)
    val totalRevenue: State<Long> = _totalRevenue

    private val _totalWithdrawal = mutableStateOf(0L)
    val totalWithdrawal: State<Long> = _totalWithdrawal

    // Rekening Aktif
    private val _bankName = mutableStateOf("")
    val bankName: State<String> = _bankName

    private val _bankAccountNumber = mutableStateOf("")
    val bankAccountNumber: State<String> = _bankAccountNumber

    private val _bankAccountName = mutableStateOf("")
    val bankAccountName: State<String> = _bankAccountName

    // Request Perubahan Rekening
    private val _bankChangeRequestStatus = mutableStateOf("NONE") // NONE, PENDING, APPROVED, REJECTED
    val bankChangeRequestStatus: State<String> = _bankChangeRequestStatus

    private val _pendingBankName = mutableStateOf("")
    val pendingBankName: State<String> = _pendingBankName

    private val _pendingBankAccountNumber = mutableStateOf("")
    val pendingBankAccountNumber: State<String> = _pendingBankAccountNumber

    private val _pendingBankAccountName = mutableStateOf("")
    val pendingBankAccountName: State<String> = _pendingBankAccountName

    init {
        refresh()
    }

    fun refresh() {
        listenProfile()
        listenOrders()
    }

    private fun listenProfile() {
        val uid = auth.currentUser?.uid ?: return
        profileListener?.remove()
        profileListener = db.collection("users").document(uid)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    _laundryName.value = doc.getString("name") ?: "Mitra Laundry"
                    _balance.value = doc.getLong("balance") ?: 0L
                    _totalRevenue.value = doc.getLong("totalRevenue") ?: 0L
                    _totalWithdrawal.value = doc.getLong("totalWithdrawal") ?: 0L
                    
                    _bankName.value = doc.getString("bankName") ?: ""
                    _bankAccountNumber.value = doc.getString("bankAccountNumber") ?: ""
                    _bankAccountName.value = doc.getString("bankAccountName") ?: ""
                    _bankChangeRequestStatus.value = doc.getString("bankChangeRequestStatus") ?: "NONE"

                    _pendingBankName.value = doc.getString("pendingBankName") ?: ""
                    _pendingBankAccountNumber.value = doc.getString("pendingBankAccountNumber") ?: ""
                    _pendingBankAccountName.value = doc.getString("pendingBankAccountName") ?: ""
                }
            }
    }

    fun requestBankChange(newBankName: String, newAccountName: String, newAccountNumber: String) {
        val uid = auth.currentUser?.uid ?: return
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                db.collection("users").document(uid).update(
                    mapOf(
                        "pendingBankName" to newBankName,
                        "pendingBankAccountName" to newAccountName,
                        "pendingBankAccountNumber" to newAccountNumber,
                        "bankChangeRequestStatus" to "PENDING",
                        "bankChangeRequestedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                _isProcessing.value = false
            } catch (e: Exception) {
                _isProcessing.value = false
                _errorMessage.value = e.localizedMessage ?: "Gagal mengajukan perubahan rekening"
            }
        }
    }

    fun listenOrders() {
        val uid = auth.currentUser?.uid ?: return
        ordersListener?.remove()
        ordersListener = db.collection("orders")
            .whereEqualTo("laundryUid", uid)
            .limit(80)
            .addSnapshotListener { snap, e ->
                if (e != null || snap == null) return@addSnapshotListener
                val all = snap.documents.mapNotNull { mapOrder(it) }
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
                _incomingOrders.value = all.filter { it.status == "DI_LAUNDRY" || it.status == "ON_HOLD" }
                _processingOrders.value = all.filter { it.status == "DIPROSES" }
                _completedOrders.value = all.filter { it.status == "MENUNGGU_DIANTAR" || it.status == "SELESAI" }
            }
    }

    fun withdrawBalance(amount: Long, onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        if (amount <= 0) {
            _errorMessage.value = "Nominal harus lebih dari 0"
            return
        }
        if (amount > _balance.value) {
            _errorMessage.value = "Saldo tidak mencukupi"
            return
        }
        if (_bankAccountNumber.value.isEmpty()) {
            _errorMessage.value = "Atur rekening tujuan terlebih dahulu"
            return
        }

        _isProcessing.value = true
        viewModelScope.launch {
            try {
                db.runTransaction { transaction ->
                    val userRef = db.collection("users").document(uid)
                    val snap = transaction.get(userRef)
                    val currentBalance = snap.getLong("balance") ?: 0L
                    
                    if (currentBalance < amount) throw Exception("Saldo tidak mencukupi")
                    
                    transaction.update(userRef, mapOf(
                        "balance" to FieldValue.increment(-amount),
                        "totalWithdrawal" to FieldValue.increment(amount)
                    ))
                    
                    // Catat riwayat withdrawal
                    val withdrawalId = db.collection("withdrawals").document().id
                    transaction.set(db.collection("withdrawals").document(withdrawalId), mapOf(
                        "userId" to uid,
                        "amount" to amount,
                        "status" to "COMPLETED",
                        "bankName" to _bankName.value,
                        "bankAccountName" to _bankAccountName.value,
                        "bankAccountNumber" to _bankAccountNumber.value,
                        "createdAt" to FieldValue.serverTimestamp()
                    ))
                }.await()
                _isProcessing.value = false
                onSuccess()
            } catch (e: Exception) {
                _isProcessing.value = false
                _errorMessage.value = e.localizedMessage ?: "Gagal melakukan penarikan"
            }
        }
    }

    /** Timbang di Pesanan Masuk. Jika saldo kurang -> ON_HOLD (draft tersimpan). */
    fun weighAndProcess(order: Order, weight: Double, actualSubtotal: Long) {
        _isProcessing.value = true
        _errorMessage.value = null
        val totalActual = actualSubtotal + order.serviceFee + order.deliveryFee
        viewModelScope.launch {
            val result = repository.weighOrder(order.id, weight, actualSubtotal)
            _isProcessing.value = false
            result.onSuccess { outcome -> handleOutcome(order, totalActual, outcome) }
            result.onFailure { e -> _errorMessage.value = e.localizedMessage ?: "Gagal memproses timbangan" }
        }
    }

    /** Cek pembayaran order ON_HOLD; jika saldo customer sudah cukup -> DIPROSES. */
    fun confirmHold(order: Order) {
        _isProcessing.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            val result = repository.confirmHold(order.id)
            _isProcessing.value = false
            result.onSuccess { outcome -> handleOutcome(order, order.totalPrice, outcome) }
            result.onFailure { e -> _errorMessage.value = e.localizedMessage ?: "Gagal memeriksa pembayaran" }
        }
    }

    private fun handleOutcome(order: Order, totalActual: Long, outcome: WeighOutcome) {
        when (outcome) {
            is WeighOutcome.OnHold -> {
                _errorMessage.value = "Saldo customer kurang Rp ${outcome.shortfall}. Pesanan ditahan (ON HOLD) menunggu pembayaran."
                sendAutoMessage(
                    order, _laundryName.value,
                    "Halo, total cucian Anda menjadi Rp $totalActual. Saldo Anda masih kurang Rp ${outcome.shortfall}. " +
                        "Mohon top up saldo agar cucian dapat kami proses. Setelah top up, mohon konfirmasi lewat chat ini 🙏"
                )
            }
            is WeighOutcome.Processed -> when {
                outcome.adjustment < 0 -> sendAutoMessage(
                    order, "KlinKlin Support",
                    "Kabar baik! Total akhir cucian Anda lebih murah dari estimasi. Kelebihan Rp ${-outcome.adjustment} " +
                        "telah dikembalikan ke saldo KlinPay Anda oleh KlinKlin Support. Terima kasih 🙏"
                )
                outcome.adjustment > 0 -> sendAutoMessage(
                    order, _laundryName.value,
                    "Pembayaran diterima. Biaya menyesuaikan berat aktual (+Rp ${outcome.adjustment}). " +
                        "Cucian Anda sedang kami proses 🧺"
                )
                else -> sendAutoMessage(
                    order, _laundryName.value,
                    "Cucian Anda sudah kami timbang dan sedang diproses 🧺"
                )
            }
        }
    }

    /** Selesai diproses laundry -> MENUNGGU_DIANTAR (driver antar balik & menyelesaikan). */
    fun finishOrder(order: Order) {
        _isProcessing.value = true
        _errorMessage.value = null
        db.collection("orders").document(order.id).update(
            mapOf(
                "status" to "MENUNGGU_DIANTAR",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
            .addOnSuccessListener { _isProcessing.value = false }
            .addOnFailureListener { e ->
                _isProcessing.value = false
                _errorMessage.value = e.localizedMessage ?: "Gagal menyelesaikan proses"
            }
    }

    private fun sendAutoMessage(order: Order, senderName: String, text: String) {
        val uid = auth.currentUser?.uid ?: return
        val chatId = "${order.customerUid}_$uid"
        val chatDoc = db.collection("laundry_chats").document(chatId)
        chatDoc.set(mapOf("chatId" to chatId, "updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
        chatDoc.collection("messages").add(
            mapOf(
                "senderId" to uid,
                "senderName" to senderName,
                "message" to text,
                "type" to "SYSTEM",
                "isRead" to false,
                "timestamp" to Timestamp.now()
            )
        )
    }

    fun clearError() { _errorMessage.value = null }

    private fun mapOrder(doc: DocumentSnapshot): Order? {
        return try {
            Order(
                id = doc.id,
                customerUid = doc.getString("customerUid") ?: "",
                laundryUid = doc.getString("laundryUid") ?: "",
                driverUid = doc.getString("driverUid") ?: "",
                status = doc.getString("status") ?: "",
                weight = doc.getDouble("weight") ?: 0.0,
                estimatedPrice = doc.getLong("estimatedPrice") ?: 0L,
                serviceFee = doc.getLong("serviceFee") ?: 0L,
                deliveryFee = doc.getLong("deliveryFee") ?: 0L,
                laundrySubtotal = doc.getLong("laundrySubtotal") ?: 0L,
                totalPrice = doc.getLong("totalPrice") ?: 0L,
                customerName = doc.getString("customerName") ?: "Customer",
                customerPhone = doc.getString("customerPhone") ?: "",
                address = doc.getString("address") ?: "",
                service = doc.getString("service") ?: "",
                driverName = doc.getString("driverName") ?: "",
                driverPhone = doc.getString("driverPhone") ?: "",
                escrowHeld = doc.getLong("escrowHeld") ?: 0L,
                priceAdjustment = doc.getLong("priceAdjustment") ?: 0L,
                createdAt = doc.getTimestamp("createdAt")
            )
        } catch (e: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        ordersListener?.remove()
        profileListener?.remove()
    }
}
