package com.example.klinklinapps.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.klinklinapps.data.Order
import com.example.klinklinapps.data.OrderRepository
import com.example.klinklinapps.data.ReminderInfo
import com.example.klinklinapps.data.ReminderService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrdersViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private val repository = OrderRepository()
    private val reminderService = ReminderService()
    private var ordersListener: ListenerRegistration? = null

    private val _orders = mutableStateOf<List<Order>>(emptyList())
    val orders: State<List<Order>> = _orders

    private val _reminder = mutableStateOf<ReminderInfo?>(null)
    val reminder: State<ReminderInfo?> = _reminder

    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        listenToOrders()
    }

    fun listenToOrders() {
        val user = auth.currentUser ?: return
        
        ordersListener?.remove()
        
        ordersListener = db.collection("orders")
            .whereEqualTo("customerUid", user.uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val ordersList = snapshot.documents.mapNotNull { doc ->
                        try {
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
                                driverName = doc.getString("driverName") ?: "",
                                driverPhone = doc.getString("driverPhone") ?: "",
                                createdAt = doc.getTimestamp("createdAt"),
                                
                                // Mapping Field Tracking Baru
                                customerLat = doc.getDouble("customerLat") ?: 0.0,
                                customerLng = doc.getDouble("customerLng") ?: 0.0,
                                laundryLat = doc.getDouble("laundryLat") ?: 0.0,
                                laundryLng = doc.getDouble("laundryLng") ?: 0.0,
                                driverLat = doc.getDouble("driverLat") ?: 0.0,
                                driverLng = doc.getDouble("driverLng") ?: 0.0
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    _orders.value = ordersList.sortedByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE }
                    
                    // Trigger reminder check when orders are loaded
                    checkReminders(user.uid)
                }
            }
    }

    private fun checkReminders(uid: String) {
        viewModelScope.launch {
            _reminder.value = reminderService.checkLaundryCycle(uid)
        }
    }

    fun placeOrder(
        service: String, 
        estimatedPrice: Long,
        serviceFee: Long,
        deliveryFee: Long,
        userName: String,
        userPhone: String,
        userAddress: String,
        onSuccess: () -> Unit
    ) {
        val user = auth.currentUser ?: return
        _isProcessing.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                db.runTransaction { transaction ->
                    val userRef = db.collection("users").document(user.uid)
                    val adminRef = db.collection("system").document("escrow")
                    
                    // 1. Cek Saldo User
                    val userSnap = transaction.get(userRef)
                    val currentBalance = userSnap.getLong("balance") ?: 0L
                    
                    if (currentBalance < estimatedPrice) {
                        throw Exception("Saldo Token tidak mencukupi (Rp $estimatedPrice). Silahkan Top Up!")
                    }
                    
                    // 2. Kurangi Saldo User
                    transaction.update(userRef, "balance", FieldValue.increment(-estimatedPrice))
                    
                    // 3. Tambah ke Escrow (Gunakan set merge agar tidak error jika dokumen belum ada)
                    transaction.set(adminRef, mapOf("totalHold" to FieldValue.increment(estimatedPrice)), SetOptions.merge())
                    
                    // 4. Buat Dokumen Pesanan
                    val orderId = db.collection("orders").document().id
                    val newOrder = hashMapOf(
                        "customerUid" to user.uid,
                        "customerName" to userName,
                        "customerPhone" to userPhone,
                        "address" to userAddress,
                        "service" to service,
                        "status" to "MENUNGGU_PICKUP",
                        "estimatedPrice" to estimatedPrice,
                        "serviceFee" to serviceFee,
                        "deliveryFee" to deliveryFee,
                        "laundrySubtotal" to 0L,
                        "totalPrice" to 0L,
                        "weight" to 0.0,
                        "createdAt" to FieldValue.serverTimestamp(),
                        
                        // Default Coordinates (Simulasi: Sebaiknya dapet dari FusedLocation)
                        "customerLat" to -6.2088, 
                        "customerLng" to 106.8456,
                        "laundryLat" to -6.2100,
                        "laundryLng" to 106.8500
                    )
                    transaction.set(db.collection("orders").document(orderId), newOrder)
                }.await()
                
                _isProcessing.value = false
                listenToOrders()
                onSuccess()
            } catch (e: Exception) {
                _isProcessing.value = false
                _errorMessage.value = e.localizedMessage ?: "Gagal membuat pesanan"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ordersListener?.remove()
    }

    fun completeOrder(orderId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.updateStatusPesanan(orderId, "SELESAI")
            _isProcessing.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Gagal menyelesaikan pesanan"
            }
        }
    }

    fun dismissReminder() {
        _reminder.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
