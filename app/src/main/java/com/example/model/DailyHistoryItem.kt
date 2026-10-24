package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_history")
data class DailyHistoryItem(
    @PrimaryKey
    val dateKey: String, // Format: YYYY-MM-DD
    val dayLabel: String, // e.g., "Mon", "Tue", "Today"
    val completedCount: Int,
    val incompleteCount: Int,
    val totalCount: Int = completedCount + incompleteCount,
    val timestamp: Long = System.currentTimeMillis()
)
