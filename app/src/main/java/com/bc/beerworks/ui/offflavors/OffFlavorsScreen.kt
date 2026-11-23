package com.bc.beerworks.ui.offflavors

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.OffFlavor
import com.bc.beerworks.data.model.OffFlavorFilterCriteria
import com.bc.beerworks.ui.components.IngredientCatalogScaffold
import com.bc.beerworks.ui.components.OffFlavorCard
import com.bc.beerworks.ui.components.OffFlavorFilterSheet

@Composable
fun OffFlavorsScreen(
    viewModel: OffFlavorsViewModel,
    onOffFlavorClick: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    val criteria by viewModel.filterCriteria.collectAsState()
    val offFlavors by viewModel.offFlavors.collectAsState()
    val categories by viewModel.categories.collectAsState()

    OffFlavorsContent(
        offFlavors = offFlavors,
        criteria = criteria,
        categories = categories,
        onQueryChange = viewModel::onQueryChange,
        onCategorySelected = viewModel::onCategorySelected,
        onFilterCriteriaChange = viewModel::onFilterCriteriaChange,
        onResetFilters = viewModel::resetFilters,
        onOffFlavorClick = onOffFlavorClick,
        onMenuClick = onMenuClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffFlavorsContent(
    offFlavors: List<OffFlavor>,
    criteria: OffFlavorFilterCriteria,
    categories: List<String>,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onFilterCriteriaChange: (OffFlavorFilterCriteria) -> Unit,
    onResetFilters: () -> Unit,
    onOffFlavorClick: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    IngredientCatalogScaffold(
        title = "Off-Flavors",
        items = offFlavors,
        query = criteria.query,
        isFilterActive = criteria.isFilterActive,
        emptyIcon = Icons.Default.Science,
        emptyTitle = "No off-flavors found",
        onQueryChange = onQueryChange,
        onResetFilters = onResetFilters,
        onMenuClick = onMenuClick,
        filterKey = criteria,
        quickFilterStrip = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = criteria.selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) }
                    )
                }
            }
        },
        filterSheet = { onDismiss ->
            OffFlavorFilterSheet(
                criteria = criteria,
                categories = categories,
                onFilterChange = onFilterCriteriaChange,
                onReset = onResetFilters,
                onDismiss = onDismiss
            )
        }
    ) { offFlavor ->
        OffFlavorCard(
            offFlavor = offFlavor,
            onClick = { onOffFlavorClick(offFlavor.id) }
        )
    }
}
