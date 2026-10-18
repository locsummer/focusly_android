package com.focusly.app.domain.model

data class AppLimit(
    val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int,
    val currentUsedMinutes: Int = 0,
    val isEnabled: Boolean = true
) {
    val isExceeded: Boolean get() = isEnabled && currentUsedMinutes >= dailyLimitMinutes
    val remainingMinutes: Int get() = (dailyLimitMinutes - currentUsedMinutes).coerceAtLeast(0)
    val progress: Float
        get() = if (dailyLimitMinutes > 0) {
            (currentUsedMinutes.toFloat() / dailyLimitMinutes).coerceIn(0f, 1f)
        } else 0f
}
