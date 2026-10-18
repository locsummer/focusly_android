package com.focusly.app.data.repository

import com.focusly.app.domain.manager.UsageStatsHelper
import com.focusly.app.domain.model.AppUsageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsageStatsRepository(private val usageStatsHelper: UsageStatsHelper) {

    suspend fun getTodayUsage(): List<AppUsageInfo> = withContext(Dispatchers.IO) {
        usageStatsHelper.getTodayUsageStats()
    }

    suspend fun getTotalScreenTimeMinutesToday(): Long = withContext(Dispatchers.IO) {
        usageStatsHelper.getTodayUsageStats().sumOf { it.usageMinutes }
    }

    suspend fun getTodayUnlocks(): Int = withContext(Dispatchers.IO) {
        usageStatsHelper.getDeviceUnlockCountToday()
    }
}
