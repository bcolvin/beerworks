package com.bc.beerworks.ui.maltdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.MaltVariety
import com.bc.beerworks.ui.components.DiastaticPowerBadge
import com.bc.beerworks.ui.components.MaltCategoryBadge
import com.bc.beerworks.ui.ingredientdetail.AssociatedStylesSection
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailLoadingOrContent
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailScaffold
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailViewModel
import com.bc.beerworks.ui.ingredientdetail.IngredientSensoryProfileCard
import com.bc.beerworks.ui.ingredientdetail.IngredientSubstitutesCard
import com.bc.beerworks.util.SrmColorConverter

@Composable
fun MaltDetailScreen(
    viewModel: IngredientDetailViewModel<MaltVariety>,
    onBackClick: () -> Unit,
    onMaltClick: (String) -> Unit,
    onStyleClick: (BeerStyle) -> Unit
) {
    val malt by viewModel.item.collectAsState()
    val substituteMalts by viewModel.substitutes.collectAsState()
    val commonStyles by viewModel.commonStyles.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    IngredientDetailLoadingOrContent(item = malt) { currentMalt ->
        MaltDetailContent(
            malt = currentMalt,
            substituteMalts = substituteMalts,
            commonStyles = commonStyles,
            onBackClick = onBackClick,
            onMaltClick = onMaltClick,
            onStyleClick = onStyleClick,
            onToggleStyleFavorite = viewModel::toggleStyleFavorite,
            isFavorite = isFavorite,
            onToggleFavorite = viewModel::toggleFavorite
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaltDetailContent(
    malt: MaltVariety,
    substituteMalts: List<MaltVariety>,
    commonStyles: List<BeerStyle>,
    onBackClick: () -> Unit,
    onMaltClick: (String) -> Unit,
    onStyleClick: (BeerStyle) -> Unit,
    onToggleStyleFavorite: (BeerStyle) -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
    var showDiastaticDialog by remember { mutableStateOf(false) }

    IngredientDetailScaffold(
        title = malt.name,
        isFavorite = isFavorite,
        onBackClick = onBackClick,
        onToggleFavorite = onToggleFavorite
    ) {
        // 1. HERO HEADER
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = malt.countryFlag,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = malt.origin,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    MaltCategoryBadge(category = malt.category)
                }

                if (malt.grainType != "Barley") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Grain Type: ${malt.grainType}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                if (malt.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = malt.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. TYPICAL BREWING USAGE
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
                Text(
                    text = "Typical Brewing Usage",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = malt.typicalUsage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (malt.functionalTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Functional Recipe Roles",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        malt.functionalTags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. ORGANOLEPTIC FLAVOR PROFILE
        IngredientSensoryProfileCard(
            profile = malt.radarProfile,
            descriptors = malt.descriptors,
            title = "Sensory Flavor Profile"
        )

        // 4. TECHNICAL BREWING SPECS
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
                Text(
                    text = "Technical Brewing Specs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Color row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Color Rating",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val swatchColor = SrmColorConverter.srmToColor(malt.averageColor)
                        Box(
                            modifier = Modifier
                                .size(24.dp, 16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(swatchColor)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = malt.colorRangeText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Extract Potential
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Extract Potential",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${malt.extractPotentialDbfg}% DBFG",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Max Grist %
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Max Recommended Grist",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${malt.maxGristPercentage}% of grist",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Diastatic Power
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Diastatic Power",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = { showDiastaticDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Diastatic Power Information",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${malt.diastaticPowerLintner} °Lintner",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        DiastaticPowerBadge(tier = malt.diastaticTier)
                    }
                }
            }
        }

        // 5. MALT SUBSTITUTES
        IngredientSubstitutesCard(
            title = "Malt Substitutes (${substituteMalts.size})",
            substitutes = substituteMalts,
            chipLabel = { sub -> "${sub.countryFlag} ${sub.name} (${sub.category})" },
            onSubstituteClick = onMaltClick
        )

        // 6. NOTABLE MALTSTERS & COMMERCIAL EXAMPLES
        if (malt.commercialExamples.isNotEmpty()) {
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
                    Text(
                        text = "Notable Maltsters & Commercial Brands",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        malt.commercialExamples.forEach { example ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = example,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. ASSOCIATED BEER STYLES
        AssociatedStylesSection(
            title = "Featured In Beer Styles",
            styles = commonStyles,
            onStyleClick = onStyleClick,
            onToggleStyleFavorite = onToggleStyleFavorite
        )
    }

    if (showDiastaticDialog) {
        AlertDialog(
            onDismissRequest = { showDiastaticDialog = false },
            title = {
                Text(
                    text = "Diastatic Power Explained",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Diastatic Power (${malt.diastaticPowerLintner} °Lintner):",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Diastatic power measures the enzymatic capacity (alpha- and beta-amylase) of malted grain to break down starches into fermentable sugars during mashing."
                    )
                    Text(
                        text = "• Self-Conversion Threshold: A grist requires approximately 30–35 °Lintner average to convert its own starches without remaining starchy haze."
                    )
                    Text(
                        text = "• Adjunct Conversion: High-enzyme malts (≥ 100 °Lintner) can convert up to 30–50% unmalted grains (like flaked corn, flaked oats, or raw wheat) or non-enzymatic specialty malts."
                    )
                    Text(
                        text = "• Specialty Grains: Most crystal, caramel, and dark roasted malts have 0 °Lintner because kilning heat denatured their enzymes, meaning they rely on base malts for conversion."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDiastaticDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }
}
