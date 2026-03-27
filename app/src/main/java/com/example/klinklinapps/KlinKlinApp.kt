package com.example.klinklinapps

import android.app.Application
import androidx.work.*
import com.example.klinklinapps.data.DeadlineWorker
import java.util.concurrent.TimeUnit

class KlinKlinApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setupDeadlineWorker()
    }

    private fun setupDeadlineWorker() {
        val workManager = WorkManager.getInstance(this)

        // 1. Terjadwal Harian
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<DeadlineWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .addTag("deadline_check")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "deadline_unique_work",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )

        // 2. PEMICU TESTING: Langsung jalan setiap kali aplikasi dibuka
        val testRequest = OneTimeWorkRequestBuilder<DeadlineWorker>()
            .build()
        workManager.enqueue(testRequest)
    }
}
