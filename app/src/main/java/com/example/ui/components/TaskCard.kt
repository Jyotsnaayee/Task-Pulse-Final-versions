package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TaskCategory
import com.example.model.TaskItem
import com.example.ui.theme.ColorDaily
import com.example.ui.theme.ColorImportant
import com.example.ui.theme.ColorLessImportant

@Composable
fun TaskCard(
    task: TaskItem,
    onToggle: (TaskItem) -> Unit,
    onEdit: (TaskItem) -> Unit,
    onDelete: (TaskItem) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (task.category) {
        TaskCategory.IMPORTANT -> ColorImportant
        TaskCategory.LESS_IMPORTANT -> ColorLessImportant
        TaskCategory.DAILY -> ColorDaily
    }

    val cardBg = if (task.isCompleted) {
        if (isDarkTheme) Color(0xFF161E2E) else Color(0xFFF1F5F9)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val checkBgColor by animateColorAsState(
        targetValue = if (task.isCompleted) Color(0xFF10B981) else Color.Transparent,
        animationSpec = tween(250),
        label = "checkBgColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}")
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggle(task) },
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        tonalElevation = if (task.isCompleted) 0.dp else 2.dp,
        border = if (task.isCompleted) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority category indicator strip (left)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (task.isCompleted) categoryColor.copy(alpha = 0.35f) else categoryColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Custom Large Touch-Target Checkbox (48dp interactive area)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onToggle(task) }
                    .testTag("task_checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(checkBgColor)
                        .then(
                            if (!task.isCompleted) {
                                Modifier.background(
                                    Color.Transparent,
                                    RoundedCornerShape(7.dp)
                                )
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color.Transparent)
                                .then(
                                    androidx.compose.foundation.BorderStroke(
                                        2.dp,
                                        if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)
                                    ).let {
                                        Modifier.background(Color.Transparent)
                                    }
                                )
                        ) {
                            // Border outline for unchecked
                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)
                                ),
                                color = Color.Transparent,
                                modifier = Modifier.size(24.dp)
                            ) {}
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Task Title & Metadata
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (task.isCompleted) 0.5f else 0.8f
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    )
                }

                // Reminder Pill if enabled
                val reminderStr = task.formatReminderTime()
                if (task.reminderEnabled && reminderStr != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Reminder at $reminderStr",
                                tint = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(12.dp)
                            )
                            val soundIconPrefix = if (task.alarmSoundType == com.example.model.AlarmSoundType.SPOTIFY) "🎵 Spotify" else "🔔 Alarm"
                            Text(
                                text = "$reminderStr • ${task.formatDurationLabel()} • $soundIconPrefix",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                }
            }

            // Quick actions (Edit / Delete)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onEdit(task) },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("task_edit_button_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Task",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { onDelete(task) },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("task_delete_button_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Task",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
