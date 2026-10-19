package com.focusly.app.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusly.app.domain.manager.UsageStatsHelper
import com.focusly.app.ui.components.DayUsageData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatisticsUiState(
    val weeklyAverageHours: Float = 0f,
    val totalHoursThisWeek: Float = 0f,
    val pickupsAverage: Int = 0,
    val weekDaysData: List<DayUsageData> = emptyList(),
    val smartInsights: String = "Tracking active screen time to help you reach your daily focus goals."
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val usageStatsHelper = UsageStatsHelper(application)

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadWeeklyData()
    }

    fun loadWeeklyData() {
        viewModelScope.launch {
            val rawWeekly = usageStatsHelper.getWeeklyUsagePerDay()
            val daysData = if (rawWeekly.isNotEmpty()) {
                rawWeekly.map { DayUsageData(it.first, it.second) }
            } else {
                emptyList()
            }

            val totalMins = daysData.sumOf { it.minutes }
            val totalHours = totalMins / 60f
            val avgHours = if (daysData.isNotEmpty()) totalHours / daysData.size else 0f
            val todayPickups = usageStatsHelper.getDeviceUnlockCountToday()

            val insight = when {
                totalHours == 0f -> "No screen time recorded yet. Make sure usage access permission is granted."
                avgHours <= 2.0f -> "Great job! Your daily average screen time (${String.format("%.1f", avgHours)}h) is within your target limit."
                avgHours <= 4.0f -> "Moderate usage detected (${String.format("%.1f", avgHours)}h/day). Setting app limits on social apps can help save an extra hour."
                else -> "High screen time detected (${String.format("%.1f", avgHours)}h/day). Try scheduling focus sessions during work hours."
            }

            _uiState.update {
                it.copy(
                    weekDaysData = daysData,
                    totalHoursThisWeek = (Math.round(totalHours * 10) / 10f),
                    weeklyAverageHours = (Math.round(avgHours * 10) / 10f),
                    pickupsAverage = todayPickups,
                    smartInsights = insight
                )
            }
        }
    }
}
