package com.bc.beerworks.ui.styledetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.ui.components.SrmVerticalScrollbar
import com.bc.beerworks.ui.theme.BeerworksTheme
import com.bc.beerworks.util.SrmColorConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleDetailContent(
    style: BeerStyle,
    onBackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    commonHops: List<HopVariety> = emptyList(),
    onHopClick: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    val srmMin = style.srmMin ?: 2.0
    val srmMax = style.srmMax ?: srmMin
    val srmColorMax = SrmColorConverter.srmToColor(srmMax)
    val isDarkBeer = ((srmMin + srmMax) / 2.0) >= 10.0
    val accentTitleColor = if (isDarkBeer) MaterialTheme.colorScheme.primary else srmColorMax

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${style.number} ${style.name}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = style.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (style.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (style.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 28.dp, top = 12.dp, bottom = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                if (style.overallImpression.isNotBlank()) {
                    DetailSectionCard(title = "Overall Impression", contentText = style.overallImpression, titleColor = accentTitleColor)
                }
                if (style.aroma.isNotBlank()) {
                    DetailSectionCard(title = "Aroma", contentText = style.aroma, titleColor = accentTitleColor)
                }
                if (style.appearance.isNotBlank()) {
                    DetailSectionCard(title = "Appearance", contentText = style.appearance, titleColor = accentTitleColor)
                }
                if (style.flavor.isNotBlank()) {
                    DetailSectionCard(title = "Flavor", contentText = style.flavor, titleColor = accentTitleColor)
                }
                if (style.mouthfeel.isNotBlank()) {
                    DetailSectionCard(title = "Mouthfeel", contentText = style.mouthfeel, titleColor = accentTitleColor)
                }
                if (style.comments.isNotBlank()) {
                    DetailSectionCard(title = "Comments", contentText = style.comments, titleColor = accentTitleColor)
                }
                if (style.history.isNotBlank()) {
                    DetailSectionCard(title = "History", contentText = style.history, titleColor = accentTitleColor)
                }
                if (style.characteristicIngredients.isNotBlank()) {
                    DetailSectionCard(title = "Characteristic Ingredients", contentText = style.characteristicIngredients, titleColor = accentTitleColor)
                }
                if (style.styleComparison.isNotBlank()) {
                    DetailSectionCard(title = "Style Comparison", contentText = style.styleComparison, titleColor = accentTitleColor)
                }
                if (style.commercialExamples.isNotBlank()) {
                    DetailSectionCard(title = "Commercial Examples", contentText = style.commercialExamples, titleColor = accentTitleColor)
                }

                // Vital Statistics Card (moved below Commercial Examples)
                DetailSectionCard(title = "Vital Statistics", titleColor = accentTitleColor) {
                    Column {
                        StatRow("OG Range", formatRange(style.ogMin, style.ogMax, ""))
                        StatRow("FG Range", formatRange(style.fgMin, style.fgMax, ""))
                        StatRow("IBU Range", formatRange(style.ibuMin, style.ibuMax, " IBU"))
                        StatRow("ABV Range", formatRange(style.abvMin, style.abvMax, "%"))
                    }
                }

                if (commonHops.isNotEmpty()) {
                    CollapsibleCommonHopsCard(
                        commonHops = commonHops,
                        onHopClick = onHopClick
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Full-width SRM Horizontal Color Range Banner at the end of the scrollable content
                SrmBottomBanner(
                    minSrm = style.srmMin,
                    maxSrm = style.srmMax,
                    modifier = Modifier.clip(CardDefaults.shape)
                )
            }

            // Custom Vertical SRM Gradient Scrollbar with Foam Head Indicator
            SrmVerticalScrollbar(
                scrollState = scrollState,
                minSrm = style.srmMin,
                maxSrm = style.srmMax,
                headDescription = style.appearance,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = 8.dp, bottom = 8.dp, end = 2.dp)
            )
        }
    }
}

@Composable
fun SrmBottomBanner(
    minSrm: Double?,
    maxSrm: Double?,
    modifier: Modifier = Modifier
) {
    val minColor = SrmColorConverter.srmToColor(minSrm ?: 2.0)
    val maxColor = SrmColorConverter.srmToColor(maxSrm ?: minSrm ?: 4.0)

    val brush = Brush.horizontalGradient(
        colors = listOf(minColor, maxColor)
    )

    val srmText = when {
        minSrm != null && maxSrm != null -> "${minSrm.toInt()} – ${maxSrm.toInt()} SRM Color Range"
        minSrm != null -> "${minSrm.toInt()} SRM Color"
        else -> "SRM Color Range"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(brush)
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.3f), shape = CardDefaults.shape),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.45f)
            )
        ) {
            Text(
                text = srmText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollapsibleCommonHopsCard(
    commonHops: List<HopVariety>,
    onHopClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Common Hops (${commonHops.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commonHops.forEach { hop ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .clickable { onHopClick(hop.id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${hop.countryFlag} ${hop.name} (${hop.purpose})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSectionCard(
    title: String,
    contentText: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    customContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (contentText != null) {
                Text(
                    text = contentText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            customContent?.invoke()
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(text = "$label: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

fun formatRange(min: Double?, max: Double?, suffix: String): String {
    return when {
        min != null && max != null -> "$min – $max$suffix"
        min != null -> "$min$suffix"
        else -> "N/A"
    }
}

@Preview(showBackground = true)
@Composable
fun StyleDetailContentPreview() {
    BeerworksTheme {
        StyleDetailContent(
            style = BeerStyle(
                id = "7B",
                name = "Altbier",
                category = "Amber Bitter European Beer",
                categoryNumber = "7",
                number = "7B",
                overallImpression = "A moderately colored, well-attenuated, bitter beer.",
                aroma = "Malty and rich with grainy characteristics.",
                appearance = "Amber to deep copper color. Brilliant clarity.",
                flavor = "Malt profile similar to the aroma, with an assertive hop bitterness.",
                mouthfeel = "Medium body. Smooth. Medium to medium-high carbonation.",
                comments = "Classic, traditional examples served from casks in Düsseldorf.",
                history = "Developed in the late 19th century in Düsseldorf.",
                characteristicIngredients = "German base malts, Spalt hops.",
                styleComparison = "More bitter and malty than International Amber Lagers.",
                ibuMin = 25.0,
                ibuMax = 50.0,
                ogMin = 1.044,
                ogMax = 1.052,
                fgMin = 1.008,
                fgMax = 1.014,
                abvMin = 4.3,
                abvMax = 5.5,
                srmMin = 9.0,
                srmMax = 17.0,
                commercialExamples = "Uerige Altbier, Füchschen Alt",
                tags = listOf("amber", "bitter"),
                guidelineType = GuidelineType.BJCP_2021,
                isFavorite = true
            ),
            onBackClick = {},
            onToggleFavorite = {}
        )
    }
}
