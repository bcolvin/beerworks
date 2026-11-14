package com.bc.beerworks.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bc.beerworks.data.model.PomeVariety

@Composable
fun PomeCard(
    pome: PomeVariety,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IngredientCardScaffold(
        countryFlag = pome.countryFlag,
        name = pome.name,
        subtitle = pome.botanicalName,
        origin = pome.origin,
        description = pome.description,
        descriptors = pome.descriptors,
        onClick = onClick,
        modifier = modifier
    ) {
        FruitTypeBadge(fruitType = pome.fruitType)
        PomeClassificationBadge(classification = pome.classification)
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
    IngredientBadge(
        text = "$emoji $fruitType",
        backgroundColor = bgColor,
        contentColor = textColor,
        modifier = modifier
    )
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
    IngredientBadge(
        text = classification,
        backgroundColor = bgColor,
        contentColor = textColor,
        modifier = modifier
    )
}
