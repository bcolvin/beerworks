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
import com.bc.beerworks.data.model.OffFlavorFilterCriteria

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OffFlavorFilterSheet(
    criteria: OffFlavorFilterCriteria,
    categories: List<String>,
    onFilterChange: (OffFlavorFilterCriteria) -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Off-Flavors",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onReset) {
                    Text("Reset All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Off-Flavor Category
            FilterChipSection(
                title = "Category",
                options = categories,
                selected = criteria.selectedCategory,
                onSelect = { selected ->
                    onFilterChange(criteria.copy(selectedCategory = selected))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Detected In (Sensory Modality)
            FilterChipSection(
                title = "Detected In",
                options = OffFlavorFilterCriteria.MODALITIES,
                selected = criteria.selectedModality,
                onSelect = { selected ->
                    onFilterChange(criteria.copy(selectedModality = selected))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Beverage Discipline
            FilterChipSection(
                title = "Beverage Discipline",
                options = OffFlavorFilterCriteria.DISCIPLINES,
                selected = criteria.selectedDiscipline,
                onSelect = { selected ->
                    onFilterChange(criteria.copy(selectedDiscipline = selected))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Style Permissibility
            FilterChipSection(
                title = "Style Permissibility",
                options = OffFlavorFilterCriteria.PERMISSIBILITIES,
                selected = criteria.selectedPermissibility,
                onSelect = { selected ->
                    onFilterChange(criteria.copy(selectedPermissibility = selected))
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterChipSection(
    title: String,
    options: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(6.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(if (isSelected) null else option) },
                label = {
                    Text(
                        text = option,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            )
        }
    }
}
