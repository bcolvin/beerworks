package com.bc.beerworks.ui.yeasts

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.YeastFilterCriteria
import com.bc.beerworks.data.model.YeastStrain
import com.bc.beerworks.ui.components.IngredientCatalogScaffold
import com.bc.beerworks.ui.components.YeastCard
import com.bc.beerworks.ui.components.YeastFilterSheet

@Composable
fun YeastsScreen(
    viewModel: YeastsViewModel,
    onYeastClick: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    val criteria by viewModel.filterCriteria.collectAsState()
    val yeasts by viewModel.yeasts.collectAsState()
    val classifications by viewModel.classifications.collectAsState()
    val flocculations by viewModel.flocculations.collectAsState()

    YeastsContent(
        yeasts = yeasts,
        criteria = criteria,
        classifications = classifications,
        flocculations = flocculations,
        onQueryChange = viewModel::onQueryChange,
        onClassificationSelected = viewModel::onClassificationSelected,
        onFilterCriteriaChange = viewModel::onFilterCriteriaChange,
        onResetFilters = viewModel::resetFilters,
        onYeastClick = onYeastClick,
        onMenuClick = onMenuClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YeastsContent(
    yeasts: List<YeastStrain>,
    criteria: YeastFilterCriteria,
    classifications: List<String>,
    flocculations: List<String>,
    onQueryChange: (String) -> Unit,
    onClassificationSelected: (String?) -> Unit,
    onFilterCriteriaChange: (YeastFilterCriteria) -> Unit,
    onResetFilters: () -> Unit,
    onYeastClick: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    IngredientCatalogScaffold(
        title = "Yeast",
        items = yeasts,
        query = criteria.query,
        isFilterActive = criteria.isFilterActive,
        emptyIcon = Icons.Default.BubbleChart,
        emptyTitle = "No yeast strains found",
        onQueryChange = onQueryChange,
        onResetFilters = onResetFilters,
        onMenuClick = onMenuClick,
        quickFilterStrip = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                classifications.forEach { classification ->
                    val isSelected = criteria.selectedClassification == classification
                    FilterChip(
                        selected = isSelected,
                        onClick = { onClassificationSelected(classification) },
                        label = { Text(classification) }
                    )
                }
            }
        },
        filterSheet = { onDismiss ->
            YeastFilterSheet(
                criteria = criteria,
                classifications = classifications,
                flocculations = flocculations,
                onFilterChange = onFilterCriteriaChange,
                onReset = onResetFilters,
                onDismiss = onDismiss
            )
        }
    ) { yeast ->
        YeastCard(
            yeast = yeast,
            onClick = { onYeastClick(yeast.id) }
        )
    }
}
