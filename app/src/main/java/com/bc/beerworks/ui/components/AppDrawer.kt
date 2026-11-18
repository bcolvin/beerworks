package com.bc.beerworks.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

sealed class DrawerDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object StyleGuide : DrawerDestination("style_list", "Style Guide", Icons.AutoMirrored.Filled.MenuBook)
    data object Hops : DrawerDestination("hops", "Hops", Icons.Default.Grass)
    data object Malt : DrawerDestination("malt", "Malts", Icons.Default.CandlestickChart)
    data object Honey : DrawerDestination("honey", "Honey", Icons.Default.Hive)
    data object Pomes : DrawerDestination("pomes", "Pomes", Icons.Default.Nature)
    data object Yeast : DrawerDestination("yeasts", "Yeast", Icons.Default.BubbleChart)
    data object OffFlavors : DrawerDestination("off_flavors", "Off-Flavors", Icons.Default.Science)
    data object Favorites : DrawerDestination("favorites", "Favorites", Icons.Default.Favorite)
    data object ReviewHistory : DrawerDestination("review_history", "Reviews", Icons.Default.History)
    data object Settings : DrawerDestination("settings", "Settings", Icons.Default.Settings)

    companion object {
        val ingredientItems: List<DrawerDestination>
            get() = listOf(Hops, Malt, Honey, Pomes, Yeast)

        val items: List<DrawerDestination>
            get() = listOf(StyleGuide, Hops, Malt, Honey, Pomes, Yeast, OffFlavors, Favorites, ReviewHistory, Settings)
    }
}

@Composable
fun AppDrawer(
    currentRoute: String,
    onNavigate: (DrawerDestination) -> Unit,
    modifier: Modifier = Modifier,
    ingredientsExpanded: Boolean = true,
    onIngredientsExpandedChange: (Boolean) -> Unit = {},
    keepScreenOn: Boolean = false,
    onKeepScreenOnChange: ((Boolean) -> Unit)? = null
) {
    ModalDrawerSheet(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Beerworks",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Brewing Reference",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            DrawerNavItem(
                item = DrawerDestination.StyleGuide,
                selected = currentRoute == DrawerDestination.StyleGuide.route,
                onClick = { onNavigate(DrawerDestination.StyleGuide) }
            )

            val hasActiveIngredient = DrawerDestination.ingredientItems.any { it.route == currentRoute }
            NavigationDrawerItem(
                label = { Text(text = "Ingredients") },
                icon = { Icon(imageVector = Icons.Default.Category, contentDescription = "Ingredients") },
                badge = {
                    Icon(
                        imageVector = if (ingredientsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (ingredientsExpanded) "Collapse Ingredients" else "Expand Ingredients"
                    )
                },
                selected = !ingredientsExpanded && hasActiveIngredient,
                onClick = { onIngredientsExpandedChange(!ingredientsExpanded) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            AnimatedVisibility(visible = ingredientsExpanded) {
                Column {
                    DrawerDestination.ingredientItems.forEach { item ->
                        DrawerNavItem(
                            item = item,
                            selected = currentRoute == item.route,
                            onClick = { onNavigate(item) },
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            listOf(
                DrawerDestination.OffFlavors,
                DrawerDestination.Favorites,
                DrawerDestination.ReviewHistory,
                DrawerDestination.Settings
            ).forEach { item ->
                DrawerNavItem(
                    item = item,
                    selected = currentRoute == item.route,
                    onClick = { onNavigate(item) }
                )
            }
        }

        if (onKeepScreenOnChange != null) {
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.StayCurrentPortrait,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Keep Screen Awake",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Prevent screen timeout",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = keepScreenOn,
                    onCheckedChange = onKeepScreenOnChange
                )
            }
        }
    }
}

@Composable
private fun DrawerNavItem(
    item: DrawerDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationDrawerItem(
        label = { Text(text = item.title) },
        icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
        selected = selected,
        onClick = onClick,
        modifier = modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
