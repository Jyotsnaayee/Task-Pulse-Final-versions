package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.model.TaskItem
import java.util.Calendar

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun scheduleReminder(task: TaskItem) {
        if (alarmManager == null) return
        val hour = task.reminderHour ?: return
        val minute = task.reminderMinute ?: return
        if (!task.reminderEnabled || task.isCompleted) {
            cancelReminder(task.id)
            return
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(TaskReminderReceiver.EXTRA_DURATION_SECONDS, task.alarmDurationSeconds)
            putExtra(TaskReminderReceiver.EXTRA_SOUND_TYPE, task.alarmSoundType.name)
            putExtra(TaskReminderReceiver.EXTRA_SPOTIFY_URI, task.spotifyUri)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e("ReminderScheduler", "SecurityException setting alarm: ${e.message}")
        }
    }

    fun cancelReminder(taskId: Long) {
        if (alarmManager == null) return
        TaskReminderReceiver.stopActiveAlarm(context, taskId.toInt())

        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun cancelAll(taskIds: List<Long>) {
        taskIds.forEach { cancelReminder(it) }
    }
}
