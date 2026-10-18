package com.focusly.app.domain.model

import android.graphics.drawable.Drawable

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageTimeMillis: Long,
    val lastTimeUsed: Long = 0,
    val launchCount: Int = 0,
    val icon: Drawable? = null
) {
    val usageMinutes: Long get() = usageTimeMillis / (1000 * 60)
    val formattedUsage: String
        get() {
            val hours = usageMinutes / 60
            val mins = usageMinutes % 60
            return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
        }
}
