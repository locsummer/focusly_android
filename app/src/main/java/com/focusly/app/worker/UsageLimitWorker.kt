package com.focusly.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.focusly.app.data.local.AppDatabase
import com.focusly.app.domain.manager.UsageStatsHelper

class UsageLimitWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "focusly_limits_channel"
    }

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(context)
        val usageHelper = UsageStatsHelper(context)
        val limits = database.appLimitDao().getEnabledLimits()
        val todayUsage = usageHelper.getTodayUsageStats().associateBy { it.packageName }

        createChannel()

        for (limit in limits) {
            val usageInfo = todayUsage[limit.packageName] ?: continue
            val usedMinutes = usageInfo.usageMinutes

            // Send notification when usage is >= 80% or reached limit
            if (usedMinutes >= limit.dailyLimitMinutes) {
                sendLimitNotification(
                    id = limit.packageName.hashCode(),
                    title = "Limit Reached: ${limit.appName}",
                    message = "You have reached your daily limit of ${limit.dailyLimitMinutes} minutes on ${limit.appName}."
                )
            } else if (usedMinutes >= (limit.dailyLimitMinutes * 0.8).toInt()) {
                sendLimitNotification(
                    id = limit.packageName.hashCode() + 1,
                    title = "Limit Approaching: ${limit.appName}",
                    message = "You have used ${usedMinutes}m of your ${limit.dailyLimitMinutes}m limit."
                )
            }
        }

        return Result.success()
    }

    private fun sendLimitNotification(id: Int, title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Time Limit Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when approaching or exceeding app limits"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
