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
import com.bc.beerworks.data.model.MaltFilterCriteria

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MaltFilterSheet(
    criteria: MaltFilterCriteria,
    categories: List<String>,
    grainTypes: List<String>,
    functionalTags: List<String>,
    baseCategories: List<String>,
    onFilterChange: (MaltFilterCriteria) -> Unit,
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

            // 1. Malt Category
            Text(
                text = "Malt Category",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = criteria.selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onFilterChange(
                                criteria.copy(
                                    selectedCategory = if (isSelected) null else category
                                )
                            )
                        },
                        label = {
                            Text(
                                text = category,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Grain Type
            Text(
                text = "Grain Type",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grainTypes.forEach { grain ->
                    val isSelected = criteria.selectedGrainType == grain
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onFilterChange(
                                criteria.copy(
                                    selectedGrainType = if (isSelected) null else grain
                                )
                            )
                        },
                        label = {
                            Text(
                                text = grain,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Diastatic Power
            Text(
                text = "Diastatic Power (Enzymes)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tiers = listOf(
                    "High (≥100°L)" to "High",
                    "Moderate (30–99°L)" to "Moderate",
                    "None (0°L)" to "None"
                )
                tiers.forEach { (label, tierValue) ->
                    val isSelected = criteria.selectedDiastaticTier == tierValue
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onFilterChange(
                                criteria.copy(
                                    selectedDiastaticTier = if (isSelected) null else tierValue
                                )
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Color Range
            Text(
                text = "Color Range (Lovibond)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val colorTiers = listOf(
                    "Pale (<10°L)" to "Pale (<10°L)",
                    "Amber (10–50°L)" to "Amber (10–50°L)",
                    "Brown (50–150°L)" to "Brown (50–150°L)",
                    "Dark (>150°L)" to "Dark (>150°L)"
                )
                colorTiers.forEach { (label, tier) ->
                    val isSelected = criteria.selectedColorTier == tier
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onFilterChange(
                                criteria.copy(
                                    selectedColorTier = if (isSelected) null else tier
                                )
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Functional Usage Tags
            if (functionalTags.isNotEmpty()) {
                Text(
                    text = "Functional Recipe Roles",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    functionalTags.forEach { tag ->
                        val isSelected = criteria.selectedFunctionalTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val updated = if (isSelected) {
                                    criteria.selectedFunctionalTags - tag
                                } else {
                                    criteria.selectedFunctionalTags + tag
                                }
                                onFilterChange(criteria.copy(selectedFunctionalTags = updated))
                            },
                            label = {
                                Text(
                                    text = tag,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 6. Associated Base Beer Styles
            if (baseCategories.isNotEmpty()) {
                Text(
                    text = "Featured In Style Category",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    baseCategories.forEach { cat ->
                        val isSelected = criteria.selectedBaseCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onFilterChange(
                                    criteria.copy(
                                        selectedBaseCategory = if (isSelected) null else cat
                                    )
                                )
                            },
                            label = {
                                Text(
                                    text = cat,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
