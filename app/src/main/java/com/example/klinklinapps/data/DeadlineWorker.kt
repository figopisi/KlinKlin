package com.example.klinklinapps.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.klinklinapps.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class DeadlineWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userId = Firebase.auth.currentUser?.uid ?: return Result.success()
        val db = FirebaseFirestore.getInstance()
        val today = LocalDate.now()
        
        Log.d("DeadlineWorker", "Checking deadlines for user: $userId on $today")

        return try {
            val snapshot = db.collection("laundry_plans")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                val planId = doc.id
                val eventName = doc.getString("eventName") ?: "Acara"
                val deadlineTs = doc.getTimestamp("computedDeadline")
                val alreadyNotified = doc.getBoolean("notifiedInChat") ?: false
                
                deadlineTs?.let {
                    val deadlineDate = it.toDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    if (deadlineDate == today && !alreadyNotified) {
                        // 1. Kirim Pesan Chat Otomatis
                        sendChatMessage(userId, eventName)
                        
                        // 2. Tandai agar tidak kirim chat berulang kali hari ini
                        db.collection("laundry_plans").document(planId)
                            .update("notifiedInChat", true)
                        
                        // 3. Tetap munculkan Notifikasi Sistem (Opsional tapi bagus)
                        showNotification(eventName)
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("DeadlineWorker", "Error checking deadlines", e)
            Result.retry()
        }
    }

    private suspend fun sendChatMessage(userId: String, eventName: String) {
        val db = FirebaseFirestore.getInstance()
        val messageData = hashMapOf(
            "senderId" to "ADMIN_KLINKLIN",
            "receiverId" to userId,
            "message" to "Halo Kak! Sekadar mengingatkan, hari ini adalah batas terakhir kirim laundry untuk acara '$eventName'. Yuk pesan antar-jemput sekarang agar baju siap tepat waktu!",
            "timestamp" to Timestamp(Date()),
            "isRead" to false,
            "type" to "SYSTEM_REMINDER"
        )
        
        try {
            db.collection("chats").document(userId)
              .collection("messages").add(messageData).await()
            Log.d("DeadlineWorker", "Chat message sent to $userId")
        } catch (e: Exception) {
            Log.e("DeadlineWorker", "Failed to send chat message", e)
        }
    }

    private fun showNotification(eventName: String) {
        val channelId = "deadline_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Laundry Deadline", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Batas Terakhir Kirim Laundry!")
            .setContentText("Cek chat! Hari ini batas terakhir kirim laundry untuk $eventName.")
            .setSmallIcon(R.drawable.logo_klinklin)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
