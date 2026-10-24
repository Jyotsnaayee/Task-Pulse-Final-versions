package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RainbowColors

@Composable
fun RainbowProgressBar(
    completedCount: Int,
    totalCount: Int,
    fraction: Float,
    percentage: Int,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Smooth animation for the progress width
    val animatedProgress by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "rainbowProgressAnimation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 6.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = RainbowColors.first().copy(alpha = if (isDarkTheme) 0.3f else 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Header stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(RainbowColors[0], RainbowColors[2])
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Daily Momentum",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Daily Momentum",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Completion status pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (percentage == 100 && totalCount > 0) {
                        Color(0xFF10B981).copy(alpha = 0.18f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (percentage == 100 && totalCount > 0) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "All complete",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = if (totalCount == 0) "0 of 0 tasks" else "$completedCount of $totalCount completed",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (percentage == 100 && totalCount > 0) {
                                Color(0xFF10B981)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rainbow Canvas Bar
            val trackColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFE2E8F0)
            val glowColor = RainbowColors[3].copy(alpha = if (isDarkTheme) 0.35f else 0.15f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .testTag("rainbow_progress_bar"),
                contentAlignment = Alignment.CenterStart
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                ) {
                    val cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())

                    // Background track
                    drawRoundRect(
                        color = trackColor,
                        topLeft = Offset.Zero,
                        size = size,
                        cornerRadius = cornerRadius
                    )

                    // Rainbow filled segment
                    val fillWidth = size.width * animatedProgress
                    if (fillWidth > 0f) {
                        val rainbowBrush = Brush.horizontalGradient(
                            colors = RainbowColors,
                            startX = 0f,
                            endX = size.width // Anchor gradient across full container for consistency
                        )

                        drawRoundRect(
                            brush = rainbowBrush,
                            topLeft = Offset.Zero,
                            size = Size(fillWidth, size.height),
                            cornerRadius = cornerRadius
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Percentage label and Motivational tagline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val motivationalText = when {
                    totalCount == 0 -> "Add tasks below to start your pulse"
                    percentage == 100 -> "✨ All tasks done! Outstanding day!"
                    percentage >= 75 -> "🔥 Almost at the finish line!"
                    percentage >= 50 -> "⚡ Over halfway there, keep it up!"
                    percentage > 0 -> "🌱 Great momentum, keep pushing!"
                    else -> "Ready to check off your first victory?"
                }

                Text(
                    text = motivationalText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (percentage == 100) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("progress_percentage_text")
                )
            }
        }
    }
}
