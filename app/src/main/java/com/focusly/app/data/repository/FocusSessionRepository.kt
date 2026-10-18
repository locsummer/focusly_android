package com.focusly.app.data.repository

import com.focusly.app.data.local.dao.FocusSessionDao
import com.focusly.app.data.local.entity.FocusSessionEntity
import com.focusly.app.domain.model.FocusMode
import com.focusly.app.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class FocusSessionRepository(private val dao: FocusSessionDao) {

    fun getAllSessions(): Flow<List<FocusSession>> {
        return dao.getAllSessions().map { entities ->
            entities.map { entity ->
                FocusSession(
                    id = entity.id,
                    mode = try { FocusMode.valueOf(entity.mode) } catch (e: Exception) { FocusMode.POMODORO },
                    durationMinutes = entity.durationMinutes,
                    timestamp = entity.timestamp,
                    isCompleted = entity.isCompleted
                )
            }
        }
    }

    suspend fun saveSession(session: FocusSession): Long {
        return dao.insert(
            FocusSessionEntity(
                mode = session.mode.name,
                durationMinutes = session.durationMinutes,
                timestamp = session.timestamp,
                isCompleted = session.isCompleted
            )
        )
    }

    suspend fun getTodayFocusMinutes(): Int {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return dao.getTotalFocusMinutesSince(calendar.timeInMillis) ?: 0
    }
}
