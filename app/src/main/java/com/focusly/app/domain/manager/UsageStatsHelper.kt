package com.focusly.app.domain.manager

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.focusly.app.domain.model.AppUsageInfo
import java.util.Calendar

class UsageStatsHelper(private val context: Context) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private val packageManager = context.packageManager

    fun getTodayUsageStats(): List<AppUsageInfo> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        return getUsageBetween(startTime, endTime)
    }

    fun getUsageBetween(startTime: Long, endTime: Long): List<AppUsageInfo> {
        if (usageStatsManager == null) return emptyList()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return emptyList()

        val aggregatedMap = mutableMapOf<String, Long>()
        for (usageStat in stats) {
            val total = aggregatedMap.getOrDefault(usageStat.packageName, 0L)
            aggregatedMap[usageStat.packageName] = total + usageStat.totalTimeInForeground
        }

        val resultList = mutableListOf<AppUsageInfo>()
        for ((pkg, duration) in aggregatedMap) {
            if (duration <= 0 || pkg == context.packageName) continue

            val appName: String
            val icon = try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                appName = packageManager.getApplicationLabel(appInfo).toString()
                packageManager.getApplicationIcon(appInfo)
            } catch (e: PackageManager.NameNotFoundException) {
                appName = pkg
                null
            }

            resultList.add(
                AppUsageInfo(
                    packageName = pkg,
                    appName = appName,
                    usageTimeMillis = duration,
                    icon = icon
                )
            )
        }

        return resultList.sortedByDescending { it.usageTimeMillis }
    }

    fun getDeviceUnlockCountToday(): Int {
        if (usageStatsManager == null) return 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val events = usageStatsManager.queryEvents(calendar.timeInMillis, System.currentTimeMillis())
        var unlockCount = 0
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN ||
                event.eventType == UsageEvents.Event.SCREEN_INTERACTIVE
            ) {
                unlockCount++
            }
        }
        return unlockCount
    }
}
