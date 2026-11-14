package com.bc.beerworks.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.bc.beerworks.data.model.HoneyVariety

@Composable
fun HoneyCard(
    honey: HoneyVariety,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IngredientCardScaffold(
        countryFlag = honey.countryFlag,
        name = honey.name,
        subtitle = honey.botanicalSource,
        origin = honey.origin,
        description = honey.description,
        descriptors = honey.descriptors,
        onClick = onClick,
        modifier = modifier
    ) {
        FloralClassificationBadge(classification = honey.floralClassification)
    }
}

@Composable
fun FloralClassificationBadge(
    classification: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (classification) {
        "Monofloral" -> Color(0xFFFEF3C7) to Color(0xFF92400E) // Warm golden amber
        "Polyfloral" -> Color(0xFFE0E7FF) to Color(0xFF3730A3) // Violet/Indigo wildflower
        "Honeydew" -> Color(0xFFDCFCE7) to Color(0xFF166534) // Forest green
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    IngredientBadge(
        text = classification,
        backgroundColor = bgColor,
        contentColor = textColor,
        modifier = modifier
    )
}

@Composable
fun FlavorIntensityBadge(
    intensity: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (intensity) {
        "Delicate" -> Color(0xFFF3F4F6) to Color(0xFF374151) // Soft neutral
        "Moderate" -> Color(0xFFFEF3C7) to Color(0xFF92400E) // Amber
        "Robust" -> Color(0xFFFEE2E2) to Color(0xFF991B1B) // Coral red
        else -> Color(0xFFF3F4F6) to Color(0xFF4B5563)
    }
    IngredientBadge(
        text = "Intensity: $intensity",
        backgroundColor = bgColor,
        contentColor = textColor,
        modifier = modifier,
        fontWeight = FontWeight.SemiBold
    )
}
