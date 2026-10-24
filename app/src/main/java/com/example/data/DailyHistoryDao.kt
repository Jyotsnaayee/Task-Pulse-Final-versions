package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.DailyHistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHistoryDao {
    @Query("SELECT * FROM daily_history ORDER BY dateKey ASC LIMIT 7")
    fun getRecentHistory(): Flow<List<DailyHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: DailyHistoryItem)

    @Query("SELECT * FROM daily_history WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getByDateKey(dateKey: String): DailyHistoryItem?

    @Query("SELECT SUM(completedCount) FROM daily_history")
    suspend fun getTotalCompletedHistory(): Int?

    @Query("SELECT SUM(totalCount) FROM daily_history")
    suspend fun getTotalQuestHistory(): Int?
}
