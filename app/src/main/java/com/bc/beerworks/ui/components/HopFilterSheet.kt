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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.HopFilterCriteria

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HopFilterSheet(
    criteria: HopFilterCriteria,
    availableOrigins: List<String>,
    availableCategories: List<String>,
    onFilterChange: (HopFilterCriteria) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var currentCriteria by remember(criteria) { mutableStateOf(criteria) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Hops",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    currentCriteria = HopFilterCriteria(query = currentCriteria.query)
                    onReset()
                }) {
                    Text("Reset All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Purpose Section
            Text(
                text = "Purpose",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            val purposes = listOf("Aroma", "Bittering", "Dual-Purpose")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                purposes.forEach { purposeOption ->
                    val isSelected = currentCriteria.selectedPurpose == purposeOption
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newPurpose = if (isSelected) null else purposeOption
                            currentCriteria = currentCriteria.copy(selectedPurpose = newPurpose)
                        },
                        label = { Text(purposeOption) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Alpha Acid Range Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Alpha Acid Range",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${currentCriteria.minAlphaAcid.toInt()}% – ${currentCriteria.maxAlphaAcid.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            RangeSlider(
                value = currentCriteria.minAlphaAcid.toFloat()..currentCriteria.maxAlphaAcid.toFloat(),
                onValueChange = { range ->
                    currentCriteria = currentCriteria.copy(
                        minAlphaAcid = range.start.toDouble(),
                        maxAlphaAcid = range.endInclusive.toDouble()
                    )
                },
                valueRange = 0f..25f,
                steps = 24
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Origins Section
            if (availableOrigins.isNotEmpty()) {
                Text(
                    text = "Origin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableOrigins.forEach { origin ->
                        val isSelected = currentCriteria.selectedOrigins.contains(origin)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val updatedOrigins = if (isSelected) {
                                    currentCriteria.selectedOrigins - origin
                                } else {
                                    currentCriteria.selectedOrigins + origin
                                }
                                currentCriteria = currentCriteria.copy(selectedOrigins = updatedOrigins)
                            },
                            label = { Text(origin) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Common Beer Style Categories Section
            if (availableCategories.isNotEmpty()) {
                Text(
                    text = "Common Beer Styles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableCategories.forEach { category ->
                        val isSelected = currentCriteria.selectedBaseCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val newCategory = if (isSelected) null else category
                                currentCriteria = currentCriteria.copy(selectedBaseCategory = newCategory)
                            },
                            label = { Text(category) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = {
                    onFilterChange(currentCriteria)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Apply Filters", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
