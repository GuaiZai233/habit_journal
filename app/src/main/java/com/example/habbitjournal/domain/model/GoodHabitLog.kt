package com.example.habbitjournal.domain.model

import java.time.LocalDate

data class GoodHabitLog(
    val recordDate: LocalDate,
    val count: Int,
    val updatedAt: String,
    val syncState: SyncState,
)

enum class SyncState {
    PENDING,
    SYNCED,
    FAILED,
}
