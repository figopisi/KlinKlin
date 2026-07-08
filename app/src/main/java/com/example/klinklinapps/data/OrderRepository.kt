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
    val serviceFee: Long = 0,      // Biaya Jasa Tetap
    val deliveryFee: Long = 0,     // Ongkir Tetap
    val laundrySubtotal: Long = 0, // Harga murni dari laundry (setelah timbang)
    val totalPrice: Long = 0,      // Harga Total Akhir (Subtotal + Jasa + Ongkir)
    val customerName: String = "",
    val customerPhone: String = "",
    val address: String = "",
    val service: String = "",
    val laundryName: String = "",
    val laundryAddress: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val createdAt: Timestamp? = null,

    // Escrow (per-order, transparan)
    val escrowHeld: Long = 0,       // dana yang ditahan di escrow untuk order ini
    val priceAdjustment: Long = 0,  // selisih saat timbang (+ = tambah bayar, - = refund)

    // Field untuk Tracking Real-time
    val customerLat: Double = 0.0,
    val customerLng: Double = 0.0,
    val laundryLat: Double = 0.0,
    val laundryLng: Double = 0.0,
    val driverLat: Double = 0.0,
    val driverLng: Double = 0.0
)

/** Hasil proses timbang / cek pembayaran escrow. */
sealed class WeighOutcome {
    /** Escrow sudah disesuaikan, order lanjut diproses. adjustment: + tambah bayar, - refund, 0 pas. */
    data class Processed(val adjustment: Long) : WeighOutcome()
    /** Saldo customer kurang; draft tersimpan, order ON_HOLD menunggu pembayaran. */
    data class OnHold(val shortfall: Long, val adjustment: Long) : WeighOutcome()
}

/**
 * ESCROW MODEL (per-order, konservatif):
 */
class OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private fun orderRef(id: String) = db.collection("orders").document(id)
    private fun userRef(uid: String) = db.collection("users").document(uid)
    private val escrowRef = db.collection("system").document("escrow")

    /** Update status sederhana (mis. driver: menjemput/antar/sampai). */
    suspend fun updateStatus(orderId: String, newStatus: String): Result<Unit> = try {
        orderRef(orderId).update(
            "status", newStatus,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Laundry menimbang & menetapkan harga asli. */
    suspend fun weighOrder(orderId: String, weight: Double, actualSubtotal: Long): Result<WeighOutcome> {
        if (actualSubtotal <= 0 || weight <= 0.0) {
            return Result.failure(Exception("Berat dan subtotal laundry harus diisi"))
        }
        return try {
            val outcome = db.runTransaction { tx ->
                val o = tx.get(orderRef(orderId))
                val customerUid = o.getString("customerUid") ?: throw Exception("Order tidak valid")
                val serviceFee = o.getLong("serviceFee") ?: 0L
                val deliveryFee = o.getLong("deliveryFee") ?: 0L
                val estimated = o.getLong("estimatedPrice") ?: 0L
                val totalActual = actualSubtotal + serviceFee + deliveryFee
                val adjustment = totalActual - estimated

                val balance = tx.get(userRef(customerUid)).getLong("balance") ?: 0L

                if (adjustment > 0 && balance < adjustment) {
                    tx.update(orderRef(orderId), mapOf(
                        "status" to "ON_HOLD",
                        "weight" to weight,
                        "laundrySubtotal" to actualSubtotal,
                        "totalPrice" to totalActual,
                        "priceAdjustment" to adjustment,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ))
                    WeighOutcome.OnHold(shortfall = adjustment - balance, adjustment = adjustment)
                } else {
                    applyAdjustment(tx, orderId, customerUid, adjustment, totalActual, weight, actualSubtotal)
                    WeighOutcome.Processed(adjustment)
                }
            }.await()
            Result.success(outcome)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Cek ulang pembayaran order ON_HOLD; jika saldo cukup -> DIPROSES. */
    suspend fun confirmHold(orderId: String): Result<WeighOutcome> {
        return try {
            val outcome = db.runTransaction { tx ->
                val o = tx.get(orderRef(orderId))
                val customerUid = o.getString("customerUid") ?: throw Exception("Order tidak valid")
                val adjustment = o.getLong("priceAdjustment") ?: 0L
                val totalActual = o.getLong("totalPrice") ?: 0L
                val weight = o.getDouble("weight") ?: 0.0
                val subtotal = o.getLong("laundrySubtotal") ?: 0L
                val balance = tx.get(userRef(customerUid)).getLong("balance") ?: 0L

                if (adjustment > 0 && balance < adjustment) {
                    WeighOutcome.OnHold(shortfall = adjustment - balance, adjustment = adjustment)
                } else {
                    applyAdjustment(tx, orderId, customerUid, adjustment, totalActual, weight, subtotal)
                    WeighOutcome.Processed(adjustment)
                }
            }.await()
            Result.success(outcome)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun applyAdjustment(
        tx: com.google.firebase.firestore.Transaction,
        orderId: String,
        customerUid: String,
        adjustment: Long,
        totalActual: Long,
        weight: Double,
        actualSubtotal: Long
    ) {
        if (adjustment != 0L) {
            tx.update(userRef(customerUid), "balance", FieldValue.increment(-adjustment))
            tx.set(escrowRef, mapOf("totalHold" to FieldValue.increment(adjustment)), SetOptions.merge())
        }
        tx.update(orderRef(orderId), mapOf(
            "status" to "DIPROSES",
            "weight" to weight,
            "laundrySubtotal" to actualSubtotal,
            "totalPrice" to totalActual,
            "priceAdjustment" to adjustment,
            "escrowHeld" to totalActual,
            "updatedAt" to FieldValue.serverTimestamp()
        ))
    }

    /** Driver menyelesaikan pengantaran akhir: dana escrow dibayarkan ke laundry -> SELESAI. */
    suspend fun completeOrder(orderId: String): Result<Unit> {
        return try {
            db.runTransaction { tx ->
                val o = tx.get(orderRef(orderId))
                val laundryUid = o.getString("laundryUid") ?: ""
                val total = o.getLong("totalPrice") ?: 0L
                if (total <= 0) throw Exception("Data harga pesanan tidak valid")

                tx.set(escrowRef, mapOf("totalHold" to FieldValue.increment(-total)), SetOptions.merge())
                if (laundryUid.isNotEmpty()) {
                    // Update Saldo Laundry DAN Total Pendapatan
                    tx.update(userRef(laundryUid), mapOf(
                        "balance" to FieldValue.increment(total),
                        "totalRevenue" to FieldValue.increment(total)
                    ))
                }
                tx.update(orderRef(orderId), mapOf(
                    "status" to "SELESAI",
                    "escrowHeld" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
