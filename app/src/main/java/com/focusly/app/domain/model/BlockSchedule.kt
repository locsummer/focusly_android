package com.focusly.app.domain.model

data class BlockSchedule(
    val id: Long = 0,
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: List<Int>, // 1: Sunday, ..., 7: Saturday
    val blockedPackages: List<String>,
    val isEnabled: Boolean = true
) {
    val timeRangeString: String
        get() = String.format("%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute)
}
