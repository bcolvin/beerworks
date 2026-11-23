package com.bc.beerworks.ui.hops

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bc.beerworks.data.model.HopFilterCriteria
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.ui.components.HopCard
import com.bc.beerworks.ui.components.HopFilterSheet
import com.bc.beerworks.ui.components.IngredientCatalogScaffold

@Composable
fun HopsScreen(
    viewModel: HopsViewModel,
    onHopClick: (HopVariety) -> Unit,
    onMenuClick: () -> Unit
) {
    val hops by viewModel.hops.collectAsState()
    val criteria by viewModel.filterCriteria.collectAsState()
    val origins by viewModel.origins.collectAsState()
    val categories by viewModel.categories.collectAsState()

    HopsContent(
        hops = hops,
        criteria = criteria,
        availableOrigins = origins,
        availableCategories = categories,
        onQueryChange = viewModel::onQueryChange,
        onPurposeSelected = viewModel::onPurposeSelected,
        onSensoryCategorySelected = viewModel::onSensoryCategorySelected,
        onFilterCriteriaChange = viewModel::onFilterCriteriaChange,
        onResetFilters = viewModel::resetFilters,
        onHopClick = onHopClick,
        onMenuClick = onMenuClick
    )
}

@Composable
fun HopsContent(
    hops: List<HopVariety>,
    criteria: HopFilterCriteria,
    availableOrigins: List<String>,
    availableCategories: List<String>,
    onQueryChange: (String) -> Unit,
    onPurposeSelected: (String?) -> Unit = {},
    onSensoryCategorySelected: (String?) -> Unit = {},
    onFilterCriteriaChange: (HopFilterCriteria) -> Unit,
    onResetFilters: () -> Unit,
    onHopClick: (HopVariety) -> Unit,
    onMenuClick: () -> Unit
) {
    IngredientCatalogScaffold(
        title = "Hops",
        items = hops,
        query = criteria.query,
        isFilterActive = criteria.isFilterActive,
        emptyIcon = Icons.Default.LocalFlorist,
        emptyTitle = "No hops found",
        emptySubtitle = "No hop varieties match your search or filter criteria.",
        onQueryChange = onQueryChange,
        onResetFilters = onResetFilters,
        onMenuClick = onMenuClick,
        filterKey = criteria,
        filterSheet = { onDismiss ->
            HopFilterSheet(
                criteria = criteria,
                availableOrigins = availableOrigins,
                availableCategories = availableCategories,
                onFilterChange = onFilterCriteriaChange,
                onReset = onResetFilters,
                onDismiss = onDismiss
            )
        }
    ) { hop ->
        HopCard(
            hop = hop,
            onClick = { onHopClick(hop) }
        )
    }
}
