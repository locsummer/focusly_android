package com.focusly.app.ui.focus

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusly.app.domain.model.FocusMode
import com.focusly.app.service.FocusTimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FocusTimerUiState(
    val selectedMode: FocusMode = FocusMode.POMODORO,
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false
) {
    val progress: Float
        get() = if (totalSeconds > 0) {
            1f - (remainingSeconds.toFloat() / totalSeconds)
        } else 0f

    val formattedTime: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }
}

class FocusTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FocusTimerUiState())
    val uiState: StateFlow<FocusTimerUiState> = _uiState.asStateFlow()

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == FocusTimerService.BROADCAST_TIMER_TICK) {
                val remaining = intent.getIntExtra(FocusTimerService.EXTRA_REMAINING_SECONDS, 0)
                val running = intent.getBooleanExtra(FocusTimerService.EXTRA_IS_RUNNING, false)
                _uiState.update {
                    it.copy(
                        remainingSeconds = remaining,
                        isRunning = running,
                        isPaused = !running && remaining > 0 && remaining < it.totalSeconds
                    )
                }
            }
        }
    }

    init {
        val filter = IntentFilter(FocusTimerService.BROADCAST_TIMER_TICK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(timerReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            application.registerReceiver(timerReceiver, filter)
        }
    }

    fun selectMode(mode: FocusMode) {
        val seconds = mode.defaultMinutes * 60
        _uiState.update {
            it.copy(
                selectedMode = mode,
                remainingSeconds = seconds,
                totalSeconds = seconds,
                isRunning = false,
                isPaused = false
            )
        }
    }

    fun startTimer() {
        val app = getApplication<Application>()
        val intent = Intent(app, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_DURATION_SECONDS, _uiState.value.totalSeconds)
            putExtra(FocusTimerService.EXTRA_SESSION_MODE, _uiState.value.selectedMode.title)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            app.startForegroundService(intent)
        } else {
            app.startService(intent)
        }
        _uiState.update { it.copy(isRunning = true, isPaused = false) }
    }

    fun pauseTimer() {
        val app = getApplication<Application>()
        val intent = Intent(app, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_PAUSE
        }
        app.startService(intent)
        _uiState.update { it.copy(isRunning = false, isPaused = true) }
    }

    fun resumeTimer() {
        val app = getApplication<Application>()
        val intent = Intent(app, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_RESUME
        }
        app.startService(intent)
        _uiState.update { it.copy(isRunning = true, isPaused = false) }
    }

    fun stopTimer() {
        val app = getApplication<Application>()
        val intent = Intent(app, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_STOP
        }
        app.startService(intent)
        val defaultSecs = _uiState.value.selectedMode.defaultMinutes * 60
        _uiState.update {
            it.copy(
                remainingSeconds = defaultSecs,
                totalSeconds = defaultSecs,
                isRunning = false,
                isPaused = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(timerReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }
}
