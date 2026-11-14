package com.bc.beerworks.ui.yeastdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.CommercialYeastEquivalent
import com.bc.beerworks.data.model.TemperatureUnit
import com.bc.beerworks.data.model.YeastStrain
import com.bc.beerworks.ui.components.CompactGeneticChip
import com.bc.beerworks.ui.components.YeastCard
import com.bc.beerworks.ui.components.YeastClassificationBadge
import com.bc.beerworks.ui.ingredientdetail.AssociatedStylesSection
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailLoadingOrContent
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailScaffold
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailViewModel
import com.bc.beerworks.ui.ingredientdetail.IngredientSensoryProfileCard

@Composable
fun YeastDetailScreen(
    viewModel: IngredientDetailViewModel<YeastStrain>,
    onBackClick: () -> Unit,
    onYeastClick: (String) -> Unit,
    onStyleClick: (BeerStyle) -> Unit
) {
    val yeast by viewModel.item.collectAsState()
    val temperatureUnit by viewModel.temperatureUnit.collectAsState()
    val substituteYeasts by viewModel.substitutes.collectAsState()
    val commonStyles by viewModel.commonStyles.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    IngredientDetailLoadingOrContent(item = yeast) { currentYeast ->
        YeastDetailContent(
            yeast = currentYeast,
            temperatureUnit = temperatureUnit,
            substituteYeasts = substituteYeasts,
            commonStyles = commonStyles,
            onBackClick = onBackClick,
            onYeastClick = onYeastClick,
            onStyleClick = onStyleClick,
            onToggleStyleFavorite = viewModel::toggleStyleFavorite,
            isFavorite = isFavorite,
            onToggleFavorite = viewModel::toggleFavorite
        )
    }
}

@Composable
fun YeastDetailContent(
    yeast: YeastStrain,
    temperatureUnit: TemperatureUnit,
    substituteYeasts: List<YeastStrain>,
    commonStyles: List<BeerStyle>,
    onBackClick: () -> Unit,
    onYeastClick: (String) -> Unit,
    onStyleClick: (BeerStyle) -> Unit,
    onToggleStyleFavorite: (BeerStyle) -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
    IngredientDetailScaffold(
        title = yeast.name,
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
                            text = yeast.countryFlag,
                            fontSize = 28.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column {
                            Text(
                                text = yeast.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${yeast.scientificName} • ${yeast.origin}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    YeastClassificationBadge(classification = yeast.classification)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DisciplineChip(discipline = yeast.discipline)
                    FormChip(form = yeast.form)
                    Spacer(modifier = Modifier.weight(1f))
                    CompactGeneticChip(text = yeast.sta1Text)
                    CompactGeneticChip(text = yeast.pofText)
                }

                if (yeast.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = yeast.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // 2. Fermentation Specifications Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Fermentation Specifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpecMetricBox(
                        label = "Attenuation",
                        value = yeast.attenuationText,
                        modifier = Modifier.weight(1f)
                    )
                    SpecMetricBox(
                        label = "Flocculation",
                        value = yeast.flocculation,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpecMetricBox(
                        label = "Optimum Temp",
                        value = yeast.formattedTemperatureRange(temperatureUnit),
                        modifier = Modifier.weight(1f)
                    )
                    SpecMetricBox(
                        label = "Alcohol Tolerance",
                        value = yeast.alcoholToleranceText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Sensory Profile Radar Chart & Descriptors
        IngredientSensoryProfileCard(
            profile = yeast.radarProfile,
            descriptors = yeast.descriptors,
            title = "Sensory Radar Profile"
        )

        // 4. Commercial Laboratory Equivalents
        if (yeast.commercialEquivalents.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Commercial Lab Equivalents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        yeast.commercialEquivalents.forEach { equivalent ->
                            CommercialEquivalentRow(equivalent = equivalent)
                        }
                    }
                }
            }
        }

        // 5. Pitching Dynamics & Narrative Guidance
        if (yeast.pitchingDynamics.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Pitching Dynamics & Fermentation Behavior",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = yeast.pitchingDynamics,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // 6. Interchangeable Substitute Strains
        if (substituteYeasts.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Interchangeable Substitute Strains",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        substituteYeasts.forEach { sub ->
                            YeastCard(
                                yeast = sub,
                                onClick = { onYeastClick(sub.id) }
                            )
                        }
                    }
                }
            }
        }

        // 7. Associated Beverage Styles
        AssociatedStylesSection(
            title = "Associated Beverage Styles",
            styles = commonStyles,
            onStyleClick = onStyleClick,
            onToggleStyleFavorite = onToggleStyleFavorite
        )
    }
}

@Composable
private fun SpecMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CommercialEquivalentRow(
    equivalent: CommercialYeastEquivalent,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = equivalent.laboratory,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = equivalent.productCode,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (equivalent.productName.isNotBlank()) {
                Text(
                    text = equivalent.productName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (equivalent.form == "Dry") Color(0xFFFEF3C7) else Color(0xFFE0F2FE)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = equivalent.form,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (equivalent.form == "Dry") Color(0xFF92400E) else Color(0xFF0369A1),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun DisciplineChip(
    discipline: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = discipline,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun FormChip(
    form: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = form,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            fontSize = 11.sp
        )
    }
}
