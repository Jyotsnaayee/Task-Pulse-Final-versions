package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TaskDatabase
import com.example.data.TaskRepository
import com.example.model.DailyHistoryItem
import com.example.model.TaskCategory
import com.example.model.TaskItem
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class TaskPulseUiState(
    val tasks: List<TaskItem> = emptyList(),
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val progressFraction: Float = 0f,
    val progressPercentage: Int = 0,
    val isDarkTheme: Boolean = true,
    val editingTask: TaskItem? = null,
    val showAddEditSheet: Boolean = false,
    val showResetDialog: Boolean = false,
    val showProfileSheet: Boolean = false,
    val selectedFilterCategory: TaskCategory? = null,
    // History & User Profile Stats (Per User Request: Starts from 0 on clean install)
    val dailyHistoryList: List<DailyHistoryItem> = emptyList(),
    val daysActive: Int = 0,
    val totalQuestsGiven: Int = 0,
    val totalQuestsCompleted: Int = 0,
    val userDisplayName: String = "Productivity Champion"
)

class TaskPulseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val prefs = application.getSharedPreferences("taskpulse_prefs", Application.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _editingTask = MutableStateFlow<TaskItem?>(null)
    private val _showAddEditSheet = MutableStateFlow(false)
    private val _showResetDialog = MutableStateFlow(false)
    private val _showProfileSheet = MutableStateFlow(false)
    private val _selectedFilterCategory = MutableStateFlow<TaskCategory?>(null)

    private val _totalHistoricalQuestsGiven = MutableStateFlow(prefs.getInt("total_historical_quests_given", 0))
    private val _totalHistoricalQuestsCompleted = MutableStateFlow(prefs.getInt("total_historical_quests_completed", 0))

    init {
        val database = TaskDatabase.getDatabase(application)
        val scheduler = ReminderScheduler(application)
        repository = TaskRepository(database.taskDao(), database.dailyHistoryDao(), scheduler)

        // Ensure install timestamp is stored
        if (!prefs.contains("install_time_millis")) {
            prefs.edit().putLong("install_time_millis", System.currentTimeMillis()).apply()
        }
    }

    private fun calculateDaysActive(): Int {
        val installTime = prefs.getLong("install_time_millis", System.currentTimeMillis())
        val diffMillis = System.currentTimeMillis() - installTime
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
        // Day 0 on newly installed device; increments to 1 after 24 hours
        return days.coerceAtLeast(0)
    }

    private val dialogsFlow = combine(
        _editingTask,
        _showAddEditSheet,
        _showResetDialog,
        _showProfileSheet,
        _selectedFilterCategory
    ) { editing, showSheet, showReset, showProfile, filterCat ->
        DialogsState(editing, showSheet, showReset, showProfile, filterCat)
    }

    val uiState: StateFlow<TaskPulseUiState> = combine(
        repository.allTasks,
        repository.recentHistory,
        _isDarkTheme,
        dialogsFlow
    ) { tasks: List<TaskItem>, history: List<DailyHistoryItem>, isDark: Boolean, dialogs: DialogsState ->
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val incomplete = total - completed
        val fraction = if (total > 0) completed.toFloat() / total.toFloat() else 0f
        val percentage = (fraction * 100).toInt()

        // Create today's live bar item for real-time responsiveness
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayItem = DailyHistoryItem(
            dateKey = todayKey,
            dayLabel = "Today",
            completedCount = completed,
            incompleteCount = incomplete,
            totalCount = total,
            timestamp = System.currentTimeMillis()
        )

        // Combine recent past days with today's live item
        val filteredHistory = history.filter { it.dateKey != todayKey }.takeLast(5)
        val combinedBarData = filteredHistory + todayItem

        val daysActive = calculateDaysActive()

        // Cumulative quests calculated purely from user activity starting from 0
        val historyCompletedSum = history.filter { it.dateKey != todayKey }.sumOf { it.completedCount }
        val historyTotalSum = history.filter { it.dateKey != todayKey }.sumOf { it.totalCount }

        val totalQuests = maxOf(
            historyTotalSum + total,
            _totalHistoricalQuestsGiven.value
        )
        val completedQuests = maxOf(
            historyCompletedSum + completed,
            _totalHistoricalQuestsCompleted.value
        )

        TaskPulseUiState(
            tasks = tasks,
            totalCount = total,
            completedCount = completed,
            progressFraction = fraction,
            progressPercentage = percentage,
            isDarkTheme = isDark,
            editingTask = dialogs.editingTask,
            showAddEditSheet = dialogs.showAddEditSheet,
            showResetDialog = dialogs.showResetDialog,
            showProfileSheet = dialogs.showProfileSheet,
            selectedFilterCategory = dialogs.selectedFilterCategory,
            dailyHistoryList = combinedBarData,
            daysActive = daysActive,
            totalQuestsGiven = totalQuests,
            totalQuestsCompleted = completedQuests
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskPulseUiState()
    )

    fun toggleTheme() {
        val newValue = !_isDarkTheme.value
        _isDarkTheme.value = newValue
        prefs.edit().putBoolean("dark_theme", newValue).apply()
    }

    fun onTaskToggle(task: TaskItem) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
            if (!task.isCompleted) {
                // Moving to completed: increment total historical completed
                val updatedCompleted = _totalHistoricalQuestsCompleted.value + 1
                _totalHistoricalQuestsCompleted.value = updatedCompleted
                prefs.edit().putInt("total_historical_quests_completed", updatedCompleted).apply()
            }
        }
    }

    fun openAddTask(category: TaskCategory? = null) {
        _editingTask.value = if (category != null) {
            TaskItem(
                title = "",
                category = category
            )
        } else {
            null
        }
        _showAddEditSheet.value = true
    }

    fun openEditTask(task: TaskItem) {
        _editingTask.value = task
        _showAddEditSheet.value = true
    }

    fun dismissAddEditSheet() {
        _showAddEditSheet.value = false
        _editingTask.value = null
    }

    fun openProfileSheet() {
        _showProfileSheet.value = true
    }

    fun dismissProfileSheet() {
        _showProfileSheet.value = false
    }

    fun saveTask(
        id: Long = 0,
        title: String,
        notes: String,
        category: TaskCategory,
        hour: Int?,
        minute: Int?,
        reminderEnabled: Boolean,
        alarmDurationSeconds: Int = 30,
        alarmSoundType: com.example.model.AlarmSoundType = com.example.model.AlarmSoundType.SYSTEM_ALARM,
        spotifyUri: String = "spotify:genre:focus"
    ) {
        if (title.isBlank()) return

        viewModelScope.launch {
            if (id == 0L) {
                val newTask = TaskItem(
                    title = title.trim(),
                    notes = notes.trim(),
                    category = category,
                    isCompleted = false,
                    reminderHour = hour,
                    reminderMinute = minute,
                    reminderEnabled = reminderEnabled && hour != null && minute != null,
                    alarmDurationSeconds = alarmDurationSeconds,
                    alarmSoundType = alarmSoundType,
                    spotifyUri = spotifyUri
                )
                repository.addTask(newTask)
                val newTotal = _totalHistoricalQuestsGiven.value + 1
                _totalHistoricalQuestsGiven.value = newTotal
                prefs.edit().putInt("total_historical_quests_given", newTotal).apply()
            } else {
                val existing = _editingTask.value
                val updatedTask = TaskItem(
                    id = id,
                    title = title.trim(),
                    notes = notes.trim(),
                    category = category,
                    isCompleted = existing?.isCompleted ?: false,
                    reminderHour = hour,
                    reminderMinute = minute,
                    reminderEnabled = reminderEnabled && hour != null && minute != null,
                    alarmDurationSeconds = alarmDurationSeconds,
                    alarmSoundType = alarmSoundType,
                    spotifyUri = spotifyUri,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis()
                )
                repository.updateTask(updatedTask)
            }
            dismissAddEditSheet()
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
            if (_editingTask.value?.id == task.id) {
                dismissAddEditSheet()
            }
        }
    }

    fun showResetConfirmation() {
        _showResetDialog.value = true
    }

    fun dismissResetDialog() {
        _showResetDialog.value = false
    }

    fun confirmResetDay() {
        viewModelScope.launch {
            val currentTasks = uiState.value.tasks
            repository.resetDay(currentTasks)
            _showResetDialog.value = false
        }
    }

    private data class DialogsState(
        val editingTask: TaskItem?,
        val showAddEditSheet: Boolean,
        val showResetDialog: Boolean,
        val showProfileSheet: Boolean,
        val selectedFilterCategory: TaskCategory?
    )
}
