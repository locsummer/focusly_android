package com.focusly.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.focusly.app.data.local.AppDatabase
import com.focusly.app.domain.manager.UsageStatsHelper
import com.focusly.app.ui.overlay.BlockOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

class FocuslyAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: AppDatabase
    private lateinit var usageStatsHelper: UsageStatsHelper

    companion object {
        @Volatile
        var isFocusModeActive: Boolean = false
        @Volatile
        var focusBlockedPackages: Set<String> = emptySet()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        database = AppDatabase.getDatabase(this)
        usageStatsHelper = UsageStatsHelper(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == this.packageName || packageName == "com.android.systemui") return

        serviceScope.launch {
            checkAndBlockIfNeeded(packageName)
        }
    }

    private suspend fun checkAndBlockIfNeeded(packageName: String) {
        // 1. Check if Focus Mode (Pomodoro) is active and this app is in blocked list
        if (isFocusModeActive && (focusBlockedPackages.isEmpty() || focusBlockedPackages.contains(packageName))) {
            triggerBlockOverlay(packageName, "Focus Session in Progress")
            return
        }

        // 2. Check if App Limit exceeded for today
        val limit = database.appLimitDao().getLimitByPackage(packageName)
        if (limit != null && limit.isEnabled) {
            val todayUsage = usageStatsHelper.getTodayUsageStats().find { it.packageName == packageName }
            val usedMinutes = todayUsage?.usageMinutes ?: 0L
            if (usedMinutes >= limit.dailyLimitMinutes) {
                triggerBlockOverlay(packageName, "Daily Limit Reached (${limit.dailyLimitMinutes}m)")
                return
            }
        }

        // 3. Check if any Block Schedule is currently active
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        val currentMinuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        val schedules = database.blockScheduleDao().getEnabledSchedules()
        for (schedule in schedules) {
            val days = schedule.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
            if (days.contains(currentDay)) {
                val start = schedule.startHour * 60 + schedule.startMinute
                val end = schedule.endHour * 60 + schedule.endMinute
                val isTimeInRange = if (start <= end) {
                    currentMinuteOfDay in start..end
                } else {
                    // Over midnight schedule (e.g. 22:00 to 06:00)
                    currentMinuteOfDay >= start || currentMinuteOfDay <= end
                }

                if (isTimeInRange) {
                    val pkgs = schedule.blockedPackages.split(",").map { it.trim() }
                    if (pkgs.contains(packageName)) {
                        triggerBlockOverlay(packageName, "Schedule: ${schedule.title}")
                        return
                    }
                }
            }
        }
    }

    private fun triggerBlockOverlay(packageName: String, reason: String) {
        val intent = Intent(this, BlockOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, packageName)
            putExtra(BlockOverlayActivity.EXTRA_BLOCK_REASON, reason)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
