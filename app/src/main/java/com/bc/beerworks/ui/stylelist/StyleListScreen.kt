package com.bc.beerworks.ui.stylelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
import com.bc.beerworks.ui.components.MAX_OVERALL_IMPRESSION_LINES
import com.bc.beerworks.ui.components.StyleCard
import com.bc.beerworks.ui.theme.BeerworksTheme
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@Composable
fun StyleListScreen(
    viewModel: StyleListViewModel,
    onCategoryClick: (BaseStyleGroup) -> Unit,
    onStyleClick: (BeerStyle) -> Unit,
    onMenuClick: () -> Unit
) {
    val groupedStyles by viewModel.groupedStyles.collectAsState()
    val expandedCategoryNumber by viewModel.expandedCategoryNumber.collectAsState()
    val criteria by viewModel.filterCriteria.collectAsState()
    val selectedGuideline by viewModel.selectedGuideline.collectAsState()

    StyleListContent(
        groupedStyles = groupedStyles,
        expandedCategoryNumber = expandedCategoryNumber,
        criteria = criteria,
        selectedGuideline = selectedGuideline,
        onGuidelineChange = viewModel::onGuidelineChange,
        onQueryChange = viewModel::onQueryChange,
        onFilterCriteriaChange = viewModel::onFilterCriteriaChange,
        onResetFilters = viewModel::resetFilters,
        onCategoryClick = { group ->
            viewModel.setExpandedCategory(group.categoryNumber)
            onCategoryClick(group)
        },
        onToggleExpand = viewModel::toggleCategoryExpansion,
        onStyleClick = { style ->
            viewModel.setExpandedCategory(style.categoryNumber)
            onStyleClick(style)
        },
        onToggleFavorite = viewModel::toggleFavorite,
        onMenuClick = onMenuClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleListContent(
    groupedStyles: List<BaseStyleGroup>,
    expandedCategoryNumber: String?,
    criteria: FilterCriteria,
    selectedGuideline: GuidelineType = GuidelineType.BJCP_2021,
    onGuidelineChange: (GuidelineType) -> Unit = {},
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
    val density = LocalDensity.current
    val bottomPaddingPx = remember(density) { with(density) { 16.dp.roundToPx() } }
    val spacingPx = remember(density) { with(density) { 10.dp.roundToPx() } }
    val listState = remember(criteria) { LazyListState(0, 0) }

    LaunchedEffect(criteria, groupedStyles) {
        if (groupedStyles.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(expandedCategoryNumber) {
        val targetCatNum = expandedCategoryNumber ?: return@LaunchedEffect
        val targetGroup = groupedStyles.firstOrNull { it.categoryNumber == targetCatNum } ?: return@LaunchedEffect
        if (targetGroup.styles.isEmpty()) return@LaunchedEffect

        // Wait until LazyColumn layout reflects the newly expanded category
        snapshotFlow { listState.layoutInfo }
            .filter { info ->
                val catIndex = calculateCategoryItemIndex(
                    groupedStyles = groupedStyles,
                    targetCategoryNumber = targetCatNum,
                    expandedCategoryNumber = targetCatNum,
                    searchQuery = criteria.query
                )
                if (catIndex < 0) return@filter false

                val sub0Index = catIndex + 1
                val hasSubCardLaidOut = info.visibleItemsInfo.any { it.index == sub0Index }
                val catItem = info.visibleItemsInfo.firstOrNull { it.index == catIndex }
                hasSubCardLaidOut || (catItem != null && catItem.index == info.visibleItemsInfo.lastOrNull()?.index)
            }
            .first()

        val catIndex = calculateCategoryItemIndex(
            groupedStyles = groupedStyles,
            targetCategoryNumber = targetCatNum,
            expandedCategoryNumber = targetCatNum,
            searchQuery = criteria.query
        )

        if (catIndex >= 0) {
            val scrollDelta = calculateAutoScrollDelta(
                categoryIndex = catIndex,
                subStylesCount = targetGroup.styles.size,
                layoutInfo = listState.layoutInfo,
                spacing = spacingPx,
                bottomPadding = bottomPaddingPx
            )
            if (scrollDelta > 0f) {
                listState.animateScrollBy(scrollDelta)
            }
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
            },
        topBar = {
            Column {
                var showGuidelinePopup by remember { mutableStateOf(false) }

                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            showGuidelinePopup = true
                                        }
                                    )
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Style Guidelines",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${selectedGuideline.displayName} ${com.bc.beerworks.ui.components.getDisciplineEmoji(selectedGuideline)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (showGuidelinePopup) {
                            AlertDialog(
                                onDismissRequest = { showGuidelinePopup = false },
                                title = {
                                    Text(
                                        text = "Select Guideline Standard",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        GuidelineType.values().forEach { guideline ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        showGuidelinePopup = false
                                                        onGuidelineChange(guideline)
                                                    }
                                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = selectedGuideline == guideline,
                                                    onClick = {
                                                        showGuidelinePopup = false
                                                        onGuidelineChange(guideline)
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = guideline.displayName,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = if (selectedGuideline == guideline) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showGuidelinePopup = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        com.bc.beerworks.ui.components.FilterActionIcon(
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = com.bc.beerworks.ui.components.getDisciplineIcon(selectedGuideline),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val emptyText = when (selectedGuideline) {
                            GuidelineType.BJCP_MEAD -> "No mead styles found"
                            GuidelineType.BJCP_CIDER -> "No cider styles found"
                            else -> "No styles found"
                        }
                        Text(
                            text = emptyText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try adjusting your search query or filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = onResetFilters) {
                            Text("Reset Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
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
                                        maxOverallImpressionLines = MAX_OVERALL_IMPRESSION_LINES,
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
                    selectedGuideline = selectedGuideline,
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
