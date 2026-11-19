package com.bc.beerworks.ui.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.BjcpScoringTier
import com.bc.beerworks.data.model.OffFlavor
import com.bc.beerworks.data.model.isCider
import com.bc.beerworks.data.model.isMead
import com.bc.beerworks.util.formattedAbvRange
import com.bc.beerworks.util.formattedFgRange
import com.bc.beerworks.util.formattedIbuRange
import com.bc.beerworks.util.formattedOgRange
import com.bc.beerworks.util.formattedSrmRange

@Immutable
internal data class SectionGuidelineInfo(
    val sectionTitle: String,
    val linkedStyle: BeerStyle?,
    val guidelineText: String,
    val fallbackTip: String,
    val styleLabel: String = "BJCP Beer Style"
)

@Composable
internal fun StyleQuickPeekSheet(style: BeerStyle) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "${style.number} • ${style.name}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = style.category,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
        if (style.overallImpression.isNotBlank()) {
            Text(
                text = style.overallImpression,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (style.guidelineType.isMead || style.guidelineType.isCider) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VitalStat("OG", style.formattedOgRange())
                VitalStat("FG", style.formattedFgRange())
                VitalStat("ABV", style.formattedAbvRange())
            }
            listOfNotNull(
                style.sweetness?.takeIf { it.isNotBlank() }?.let { "Sweetness" to it },
                style.carbonation?.takeIf { it.isNotBlank() }?.let { "Carbonation" to it },
                style.strength?.takeIf { it.isNotBlank() }?.let { "Strength" to it }
            ).forEach { (label, value) ->
                Text(
                    text = "$label: $value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VitalStat("OG", style.formattedOgRange())
                VitalStat("FG", style.formattedFgRange())
                VitalStat("IBU", style.formattedIbuRange())
                VitalStat("SRM", style.formattedSrmRange())
                VitalStat("ABV", style.formattedAbvRange())
            }
        }
    }
}

@Composable
private fun VitalStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun OffFlavorQuickPeekSheet(offFlavor: OffFlavor) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "${offFlavor.emoji} ${offFlavor.name}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${offFlavor.chemicalCompound} • Threshold: ${offFlavor.perceptionThreshold}",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
        if (offFlavor.description.isNotBlank()) {
            Text(text = offFlavor.description, style = MaterialTheme.typography.bodyMedium)
        }
        if (offFlavor.aromaPerception.isNotBlank()) {
            Text(text = "Aroma: ${offFlavor.aromaPerception}", style = MaterialTheme.typography.bodySmall)
        }
        if (offFlavor.flavorPerception.isNotBlank()) {
            Text(text = "Flavor: ${offFlavor.flavorPerception}", style = MaterialTheme.typography.bodySmall)
        }
        if (offFlavor.mouthfeelPerception.isNotBlank()) {
            Text(text = "Mouthfeel: ${offFlavor.mouthfeelPerception}", style = MaterialTheme.typography.bodySmall)
        }
        if (offFlavor.typicalOrigins.isNotEmpty()) {
            Text(
                text = "Typical Origins: ${offFlavor.typicalOrigins.joinToString("; ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun SectionInfoSheet(info: SectionGuidelineInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (info.linkedStyle != null) {
            Text(
                text = "${info.linkedStyle.number} • ${info.linkedStyle.name}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider()
        }
        if (info.guidelineText.isNotBlank()) {
            Text(
                text = info.guidelineText,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = info.fallbackTip,
                style = MaterialTheme.typography.bodyMedium
            )
            if (info.linkedStyle == null) {
                Text(
                    text = "Tip: Select a ${info.styleLabel} under Entry Details to view style-specific ${info.sectionTitle.lowercase()} guidelines here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun BjcpScoringGuideSheet(activeTier: BjcpScoringTier) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "BJCP Scoring Guide",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider()
        BjcpScoringTier.entries.forEach { tier ->
            val isActive = tier == activeTier
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isActive) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tier.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tier.rangeText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tier.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
