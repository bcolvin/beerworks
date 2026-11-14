package com.bc.beerworks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bc.beerworks.data.model.YeastStrain

@Composable
fun YeastCard(
    yeast: YeastStrain,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IngredientCardScaffold(
        countryFlag = yeast.countryFlag,
        name = yeast.name,
        subtitle = yeast.scientificName,
        origin = yeast.origin,
        description = yeast.description,
        descriptors = yeast.descriptors,
        onClick = onClick,
        modifier = modifier
    ) {
        YeastClassificationBadge(classification = yeast.classification)
    }
}

@Composable
fun YeastClassificationBadge(
    classification: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (classification) {
        "Ale" -> Color(0xFFFEF3C7) to Color(0xFF92400E) // Warm Amber
        "Lager" -> Color(0xFFE0F2FE) to Color(0xFF0369A1) // Crisp Sky Blue
        "Wheat" -> Color(0xFFFEF9C3) to Color(0xFF854D0E) // Golden Wheat
        "Kveik" -> Color(0xFFFFEDD5) to Color(0xFFC2410C) // Vibrant Orange
        "Wild / Brett" -> Color(0xFFF3E8FF) to Color(0xFF6B21A8) // Mystic Purple
        "Bacteria" -> Color(0xFFFEE2E2) to Color(0xFF991B1B) // Crimson Tart
        "Wine / Mead / Cider" -> Color(0xFFDCFCE7) to Color(0xFF166534) // Forest Green
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    IngredientBadge(
        text = classification,
        backgroundColor = bgColor,
        contentColor = textColor,
        modifier = modifier
    )
}

@Composable
fun CompactGeneticChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}
