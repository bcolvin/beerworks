package com.bc.beerworks.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bc.beerworks.data.model.MaltVariety

@Composable
fun MaltCard(
    malt: MaltVariety,
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
                        text = malt.countryFlag,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = malt.name,
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
                    MaltCategoryBadge(category = malt.category)
                }
            }

            if (malt.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = malt.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (malt.descriptors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                SingleRowChipLayout(spacing = 6.dp) {
                    malt.descriptors.forEach { descriptor ->
                        DescriptorChip(text = descriptor)
                    }
                }
            }
        }
    }
}

@Composable
fun MaltCategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (category) {
        "Base Malt" -> Color(0xFFFEF3C7) to Color(0xFF92400E) // Pale gold
        "Kilned & Toasted" -> Color(0xFFFFEDD5) to Color(0xFF9A3412) // Warm amber
        "Caramel & Crystal" -> Color(0xFFFEE2E2) to Color(0xFF991B1B) // Copper red
        "Roasted & Dark" -> Color(0xFF374151) to Color(0xFFF9FAFB) // Charcoal
        "Adjunct & Raw Grain" -> Color(0xFFE0E7FF) to Color(0xFF3730A3) // Indigo/pale
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 11.sp
        )
    }
}

@Composable
fun DiastaticPowerBadge(
    tier: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (tier) {
        "High" -> Color(0xFFD1FAE5) to Color(0xFF065F46) // Emerald green
        "Moderate" -> Color(0xFFFEF3C7) to Color(0xFF92400E) // Amber
        else -> Color(0xFFF3F4F6) to Color(0xFF4B5563) // Slate neutral
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Enzymes: $tier",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            fontSize = 11.sp
        )
    }
}
