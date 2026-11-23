package com.bc.beerworks.ui.honey

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bc.beerworks.data.model.HoneyFilterCriteria
import com.bc.beerworks.data.model.HoneyVariety
import com.bc.beerworks.ui.components.HoneyCard
import com.bc.beerworks.ui.components.HoneyFilterSheet
import com.bc.beerworks.ui.components.IngredientCatalogScaffold

@Composable
fun HoneyScreen(
    viewModel: HoneyViewModel,
    onHoneyClick: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    val criteria by viewModel.filterCriteria.collectAsState()
    val honeys by viewModel.honeys.collectAsState()
    val classifications by viewModel.classifications.collectAsState()
    val usdaColors by viewModel.usdaColors.collectAsState()
    val flavorIntensities by viewModel.flavorIntensities.collectAsState()
    val crystallizationTendencies by viewModel.crystallizationTendencies.collectAsState()

    HoneyContent(
        honeys = honeys,
        criteria = criteria,
        classifications = classifications,
        usdaColors = usdaColors,
        flavorIntensities = flavorIntensities,
        crystallizationTendencies = crystallizationTendencies,
        onQueryChange = viewModel::onQueryChange,
        onFilterCriteriaChange = viewModel::onFilterCriteriaChange,
        onResetFilters = viewModel::resetFilters,
        onHoneyClick = onHoneyClick,
        onMenuClick = onMenuClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoneyContent(
    honeys: List<HoneyVariety>,
    criteria: HoneyFilterCriteria,
    classifications: List<String>,
    usdaColors: List<String>,
    flavorIntensities: List<String>,
    crystallizationTendencies: List<String>,
    onQueryChange: (String) -> Unit,
    onClassificationSelected: (String?) -> Unit = {},
    onFilterCriteriaChange: (HoneyFilterCriteria) -> Unit,
    onResetFilters: () -> Unit,
    onHoneyClick: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    IngredientCatalogScaffold(
        title = "Honey",
        items = honeys,
        query = criteria.query,
        isFilterActive = criteria.isFilterActive,
        emptyIcon = Icons.Default.Hive,
        emptyTitle = "No honeys found",
        onQueryChange = onQueryChange,
        onResetFilters = onResetFilters,
        onMenuClick = onMenuClick,
        filterKey = criteria,
        filterSheet = { onDismiss ->
            HoneyFilterSheet(
                criteria = criteria,
                classifications = classifications,
                usdaColors = usdaColors,
                flavorIntensities = flavorIntensities,
                crystallizationTendencies = crystallizationTendencies,
                onFilterChange = onFilterCriteriaChange,
                onReset = onResetFilters,
                onDismiss = onDismiss
            )
        }
    ) { honey ->
        HoneyCard(
            honey = honey,
            onClick = { onHoneyClick(honey.id) }
        )
    }
}
