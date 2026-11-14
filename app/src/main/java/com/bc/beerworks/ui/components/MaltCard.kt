package com.bc.beerworks.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.bc.beerworks.data.model.MaltVariety

@Composable
fun MaltCard(
    malt: MaltVariety,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IngredientCardScaffold(
        countryFlag = malt.countryFlag,
        name = malt.name,
        subtitle = malt.grainType,
        origin = malt.origin,
        description = malt.description,
        descriptors = malt.descriptors,
        onClick = onClick,
        modifier = modifier
    ) {
        MaltCategoryBadge(category = malt.category)
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
    IngredientBadge(
        text = category,
        backgroundColor = bgColor,
        contentColor = textColor,
        modifier = modifier
    )
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
    IngredientBadge(
        text = "Enzymes: $tier",
        backgroundColor = bgColor,
        contentColor = textColor,
        modifier = modifier,
        fontWeight = FontWeight.SemiBold
    )
}
