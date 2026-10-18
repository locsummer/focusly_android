package com.focusly.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusly.app.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE timestamp >= :sinceTimestamp")
    suspend fun getSessionsSince(sinceTimestamp: Long): List<FocusSessionEntity>

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE isCompleted = 1 AND timestamp >= :sinceTimestamp")
    suspend fun getTotalFocusMinutesSince(sinceTimestamp: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FocusSessionEntity): Long
}
