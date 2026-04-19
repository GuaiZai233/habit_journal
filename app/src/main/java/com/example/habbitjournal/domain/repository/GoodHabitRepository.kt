package com.example.habbitjournal.domain.repository

import com.example.habbitjournal.domain.model.GoodHabitLog
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface GoodHabitRepository {
    fun observeLatestLog(): Flow<GoodHabitLog?>
    fun observeMonthLogs(month: YearMonth): Flow<List<GoodHabitLog>>
    suspend fun addTodayLog()
    suspend fun exportCsv(): String
    suspend fun sync(serverUrl: String): String
}
