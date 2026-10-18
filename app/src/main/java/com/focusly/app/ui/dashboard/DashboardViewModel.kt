package com.focusly.app.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusly.app.data.local.AppDatabase
import com.focusly.app.data.repository.AppLimitRepository
import com.focusly.app.data.repository.FocusSessionRepository
import com.focusly.app.data.repository.UsageStatsRepository
import com.focusly.app.domain.manager.PermissionHelper
import com.focusly.app.domain.manager.UsageStatsHelper
import com.focusly.app.domain.model.AppUsageInfo
import com.focusly.app.service.FocuslyAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalScreenTimeMinutes: Long = 0,
    val todayFocusMinutes: Int = 0,
    val dailyGoalMinutes: Int = 120,
    val phonePickupsToday: Int = 0,
    val topApps: List<AppUsageInfo> = emptyList(),
    val isQuickBlockActive: Boolean = false,
    val activeLimitsCount: Int = 0,
    val activeSchedulesCount: Int = 0,
    val hasAllPermissions: Boolean = false,
    val isLoading: Boolean = true
) {
    val progressPercentage: Float
        get() = if (dailyGoalMinutes > 0) {
            (todayFocusMinutes.toFloat() / dailyGoalMinutes).coerceIn(0f, 1f)
        } else 0f
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val permissionHelper = PermissionHelper(application)
    private val usageStatsHelper = UsageStatsHelper(application)
    private val database = AppDatabase.getDatabase(application)
    private val usageRepo = UsageStatsRepository(usageStatsHelper)
    private val limitRepo = AppLimitRepository(database.appLimitDao(), database.blockScheduleDao(), usageStatsHelper)
    private val focusRepo = FocusSessionRepository(database.focusSessionDao())

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        val hasPermissions = permissionHelper.hasAllRequiredPermissions()
        _uiState.update { it.copy(hasAllPermissions = hasPermissions) }

        if (!hasPermissions) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        viewModelScope.launch {
            val totalMinutes = usageRepo.getTotalScreenTimeMinutesToday()
            val pickups = usageRepo.getTodayUnlocks()
            val todayApps = usageRepo.getTodayUsage().take(5)
            val focusMins = focusRepo.getTodayFocusMinutes()

            val limits = database.appLimitDao().getEnabledLimits().size
            val schedules = database.blockScheduleDao().getEnabledSchedules().size

            _uiState.update {
                it.copy(
                    totalScreenTimeMinutes = totalMinutes,
                    todayFocusMinutes = focusMins,
                    phonePickupsToday = pickups,
                    topApps = todayApps,
                    activeLimitsCount = limits,
                    activeSchedulesCount = schedules,
                    isQuickBlockActive = FocuslyAccessibilityService.isFocusModeActive,
                    isLoading = false
                )
            }
        }
    }

    fun toggleQuickBlock() {
        val current = _uiState.value.isQuickBlockActive
        val newStatus = !current
        FocuslyAccessibilityService.isFocusModeActive = newStatus
        _uiState.update { it.copy(isQuickBlockActive = newStatus) }
    }
}
