package com.bc.beerworks.ui.pomedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.PomeVariety
import com.bc.beerworks.ui.components.FruitTypeBadge
import com.bc.beerworks.ui.components.LongAshtonMatrixGauge
import com.bc.beerworks.ui.components.PomeClassificationBadge
import com.bc.beerworks.ui.ingredientdetail.AssociatedStylesSection
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailLoadingOrContent
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailScaffold
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailViewModel
import com.bc.beerworks.ui.ingredientdetail.IngredientSensoryProfileCard
import com.bc.beerworks.ui.ingredientdetail.IngredientSubstitutesCard
import java.util.Locale

@Composable
fun PomeDetailScreen(
    viewModel: IngredientDetailViewModel<PomeVariety>,
    onBackClick: () -> Unit,
    onPomeClick: (String) -> Unit,
    onStyleClick: (BeerStyle) -> Unit
) {
    val pome by viewModel.item.collectAsState()
    val substitutePomes by viewModel.substitutes.collectAsState()
    val commonStyles by viewModel.commonStyles.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    IngredientDetailLoadingOrContent(item = pome) { currentPome ->
        PomeDetailContent(
            pome = currentPome,
            substitutePomes = substitutePomes,
            commonStyles = commonStyles,
            onBackClick = onBackClick,
            onPomeClick = onPomeClick,
            onStyleClick = onStyleClick,
            onToggleStyleFavorite = viewModel::toggleStyleFavorite,
            isFavorite = isFavorite,
            onToggleFavorite = viewModel::toggleFavorite
        )
    }
}

@Composable
fun PomeDetailContent(
    pome: PomeVariety,
    substitutePomes: List<PomeVariety>,
    commonStyles: List<BeerStyle>,
    onBackClick: () -> Unit,
    onPomeClick: (String) -> Unit,
    onStyleClick: (BeerStyle) -> Unit,
    onToggleStyleFavorite: (BeerStyle) -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
    var showSorbitolDialog by remember { mutableStateOf(false) }

    IngredientDetailScaffold(
        title = pome.name,
        isFavorite = isFavorite,
        onBackClick = onBackClick,
        onToggleFavorite = onToggleFavorite
    ) {
        // 1. Hero Header Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                            text = pome.countryFlag,
                            fontSize = 28.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column {
                            Text(
                                text = pome.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${pome.botanicalName} • ${pome.origin}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FruitTypeBadge(fruitType = pome.fruitType)
                        PomeClassificationBadge(classification = pome.classification)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fruit Use Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Use: ${pome.fruitUse}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (pome.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = pome.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 2. Long Ashton Matrix Gauge
        LongAshtonMatrixGauge(
            classification = pome.classification,
            acidityMalicPercent = pome.acidityMalicPercent,
            tanninPolyphenolPercent = pome.tanninPolyphenolPercent,
            tanninLevel = pome.tanninLevel
        )

        // 3. Organoleptic Flavor Profile (Radar Chart + Descriptors)
        IngredientSensoryProfileCard(
            profile = pome.radarProfile,
            descriptors = pome.descriptors,
            title = "Organoleptic Flavor Profile"
        )

        // 4. Pomological & Chemical Specs Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pomological & Chemical Specs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Harvest Season", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = pome.harvestSeason,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sugar Potential", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = pome.sgRangeText,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Titratable Acidity", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format(Locale.US, "%.2f%% Malic", pome.acidityMalicPercent),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tannin Concentration", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format(Locale.US, "%.2f%% (%s)", pome.tanninPolyphenolPercent, pome.tanninLevel),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (pome.sorbitolGramsPerLiter != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showSorbitolDialog = true }
                        ) {
                            Text(
                                text = "Sorbitol Content",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Sorbitol info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = String.format(Locale.US, "~%.1f g/L", pome.sorbitolGramsPerLiter),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 5. Cidermaking & Pressing Dynamics Card
        if (pome.blendingGuidance.isNotBlank() || pome.pressingCharacteristics.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cidermaking & Pressing Dynamics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (pome.blendingGuidance.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Blending Role & Formulation",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = pome.blendingGuidance,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (pome.pressingCharacteristics.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Milling & Pressing Characteristics",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = pome.pressingCharacteristics,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 6. Pome Substitutes Card
        IngredientSubstitutesCard(
            title = "Similar Varieties & Substitutes",
            substitutes = substitutePomes,
            chipLabel = { sub -> "${sub.countryFlag} ${sub.name} (${sub.classification})" },
            onSubstituteClick = onPomeClick
        )

        // 7. Associated Cider & Beverage Styles Section
        AssociatedStylesSection(
            title = "Featured in Cider & Beverage Styles",
            styles = commonStyles,
            onStyleClick = onStyleClick,
            onToggleStyleFavorite = onToggleStyleFavorite
        )
    }

    if (showSorbitolDialog) {
        AlertDialog(
            onDismissRequest = { showSorbitolDialog = false },
            title = {
                Text(
                    text = "What is Sorbitol in Perry?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Sorbitol is a naturally occurring sugar alcohol found in high concentrations in perry pears (often 25–45 g/L) and in only trace amounts in apples.\n\n" +
                            "Because standard brewing and wine yeasts cannot ferment sorbitol into alcohol, it remains entirely in the finished beverage, providing a permanent natural residual sweetness, velvety mouthfeel, and substantive body even when all fructose and glucose have fermented completely dry.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showSorbitolDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}
