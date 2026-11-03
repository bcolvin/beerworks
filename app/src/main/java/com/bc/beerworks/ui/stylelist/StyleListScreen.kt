package com.bc.beerworks.ui.stylelist

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.BaseStyleGroup
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.FilterCriteria
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.ui.components.CategoryCard
import com.bc.beerworks.ui.components.FilterSheet
import com.bc.beerworks.ui.components.StyleCard
import com.bc.beerworks.ui.theme.BeerworksTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleListContent(
    groupedStyles: List<BaseStyleGroup>,
    expandedCategoryNumber: String?,
    criteria: FilterCriteria,
    categories: List<String>,
    onQueryChange: (String) -> Unit,
    onFilterCriteriaChange: (FilterCriteria) -> Unit,
    onResetFilters: () -> Unit,
    onCategoryClick: (BaseStyleGroup) -> Unit,
    onToggleExpand: (String) -> Unit,
    onStyleClick: (BeerStyle) -> Unit,
    onToggleFavorite: (BeerStyle) -> Unit,
    onMenuClick: () -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

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
            },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Style Guidelines",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // Borderless Pill Search Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 1.dp
                ) {
                    TextField(
                        value = criteria.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { focusManager.clearFocus() }
                        ),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (criteria.query.isNotBlank()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            } else {
                                IconButton(onClick = {
                                    focusManager.clearFocus()
                                    showFilterSheet = true
                                }) {
                                    Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter")
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (groupedStyles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No beer styles match your criteria.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    groupedStyles.forEach { group ->
                        val isExpanded = expandedCategoryNumber == group.categoryNumber || criteria.query.isNotBlank()

                        item(key = "cat_${group.categoryNumber}_${group.categoryName}") {
                            CategoryCard(
                                group = group,
                                isExpanded = isExpanded,
                                onCategoryClick = { onCategoryClick(group) },
                                onToggleExpand = { onToggleExpand(group.categoryNumber) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (isExpanded) {
                            items(
                                items = group.styles,
                                key = { "style_${it.guidelineType.name}_${it.id}" }
                            ) { style ->
                                Box(modifier = Modifier.padding(start = 16.dp, bottom = 10.dp)) {
                                    StyleCard(
                                        style = style,
                                        searchQuery = criteria.query,
                                        onClick = { onStyleClick(style) },
                                        onToggleFavorite = { onToggleFavorite(style) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showFilterSheet) {
                FilterSheet(
                    criteria = criteria,
                    categories = categories,
                    onCriteriaChange = onFilterCriteriaChange,
                    onReset = onResetFilters,
                    onDismiss = { showFilterSheet = false }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StyleListContentPreview() {
    BeerworksTheme {
        StyleListContent(
            groupedStyles = listOf(
                BaseStyleGroup(
                    categoryNumber = "1",
                    categoryName = "Standard American Beer",
                    styles = listOf(
                        BeerStyle(
                            id = "1A",
                            name = "American Light Lager",
                            category = "Standard American Beer",
                            categoryNumber = "1",
                            number = "1A",
                            overallImpression = "A highly carbonated, light-bodied lager.",
                            aroma = "",
                            appearance = "",
                            flavor = "",
                            mouthfeel = "",
                            comments = "",
                            history = "",
                            characteristicIngredients = "",
                            styleComparison = "",
                            ibuMin = 8.0,
                            ibuMax = 12.0,
                            ogMin = 1.028,
                            ogMax = 1.040,
                            fgMin = 0.998,
                            fgMax = 1.008,
                            abvMin = 2.8,
                            abvMax = 4.2,
                            srmMin = 2.0,
                            srmMax = 3.0,
                            commercialExamples = "Bud Light",
                            tags = listOf("session", "pale"),
                            guidelineType = GuidelineType.BJCP_2021,
                            isFavorite = false
                        )
                    ),
                    guidelineType = GuidelineType.BJCP_2021
                )
            ),
            expandedCategoryNumber = "1",
            criteria = FilterCriteria(),
            categories = listOf("Standard American Beer"),
            onQueryChange = {},
            onFilterCriteriaChange = {},
            onResetFilters = {},
            onCategoryClick = {},
            onToggleExpand = {},
            onStyleClick = {},
            onToggleFavorite = {},
            onMenuClick = {}
        )
    }
}
