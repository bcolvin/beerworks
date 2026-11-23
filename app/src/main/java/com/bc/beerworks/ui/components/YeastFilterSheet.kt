package com.bc.beerworks.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.YeastEsterProfileFilter
import com.bc.beerworks.data.model.YeastFilterCriteria
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun YeastFilterSheet(
    criteria: YeastFilterCriteria,
    classifications: List<String>,
    flocculations: List<String> = listOf("Low", "Medium", "Medium-High", "High", "Very High"),
    onFilterChange: (YeastFilterCriteria) -> Unit,
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
                    text = "Filter",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onReset) {
                    Text("Reset All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Classification
            Text(
                text = "Classification",
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
                        label = {
                            Text(
                                text = classification,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Ester & Fermentation Profile
            Text(
                text = "Ester & Fermentation Profile",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                YeastEsterProfileFilter.OPTIONS.forEach { profileOption ->
                    val isSelected = criteria.selectedEsterProfile == profileOption
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newProfile = if (isSelected) null else profileOption
                            onFilterChange(criteria.copy(selectedEsterProfile = newProfile))
                        },
                        label = {
                            Text(
                                text = profileOption,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Flocculation
            Text(
                text = "Flocculation",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                flocculations.forEach { flocculation ->
                    val isSelected = criteria.selectedFlocculation == flocculation
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newFloc = if (isSelected) null else flocculation
                            onFilterChange(criteria.copy(selectedFlocculation = newFloc))
                        },
                        label = {
                            Text(
                                text = flocculation,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Minimum Attenuation % Slider
            val currentMinAtt = criteria.minAttenuation ?: 60.0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Minimum Attenuation",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (criteria.minAttenuation != null) "≥ ${currentMinAtt.roundToInt()}%" else "Any",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Slider(
                value = currentMinAtt.toFloat(),
                onValueChange = { newVal ->
                    onFilterChange(criteria.copy(minAttenuation = newVal.toDouble().roundToInt().toDouble()))
                },
                valueRange = 60f..95f,
                steps = 6
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Minimum Alcohol Tolerance % Slider
            val currentMinAbv = criteria.minAlcoholTolerance ?: 5.0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Minimum Alcohol Tolerance",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (criteria.minAlcoholTolerance != null) "≥ ${currentMinAbv.roundToInt()}% ABV" else "Any",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Slider(
                value = currentMinAbv.toFloat(),
                onValueChange = { newVal ->
                    onFilterChange(criteria.copy(minAlcoholTolerance = newVal.toDouble().roundToInt().toDouble()))
                },
                valueRange = 5f..18f,
                steps = 12
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
