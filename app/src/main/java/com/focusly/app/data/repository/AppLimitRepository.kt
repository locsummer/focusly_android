package com.focusly.app.data.repository

import com.focusly.app.data.local.dao.AppLimitDao
import com.focusly.app.data.local.dao.BlockScheduleDao
import com.focusly.app.data.local.entity.AppLimitEntity
import com.focusly.app.data.local.entity.BlockScheduleEntity
import com.focusly.app.domain.manager.UsageStatsHelper
import com.focusly.app.domain.model.AppLimit
import com.focusly.app.domain.model.BlockSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppLimitRepository(
    private val appLimitDao: AppLimitDao,
    private val blockScheduleDao: BlockScheduleDao,
    private val usageStatsHelper: UsageStatsHelper
) {

    fun getAllLimits(): Flow<List<AppLimit>> {
        return appLimitDao.getAllLimits().map { entities ->
            val todayUsage = usageStatsHelper.getTodayUsageStats().associateBy { it.packageName }
            entities.map { entity ->
                val usedMins = todayUsage[entity.packageName]?.usageMinutes?.toInt() ?: 0
                AppLimit(
                    packageName = entity.packageName,
                    appName = entity.appName,
                    dailyLimitMinutes = entity.dailyLimitMinutes,
                    currentUsedMinutes = usedMins,
                    isEnabled = entity.isEnabled
                )
            }
        }
    }

    suspend fun saveLimit(limit: AppLimit) {
        appLimitDao.insertOrUpdate(
            AppLimitEntity(
                packageName = limit.packageName,
                appName = limit.appName,
                dailyLimitMinutes = limit.dailyLimitMinutes,
                isEnabled = limit.isEnabled
            )
        )
    }

    suspend fun toggleLimit(pkg: String, isEnabled: Boolean) {
        val existing = appLimitDao.getLimitByPackage(pkg) ?: return
        appLimitDao.update(existing.copy(isEnabled = isEnabled))
    }

    suspend fun deleteLimit(pkg: String) {
        val existing = appLimitDao.getLimitByPackage(pkg) ?: return
        appLimitDao.delete(existing)
    }

    fun getAllSchedules(): Flow<List<BlockSchedule>> {
        return blockScheduleDao.getAllSchedules().map { entities ->
            entities.map { entity ->
                val days = entity.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
                val pkgs = entity.blockedPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                BlockSchedule(
                    id = entity.id,
                    title = entity.title,
                    startHour = entity.startHour,
                    startMinute = entity.startMinute,
                    endHour = entity.endHour,
                    endMinute = entity.endMinute,
                    daysOfWeek = days,
                    blockedPackages = pkgs,
                    isEnabled = entity.isEnabled
                )
            }
        }
    }

    suspend fun saveSchedule(schedule: BlockSchedule) {
        val daysString = schedule.daysOfWeek.joinToString(",")
        val pkgsString = schedule.blockedPackages.joinToString(",")
        blockScheduleDao.insert(
            BlockScheduleEntity(
                id = schedule.id,
                title = schedule.title,
                startHour = schedule.startHour,
                startMinute = schedule.startMinute,
                endHour = schedule.endHour,
                endMinute = schedule.endMinute,
                daysOfWeek = daysString,
                blockedPackages = pkgsString,
                isEnabled = schedule.isEnabled
            )
        )
    }

    suspend fun deleteSchedule(id: Long) {
        val entity = BlockScheduleEntity(id = id, title = "", startHour = 0, startMinute = 0, endHour = 0, endMinute = 0, daysOfWeek = "", blockedPackages = "")
        blockScheduleDao.delete(entity)
    }
}
