package com.focusly.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusly.app.data.local.entity.AppLimitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLimitDao {
    @Query("SELECT * FROM app_limits")
    fun getAllLimits(): Flow<List<AppLimitEntity>>

    @Query("SELECT * FROM app_limits WHERE isEnabled = 1")
    suspend fun getEnabledLimits(): List<AppLimitEntity>

    @Query("SELECT * FROM app_limits WHERE packageName = :pkg LIMIT 1")
    suspend fun getLimitByPackage(pkg: String): AppLimitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(limit: AppLimitEntity)

    @Update
    suspend fun update(limit: AppLimitEntity)

    @Delete
    suspend fun delete(limit: AppLimitEntity)
}
