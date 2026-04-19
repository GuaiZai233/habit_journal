package com.example.habbitjournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "good_habit_logs")
data class GoodHabitLogEntity(
    @PrimaryKey val recordDate: String,
    val count: Int,
    val updatedAt: String,
    val syncState: String,
    val deletedAt: String? = null,
)
