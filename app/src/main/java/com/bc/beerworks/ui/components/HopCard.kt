package com.bc.beerworks.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bc.beerworks.data.model.HopVariety

@Composable
fun HopCard(
    hop: HopVariety,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IngredientCardScaffold(
        countryFlag = hop.countryFlag,
        name = hop.name,
        subtitle = "Humulus lupulus",
        origin = hop.origin,
        description = hop.description,
        descriptors = (hop.sensoryCategories + hop.descriptors).distinct(),
        onClick = onClick,
        modifier = modifier
    ) {
        if (hop.purpose != "Dual-Purpose") {
            PurposeBadge(purpose = hop.purpose)
        }
        IngredientBadge(
            text = hop.alphaAcidRangeText,
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PurposeBadge(purpose: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (purpose.lowercase()) {
        "aroma" -> Color(0xFF0D9488).copy(alpha = 0.15f) to Color(0xFF0F766E)
        "bittering" -> Color(0xFFF59E0B).copy(alpha = 0.18f) to Color(0xFFB45309)
        else -> Color(0xFF8B5CF6).copy(alpha = 0.15f) to Color(0xFF6D28D9)
    }
    IngredientBadge(
        text = purpose,
        backgroundColor = bgColor,
        contentColor = textColor,
        modifier = modifier
    )
}
