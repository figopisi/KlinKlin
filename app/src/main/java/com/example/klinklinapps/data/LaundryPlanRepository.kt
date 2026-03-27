package com.example.klinklinapps.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class LaundryPlanRepository {
    private val db = FirebaseFirestore.getInstance()

    fun calculateDeadline(eventDate: Long, serviceType: String): Long {
        val eventLocalDate = Instant.ofEpochMilli(eventDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val processDays = if (serviceType == "Satuan/Jas") 5L else 3L
        val deliveryDays = 1L
        val bufferDays = 1L
        
        val totalDaysBack = processDays + deliveryDays + bufferDays
        val deadlineDate = eventLocalDate.minusDays(totalDaysBack)
        
        return deadlineDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    suspend fun saveLaundryPlan(
        userId: String,
        eventName: String,
        eventDate: Long,
        serviceType: String
    ): Result<Unit> {
        return try {
            val deadlineMillis = calculateDeadline(eventDate, serviceType)
            
            db.runTransaction { transaction ->
                val planRef = db.collection("laundry_plans").document()
                val data = hashMapOf(
                    "userId" to userId,
                    "eventName" to eventName,
                    "eventDate" to Timestamp(Date(eventDate)),
                    "serviceType" to serviceType,
                    "computedDeadline" to Timestamp(Date(deadlineMillis))
                )
                transaction.set(planRef, data)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLaundryPlans(userId: String): List<LaundryPlan> {
        return try {
            val snapshot = db.collection("laundry_plans")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(LaundryPlan::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
