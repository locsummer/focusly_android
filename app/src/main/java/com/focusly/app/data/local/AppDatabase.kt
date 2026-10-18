package com.focusly.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.focusly.app.data.local.dao.AppLimitDao
import com.focusly.app.data.local.dao.BlockScheduleDao
import com.focusly.app.data.local.dao.FocusSessionDao
import com.focusly.app.data.local.entity.AppLimitEntity
import com.focusly.app.data.local.entity.BlockScheduleEntity
import com.focusly.app.data.local.entity.FocusSessionEntity

@Database(
    entities = [
        AppLimitEntity::class,
        BlockScheduleEntity::class,
        FocusSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appLimitDao(): AppLimitDao
    abstract fun blockScheduleDao(): BlockScheduleDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "focusly_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
