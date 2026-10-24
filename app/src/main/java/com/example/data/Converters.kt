package com.example.data

import androidx.room.TypeConverter
import com.example.model.AlarmSoundType
import com.example.model.TaskCategory

class Converters {
    @TypeConverter
    fun fromCategory(category: TaskCategory?): String? {
        return category?.name
    }

    @TypeConverter
    fun toCategory(value: String?): TaskCategory {
        return TaskCategory.fromString(value)
    }

    @TypeConverter
    fun fromAlarmSoundType(type: AlarmSoundType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toAlarmSoundType(value: String?): AlarmSoundType {
        return if (value != null) {
            try {
                AlarmSoundType.valueOf(value)
            } catch (e: Exception) {
                AlarmSoundType.SYSTEM_ALARM
            }
        } else {
            AlarmSoundType.SYSTEM_ALARM
        }
    }
}
