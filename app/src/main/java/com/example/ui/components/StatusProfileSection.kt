package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RainbowColors

@Composable
fun StatusProfileSection(
    daysActive: Int,
    totalQuestsGiven: Int,
    totalQuestsCompleted: Int,
    isDarkTheme: Boolean,
    onViewProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("status_profile_section")
            .shadow(
                elevation = if (isDarkTheme) 6.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // User Header Row (clickable for profile modal)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onViewProfileClick() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile Avatar with dynamic gradient
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(RainbowColors[4], RainbowColors[6])
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Profile & Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Active user",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "Daily tracker & quest overview",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Profile Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Required Status Cards:
            // 1. "Active": How many days you were active on the app after installing it
            // 2. "Total Quests Given": Total number of questions/quests written in the app
            // 3. "Total Quests Completed": Total number of quests completed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Active Days
                StatusMetricPill(
                    icon = Icons.Default.CalendarMonth,
                    title = "Active",
                    value = "$daysActive ${if (daysActive == 1) "day" else "days"}",
                    subtitle = "Since install",
                    accentColor = Color(0xFF3B82F6), // Sky Blue
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("status_active_days")
                )

                // Card 2: Total Quests Given
                StatusMetricPill(
                    icon = Icons.Default.FormatListNumbered,
                    title = "Quests Given",
                    value = "$totalQuestsGiven",
                    subtitle = "Total in app",
                    accentColor = Color(0xFFF59E0B), // Amber
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("status_quests_given")
                )

                // Card 3: Total Quests Completed
                StatusMetricPill(
                    icon = Icons.Default.AssignmentTurnedIn,
                    title = "Completed",
                    value = "$totalQuestsCompleted",
                    subtitle = "Finished",
                    accentColor = Color(0xFF10B981), // Emerald Green
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("status_quests_completed")
                )
            }
        }
    }
}

@Composable
private fun StatusMetricPill(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDarkTheme) 0.5f else 0.65f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
                maxLines = 1
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}
