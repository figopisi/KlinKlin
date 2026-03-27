package com.example.klinklinapps.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderService {
    private val db = FirebaseFirestore.getInstance()

    suspend fun checkLaundryCycle(customerUid: String): ReminderInfo? {
        try {
            // Ambil data 3 bulan terakhir
            val threeMonthsAgo = Calendar.getInstance()
            threeMonthsAgo.add(Calendar.MONTH, -3)
            val startTime = Timestamp(threeMonthsAgo.time)

            val snapshot = db.collection("orders")
                .whereEqualTo("customerUid", customerUid)
                .whereGreaterThanOrEqualTo("createdAt", startTime)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.toObjects(Order::class.java)
            if (orders.size < 2) return null // Butuh minimal 2 pesanan untuk hitung interval

            // Hitung selisih hari antar pesanan
            val intervals = mutableListOf<Long>()
            for (i in 0 until orders.size - 1) {
                val currentOrderTime = orders[i].createdAt?.toDate()?.time ?: continue
                val nextOrderTime = orders[i+1].createdAt?.toDate()?.time ?: continue
                
                val diffInMillis = currentOrderTime - nextOrderTime
                val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                if (diffInDays > 0) {
                    intervals.add(diffInDays)
                }
            }

            if (intervals.isEmpty()) return null

            val averageInterval = intervals.average()
            val lastOrderTime = orders.first().createdAt?.toDate()?.time ?: return null
            val currentTime = System.currentTimeMillis()
            val daysSinceLastOrder = TimeUnit.MILLISECONDS.toDays(currentTime - lastOrderTime)

            // Logic H-1: Jika sudah mendekati rata-rata interval - 1 hari
            if (daysSinceLastOrder >= (averageInterval - 1) && daysSinceLastOrder < averageInterval + 2) {
                return ReminderInfo(
                    message = "Hai! Sepertinya keranjang cucianmu sudah mulai penuh, yuk pesan antar-jemput sekarang!",
                    averageInterval = averageInterval.toInt()
                )
            }

            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

data class ReminderInfo(
    val message: String,
    val averageInterval: Int
)
