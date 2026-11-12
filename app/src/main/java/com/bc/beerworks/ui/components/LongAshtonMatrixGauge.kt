package com.bc.beerworks.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun LongAshtonMatrixGauge(
    classification: String,
    acidityMalicPercent: Double,
    tanninPolyphenolPercent: Double,
    tanninLevel: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    val (activeColor, activeBg) = when (classification) {
        "Bittersweet" -> Color(0xFFD97706) to Color(0xFFFEF3C7) // Rich amber
        "Bittersharp" -> Color(0xFFDC2626) to Color(0xFFFEE2E2) // Crimson
        "Sharp" -> Color(0xFF16A34A) to Color(0xFFDCFCE7) // Crisp green
        "Sweet" -> Color(0xFF2563EB) to Color(0xFFDBEAFE) // Royal blue
        else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(16.dp)
    ) {
        // Header Row: Title & Active Classification Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Long Ashton Classification",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Barker Acidity & Tannin Matrix",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(activeBg)
                    .border(1.dp, activeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = classification,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = activeColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2x2 Matrix Graphic Canvas
        val gridOutlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
        val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val padX = 24.dp.toPx()
                val padY = 20.dp.toPx()
                val w = size.width - padX * 2
                val h = size.height - padY * 2

                val midX = padX + w * 0.45f // 0.45 malic threshold positioned at ~45% width
                val midY = padY + h * 0.50f // 0.20% tannin threshold positioned at ~50% height

                // Background quadrants tinting
                // Top-Left (Bittersweet): Low Acid, High Tannin
                val bsAlpha = if (classification == "Bittersweet") 0.25f else 0.05f
                drawRect(
                    color = Color(0xFFD97706).copy(alpha = bsAlpha),
                    topLeft = Offset(padX, padY),
                    size = Size(midX - padX, midY - padY)
                )

                // Top-Right (Bittersharp): High Acid, High Tannin
                val bsharpAlpha = if (classification == "Bittersharp") 0.25f else 0.05f
                drawRect(
                    color = Color(0xFFDC2626).copy(alpha = bsharpAlpha),
                    topLeft = Offset(midX, padY),
                    size = Size(padX + w - midX, midY - padY)
                )

                // Bottom-Left (Sweet): Low Acid, Low Tannin
                val swAlpha = if (classification == "Sweet") 0.25f else 0.05f
                drawRect(
                    color = Color(0xFF2563EB).copy(alpha = swAlpha),
                    topLeft = Offset(padX, midY),
                    size = Size(midX - padX, padY + h - midY)
                )

                // Bottom-Right (Sharp): High Acid, Low Tannin
                val shAlpha = if (classification == "Sharp") 0.25f else 0.05f
                drawRect(
                    color = Color(0xFF16A34A).copy(alpha = shAlpha),
                    topLeft = Offset(midX, midY),
                    size = Size(padX + w - midX, padY + h - midY)
                )

                // Outer boundary
                drawRect(
                    color = gridOutlineColor,
                    topLeft = Offset(padX, padY),
                    size = Size(w, h),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Dashed Threshold lines
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)

                // Vertical Acid Threshold Line (0.45%)
                drawLine(
                    color = dividerColor,
                    start = Offset(midX, padY),
                    end = Offset(midX, padY + h),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = dashEffect
                )

                // Horizontal Tannin Threshold Line (0.20%)
                drawLine(
                    color = dividerColor,
                    start = Offset(padX, midY),
                    end = Offset(padX + w, midY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = dashEffect
                )

                // Quadrant Name Labels
                val titleStyle = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelColor
                )

                // Bittersweet (Top-Left)
                drawText(
                    textMeasurer = textMeasurer,
                    text = "BITTERSWEET",
                    style = titleStyle.copy(
                        color = if (classification == "Bittersweet") Color(0xFFD97706) else labelColor
                    ),
                    topLeft = Offset(padX + 8.dp.toPx(), padY + 6.dp.toPx())
                )

                // Bittersharp (Top-Right)
                drawText(
                    textMeasurer = textMeasurer,
                    text = "BITTERSHARP",
                    style = titleStyle.copy(
                        color = if (classification == "Bittersharp") Color(0xFFDC2626) else labelColor
                    ),
                    topLeft = Offset(midX + 8.dp.toPx(), padY + 6.dp.toPx())
                )

                // Sweet (Bottom-Left)
                drawText(
                    textMeasurer = textMeasurer,
                    text = "SWEET",
                    style = titleStyle.copy(
                        color = if (classification == "Sweet") Color(0xFF2563EB) else labelColor
                    ),
                    topLeft = Offset(padX + 8.dp.toPx(), midY + 6.dp.toPx())
                )

                // Sharp (Bottom-Right)
                drawText(
                    textMeasurer = textMeasurer,
                    text = "SHARP",
                    style = titleStyle.copy(
                        color = if (classification == "Sharp") Color(0xFF16A34A) else labelColor
                    ),
                    topLeft = Offset(midX + 8.dp.toPx(), midY + 6.dp.toPx())
                )

                // Plot the specimen position dot
                // Acid scale: 0.0% to 1.0% malic mapped to padX..padX+w (clamped)
                // Tannin scale: 0.0% to 0.5% tannin mapped to (padY+h)..padY (inverted Y, clamped)
                val clampedAcid = acidityMalicPercent.toFloat().coerceIn(0.05f, 1.0f)
                val clampedTannin = tanninPolyphenolPercent.toFloat().coerceIn(0.04f, 0.50f)

                val dotX = if (clampedAcid <= 0.45f) {
                    padX + (clampedAcid / 0.45f) * (midX - padX)
                } else {
                    midX + ((clampedAcid - 0.45f) / 0.55f) * (padX + w - midX)
                }.coerceIn(padX + 6.dp.toPx(), padX + w - 6.dp.toPx())

                val dotY = if (clampedTannin >= 0.20f) {
                    midY - ((clampedTannin - 0.20f) / 0.30f) * (midY - padY)
                } else {
                    padY + h - (clampedTannin / 0.20f) * (padY + h - midY)
                }.coerceIn(padY + 6.dp.toPx(), padY + h - 6.dp.toPx())

                // Outer halo
                drawCircle(
                    color = activeColor.copy(alpha = 0.3f),
                    radius = 9.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
                // Solid dot
                drawCircle(
                    color = activeColor,
                    radius = 5.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
                // Center white point
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chemistry Breakdown Values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Acidity Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = "Titratable Acidity",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.US, "%.2f%% Malic", acidityMalicPercent),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (acidityMalicPercent > 0.45) "> 0.45% (High Acid)" else "≤ 0.45% (Low Acid)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (acidityMalicPercent > 0.45) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            // Tannin Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = "Tannin Polyphenols",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.US, "%.2f%% (%s)", tanninPolyphenolPercent, tanninLevel),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (tanninPolyphenolPercent >= 0.20) "≥ 0.20% (High Tannin)" else "< 0.20% (Low Tannin)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (tanninPolyphenolPercent >= 0.20) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
