package com.example.data

import com.example.model.DailyHistoryItem
import com.example.model.TaskItem
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskRepository(
    private val taskDao: TaskDao,
    private val dailyHistoryDao: DailyHistoryDao,
    private val reminderScheduler: ReminderScheduler
) {
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()
    val recentHistory: Flow<List<DailyHistoryItem>> = dailyHistoryDao.getRecentHistory()

    suspend fun getTaskById(id: Long): TaskItem? = taskDao.getTaskById(id)

    suspend fun addTask(task: TaskItem): Long {
        val insertedId = taskDao.insertTask(task)
        val savedTask = task.copy(id = insertedId)
        if (savedTask.reminderEnabled) {
            reminderScheduler.scheduleReminder(savedTask)
        }
        return insertedId
    }

    suspend fun updateTask(task: TaskItem) {
        taskDao.updateTask(task)
        if (task.reminderEnabled && !task.isCompleted) {
            reminderScheduler.scheduleReminder(task)
        } else {
            reminderScheduler.cancelReminder(task.id)
        }
    }

    suspend fun toggleTaskCompletion(task: TaskItem) {
        val newStatus = !task.isCompleted
        val updated = task.copy(isCompleted = newStatus)
        taskDao.updateTask(updated)
        if (newStatus) {
            reminderScheduler.cancelReminder(task.id)
        } else if (updated.reminderEnabled) {
            reminderScheduler.scheduleReminder(updated)
        }
    }

    suspend fun deleteTask(task: TaskItem) {
        reminderScheduler.cancelReminder(task.id)
        taskDao.deleteTask(task)
    }

    suspend fun resetDay(tasksToArchive: List<TaskItem>) {
        // Record current day's stats into daily_history before clearing
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayLabelFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val today = Date()
        val todayKey = dateFormat.format(today)
        val dayLabel = dayLabelFormat.format(today)

        val completed = tasksToArchive.count { it.isCompleted }
        val incomplete = tasksToArchive.count { !it.isCompleted }

        if (tasksToArchive.isNotEmpty()) {
            dailyHistoryDao.insertOrUpdate(
                DailyHistoryItem(
                    dateKey = todayKey,
                    dayLabel = dayLabel,
                    completedCount = completed,
                    incompleteCount = incomplete,
                    totalCount = completed + incomplete,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        // Cancel all pending reminders for previous day
        reminderScheduler.cancelAll(tasksToArchive.map { it.id })
        // Clear all tasks for a fresh start
        taskDao.clearAllTasks()
    }

    suspend fun recordHistoryItem(item: DailyHistoryItem) {
        dailyHistoryDao.insertOrUpdate(item)
    }
}
