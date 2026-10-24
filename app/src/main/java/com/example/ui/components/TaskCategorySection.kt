package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TaskCategory
import com.example.model.TaskItem
import com.example.ui.theme.ColorDaily
import com.example.ui.theme.ColorImportant
import com.example.ui.theme.ColorLessImportant

@Composable
fun TaskCategorySection(
    category: TaskCategory,
    tasks: List<TaskItem>,
    onToggleTask: (TaskItem) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    onAddTaskInCategory: (TaskCategory) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(true) }

    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size

    val (badgeColor, categoryIcon) = when (category) {
        TaskCategory.IMPORTANT -> Pair(ColorImportant, Icons.Default.PriorityHigh)
        TaskCategory.LESS_IMPORTANT -> Pair(ColorLessImportant, Icons.Default.Schedule)
        TaskCategory.DAILY -> Pair(ColorDaily, Icons.Default.DateRange)
    }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        label = "expandRotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_section_${category.name.lowercase()}")
    ) {
        // Section Header Row
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDarkTheme) 0.5f else 0.7f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Category icon circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = category.title,
                            tint = badgeColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Counter badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (totalCount > 0 && completedCount == totalCount) {
                                    Color(0xFF10B981).copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ) {
                                Text(
                                    text = "$completedCount/$totalCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalCount > 0 && completedCount == totalCount) {
                                        Color(0xFF10B981)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = category.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { onAddTaskInCategory(category) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("add_task_in_${category.name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task to ${category.title}",
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotationAngle)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Collapsible Content
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (tasks.isEmpty()) {
                    // Empty placeholder card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No tasks in ${category.title} yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = { onAddTaskInCategory(category) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("empty_add_${category.name.lowercase()}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Task", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                } else {
                    tasks.forEach { task ->
                        TaskCard(
                            task = task,
                            onToggle = onToggleTask,
                            onEdit = onEditTask,
                            onDelete = onDeleteTask,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }
        }
    }
}
