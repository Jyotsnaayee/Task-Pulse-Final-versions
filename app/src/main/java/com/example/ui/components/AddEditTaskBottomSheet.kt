package com.example.ui.components

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AlarmSoundType
import com.example.model.TaskCategory
import com.example.model.TaskItem
import com.example.ui.theme.ColorDaily
import com.example.ui.theme.ColorImportant
import com.example.ui.theme.ColorLessImportant
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskBottomSheet(
    task: TaskItem?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        notes: String,
        category: TaskCategory,
        hour: Int?,
        minute: Int?,
        reminderEnabled: Boolean,
        alarmDurationSeconds: Int,
        alarmSoundType: AlarmSoundType,
        spotifyUri: String
    ) -> Unit,
    onDelete: ((TaskItem) -> Unit)? = null,
    isDarkTheme: Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var title by remember { mutableStateOf(task?.title ?: "") }
    var notes by remember { mutableStateOf(task?.notes ?: "") }
    var selectedCategory by remember { mutableStateOf(task?.category ?: TaskCategory.IMPORTANT) }

    val currentCal = Calendar.getInstance()
    var reminderHour by remember {
        mutableStateOf<Int?>(task?.reminderHour ?: currentCal.get(Calendar.HOUR_OF_DAY))
    }
    var reminderMinute by remember {
        mutableStateOf<Int?>(task?.reminderMinute ?: ((currentCal.get(Calendar.MINUTE) + 15) % 60))
    }
    var reminderEnabled by remember {
        mutableStateOf(task?.reminderEnabled ?: false)
    }

    // New user controls: Alarm Duration & Sound Type (System default vs Spotify)
    var alarmDurationSeconds by remember {
        mutableIntStateOf(task?.alarmDurationSeconds ?: 30)
    }
    var alarmSoundType by remember {
        mutableStateOf(task?.alarmSoundType ?: AlarmSoundType.SYSTEM_ALARM)
    }
    var spotifyUri by remember {
        mutableStateOf(task?.spotifyUri ?: "spotify:genre:focus")
    }

    val isEditMode = task != null && task.id > 0

    // Time picker dialog
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minuteOfHour ->
                reminderHour = hourOfDay
                reminderMinute = minuteOfHour
                reminderEnabled = true
            },
            reminderHour ?: 9,
            reminderMinute ?: 0,
            false // 12-hour AM/PM format
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_edit_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditMode) "Edit Task" else "Create New Task",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title *") },
                placeholder = { Text("e.g., Review project proposal") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Notes input
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Optional Notes") },
                placeholder = { Text("Add any details or key links...") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_notes_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Category Selection (3 fixed categories per PRD)
            Text(
                text = "Priority Section",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskCategory.entries.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val catColor = when (cat) {
                        TaskCategory.IMPORTANT -> ColorImportant
                        TaskCategory.LESS_IMPORTANT -> ColorLessImportant
                        TaskCategory.DAILY -> ColorDaily
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCategory = cat }
                            .testTag("select_category_${cat.name.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            catColor.copy(alpha = if (isDarkTheme) 0.2f else 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(2.dp, catColor)
                        } else {
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
                            )
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cat.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = cat.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Time-based Reminder & Alarm Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Time Reminder",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Alarm Reminder",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Rings system default alarm or launches Spotify",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { checked ->
                                reminderEnabled = checked
                                if (checked && (reminderHour == null || reminderMinute == null)) {
                                    val now = Calendar.getInstance()
                                    reminderHour = now.get(Calendar.HOUR_OF_DAY)
                                    reminderMinute = (now.get(Calendar.MINUTE) + 15) % 60
                                }
                            },
                            modifier = Modifier.testTag("reminder_switch")
                        )
                    }

                    if (reminderEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Time Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val formattedTime = remember(reminderHour, reminderMinute) {
                                val h = reminderHour ?: 9
                                val m = reminderMinute ?: 0
                                val h12 = when {
                                    h == 0 -> 12
                                    h > 12 -> h - 12
                                    else -> h
                                }
                                val amPm = if (h >= 12) "PM" else "AM"
                                val minStr = m.toString().padStart(2, '0')
                                "$h12:$minStr $amPm"
                            }

                            Text(
                                text = "Alarm Time: $formattedTime",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Button(
                                onClick = { timePickerDialog.show() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("pick_reminder_time_button")
                            ) {
                                Text("Change Time")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. CONTROL ALARM TIME DURATION
                        Text(
                            text = "Alarm Ring Duration",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val durationOptions = listOf(15, 30, 60, 120)
                            durationOptions.forEach { seconds ->
                                val isSelected = alarmDurationSeconds == seconds
                                val label = when (seconds) {
                                    15 -> "15 sec"
                                    30 -> "30 sec"
                                    60 -> "1 min"
                                    120 -> "2 min"
                                    else -> "${seconds}s"
                                }
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { alarmDurationSeconds = seconds }
                                        .testTag("alarm_duration_${seconds}s"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. BUTTON / SELECTOR FOR ALARM SOUND (System Default or Spotify Special)
                        Text(
                            text = "Alarm Sound Selection",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Choice 1: System Default Alarm Tone
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { alarmSoundType = AlarmSoundType.SYSTEM_ALARM }
                                .testTag("select_sound_system_default"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (alarmSoundType == AlarmSoundType.SYSTEM_ALARM) {
                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.25f else 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (alarmSoundType == AlarmSoundType.SYSTEM_ALARM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "System Alarm",
                                    tint = if (alarmSoundType == AlarmSoundType.SYSTEM_ALARM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "System Default Alarm Tone",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Plays mobile's native loud alarm ringtone",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Choice 2: Spotify Special Alarm
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { alarmSoundType = AlarmSoundType.SPOTIFY }
                                .testTag("select_sound_spotify"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (alarmSoundType == AlarmSoundType.SPOTIFY) {
                                Color(0xFF1DB954).copy(alpha = if (isDarkTheme) 0.25f else 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (alarmSoundType == AlarmSoundType.SPOTIFY) Color(0xFF1DB954) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Spotify Special",
                                    tint = if (alarmSoundType == AlarmSoundType.SPOTIFY) Color(0xFF1DB954) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Special: Spotify Music Alarm",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Opens Spotify focus playlist or your chosen music",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Spotify configuration & preview button
                        if (alarmSoundType == AlarmSoundType.SPOTIFY) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1DB954).copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1DB954).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Target Playlist: Focus & Flow",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1DB954)
                                        )
                                        Text(
                                            text = "Will launch Spotify music when timer fires",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUri)).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                val webIntent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://open.spotify.com/genre/focus")
                                                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                                context.startActivity(webIntent)
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("preview_spotify_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            tint = Color(0xFF1DB954),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Choose/Test",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF1DB954)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditMode && onDelete != null) {
                    IconButton(
                        onClick = {
                            task?.let { onDelete(it) }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("delete_task_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Task",
                            tint = ColorImportant
                        )
                    }
                }

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                task?.id ?: 0L,
                                title,
                                notes,
                                selectedCategory,
                                if (reminderEnabled) reminderHour else null,
                                if (reminderEnabled) reminderMinute else null,
                                reminderEnabled,
                                alarmDurationSeconds,
                                alarmSoundType,
                                spotifyUri
                            )
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_task_button")
                ) {
                    Text(
                        text = if (isEditMode) "Save Changes" else "Add Task",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
