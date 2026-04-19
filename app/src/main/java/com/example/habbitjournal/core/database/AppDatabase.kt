package com.example.habbitjournal.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.habbitjournal.data.local.dao.GoodHabitLogDao
import com.example.habbitjournal.data.local.entity.GoodHabitLogEntity

@Database(
    entities = [GoodHabitLogEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goodHabitLogDao(): GoodHabitLogDao
}
