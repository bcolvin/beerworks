package com.bc.beerworks.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bc.beerworks.data.model.PomeVariety

@Composable
fun PomeCard(
    pome: PomeVariety,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Flag + Name, plus Fruit Type and Classification Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = pome.countryFlag,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = pome.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FruitTypeBadge(fruitType = pome.fruitType)
                    PomeClassificationBadge(classification = pome.classification)
                }
            }

            if (pome.botanicalName.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${pome.botanicalName} • ${pome.origin}",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (pome.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = pome.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (pome.descriptors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                SingleRowChipLayout(spacing = 6.dp) {
                    pome.descriptors.forEach { descriptor ->
                        DescriptorChip(text = descriptor)
                    }
                }
            }
        }
    }
}

@Composable
fun FruitTypeBadge(
    fruitType: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, emoji) = when (fruitType) {
        "Apple" -> Triple(Color(0xFFDCFCE7), Color(0xFF166534), "🍏")
        "Pear" -> Triple(Color(0xFFFEF9C3), Color(0xFF854D0E), "🍐")
        else -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, "🍎")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$emoji $fruitType",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 11.sp
        )
    }
}

@Composable
fun PomeClassificationBadge(
    classification: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (classification) {
        "Bittersweet" -> Color(0xFFFEF3C7) to Color(0xFF92400E) // Amber
        "Bittersharp" -> Color(0xFFFEE2E2) to Color(0xFF991B1B) // Crimson
        "Sharp" -> Color(0xFFDCFCE7) to Color(0xFF166534) // Crisp green
        "Sweet" -> Color(0xFFDBEAFE) to Color(0xFF1E40AF) // Royal blue
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = classification,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 11.sp
        )
    }
}
