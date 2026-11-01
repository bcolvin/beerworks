package com.bc.beerworks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bc.beerworks.ui.theme.BeerworksTheme
import com.bc.beerworks.util.SrmColorConverter

@Composable
fun SrmChip(
    srm: Double?,
    modifier: Modifier = Modifier
) {
    val color = SrmColorConverter.srmToColor(srm)
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
    )
}

@Composable
fun SrmRangeSwatch(
    minSrm: Double?,
    maxSrm: Double?,
    modifier: Modifier = Modifier
) {
    val minColor = SrmColorConverter.srmToColor(minSrm ?: 2.0)
    val maxColor = SrmColorConverter.srmToColor(maxSrm ?: minSrm ?: 2.0)

    val brush = Brush.horizontalGradient(
        colors = listOf(minColor, maxColor)
    )

    val srmLabel = when {
        minSrm != null && maxSrm != null -> "${minSrm.toInt()}–${maxSrm.toInt()} SRM"
        minSrm != null -> "${minSrm.toInt()} SRM"
        else -> "N/A SRM"
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = srmLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SrmRangeSwatchPreview() {
    BeerworksTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            SrmRangeSwatch(minSrm = 2.0, maxSrm = 14.0)
        }
    }
}
