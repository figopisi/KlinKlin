package com.example.klinklinapps.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.Timestamp
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class Order(
    val id: String = "",
    val customerUid: String = "",
    val laundryUid: String = "",
    val driverUid: String = "",
    val status: String = "",
    val weight: Double = 0.0,
    val estimatedPrice: Long = 0,
    val serviceFee: Long = 0,    // Biaya Jasa Tetap
    val deliveryFee: Long = 0,   // Ongkir Tetap
    val laundrySubtotal: Long = 0, // Harga murni dari laundry (setelah timbang)
    val totalPrice: Long = 0,      // Harga Total Akhir (Subtotal + Jasa + Ongkir)
    val customerName: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val createdAt: Timestamp? = null
)

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Fungsi utama untuk update status pesanan dengan logic pembayaran token.
     */
    suspend fun updateStatusPesanan(
        orderId: String, 
        newStatus: String, 
        weight: Double? = null, 
        actualLaundrySubtotal: Long? = null
    ): Result<Unit> {
        return try {
            val orderRef = db.collection("orders").document(orderId)
            val order = orderRef.get().await().toObject(Order::class.java) 
                ?: return Result.failure(Exception("Pesanan tidak ditemukan"))

            when (newStatus) {
                "DITIMBANG" -> {
                    if (actualLaundrySubtotal == null || weight == null || actualLaundrySubtotal <= 0) {
                        return Result.failure(Exception("Berat dan harga subtotal laundry harus diisi"))
                    }
                    
                    // Logic: Harga Total Baru = Subtotal Laundry + Biaya Jasa Awal + Ongkir Awal
                    val totalActualPrice = actualLaundrySubtotal + order.serviceFee + order.deliveryFee
                    val difference = totalActualPrice - order.estimatedPrice
                    
                    processPriceAdjustment(orderId, difference, order.customerUid, totalActualPrice, actualLaundrySubtotal, weight)
                }

                "SELESAI" -> {
                    if (order.totalPrice <= 0) return Result.failure(Exception("Data harga pesanan tidak valid"))
                    processTokenPayment(orderId, order.totalPrice, "ADMIN_SYSTEM", order.laundryUid)
                }

                "DRIVER_MENGANTAR" -> {
                    orderRef.update(
                        "status", "DRIVER_MENGANTAR",
                        "updatedAt", FieldValue.serverTimestamp()
                    ).await()
                    Result.success(Unit)
                }

                else -> {
                    orderRef.update(
                        "status", newStatus,
                        "updatedAt", FieldValue.serverTimestamp()
                    ).await()
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun processPriceAdjustment(
        orderId: String, 
        difference: Long, 
        customerUid: String,
        totalActualPrice: Long,
        actualLaundrySubtotal: Long,
        weight: Double
    ): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                val customerRef = db.collection("users").document(customerUid)
                val orderRef = db.collection("orders").document(orderId)
                val adminRef = db.collection("system").document("escrow")

                if (difference > 0) {
                    val customerSnap = transaction.get(customerRef)
                    val balance = customerSnap.getLong("balance") ?: 0L
                    if (balance < difference) {
                        throw Exception("Saldo tidak cukup untuk membayar selisih Rp $difference")
                    }
                    transaction.update(customerRef, "balance", FieldValue.increment(-difference))
                    transaction.set(adminRef, mapOf("totalHold" to FieldValue.increment(difference)), SetOptions.merge())
                } else if (difference < 0) {
                    transaction.update(customerRef, "balance", FieldValue.increment(-difference))
                    transaction.set(adminRef, mapOf("totalHold" to FieldValue.increment(difference)), SetOptions.merge())
                }

                transaction.update(orderRef, mapOf(
                    "status" to "DIPROSES",
                    "laundrySubtotal" to actualLaundrySubtotal,
                    "totalPrice" to totalActualPrice,
                    "weight" to weight,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun processTokenPayment(
        orderId: String, 
        amount: Long, 
        dari: String, 
        ke: String
    ): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                val orderRef = db.collection("orders").document(orderId)
                
                if (dari != "ADMIN_SYSTEM") {
                    val pengirimRef = db.collection("users").document(dari)
                    val pengirimSnap = transaction.get(pengirimRef)
                    val saldoSekarang = pengirimSnap.getLong("balance") ?: 0L
                    
                    if (saldoSekarang < amount) {
                        throw Exception("Saldo Token tidak mencukupi")
                    }
                    transaction.update(pengirimRef, "balance", FieldValue.increment(-amount))
                } else {
                    val adminRef = db.collection("system").document("escrow")
                    transaction.set(adminRef, mapOf("totalHold" to FieldValue.increment(-amount)), SetOptions.merge())
                }

                if (ke != "ADMIN_SYSTEM") {
                    val penerimaRef = db.collection("users").document(ke)
                    transaction.update(penerimaRef, "balance", FieldValue.increment(amount))
                } else {
                    val adminRef = db.collection("system").document("escrow")
                    transaction.set(adminRef, mapOf("totalHold" to FieldValue.increment(amount)), SetOptions.merge())
                }

                transaction.update(orderRef, mapOf(
                    "status" to "SELESAI",
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
