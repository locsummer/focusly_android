package com.focusly.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.focusly.app.data.local.AppDatabase
import com.focusly.app.worker.UsageLimitWorker
import java.util.concurrent.TimeUnit

class FocuslyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Pre-initialize Room Database
        AppDatabase.getDatabase(this)

        // Schedule periodic background usage checking via WorkManager
        scheduleUsageMonitoring()
    }

    private fun scheduleUsageMonitoring() {
        val workRequest = PeriodicWorkRequestBuilder<UsageLimitWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "focusly_usage_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
