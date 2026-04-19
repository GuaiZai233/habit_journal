package com.example.habbitjournal.data.repository

import com.example.habbitjournal.core.network.SyncApiClient
import com.example.habbitjournal.core.network.SyncLogDto
import com.example.habbitjournal.data.local.dao.GoodHabitLogDao
import com.example.habbitjournal.data.local.entity.GoodHabitLogEntity
import com.example.habbitjournal.domain.model.GoodHabitLog
import com.example.habbitjournal.domain.model.SyncState
import com.example.habbitjournal.domain.repository.GoodHabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoodHabitRepositoryImpl @Inject constructor(
    private val dao: GoodHabitLogDao,
    private val syncApiClient: SyncApiClient,
) : GoodHabitRepository {
    override fun observeLatestLog(): Flow<GoodHabitLog?> {
        return dao.observeLatestLog().map { it?.toDomain() }
    }

    override fun observeMonthLogs(month: YearMonth): Flow<List<GoodHabitLog>> {
        val start = month.atDay(1).format(DateTimeFormatter.ISO_DATE)
        val end = month.atEndOfMonth().format(DateTimeFormatter.ISO_DATE)
        return dao.observeLogsInRange(start, end).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addTodayLog() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val current = dao.findByDate(today)
        val now = Instant.now().toString()
        val nextCount = (current?.count ?: 0) + 1
        dao.upsert(
            GoodHabitLogEntity(
                recordDate = today,
                count = nextCount,
                updatedAt = now,
                syncState = SyncState.PENDING.name,
                deletedAt = null,
            )
        )
    }

    override suspend fun exportCsv(): String {
        val logs = dao.listAll()
        val header = "record_date,count,updated_at,sync_state"
        val rows = logs.joinToString(separator = "\n") {
            "${it.recordDate},${it.count},${it.updatedAt},${it.syncState}"
        }
        return if (rows.isBlank()) header else "$header\n$rows"
    }

    override suspend fun sync(serverUrl: String): String {
        if (serverUrl.isBlank()) {
            return "请先设置服务器地址"
        }
        val pending = dao.listPendingSync()
        if (pending.isNotEmpty()) {
            syncApiClient.push(serverUrl, pending.map { it.toSyncDto() })
            pending.forEach { dao.updateSyncState(it.recordDate, SyncState.SYNCED.name) }
        }

        val since = dao.latestUpdatedAt().ifBlank { "1970-01-01T00:00:00Z" }
        val pulled = syncApiClient.pull(serverUrl, since)
        pulled.logs.forEach { remote ->
            val local = dao.findByDate(remote.recordDate)
            if (local == null || local.updatedAt <= remote.updatedAt) {
                dao.upsert(
                    GoodHabitLogEntity(
                        recordDate = remote.recordDate,
                        count = remote.count,
                        updatedAt = remote.updatedAt,
                        syncState = SyncState.SYNCED.name,
                        deletedAt = null,
                    )
                )
            }
        }
        return "同步完成：push ${pending.size} 条，pull ${pulled.logs.size} 条"
    }
}

private fun GoodHabitLogEntity.toDomain(): GoodHabitLog {
    return GoodHabitLog(
        recordDate = LocalDate.parse(recordDate, DateTimeFormatter.ISO_DATE),
        count = count,
        updatedAt = updatedAt,
        syncState = SyncState.valueOf(syncState),
    )
}

private fun GoodHabitLogEntity.toSyncDto(): SyncLogDto {
    return SyncLogDto(
        recordDate = recordDate,
        count = count,
        updatedAt = updatedAt,
    )
}
