package com.bc.beerworks.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bc.beerworks.ui.theme.BeerworksTheme
import com.bc.beerworks.util.SrmColorConverter
import kotlinx.coroutines.launch

fun determineFoamColor(appearanceText: String): Color {
    val text = appearanceText.lowercase()
    return when {
        text.contains("deep tan") || text.contains("brown head") || text.contains("dark tan") -> Color(0xFFC19A6B)
        text.contains("tan") -> Color(0xFFE6C280)
        text.contains("off-white") || text.contains("creamy") || text.contains("ivory") || text.contains("light tan") -> Color(0xFFFFFDD0)
        text.contains("yellowish") || text.contains("tinted") -> Color(0xFFFFF8DC)
        else -> Color(0xFFFFFFFF) // Pure white frothy head default
    }
}

@Composable
fun SrmVerticalScrollbar(
    scrollState: ScrollState,
    minSrm: Double?,
    maxSrm: Double?,
    headDescription: String,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }

    val isScrolling = scrollState.isScrollInProgress
    val expanded = isScrolling || isDragging

    val scrollFraction = if (scrollState.maxValue > 0) {
        (scrollState.value.toFloat() / scrollState.maxValue).coerceIn(0f, 1f)
    } else 0f

    val topColor = SrmColorConverter.srmToColor(minSrm ?: 2.0)
    val bottomColor = SrmColorConverter.srmToColor(maxSrm ?: minSrm ?: 4.0)

    val trackBrush = Brush.verticalGradient(
        colors = listOf(topColor, bottomColor)
    )

    val foamColor = remember(headDescription) {
        determineFoamColor(headDescription)
    }

    val barWidth by animateDpAsState(
        targetValue = if (expanded) 18.dp else 8.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "barWidth"
    )

    val barAlpha by animateFloatAsState(
        targetValue = if (expanded) 1.0f else 0.6f,
        label = "barAlpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(28.dp)
            .padding(end = 4.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        val heightPx = constraints.maxHeight.toFloat()
        val thumbHeightDp = 28.dp
        val density = LocalDensity.current
        val thumbHeightPx = with(density) { thumbHeightDp.toPx() }

        val usableHeightPx = (heightPx - thumbHeightPx).coerceAtLeast(1f)
        val thumbOffsetYPx = scrollFraction * usableHeightPx
        val thumbOffsetYDp = with(density) { thumbOffsetYPx.toDp() }

        // Track and Drag Controller
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(barWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(trackBrush)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                )
                .alpha(barAlpha)
                .pointerInput(scrollState, usableHeightPx) {
                    detectVerticalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false }
                    ) { change, _ ->
                        val targetY = change.position.y.coerceIn(0f, usableHeightPx)
                        val newFraction = targetY / usableHeightPx
                        coroutineScope.launch {
                            scrollState.scrollTo((newFraction * scrollState.maxValue).toInt())
                        }
                    }
                }
        )

        // Foam / Head Position Indicator (Thumb)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = thumbOffsetYDp)
                .width(barWidth + 4.dp)
                .height(thumbHeightDp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            foamColor,
                            foamColor.copy(alpha = 0.88f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Frothy Foam Bubbles Detail
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.95f))
                )
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.75f))
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SrmVerticalScrollbarPreview() {
    BeerworksTheme {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(300.dp)
                .background(Color.DarkGray)
        ) {
            SrmVerticalScrollbar(
                scrollState = ScrollState(0),
                minSrm = 2.0,
                maxSrm = 18.0,
                headDescription = "White frothy head",
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}
