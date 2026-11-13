package com.bc.beerworks.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bc.beerworks.data.model.HoneySensoryProfile
import com.bc.beerworks.data.model.MaltSensoryProfile
import com.bc.beerworks.data.model.PomeSensoryProfile
import com.bc.beerworks.data.model.SensoryRadarProfile
import com.bc.beerworks.data.model.YeastSensoryProfile
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun SensoryRadarChart(
    axisData: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
    strokeColor: Color = MaterialTheme.colorScheme.primary,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = min(size.width, size.height) / 2f * 0.70f
            val count = axisData.size
            if (count == 0) return@Canvas

            val angleStep = (2 * PI / count).toFloat()
            val startAngleOffset = -PI.toFloat() / 2f

            // 1. Draw concentric grid polygons (1 to 5)
            for (level in 1..5) {
                val levelRadius = maxRadius * (level / 5f)
                val gridPath = Path()
                for (i in 0 until count) {
                    val angle = startAngleOffset + i * angleStep
                    val x = center.x + levelRadius * cos(angle)
                    val y = center.y + levelRadius * sin(angle)
                    if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                }
                gridPath.close()
                drawPath(
                    path = gridPath,
                    color = gridColor,
                    style = Stroke(width = if (level == 5) 1.5f else 1f)
                )
            }

            // 2. Draw radial spokes and axis labels
            for (i in 0 until count) {
                val angle = startAngleOffset + i * angleStep
                val spokeEnd = Offset(
                    x = center.x + maxRadius * cos(angle),
                    y = center.y + maxRadius * sin(angle)
                )
                drawLine(
                    color = gridColor,
                    start = center,
                    end = spokeEnd,
                    strokeWidth = 1f
                )

                // Label
                val label = axisData[i].first
                val labelRadius = maxRadius * 1.20f
                val labelCenter = Offset(
                    x = center.x + labelRadius * cos(angle),
                    y = center.y + labelRadius * sin(angle)
                )

                val textLayoutResult = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        color = labelColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                val textOffset = Offset(
                    x = labelCenter.x - textLayoutResult.size.width / 2f,
                    y = labelCenter.y - textLayoutResult.size.height / 2f
                )
                drawText(textLayoutResult, topLeft = textOffset)
            }

            // 3. Draw data polygon
            val dataPath = Path()
            val points = mutableListOf<Offset>()

            for (i in 0 until count) {
                val score = axisData[i].second.coerceIn(0f, 5f)
                val pointRadius = maxRadius * (score / 5f)
                val angle = startAngleOffset + i * angleStep
                val point = Offset(
                    x = center.x + pointRadius * cos(angle),
                    y = center.y + pointRadius * sin(angle)
                )
                points.add(point)
                if (i == 0) dataPath.moveTo(point.x, point.y) else dataPath.lineTo(point.x, point.y)
            }
            dataPath.close()

            // Draw filled area
            drawPath(
                path = dataPath,
                color = fillColor,
                style = Fill
            )

            // Draw outer stroke
            drawPath(
                path = dataPath,
                color = strokeColor,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Draw points at vertices
            for (point in points) {
                drawCircle(
                    color = strokeColor,
                    radius = 3.5.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

@Composable
fun SensoryRadarChart(
    profile: SensoryRadarProfile,
    modifier: Modifier = Modifier,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
    strokeColor: Color = MaterialTheme.colorScheme.primary,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val axisData = remember(profile) { profile.toAxisMap() }
    SensoryRadarChart(
        axisData = axisData,
        modifier = modifier,
        fillColor = fillColor,
        strokeColor = strokeColor,
        gridColor = gridColor,
        labelColor = labelColor
    )
}

@Composable
fun SensoryRadarChart(
    profile: MaltSensoryProfile,
    modifier: Modifier = Modifier,
    fillColor: Color = Color(0xFFD97706).copy(alpha = 0.35f),
    strokeColor: Color = Color(0xFFD97706),
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val axisData = remember(profile) { profile.toAxisMap() }
    SensoryRadarChart(
        axisData = axisData,
        modifier = modifier,
        fillColor = fillColor,
        strokeColor = strokeColor,
        gridColor = gridColor,
        labelColor = labelColor
    )
}

@Composable
fun SensoryRadarChart(
    profile: HoneySensoryProfile,
    modifier: Modifier = Modifier,
    fillColor: Color = Color(0xFFE5A910).copy(alpha = 0.35f),
    strokeColor: Color = Color(0xFFE5A910),
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val axisData = remember(profile) { profile.toAxisMap() }
    SensoryRadarChart(
        axisData = axisData,
        modifier = modifier,
        fillColor = fillColor,
        strokeColor = strokeColor,
        gridColor = gridColor,
        labelColor = labelColor
    )
}

@Composable
fun SensoryRadarChart(
    profile: PomeSensoryProfile,
    modifier: Modifier = Modifier,
    fillColor: Color = Color(0xFF4D7C0F).copy(alpha = 0.35f),
    strokeColor: Color = Color(0xFF4D7C0F),
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val axisData = remember(profile) { profile.toAxisMap() }
    SensoryRadarChart(
        axisData = axisData,
        modifier = modifier,
        fillColor = fillColor,
        strokeColor = strokeColor,
        gridColor = gridColor,
        labelColor = labelColor
    )
}

@Composable
fun SensoryRadarChart(
    profile: YeastSensoryProfile,
    modifier: Modifier = Modifier,
    fillColor: Color = Color(0xFFD97706).copy(alpha = 0.35f),
    strokeColor: Color = Color(0xFFD97706),
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val axisData = remember(profile) { profile.toAxisMap() }
    SensoryRadarChart(
        axisData = axisData,
        modifier = modifier,
        fillColor = fillColor,
        strokeColor = strokeColor,
        gridColor = gridColor,
        labelColor = labelColor
    )
}
