package com.bc.beerworks.ui.malt

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bc.beerworks.data.model.MaltFilterCriteria
import com.bc.beerworks.data.model.MaltVariety
import com.bc.beerworks.ui.components.IngredientCatalogScaffold
import com.bc.beerworks.ui.components.MaltCard
import com.bc.beerworks.ui.components.MaltFilterSheet

@Composable
fun MaltScreen(
    viewModel: MaltViewModel,
    onMaltClick: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    val criteria by viewModel.filterCriteria.collectAsState()
    val malts by viewModel.malts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val grainTypes by viewModel.grainTypes.collectAsState()
    val functionalTags by viewModel.functionalTags.collectAsState()
    val baseCategories by viewModel.baseCategories.collectAsState()

    MaltContent(
        malts = malts,
        criteria = criteria,
        categories = categories,
        grainTypes = grainTypes,
        functionalTags = functionalTags,
        baseCategories = baseCategories,
        onQueryChange = viewModel::onQueryChange,
        onFilterCriteriaChange = viewModel::onFilterCriteriaChange,
        onResetFilters = viewModel::resetFilters,
        onMaltClick = onMaltClick,
        onMenuClick = onMenuClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaltContent(
    malts: List<MaltVariety>,
    criteria: MaltFilterCriteria,
    categories: List<String>,
    grainTypes: List<String>,
    functionalTags: List<String>,
    baseCategories: List<String>,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit = {},
    onFilterCriteriaChange: (MaltFilterCriteria) -> Unit,
    onResetFilters: () -> Unit,
    onMaltClick: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    IngredientCatalogScaffold(
        title = "Malts",
        items = malts,
        query = criteria.query,
        isFilterActive = criteria.isFilterActive,
        emptyIcon = Icons.Default.Grain,
        emptyTitle = "No malts found",
        onQueryChange = onQueryChange,
        onResetFilters = onResetFilters,
        onMenuClick = onMenuClick,
        filterSheet = { onDismiss ->
            MaltFilterSheet(
                criteria = criteria,
                categories = categories,
                grainTypes = grainTypes,
                functionalTags = functionalTags,
                baseCategories = baseCategories,
                onFilterChange = onFilterCriteriaChange,
                onReset = onResetFilters,
                onDismiss = onDismiss
            )
        }
    ) { malt ->
        MaltCard(
            malt = malt,
            onClick = { onMaltClick(malt.id) }
        )
    }
}
