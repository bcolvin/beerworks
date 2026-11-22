package com.bc.beerworks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A custom layout that places chip children horizontally in a single row without wrapping,
 * left-aligned to the container start edge.
 * It measures each child sequentially and stops adding children as soon as the next child
 * would exceed the available constraints.maxWidth.
 */
@Composable
fun SingleRowChipLayout(
    modifier: Modifier = Modifier,
    spacing: Dp = 6.dp,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier.fillMaxWidth(),
        content = content
    ) { measurables, constraints ->
        val spacingPx = spacing.roundToPx()
        val placeables = mutableListOf<Placeable>()
        var currentWidth = 0
        var maxHeight = 0

        for (measurable in measurables) {
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            val neededWidth = if (placeables.isEmpty()) {
                placeable.width
            } else {
                currentWidth + spacingPx + placeable.width
            }

            if (neededWidth <= constraints.maxWidth) {
                placeables.add(placeable)
                currentWidth = neededWidth
                maxHeight = maxOf(maxHeight, placeable.height)
            } else {
                // Next chip does not fit in this row without wrapping; stop placing chips.
                break
            }
        }

        val layoutWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else currentWidth

        layout(layoutWidth, maxHeight) {
            var xPosition = 0
            for (placeable in placeables) {
                val yPosition = (maxHeight - placeable.height) / 2
                placeable.placeRelative(xPosition, yPosition)
                xPosition += placeable.width + spacingPx
            }
        }
    }
}

/**
 * Standard subtle descriptor chip used across card components.
 */
@Composable
fun DescriptorChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(6.dp)
    val bgColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .then(
                if (selected) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}
