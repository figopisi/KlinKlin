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

/**
 * ViewModel Laundry Partner.
 * Order sudah di-assign ke laundry sejak customer memilih (laundryUid terisi).
 *  Pesanan Masuk : DI_LAUNDRY (perlu ditimbang) + ON_HOLD (menunggu pembayaran)
 *  Sedang Diproses: DIPROSES
 *  Selesai        : MENUNGGU_DIANTAR (menunggu driver) + SELESAI
 */
class LaundryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private val repository = OrderRepository()

    private var ordersListener: ListenerRegistration? = null

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

    init {
        refresh()
    }

    /** Pasang ulang listener + profil (dipanggil saat dashboard dibuka / ganti akun). */
    fun refresh() {
        loadProfile()
        listenOrders()
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc -> _laundryName.value = doc.getString("name") ?: "Mitra Laundry" }
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
    }
}
