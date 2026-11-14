package com.bc.beerworks.ui.honeydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.HoneyVariety
import com.bc.beerworks.ui.components.FlavorIntensityBadge
import com.bc.beerworks.ui.components.FloralClassificationBadge
import com.bc.beerworks.ui.ingredientdetail.AssociatedStylesSection
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailLoadingOrContent
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailScaffold
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailViewModel
import com.bc.beerworks.ui.ingredientdetail.IngredientSensoryProfileCard
import com.bc.beerworks.ui.ingredientdetail.IngredientSubstitutesCard

@Composable
fun HoneyDetailScreen(
    viewModel: IngredientDetailViewModel<HoneyVariety>,
    onBackClick: () -> Unit,
    onHoneyClick: (String) -> Unit,
    onStyleClick: (BeerStyle) -> Unit
) {
    val honey by viewModel.item.collectAsState()
    val substituteHoneys by viewModel.substitutes.collectAsState()
    val commonStyles by viewModel.commonStyles.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    IngredientDetailLoadingOrContent(item = honey) { currentHoney ->
        HoneyDetailContent(
            honey = currentHoney,
            substituteHoneys = substituteHoneys,
            commonStyles = commonStyles,
            onBackClick = onBackClick,
            onHoneyClick = onHoneyClick,
            onStyleClick = onStyleClick,
            onToggleStyleFavorite = viewModel::toggleStyleFavorite,
            isFavorite = isFavorite,
            onToggleFavorite = viewModel::toggleFavorite
        )
    }
}

@Composable
fun HoneyDetailContent(
    honey: HoneyVariety,
    substituteHoneys: List<HoneyVariety>,
    commonStyles: List<BeerStyle>,
    onBackClick: () -> Unit,
    onHoneyClick: (String) -> Unit,
    onStyleClick: (BeerStyle) -> Unit,
    onToggleStyleFavorite: (BeerStyle) -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
    var showCrystallizationDialog by remember { mutableStateOf(false) }

    IngredientDetailScaffold(
        title = honey.name,
        isFavorite = isFavorite,
        onBackClick = onBackClick,
        onToggleFavorite = onToggleFavorite
    ) {
        // 1. Hero Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
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
                        Text(text = honey.countryFlag, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = honey.origin,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FloralClassificationBadge(classification = honey.floralClassification)
                }

                if (honey.botanicalSource.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = honey.botanicalSource,
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (honey.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = honey.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 2. Sensory Flavor Profile Card
        IngredientSensoryProfileCard(
            profile = honey.radarProfile,
            descriptors = honey.descriptors,
            title = "Sensory Flavor Profile",
            headerTrailingContent = {
                FlavorIntensityBadge(intensity = honey.flavorIntensity)
            }
        )

        // 3. Technical Specs & Sugar Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Technical & Sugar Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Pfund Color
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Color & Pfund Scale",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(honey.color)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = honey.pfundRangeText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Moisture %
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Typical Moisture",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${honey.moistureTypicalPercent}% (Grade A < 18.6%)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Extract Potential
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Extract Yield",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${honey.extractPotentialSg} SG (per lb/gal)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Crystallization Tendency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Crystallization Tendency",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = { showCrystallizationDialog = true },
                            modifier = Modifier.size(24.dp).padding(start = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Crystallization Info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "${honey.crystallizationTendency} (F/G: ${honey.fgRatio})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (honey.crystallizationTendency) {
                            "Slow" -> Color(0xFF166534)
                            "Rapid" -> Color(0xFF991B1B)
                            else -> Color(0xFF92400E)
                        }
                    )
                }
            }
        }

        // 4. Fermentation & Recipe Guidance Card
        if (honey.fermentationBehavior.isNotBlank() || honey.pairingNotes.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Fermentation & Meadmaking Guidance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (honey.fermentationBehavior.isNotBlank()) {
                        Column {
                            Text(
                                text = "Fermentation Dynamics & Aroma Retention",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = honey.fermentationBehavior,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (honey.pairingNotes.isNotBlank()) {
                        Column {
                            Text(
                                text = "Culinary & Ingredient Pairings",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = honey.pairingNotes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 5. Honey Substitutes Section
        IngredientSubstitutesCard(
            title = "Suggested Substitutes",
            substitutes = substituteHoneys,
            chipLabel = { sub -> "${sub.countryFlag} ${sub.name}" },
            onSubstituteClick = onHoneyClick
        )

        // 6. Associated Styles Section
        AssociatedStylesSection(
            title = "Common Beverage Styles",
            styles = commonStyles,
            onStyleClick = onStyleClick,
            onToggleStyleFavorite = onToggleStyleFavorite
        )
    }

    if (showCrystallizationDialog) {
        AlertDialog(
            onDismissRequest = { showCrystallizationDialog = false },
            title = {
                Text(
                    text = "Honey Crystallization & Sugar Dynamics",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Honey crystallization is governed primarily by its Fructose-to-Glucose (F/G) ratio and Glucose-to-Water (G/W) ratio:\n\n" +
                            "• High Fructose (F/G > 1.30, e.g., Tupelo, Acacia, Sage, Chestnut): Remains liquid for months or years without granulating.\n\n" +
                            "• Balanced (F/G 1.15 – 1.30, e.g., Orange Blossom, Meadowfoam, Wildflower): Granulates moderately over months at cool cellar temperatures.\n\n" +
                            "• High Glucose (F/G < 1.10, e.g., Clover, Alfalfa, Dandelion, Cotton): Granulates rapidly within days to weeks of extraction.\n\n" +
                            "In meadmaking, crystallized raw honey dissolves completely in warm must without any loss of fermentability.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showCrystallizationDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}
