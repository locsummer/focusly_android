package com.focusly.app.ui.blocking

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusly.app.data.local.AppDatabase
import com.focusly.app.data.repository.AppLimitRepository
import com.focusly.app.domain.manager.UsageStatsHelper
import com.focusly.app.domain.model.AppLimit
import com.focusly.app.domain.model.BlockSchedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SelectableApp(
    val packageName: String,
    val appName: String,
    val isSelected: Boolean = false
)

class AppLimitsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val usageStatsHelper = UsageStatsHelper(application)
    private val repository = AppLimitRepository(database.appLimitDao(), database.blockScheduleDao(), usageStatsHelper)

    val appLimits: StateFlow<List<AppLimit>> = repository.getAllLimits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockSchedules: StateFlow<List<BlockSchedule>> = repository.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installedApps = MutableStateFlow<List<SelectableApp>>(emptyList())
    val installedApps: StateFlow<List<SelectableApp>> = _installedApps.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val pm = getApplication<Application>().packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val apps = resolveInfos.mapNotNull { ri ->
                val pkg = ri.activityInfo.packageName
                if (pkg == getApplication<Application>().packageName) null
                else {
                    val label = ri.loadLabel(pm).toString()
                    SelectableApp(packageName = pkg, appName = label)
                }
            }.distinctBy { it.packageName }.sortedBy { it.appName }

            _installedApps.value = apps
        }
    }

    fun addLimit(packageName: String, appName: String, limitMinutes: Int) {
        viewModelScope.launch {
            repository.saveLimit(
                AppLimit(
                    packageName = packageName,
                    appName = appName,
                    dailyLimitMinutes = limitMinutes,
                    isEnabled = true
                )
            )
        }
    }

    fun toggleLimit(packageName: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleLimit(packageName, isEnabled)
        }
    }

    fun deleteLimit(packageName: String) {
        viewModelScope.launch {
            repository.deleteLimit(packageName)
        }
    }

    fun addSchedule(title: String, startH: Int, startM: Int, endH: Int, endM: Int, days: List<Int>, pkgs: List<String>) {
        viewModelScope.launch {
            repository.saveSchedule(
                BlockSchedule(
                    title = title,
                    startHour = startH,
                    startMinute = startM,
                    endHour = endH,
                    endMinute = endM,
                    daysOfWeek = days,
                    blockedPackages = pkgs,
                    isEnabled = true
                )
            )
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            repository.deleteSchedule(id)
        }
    }
}
