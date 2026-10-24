package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val category: TaskCategory,
    val isCompleted: Boolean = false,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val reminderEnabled: Boolean = false,
    val alarmDurationSeconds: Int = 30, // 15, 30, 45, 60, 120 seconds
    val alarmSoundType: AlarmSoundType = AlarmSoundType.SYSTEM_ALARM,
    val spotifyUri: String = "spotify:genre:focus", // optional URI or search query
    val createdAt: Long = System.currentTimeMillis()
) {
    fun formatReminderTime(): String? {
        if (reminderHour == null || reminderMinute == null) return null
        val hour12 = when {
            reminderHour == 0 -> 12
            reminderHour > 12 -> reminderHour - 12
            else -> reminderHour
        }
        val amPm = if (reminderHour >= 12) "PM" else "AM"
        val minStr = reminderMinute.toString().padStart(2, '0')
        return "$hour12:$minStr $amPm"
    }

    fun formatDurationLabel(): String {
        return when {
            alarmDurationSeconds < 60 -> "${alarmDurationSeconds}s"
            alarmDurationSeconds % 60 == 0 -> "${alarmDurationSeconds / 60}m"
            else -> "${alarmDurationSeconds / 60}m ${alarmDurationSeconds % 60}s"
        }
    }
}
