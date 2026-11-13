package com.bc.beerworks.ui.yeasts

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.TemperatureUnit
import com.bc.beerworks.data.model.YeastFilterCriteria
import com.bc.beerworks.data.model.YeastStrain
import com.bc.beerworks.ui.components.FilterActionIcon
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
    val temperatureUnit by viewModel.temperatureUnit.collectAsState()

    YeastsContent(
        yeasts = yeasts,
        criteria = criteria,
        classifications = classifications,
        flocculations = flocculations,
        temperatureUnit = temperatureUnit,
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
    temperatureUnit: TemperatureUnit,
    onQueryChange: (String) -> Unit,
    onClassificationSelected: (String?) -> Unit,
    onFilterCriteriaChange: (YeastFilterCriteria) -> Unit,
    onResetFilters: () -> Unit,
    onYeastClick: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val listState = remember(criteria) { LazyListState(0, 0) }

    LaunchedEffect(criteria, yeasts) {
        if (yeasts.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                focusManager.clearFocus()
                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Yeast",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        FilterActionIcon(
                            isFilterActive = criteria.isFilterActive,
                            onClick = {
                                focusManager.clearFocus()
                                showFilterSheet = true
                            },
                            onReset = onResetFilters
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // Search Bar without placeholder hint text (per GEMINI.md standards)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = criteria.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (criteria.query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )
                }

                // Quick Classification Filter Strip (no "All" chip, toggle on/off)
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
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (yeasts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BubbleChart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No yeast strains found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search query or filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (criteria.isFilterActive) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onResetFilters) {
                                Text("Reset Filters")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = yeasts,
                        key = { it.id }
                    ) { yeast ->
                        YeastCard(
                            yeast = yeast,
                            onClick = { onYeastClick(yeast.id) }
                        )
                    }
                }
            }
        }

        if (showFilterSheet) {
            YeastFilterSheet(
                criteria = criteria,
                classifications = classifications,
                flocculations = flocculations,
                onFilterChange = onFilterCriteriaChange,
                onReset = onResetFilters,
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}
