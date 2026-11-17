package com.bc.beerworks.ui.favorites

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.repository.OffFlavorRepository
import com.bc.beerworks.ui.components.HoneyCard
import com.bc.beerworks.ui.components.HopCard
import com.bc.beerworks.ui.components.MaltCard
import com.bc.beerworks.ui.components.OffFlavorCard
import com.bc.beerworks.ui.components.PomeCard
import com.bc.beerworks.ui.components.StyleCard
import com.bc.beerworks.ui.components.YeastCard

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onStyleClick: (BeerStyle) -> Unit,
    onHopClick: (String) -> Unit,
    onMaltClick: (String) -> Unit,
    onHoneyClick: (String) -> Unit,
    onPomeClick: (String) -> Unit,
    onYeastClick: (String) -> Unit,
    onOffFlavorClick: (String) -> Unit = {},
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pendingRemoval by viewModel.pendingRemoval.collectAsState()

    FavoritesContent(
        uiState = uiState,
        pendingRemoval = pendingRemoval,
        onQueryChange = viewModel::onQueryChange,
        onToggleDiscipline = viewModel::onToggleDiscipline,
        onResetFilters = viewModel::resetFilters,
        onRequestRemove = viewModel::onRequestRemove,
        onConfirmRemove = viewModel::onConfirmRemove,
        onCancelRemove = viewModel::onCancelRemove,
        onStyleClick = onStyleClick,
        onHopClick = onHopClick,
        onMaltClick = onMaltClick,
        onHoneyClick = onHoneyClick,
        onPomeClick = onPomeClick,
        onYeastClick = onYeastClick,
        onOffFlavorClick = onOffFlavorClick,
        onMenuClick = onMenuClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesContent(
    uiState: FavoritesUiState,
    pendingRemoval: FavoriteItemTarget? = null,
    onQueryChange: (String) -> Unit,
    onToggleDiscipline: (FavoriteDiscipline) -> Unit,
    onResetFilters: () -> Unit,
    onRequestRemove: (FavoriteItemTarget) -> Unit,
    onConfirmRemove: (Boolean) -> Unit,
    onCancelRemove: () -> Unit,
    onStyleClick: (BeerStyle) -> Unit,
    onHopClick: (String) -> Unit,
    onMaltClick: (String) -> Unit,
    onHoneyClick: (String) -> Unit,
    onPomeClick: (String) -> Unit,
    onYeastClick: (String) -> Unit,
    onOffFlavorClick: (String) -> Unit = {},
    onMenuClick: () -> Unit
) {
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
            }
            .fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Favorites",
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

                if (uiState.totalFavoritesCount > 0) {
                    // Search Bar without placeholder hint text (per GEMINI.md standards)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.query,
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
                                if (uiState.query.isNotEmpty()) {
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

                    // Dynamic Discipline Filter Strip (no "All" chip, shows only disciplines with >= 1 favorite)
                    val availableDisciplines = uiState.availableDisciplines
                    if (availableDisciplines.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableDisciplines.forEach { discipline ->
                                val count = uiState.disciplineCounts[discipline] ?: 0
                                val isSelected = uiState.selectedDisciplines.contains(discipline)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onToggleDiscipline(discipline) },
                                    label = { Text("${discipline.label} ($count)") }
                                )
                            }
                        }
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
            when {
                uiState.totalFavoritesCount == 0 -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Favorites Yet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the heart icon on any style or ingredient detail page to save it here for quick reference.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                !uiState.hasMatchingItems -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No matching favorites",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your search query or discipline filters.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            if (uiState.isFilterActive) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = onResetFilters) {
                                    Text("Reset Filters")
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (uiState.styles.isNotEmpty()) {
                            item(key = "header_styles") {
                                DisciplineSectionHeader(
                                    icon = Icons.Default.SportsBar,
                                    title = "Styles",
                                    count = uiState.styles.size
                                )
                            }
                            items(
                                items = uiState.styles,
                                key = { "style_${it.guidelineType.name}_${it.id}" }
                            ) { style ->
                                val target = remember(style) { FavoriteItemTarget.Style(style) }
                                SwipeableFavoriteCard(
                                    targetKey = target.key,
                                    onSwipeRemove = { onRequestRemove(target) }
                                ) {
                                    StyleCard(
                                        style = style,
                                        onClick = { onStyleClick(style) },
                                        searchQuery = uiState.query
                                    )
                                }
                            }
                        }

                        if (uiState.hops.isNotEmpty()) {
                            item(key = "header_hops") {
                                DisciplineSectionHeader(
                                    icon = Icons.Default.Grass,
                                    title = "Hops",
                                    count = uiState.hops.size
                                )
                            }
                            items(
                                items = uiState.hops,
                                key = { "hop_${it.id}" }
                            ) { hop ->
                                val target = remember(hop) {
                                    FavoriteItemTarget.IngredientItem(
                                        key = "hop_${hop.id}",
                                        name = hop.name,
                                        id = hop.id,
                                        type = "HOP"
                                    )
                                }
                                SwipeableFavoriteCard(
                                    targetKey = target.key,
                                    onSwipeRemove = { onRequestRemove(target) }
                                ) {
                                    HopCard(
                                        hop = hop,
                                        onClick = { onHopClick(hop.id) }
                                    )
                                }
                            }
                        }

                        if (uiState.malts.isNotEmpty()) {
                            item(key = "header_malts") {
                                DisciplineSectionHeader(
                                    icon = Icons.Default.CandlestickChart,
                                    title = "Malts",
                                    count = uiState.malts.size
                                )
                            }
                            items(
                                items = uiState.malts,
                                key = { "malt_${it.id}" }
                            ) { malt ->
                                val target = remember(malt) {
                                    FavoriteItemTarget.IngredientItem(
                                        key = "malt_${malt.id}",
                                        name = malt.name,
                                        id = malt.id,
                                        type = "MALT"
                                    )
                                }
                                SwipeableFavoriteCard(
                                    targetKey = target.key,
                                    onSwipeRemove = { onRequestRemove(target) }
                                ) {
                                    MaltCard(
                                        malt = malt,
                                        onClick = { onMaltClick(malt.id) }
                                    )
                                }
                            }
                        }

                        if (uiState.honeys.isNotEmpty()) {
                            item(key = "header_honey") {
                                DisciplineSectionHeader(
                                    icon = Icons.Default.Hive,
                                    title = "Honey",
                                    count = uiState.honeys.size
                                )
                            }
                            items(
                                items = uiState.honeys,
                                key = { "honey_${it.id}" }
                            ) { honey ->
                                val target = remember(honey) {
                                    FavoriteItemTarget.IngredientItem(
                                        key = "honey_${honey.id}",
                                        name = honey.name,
                                        id = honey.id,
                                        type = "HONEY"
                                    )
                                }
                                SwipeableFavoriteCard(
                                    targetKey = target.key,
                                    onSwipeRemove = { onRequestRemove(target) }
                                ) {
                                    HoneyCard(
                                        honey = honey,
                                        onClick = { onHoneyClick(honey.id) }
                                    )
                                }
                            }
                        }

                        if (uiState.pomes.isNotEmpty()) {
                            item(key = "header_pomes") {
                                DisciplineSectionHeader(
                                    icon = Icons.Default.Nature,
                                    title = "Pomes",
                                    count = uiState.pomes.size
                                )
                            }
                            items(
                                items = uiState.pomes,
                                key = { "pome_${it.id}" }
                            ) { pome ->
                                val target = remember(pome) {
                                    FavoriteItemTarget.IngredientItem(
                                        key = "pome_${pome.id}",
                                        name = pome.name,
                                        id = pome.id,
                                        type = "POME"
                                    )
                                }
                                SwipeableFavoriteCard(
                                    targetKey = target.key,
                                    onSwipeRemove = { onRequestRemove(target) }
                                ) {
                                    PomeCard(
                                        pome = pome,
                                        onClick = { onPomeClick(pome.id) }
                                    )
                                }
                            }
                        }

                        if (uiState.yeasts.isNotEmpty()) {
                            item(key = "header_yeast") {
                                DisciplineSectionHeader(
                                    icon = Icons.Default.BubbleChart,
                                    title = "Yeast",
                                    count = uiState.yeasts.size
                                )
                            }
                            items(
                                items = uiState.yeasts,
                                key = { "yeast_${it.id}" }
                            ) { yeast ->
                                val target = remember(yeast) {
                                    FavoriteItemTarget.IngredientItem(
                                        key = "yeast_${yeast.id}",
                                        name = yeast.name,
                                        id = yeast.id,
                                        type = "YEAST"
                                    )
                                }
                                SwipeableFavoriteCard(
                                    targetKey = target.key,
                                    onSwipeRemove = { onRequestRemove(target) }
                                ) {
                                    YeastCard(
                                        yeast = yeast,
                                        onClick = { onYeastClick(yeast.id) }
                                    )
                                }
                            }
                        }

                        if (uiState.offFlavors.isNotEmpty()) {
                            item(key = "header_off_flavors") {
                                DisciplineSectionHeader(
                                    icon = Icons.Default.Science,
                                    title = "Off-Flavors",
                                    count = uiState.offFlavors.size
                                )
                            }
                            items(
                                items = uiState.offFlavors,
                                key = { "off_flavor_${it.id}" }
                            ) { offFlavor ->
                                val target = remember(offFlavor) {
                                    FavoriteItemTarget.IngredientItem(
                                        key = "off_flavor_${offFlavor.id}",
                                        name = offFlavor.name,
                                        id = offFlavor.id,
                                        type = OffFlavorRepository.TYPE_OFF_FLAVOR
                                    )
                                }
                                SwipeableFavoriteCard(
                                    targetKey = target.key,
                                    onSwipeRemove = { onRequestRemove(target) }
                                ) {
                                    OffFlavorCard(
                                        offFlavor = offFlavor,
                                        onClick = { onOffFlavorClick(offFlavor.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (pendingRemoval != null) {
            RemoveFavoriteConfirmationDialog(
                itemName = pendingRemoval.name,
                onConfirm = onConfirmRemove,
                onDismiss = onCancelRemove
            )
        }
    }
}

@Composable
private fun DisciplineSectionHeader(
    icon: ImageVector,
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableFavoriteCard(
    targetKey: String,
    onSwipeRemove: () -> Unit,
    content: @Composable () -> Unit
) {
    val currentOnSwipeRemove by rememberUpdatedState(onSwipeRemove)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                currentOnSwipeRemove()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {}
    ) {
        content()
    }
}

@Composable
private fun RemoveFavoriteConfirmationDialog(
    itemName: String,
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var alwaysWithoutAsking by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Remove from Favorites?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Remove $itemName from your favorites?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { alwaysWithoutAsking = !alwaysWithoutAsking }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = alwaysWithoutAsking,
                        onCheckedChange = { alwaysWithoutAsking = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Always remove without asking",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(alwaysWithoutAsking) }) {
                Text(
                    text = "Remove",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
