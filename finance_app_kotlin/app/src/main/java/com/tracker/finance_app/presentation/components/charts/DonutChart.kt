package com.tracker.finance_app.presentation.components.charts

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.tracker.finance_app.core.util.Formatters
import com.tracker.finance_app.domain.model.CategoryBreakdown
import kotlin.math.atan2

val ChartColors = listOf(
    Color(0xFF3B82F6), Color(0xFFF97316), Color(0xFF10B981), Color(0xFF8B5CF6),
    Color(0xFFEF4444), Color(0xFF06B6D4), Color(0xFFF59E0B), Color(0xFFEC4899)
)

@Composable
fun DonutChart(
    breakdowns: List<CategoryBreakdown>,
    modifier: Modifier = Modifier
) {
    if (breakdowns.isEmpty()) return

    val totalAmount = breakdowns.sumOf { it.totalAmount }
    var animationPlayed by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(key1 = breakdowns) {
        animationPlayed = true
    }

    val sweepAngleTransition by animateFloatAsState(
        targetValue = if (animationPlayed) 360f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "sweepAngle"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(breakdowns) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            var angle = (atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI).toFloat()
                            if (angle < 0) angle += 360f
                            // Adjust for starting angle at -90 (top)
                            var tapAngle = angle + 90f
                            if (tapAngle >= 360f) tapAngle -= 360f

                            var currentAngle = 0f
                            for (i in breakdowns.indices) {
                                val proportion = (breakdowns[i].totalAmount / totalAmount).toFloat()
                                val sweepAngle = proportion * 360f
                                if (tapAngle >= currentAngle && tapAngle < currentAngle + sweepAngle) {
                                    selectedIndex = if (selectedIndex == i) -1 else i
                                    break
                                }
                                currentAngle += sweepAngle
                            }
                        }
                    }
            ) {
                var startAngle = -90f
                for (i in breakdowns.indices) {
                    val breakdown = breakdowns[i]
                    val proportion = (breakdown.totalAmount / totalAmount).toFloat()
                    val sweepAngle = proportion * sweepAngleTransition

                    val isSelected = selectedIndex == i
                    val strokeWidth = if (isSelected) 40.dp.toPx() else 30.dp.toPx()

                    drawArc(
                        color = ChartColors[i % ChartColors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    )
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TOTAL SPEND", style = MaterialTheme.typography.labelSmall)
                Text(
                    text = Formatters.formatCurrency(totalAmount),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Legend
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            breakdowns.forEachIndexed { index, breakdown ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(ChartColors[index % ChartColors.size], CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = breakdown.categoryName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${String.format("%.1f", breakdown.percentage)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = Formatters.formatCurrency(breakdown.totalAmount),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
