package com.bc.beerworks.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.HoneyFilterCriteria
import com.bc.beerworks.util.PfundColorConverter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HoneyFilterSheet(
    criteria: HoneyFilterCriteria,
    classifications: List<String>,
    usdaColors: List<String>,
    flavorIntensities: List<String>,
    crystallizationTendencies: List<String>,
    onFilterChange: (HoneyFilterCriteria) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row: Title & Top-Right Reset All
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Honeys",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onReset) {
                    Text("Reset All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Floral Classification
            Text(
                text = "Floral Classification",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                classifications.forEach { classification ->
                    val isSelected = criteria.selectedClassification == classification
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newClassification = if (isSelected) null else classification
                            onFilterChange(criteria.copy(selectedClassification = newClassification))
                        },
                        label = { Text(classification) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Color (renamed from USDA Color Tier)
            Text(
                text = "Color",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                usdaColors.forEach { colorTier ->
                    val isSelected = criteria.selectedUsdaColors.contains(colorTier)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updatedColors = if (isSelected) {
                                criteria.selectedUsdaColors - colorTier
                            } else {
                                criteria.selectedUsdaColors + colorTier
                            }
                            onFilterChange(criteria.copy(selectedUsdaColors = updatedColors))
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(PfundColorConverter.usdaClassificationToColor(colorTier))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                        },
                        label = { Text(colorTier) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Flavor Intensity
            Text(
                text = "Flavor Intensity",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                flavorIntensities.forEach { intensity ->
                    val isSelected = criteria.selectedFlavorIntensity == intensity
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newIntensity = if (isSelected) null else intensity
                            onFilterChange(criteria.copy(selectedFlavorIntensity = newIntensity))
                        },
                        label = { Text(intensity) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Crystallization Tendency
            Text(
                text = "Crystallization Tendency",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                crystallizationTendencies.forEach { tendency ->
                    val isSelected = criteria.selectedCrystallizationTendency == tendency
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newTendency = if (isSelected) null else tendency
                            onFilterChange(criteria.copy(selectedCrystallizationTendency = newTendency))
                        },
                        label = { Text(tendency) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
