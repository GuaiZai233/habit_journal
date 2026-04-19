package com.example.habbitjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.habbitjournal.data.local.entity.GoodHabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoodHabitLogDao {
    @Query("SELECT * FROM good_habit_logs WHERE deletedAt IS NULL ORDER BY recordDate DESC LIMIT 1")
    fun observeLatestLog(): Flow<GoodHabitLogEntity?>

    @Query("SELECT * FROM good_habit_logs WHERE deletedAt IS NULL AND recordDate BETWEEN :startDate AND :endDate ORDER BY recordDate ASC")
    fun observeLogsInRange(startDate: String, endDate: String): Flow<List<GoodHabitLogEntity>>

    @Query("SELECT * FROM good_habit_logs WHERE deletedAt IS NULL AND recordDate = :recordDate LIMIT 1")
    suspend fun findByDate(recordDate: String): GoodHabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: GoodHabitLogEntity)

    @Query("SELECT * FROM good_habit_logs WHERE deletedAt IS NULL AND syncState <> 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun listPendingSync(): List<GoodHabitLogEntity>

    @Query("UPDATE good_habit_logs SET syncState = :syncState WHERE recordDate = :recordDate")
    suspend fun updateSyncState(recordDate: String, syncState: String)

    @Query("SELECT * FROM good_habit_logs WHERE deletedAt IS NULL ORDER BY recordDate ASC")
    suspend fun listAll(): List<GoodHabitLogEntity>

    @Query("SELECT COALESCE(MAX(updatedAt), '') FROM good_habit_logs WHERE deletedAt IS NULL")
    suspend fun latestUpdatedAt(): String
}
