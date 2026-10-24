package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.TaskCategory
import com.example.ui.components.AddEditTaskBottomSheet
import com.example.ui.components.DualColorBarGraph
import com.example.ui.components.ProfileDetailBottomSheet
import com.example.ui.components.RainbowProgressBar
import com.example.ui.components.ResetDayDialog
import com.example.ui.components.StatusProfileSection
import com.example.ui.components.TaskCategorySection
import com.example.ui.theme.RainbowColors
import com.example.viewmodel.TaskPulseViewModel

import com.example.ui.components.BannerAdView

@Composable
fun TaskPulseMainScreen(
    viewModel: TaskPulseViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDarkTheme = uiState.isDarkTheme

    // AdMob Unit ID
    val adUnitId = "ca-app-pub-4982960836378263/6650517212"

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    // Show ad when trying to close the app (PRD requirement)
    androidx.activity.compose.BackHandler {
        val app = context.applicationContext as? com.example.TaskPulseApplication
        app?.adManager?.showInterstitialAd {
            activity?.finish()
        }
    }

    // Request notification permission on Android 13+ (TIRAMISU)
    var hasRequestedPermission by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Handled gracefully */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasRequestedPermission) {
            hasRequestedPermission = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BannerAdView(adUnitId = adUnitId)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddTask() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("add_task_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp) // Responsive tablet & large screen constraint
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top App Header: Logo + App Name + Profile Icon + Reset Day + Theme Toggle
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Pulse icon pill
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(RainbowColors[0], RainbowColors[4], RainbowColors[6])
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚡",
                                    fontSize = 18.sp
                                )
                            }

                            Column {
                                Text(
                                    text = "TaskPulse",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Daily Flow & Momentum",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Profile Avatar Icon Button
                            IconButton(
                                onClick = { viewModel.openProfileSheet() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("open_profile_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Status Profile",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Reset Day Button (styled carefully per PRD 4.4)
                            OutlinedButton(
                                onClick = { viewModel.showResetConfirmation() },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("reset_day_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset Day",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Reset",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Theme Toggle Button (PRD 4.5)
                            val context = androidx.compose.ui.platform.LocalContext.current
                            IconButton(
                                onClick = {
                                    val app = context.applicationContext as? com.example.TaskPulseApplication
                                    app?.adManager?.showInterstitialAd {
                                        viewModel.toggleTheme()
                                    }
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("theme_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark Theme",
                                    tint = if (isDarkTheme) Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Global Rainbow Progress Bar (PRD 4.3)
                item {
                    RainbowProgressBar(
                        completedCount = uiState.completedCount,
                        totalCount = uiState.totalCount,
                        fraction = uiState.progressFraction,
                        percentage = uiState.progressPercentage,
                        isDarkTheme = isDarkTheme
                    )
                }

                // Status Profile Section (Required by User Prompt: Active days + Quests Given + Quests Completed)
                item {
                    StatusProfileSection(
                        daysActive = uiState.daysActive,
                        totalQuestsGiven = uiState.totalQuestsGiven,
                        totalQuestsCompleted = uiState.totalQuestsCompleted,
                        isDarkTheme = isDarkTheme,
                        onViewProfileClick = { viewModel.openProfileSheet() }
                    )
                }

                // Dual-Color Bar Graph with Lines (Required by User Prompt: Green for complete work, Red for incomplete work)
                item {
                    DualColorBarGraph(
                        history = uiState.dailyHistoryList,
                        isDarkTheme = isDarkTheme
                    )
                }

                // Section 1: Important Work (PRD 4.1)
                item {
                    TaskCategorySection(
                        category = TaskCategory.IMPORTANT,
                        tasks = uiState.tasks.filter { it.category == TaskCategory.IMPORTANT },
                        onToggleTask = { viewModel.onTaskToggle(it) },
                        onEditTask = { viewModel.openEditTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onAddTaskInCategory = { viewModel.openAddTask(it) },
                        isDarkTheme = isDarkTheme
                    )
                }

                // Section 2: Less Important Work (PRD 4.1)
                item {
                    TaskCategorySection(
                        category = TaskCategory.LESS_IMPORTANT,
                        tasks = uiState.tasks.filter { it.category == TaskCategory.LESS_IMPORTANT },
                        onToggleTask = { viewModel.onTaskToggle(it) },
                        onEditTask = { viewModel.openEditTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onAddTaskInCategory = { viewModel.openAddTask(it) },
                        isDarkTheme = isDarkTheme
                    )
                }

                // Section 3: Daily Work (PRD 4.1)
                item {
                    TaskCategorySection(
                        category = TaskCategory.DAILY,
                        tasks = uiState.tasks.filter { it.category == TaskCategory.DAILY },
                        onToggleTask = { viewModel.onTaskToggle(it) },
                        onEditTask = { viewModel.openEditTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onAddTaskInCategory = { viewModel.openAddTask(it) },
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }

    // Status Profile Detail Modal Bottom Sheet
    if (uiState.showProfileSheet) {
        ProfileDetailBottomSheet(
            daysActive = uiState.daysActive,
            totalQuestsGiven = uiState.totalQuestsGiven,
            totalQuestsCompleted = uiState.totalQuestsCompleted,
            userDisplayName = uiState.userDisplayName,
            isDarkTheme = isDarkTheme,
            onDismiss = { viewModel.dismissProfileSheet() }
        )
    }

    // Add / Edit Task Modal Bottom Sheet
    if (uiState.showAddEditSheet) {
        AddEditTaskBottomSheet(
            task = uiState.editingTask,
            onDismiss = { viewModel.dismissAddEditSheet() },
            onSave = { id, title, notes, category, hour, minute, reminderEnabled, duration, soundType, spotifyUri ->
                viewModel.saveTask(id, title, notes, category, hour, minute, reminderEnabled, duration, soundType, spotifyUri)
            },
            onDelete = { task ->
                viewModel.deleteTask(task)
            },
            isDarkTheme = isDarkTheme
        )
    }

    // Reset Day Confirmation Dialog (PRD 4.4)
    if (uiState.showResetDialog) {
        ResetDayDialog(
            onDismiss = { viewModel.dismissResetDialog() },
            onConfirm = { viewModel.confirmResetDay() }
        )
    }
}
