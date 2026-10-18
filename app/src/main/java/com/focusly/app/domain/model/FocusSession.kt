package com.focusly.app.domain.model

enum class FocusMode(val title: String, val defaultMinutes: Int) {
    POMODORO("Pomodoro", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15),
    DEEP_WORK("Deep Work", 60)
}

data class FocusSession(
    val id: Long = 0,
    val mode: FocusMode = FocusMode.POMODORO,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)
