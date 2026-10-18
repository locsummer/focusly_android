package com.focusly.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.focusly.app.data.local.AppDatabase
import com.focusly.app.data.local.entity.FocusSessionEntity
import com.focusly.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FocusTimerService : Service() {

    private var countDownTimer: CountDownTimer? = null
    private var totalSeconds: Int = 25 * 60
    private var remainingSeconds: Int = 25 * 60
    private var isRunning: Boolean = false
    private var sessionMode: String = "Pomodoro"

    companion object {
        const val CHANNEL_ID = "focusly_timer_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.focusly.app.ACTION_START"
        const val ACTION_PAUSE = "com.focusly.app.ACTION_PAUSE"
        const val ACTION_RESUME = "com.focusly.app.ACTION_RESUME"
        const val ACTION_STOP = "com.focusly.app.ACTION_STOP"

        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
        const val EXTRA_SESSION_MODE = "extra_session_mode"

        // Broadcast actions to communicate with UI
        const val BROADCAST_TIMER_TICK = "com.focusly.app.TIMER_TICK"
        const val EXTRA_REMAINING_SECONDS = "extra_remaining_seconds"
        const val EXTRA_IS_RUNNING = "extra_is_running"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                totalSeconds = intent.getIntExtra(EXTRA_DURATION_SECONDS, 25 * 60)
                sessionMode = intent.getStringExtra(EXTRA_SESSION_MODE) ?: "Pomodoro"
                remainingSeconds = totalSeconds
                startCountdown()
            }
            ACTION_PAUSE -> pauseCountdown()
            ACTION_RESUME -> resumeCountdown()
            ACTION_STOP -> stopCountdown()
        }
        return START_STICKY
    }

    private fun startCountdown() {
        isRunning = true
        FocuslyAccessibilityService.isFocusModeActive = true

        startForeground(NOTIFICATION_ID, buildNotification(remainingSeconds))

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer((remainingSeconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = (millisUntilFinished / 1000).toInt()
                updateNotification(remainingSeconds)
                broadcastTick(remainingSeconds, true)
            }

            override fun onFinish() {
                remainingSeconds = 0
                broadcastTick(0, false)
                saveCompletedSession()
                stopCountdown()
            }
        }.start()
    }

    private fun pauseCountdown() {
        isRunning = false
        countDownTimer?.cancel()
        updateNotification(remainingSeconds)
        broadcastTick(remainingSeconds, false)
    }

    private fun resumeCountdown() {
        if (remainingSeconds > 0) {
            startCountdown()
        }
    }

    private fun stopCountdown() {
        isRunning = false
        FocuslyAccessibilityService.isFocusModeActive = false
        countDownTimer?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun saveCompletedSession() {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(applicationContext)
            database.focusSessionDao().insert(
                FocusSessionEntity(
                    mode = sessionMode,
                    durationMinutes = totalSeconds / 60,
                    timestamp = System.currentTimeMillis(),
                    isCompleted = true
                )
            )
        }
    }

    private fun broadcastTick(secondsLeft: Int, running: Boolean) {
        val intent = Intent(BROADCAST_TIMER_TICK).apply {
            putExtra(EXTRA_REMAINING_SECONDS, secondsLeft)
            putExtra(EXTRA_IS_RUNNING, running)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun buildNotification(secondsLeft: Int): Notification {
        val minutes = secondsLeft / 60
        val seconds = secondsLeft % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, FocusTimerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focusly: $sessionMode Session")
            .setContentText("Time remaining: $timeString - Distractions blocked")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "End Session", stopIntent)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(secondsLeft: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(secondsLeft))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing focus timer countdown"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        countDownTimer?.cancel()
        FocuslyAccessibilityService.isFocusModeActive = false
        super.onDestroy()
    }
}
