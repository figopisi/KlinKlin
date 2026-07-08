package com.example.klinklinapps.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.klinklinapps.data.Order
import com.example.klinklinapps.data.OrderRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

/**
 * ViewModel untuk Driver Dashboard.
 *
 * Alur status yang ditangani driver:
 *  MENUNGGU_PICKUP    -> (Accept)          -> DRIVER_MENJEMPUT
 *  DRIVER_MENJEMPUT   -> (Sudah di-pickup) -> DIANTAR_KE_LAUNDRY  (muncul di dashboard user)
 *  DIANTAR_KE_LAUNDRY -> (Sampai laundry)  -> DI_LAUNDRY          (laundry mengambil alih)
 */
class DriverViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private val repository = OrderRepository()

    private var availableListener: ListenerRegistration? = null
    private var myOrdersListener: ListenerRegistration? = null

    // Order baru yang belum punya driver (status MENUNGGU_PICKUP)
    private val _availableOrders = mutableStateOf<List<Order>>(emptyList())
    val availableOrders: State<List<Order>> = _availableOrders

    // Order yang sedang di-handle driver ini (jemput / antar ke laundry / di laundry)
    private val _myOrders = mutableStateOf<List<Order>>(emptyList())
    val myOrders: State<List<Order>> = _myOrders

    // Order yang sudah selesai dari laundry & siap diantar balik ke customer
    private val _deliveryOrders = mutableStateOf<List<Order>>(emptyList())
    val deliveryOrders: State<List<Order>> = _deliveryOrders

    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _driverName = mutableStateOf("Driver")
    val driverName: State<String> = _driverName

    private val _driverPhone = mutableStateOf("")

    init {
        refresh()
    }

    /** Pasang ulang listener + profil (dipanggil saat dashboard dibuka / ganti akun). */
    fun refresh() {
        loadDriverProfile()
        listenAvailableOrders()
        listenMyOrders()
    }

    private fun loadDriverProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                _driverName.value = doc.getString("name") ?: "Driver"
                _driverPhone.value = doc.getString("phone") ?: ""
            }
    }

    /** Order yang menunggu driver (belum di-assign). */
    fun listenAvailableOrders() {
        availableListener?.remove()
        availableListener = db.collection("orders")
            .whereEqualTo("status", "MENUNGGU_PICKUP")
            .limit(50)
            .addSnapshotListener { snap, e ->
                if (e != null || snap == null) return@addSnapshotListener
                _availableOrders.value = snap.documents
                    .mapNotNull { mapOrder(it) }
                    .filter { it.driverUid.isEmpty() }
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
    }

    /** Order yang sedang di-handle driver ini. */
    fun listenMyOrders() {
        val uid = auth.currentUser?.uid ?: return
        myOrdersListener?.remove()
        myOrdersListener = db.collection("orders")
            .whereEqualTo("driverUid", uid)
            .limit(50)
            .addSnapshotListener { snap, e ->
                if (e != null || snap == null) return@addSnapshotListener
                val all = snap.documents.mapNotNull { mapOrder(it) }
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
                // Sedang berjalan (jemput -> antar ke laundry -> menunggu laundry proses)
                _myOrders.value = all.filter {
                    it.status in listOf("DRIVER_MENJEMPUT", "DIANTAR_KE_LAUNDRY", "DI_LAUNDRY", "ON_HOLD", "DIPROSES")
                }
                // Sudah selesai dari laundry, siap diantar balik ke customer
                _deliveryOrders.value = all.filter { it.status == "MENUNGGU_DIANTAR" }
            }
    }

    /** Fitur 1: Accept order. Driver mengklaim order & menuju customer. */
    fun acceptOrder(order: Order) {
        val uid = auth.currentUser?.uid ?: return
        _isProcessing.value = true
        _errorMessage.value = null
        db.collection("orders").document(order.id).update(
            mapOf(
                "driverUid" to uid,
                "driverName" to _driverName.value,
                "driverPhone" to _driverPhone.value,
                "status" to "DRIVER_MENJEMPUT",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
            .addOnSuccessListener { _isProcessing.value = false }
            .addOnFailureListener { e ->
                _isProcessing.value = false
                _errorMessage.value = e.localizedMessage ?: "Gagal menerima order"
            }
    }

    /** Lepas order: kembalikan ke antrean (status MENUNGGU_PICKUP, driver dikosongkan). */
    fun releaseOrder(order: Order) {
        _isProcessing.value = true
        _errorMessage.value = null
        db.collection("orders").document(order.id).update(
            mapOf(
                "driverUid" to "",
                "driverName" to "",
                "driverPhone" to "",
                "status" to "MENUNGGU_PICKUP",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
            .addOnSuccessListener { _isProcessing.value = false }
            .addOnFailureListener { e ->
                _isProcessing.value = false
                _errorMessage.value = e.localizedMessage ?: "Gagal melepas order"
            }
    }

    /** Pengantaran akhir ke customer selesai -> SELESAI (pembayaran cair ke laundry). */
    fun finishDelivery(order: Order) {
        _isProcessing.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            val result = repository.completeOrder(order.id)
            _isProcessing.value = false
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Gagal menyelesaikan pengantaran"
            }
        }
    }

    /** Fitur 2: Update status order (pickup -> diantar ke laundry -> sampai laundry). */
    fun advanceStatus(order: Order, newStatus: String) {
        _isProcessing.value = true
        _errorMessage.value = null
        db.collection("orders").document(order.id).update(
            mapOf(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
            .addOnSuccessListener { _isProcessing.value = false }
            .addOnFailureListener { e ->
                _isProcessing.value = false
                _errorMessage.value = e.localizedMessage ?: "Gagal mengubah status"
            }
    }

    fun clearError() { _errorMessage.value = null }

    private fun mapOrder(doc: DocumentSnapshot): Order? {
        return try {
            Order(
                id = doc.id,
                customerUid = doc.getString("customerUid") ?: "",
                laundryUid = doc.getString("laundryUid") ?: "",
                driverUid = doc.getString("driverUid") ?: "",
                status = doc.getString("status") ?: "MENUNGGU_PICKUP",
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
                createdAt = doc.getTimestamp("createdAt"),
                customerLat = doc.getDouble("customerLat") ?: 0.0,
                customerLng = doc.getDouble("customerLng") ?: 0.0,
                laundryLat = doc.getDouble("laundryLat") ?: 0.0,
                laundryLng = doc.getDouble("laundryLng") ?: 0.0,
                driverLat = doc.getDouble("driverLat") ?: 0.0,
                driverLng = doc.getDouble("driverLng") ?: 0.0
            )
        } catch (e: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        availableListener?.remove()
        myOrdersListener?.remove()
    }
}
