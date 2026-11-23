package com.bc.beerworks.ui.pomes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bc.beerworks.data.model.PomeFilterCriteria
import com.bc.beerworks.data.model.PomeVariety
import com.bc.beerworks.ui.components.IngredientCatalogScaffold
import com.bc.beerworks.ui.components.PomeCard
import com.bc.beerworks.ui.components.PomeFilterSheet

@Composable
fun PomesScreen(
    viewModel: PomesViewModel,
    onPomeClick: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    val criteria by viewModel.filterCriteria.collectAsState()
    val pomes by viewModel.pomes.collectAsState()
    val fruitTypes by viewModel.fruitTypes.collectAsState()
    val classifications by viewModel.classifications.collectAsState()
    val harvestSeasons by viewModel.harvestSeasons.collectAsState()
    val tanninLevels by viewModel.tanninLevels.collectAsState()

    PomesContent(
        pomes = pomes,
        criteria = criteria,
        fruitTypes = fruitTypes,
        classifications = classifications,
        harvestSeasons = harvestSeasons,
        tanninLevels = tanninLevels,
        onQueryChange = viewModel::onQueryChange,
        onFilterCriteriaChange = viewModel::onFilterCriteriaChange,
        onResetFilters = viewModel::resetFilters,
        onPomeClick = onPomeClick,
        onMenuClick = onMenuClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomesContent(
    pomes: List<PomeVariety>,
    criteria: PomeFilterCriteria,
    fruitTypes: List<String>,
    classifications: List<String>,
    harvestSeasons: List<String>,
    tanninLevels: List<String>,
    onQueryChange: (String) -> Unit,
    onFilterCriteriaChange: (PomeFilterCriteria) -> Unit,
    onResetFilters: () -> Unit,
    onPomeClick: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    IngredientCatalogScaffold(
        title = "Pomes",
        items = pomes,
        query = criteria.query,
        isFilterActive = criteria.isFilterActive,
        emptyIcon = Icons.Default.Nature,
        emptyTitle = "No pomes found",
        onQueryChange = onQueryChange,
        onResetFilters = onResetFilters,
        onMenuClick = onMenuClick,
        filterKey = criteria,
        filterSheet = { onDismiss ->
            PomeFilterSheet(
                criteria = criteria,
                fruitTypes = fruitTypes,
                classifications = classifications,
                harvestSeasons = harvestSeasons,
                tanninLevels = tanninLevels,
                onFilterChange = onFilterCriteriaChange,
                onReset = onResetFilters,
                onDismiss = onDismiss
            )
        }
    ) { pome ->
        PomeCard(
            pome = pome,
            onClick = { onPomeClick(pome.id) }
        )
    }
}
