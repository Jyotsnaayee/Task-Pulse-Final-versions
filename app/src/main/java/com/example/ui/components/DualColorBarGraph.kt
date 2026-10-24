package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyHistoryItem
import kotlin.math.max

// Distinct 2 colors required by user:
// Green shows complete work, Red shows incomplete work
val BarGraphGreen = Color(0xFF10B981) // Emerald Green for complete work
val BarGraphRed = Color(0xFFEF4444)   // Crimson Red for incomplete work

@Composable
fun DualColorBarGraph(
    history: List<DailyHistoryItem>,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Animation progress for entry/updates
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(history) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val maxTaskCount = remember(history) {
        val calculatedMax = history.maxOfOrNull { max(it.completedCount, it.incompleteCount) } ?: 5
        max(calculatedMax, 6)
    }

    val totalCompletedInGraph = history.sumOf { it.completedCount }
    val totalIncompleteInGraph = history.sumOf { it.incompleteCount }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quest_bar_graph_card")
            .shadow(
                elevation = if (isDarkTheme) 6.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = BarGraphGreen.copy(alpha = 0.2f)
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
            // Header Row with Icon and Legend
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
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Work Performance",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Work Completion Flow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Dual-bar comparison with trend lines",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Legend Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Green legend
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(BarGraphGreen)
                        )
                        Text(
                            text = "Complete",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) Color(0xFF6EE7B7) else Color(0xFF047857)
                        )
                    }

                    // Red legend
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(BarGraphRed)
                        )
                        Text(
                            text = "Incomplete",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Canvas Bar Graph with Connecting Trend Lines
            val gridLineColor = if (isDarkTheme) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0)
            val textColorInt = if (isDarkTheme) android.graphics.Color.parseColor("#94A3B8") else android.graphics.Color.parseColor("#64748B")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .testTag("dual_color_canvas")
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    val progress = animationProgress.value
                    val canvasWidth = size.width
                    val bottomPadding = 28.dp.toPx()
                    val topPadding = 16.dp.toPx()
                    val usableHeight = size.height - bottomPadding - topPadding

                    val itemCount = history.size
                    if (itemCount == 0) return@Canvas

                    // Draw horizontal dashed grid lines
                    val steps = 3
                    val dashedPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    for (step in 0..steps) {
                        val y = topPadding + (usableHeight / steps) * step
                        val gridValue = (maxTaskCount * (steps - step) / steps).coerceAtLeast(0)
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashedPathEffect
                        )
                    }

                    // Column calculations
                    val slotWidth = canvasWidth / itemCount
                    val barWidth = 14.dp.toPx()
                    val barSpacing = 4.dp.toPx()

                    val greenPoints = mutableListOf<Offset>()
                    val redPoints = mutableListOf<Offset>()

                    history.forEachIndexed { index, item ->
                        val centerX = slotWidth * index + slotWidth / 2f
                        val greenX = centerX - barWidth - (barSpacing / 2f)
                        val redX = centerX + (barSpacing / 2f)

                        // Green bar height (Complete)
                        val greenRatio = (item.completedCount.toFloat() / maxTaskCount.toFloat()).coerceIn(0f, 1f)
                        val greenHeight = (usableHeight * greenRatio * progress).coerceAtLeast(4.dp.toPx())
                        val greenTop = topPadding + (usableHeight - greenHeight)

                        // Red bar height (Incomplete)
                        val redRatio = (item.incompleteCount.toFloat() / maxTaskCount.toFloat()).coerceIn(0f, 1f)
                        val redHeight = (usableHeight * redRatio * progress).coerceAtLeast(4.dp.toPx())
                        val redTop = topPadding + (usableHeight - redHeight)

                        val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

                        // Draw Green bar (Complete)
                        drawRoundRect(
                            color = BarGraphGreen,
                            topLeft = Offset(greenX, greenTop),
                            size = Size(barWidth, greenHeight),
                            cornerRadius = cornerRadius
                        )

                        // Draw Red bar (Incomplete)
                        drawRoundRect(
                            color = BarGraphRed,
                            topLeft = Offset(redX, redTop),
                            size = Size(barWidth, redHeight),
                            cornerRadius = cornerRadius
                        )

                        // Record top points for connecting trend lines
                        greenPoints.add(Offset(greenX + barWidth / 2f, greenTop))
                        redPoints.add(Offset(redX + barWidth / 2f, redTop))

                        // Draw Day Label below each group
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = textColorInt
                                textSize = 11.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = item.dayLabel == "Today"
                            }
                            drawText(
                                item.dayLabel,
                                centerX,
                                size.height - 6.dp.toPx(),
                                paint
                            )
                        }
                    }

                    // Draw Green connecting trend line across complete work
                    if (greenPoints.size > 1) {
                        for (i in 0 until greenPoints.size - 1) {
                            drawLine(
                                color = BarGraphGreen.copy(alpha = 0.85f * progress),
                                start = greenPoints[i],
                                end = greenPoints[i + 1],
                                strokeWidth = 2.5.dp.toPx()
                            )
                        }
                    }

                    // Draw Red connecting trend line across incomplete work
                    if (redPoints.size > 1) {
                        for (i in 0 until redPoints.size - 1) {
                            drawLine(
                                color = BarGraphRed.copy(alpha = 0.85f * progress),
                                start = redPoints[i],
                                end = redPoints[i + 1],
                                strokeWidth = 2.5.dp.toPx()
                            )
                        }
                    }

                    // Small indicator dots at line intersections
                    greenPoints.forEach { pt ->
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = BarGraphGreen,
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }

                    redPoints.forEach { pt ->
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = BarGraphRed,
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick summary footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly summary: $totalCompletedInGraph completed • $totalIncompleteInGraph pending",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
