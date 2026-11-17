package com.bc.beerworks.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bc.beerworks.data.model.OffFlavor

@Composable
fun OffFlavorCard(
    offFlavor: OffFlavor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IngredientCardScaffold(
        countryFlag = offFlavor.countryFlag,
        name = offFlavor.name,
        subtitle = offFlavor.cardSubtitle,
        origin = "",
        description = offFlavor.description,
        descriptors = offFlavor.descriptors,
        onClick = onClick,
        modifier = modifier
    ) {
        OffFlavorCategoryBadge(category = offFlavor.category)
    }
}

@Composable
fun OffFlavorCategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (category) {
        "Fermentation & Yeast" -> Color(0xFFFEF3C7) to Color(0xFF92400E) // Warm Amber
        "Mashing & Boil" -> Color(0xFFFEF9C3) to Color(0xFF854D0E) // Golden Wheat
        "Sanitation & Microbial" -> Color(0xFFFEE2E2) to Color(0xFF991B1B) // Crimson Tart
        "Oxidation & Aging" -> Color(0xFFF3E8FF) to Color(0xFF6B21A8) // Mystic Purple
        "Water & Packaging" -> Color(0xFFE0F2FE) to Color(0xFF0369A1) // Crisp Sky Blue
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    IngredientBadge(
        text = category,
        backgroundColor = bgColor,
        contentColor = textColor,
        modifier = modifier
    )
}
