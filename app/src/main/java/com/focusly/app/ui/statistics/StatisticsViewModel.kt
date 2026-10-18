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
    val weeklyAverageHours: Float = 2.4f,
    val totalHoursThisWeek: Float = 16.8f,
    val pickupsAverage: Int = 45,
    val weekDaysData: List<DayUsageData> = emptyList(),
    val smartInsights: String = "Your screen time decreased by 18% compared to last week! Keep it up to reach your 2-hour daily goal."
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val usageStatsHelper = UsageStatsHelper(application)

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadWeeklyData()
    }

    private fun loadWeeklyData() {
        viewModelScope.launch {
            val sampleDays = listOf(
                DayUsageData("Mon", 140),
                DayUsageData("Tue", 120),
                DayUsageData("Wed", 160),
                DayUsageData("Thu", 95),
                DayUsageData("Fri", 185),
                DayUsageData("Sat", 210),
                DayUsageData("Sun", 150)
            )

            _uiState.update {
                it.copy(
                    weekDaysData = sampleDays,
                    totalHoursThisWeek = sampleDays.sumOf { d -> d.minutes } / 60f,
                    weeklyAverageHours = (sampleDays.sumOf { d -> d.minutes } / 7f) / 60f
                )
            }
        }
    }
}
