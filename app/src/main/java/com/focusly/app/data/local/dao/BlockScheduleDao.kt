package com.focusly.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusly.app.data.local.entity.BlockScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockScheduleDao {
    @Query("SELECT * FROM block_schedules")
    fun getAllSchedules(): Flow<List<BlockScheduleEntity>>

    @Query("SELECT * FROM block_schedules WHERE isEnabled = 1")
    suspend fun getEnabledSchedules(): List<BlockScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: BlockScheduleEntity): Long

    @Update
    suspend fun update(schedule: BlockScheduleEntity)

    @Delete
    suspend fun delete(schedule: BlockScheduleEntity)
}
