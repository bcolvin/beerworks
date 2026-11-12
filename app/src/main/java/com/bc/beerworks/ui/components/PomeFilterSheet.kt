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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.PomeFilterCriteria

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PomeFilterSheet(
    criteria: PomeFilterCriteria,
    fruitTypes: List<String>,
    classifications: List<String>,
    harvestSeasons: List<String> = listOf("Early", "Mid", "Late"),
    tanninLevels: List<String> = listOf("Low", "Medium", "High"),
    onFilterChange: (PomeFilterCriteria) -> Unit,
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
                    text = "Filter Pomes",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onReset) {
                    Text("Reset All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Fruit Type (Apple / Pear)
            Text(
                text = "Fruit Type",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fruitTypes.forEach { type ->
                    val isSelected = criteria.selectedFruitType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newType = if (isSelected) null else type
                            onFilterChange(criteria.copy(selectedFruitType = newType))
                        },
                        label = { Text(if (type == "Apple") "🍏 Apple" else "🍐 Pear") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Long Ashton Classification
            Text(
                text = "Classification (Long Ashton)",
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

            // 3. Harvest Ripening Season
            Text(
                text = "Harvest Season",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                harvestSeasons.forEach { season ->
                    val isSelected = criteria.selectedHarvestSeason == season
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newSeason = if (isSelected) null else season
                            onFilterChange(criteria.copy(selectedHarvestSeason = newSeason))
                        },
                        label = { Text(season) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Tannin Level
            Text(
                text = "Tannin Level",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tanninLevels.forEach { level ->
                    val isSelected = criteria.selectedTanninLevel == level
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newLevel = if (isSelected) null else level
                            onFilterChange(criteria.copy(selectedTanninLevel = newLevel))
                        },
                        label = { Text(level) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
