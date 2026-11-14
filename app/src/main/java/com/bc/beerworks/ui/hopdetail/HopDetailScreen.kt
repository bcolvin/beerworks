package com.bc.beerworks.ui.hopdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.ui.components.AcidRangeGauge
import com.bc.beerworks.ui.components.PurposeBadge
import com.bc.beerworks.ui.ingredientdetail.AssociatedStylesSection
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailLoadingOrContent
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailScaffold
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailViewModel
import com.bc.beerworks.ui.ingredientdetail.IngredientSensoryProfileCard
import com.bc.beerworks.ui.ingredientdetail.IngredientSubstitutesCard

@Composable
fun HopDetailScreen(
    viewModel: IngredientDetailViewModel<HopVariety>,
    onBackClick: () -> Unit,
    onHopClick: (String) -> Unit,
    onStyleClick: (BeerStyle) -> Unit
) {
    val hop by viewModel.item.collectAsState()
    val childHops by viewModel.relatedItems.collectAsState()
    val substituteHops by viewModel.substitutes.collectAsState()
    val commonStyles by viewModel.commonStyles.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    IngredientDetailLoadingOrContent(item = hop) { currentHop ->
        HopDetailContent(
            hop = currentHop,
            childHops = childHops,
            substituteHops = substituteHops,
            commonStyles = commonStyles,
            onBackClick = onBackClick,
            onHopClick = onHopClick,
            onStyleClick = onStyleClick,
            onToggleStyleFavorite = viewModel::toggleStyleFavorite,
            isFavorite = isFavorite,
            onToggleFavorite = viewModel::toggleFavorite
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HopDetailContent(
    hop: HopVariety,
    childHops: List<HopVariety>,
    substituteHops: List<HopVariety>,
    commonStyles: List<BeerStyle>,
    onBackClick: () -> Unit,
    onHopClick: (String) -> Unit,
    onStyleClick: (BeerStyle) -> Unit,
    onToggleStyleFavorite: (BeerStyle) -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
    IngredientDetailScaffold(
        title = hop.name,
        isFavorite = isFavorite,
        onBackClick = onBackClick,
        onToggleFavorite = onToggleFavorite
    ) {
        // Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = hop.countryFlag, fontSize = 28.sp)
                        Text(
                            text = hop.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                    PurposeBadge(purpose = hop.purpose)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Origin: ${hop.origin} (${hop.countryCode})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (hop.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = hop.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Acid Potential Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Acid & Resin Potential",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                AcidRangeGauge(
                    label = "Alpha Acid (AA%)",
                    rangeText = hop.alphaAcidRangeText,
                    minValue = hop.alphaAcidMin,
                    maxValue = hop.alphaAcidMax,
                    scaleMax = 20.0,
                    gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                    subtitle = "Thermal isomerization (Bittering)"
                )

                Spacer(modifier = Modifier.height(10.dp))

                AcidRangeGauge(
                    label = "Beta Acid (BA%)",
                    rangeText = hop.betaAcidRangeText,
                    minValue = hop.betaAcidMin,
                    maxValue = hop.betaAcidMax,
                    scaleMax = 12.0,
                    gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                    subtitle = "Oxidative aging & preservation"
                )
            }
        }

        // Sensory Profile & Radar Chart
        IngredientSensoryProfileCard(
            profile = hop.radarProfile,
            descriptors = (hop.sensoryCategories + hop.descriptors).distinct(),
            highlightedDescriptors = hop.sensoryCategories.toSet(),
            title = "Sensory Profile"
        )

        // Lineage & Genetics
        if (hop.parentHopIds.isNotEmpty() || childHops.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Lineage & Genetics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (hop.parentHopIds.isNotEmpty()) {
                        Text(
                            text = "Parent Cultivars",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            hop.parentHopIds.forEach { parentId ->
                                ClickableHopChip(
                                    name = parentId.replace("_", " ").split(" ")
                                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
                                    onClick = { onHopClick(parentId) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (childHops.isNotEmpty()) {
                        Text(
                            text = "Descendant Cultivars (${childHops.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            childHops.forEach { child ->
                                ClickableHopChip(
                                    name = "${child.countryFlag} ${child.name}",
                                    onClick = { onHopClick(child.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Substitutes Section
        IngredientSubstitutesCard(
            title = "Common Substitutes & Alternatives",
            substitutes = substituteHops,
            chipLabel = { subHop -> "${subHop.countryFlag} ${subHop.name} (${subHop.alphaAcidRangeText})" },
            onSubstituteClick = onHopClick
        )

        // Common Beer Styles
        AssociatedStylesSection(
            title = "Common Beer Styles",
            styles = commonStyles,
            onStyleClick = onStyleClick,
            onToggleStyleFavorite = onToggleStyleFavorite
        )
    }
}

@Composable
fun ClickableHopChip(
    name: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
