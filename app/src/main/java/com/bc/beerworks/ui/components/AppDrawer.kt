package com.bc.beerworks.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsBar
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

import com.bc.beerworks.data.model.GuidelineType

sealed class DrawerDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object StyleGuide : DrawerDestination("style_list", "Style Guide", Icons.Default.SportsBar)
    data object Hops : DrawerDestination("hops", "Hops", Icons.Default.Grass)
    data object Malt : DrawerDestination("malt", "Malts", Icons.Default.CandlestickChart)
    data object Honey : DrawerDestination("honey", "Honey", Icons.Default.Hive)
    data object Pomes : DrawerDestination("pomes", "Pomes", Icons.Default.Nature)
    data object Yeast : DrawerDestination("yeasts", "Yeast", Icons.Default.BubbleChart)
    data object Favorites : DrawerDestination("favorites", "Favorites", Icons.Default.Favorite)
    data object ReviewHistory : DrawerDestination("review_history", "Review History", Icons.Default.History)
    data object Settings : DrawerDestination("settings", "Settings", Icons.Default.Settings)

    companion object {
        val items: List<DrawerDestination>
            get() = listOf(StyleGuide, Hops, Malt, Honey, Pomes, Yeast, Favorites, ReviewHistory, Settings)
    }
}

@Composable
fun AppDrawer(
    currentRoute: String,
    onNavigate: (DrawerDestination) -> Unit,
    modifier: Modifier = Modifier,
    selectedGuideline: GuidelineType = GuidelineType.BJCP_2021,
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

        DrawerDestination.items.forEach { item ->
            val selected = currentRoute == item.route
            val itemIcon = if (item is DrawerDestination.StyleGuide) {
                getDisciplineIcon(selectedGuideline)
            } else {
                item.icon
            }
            NavigationDrawerItem(
                label = { Text(text = item.title) },
                icon = { Icon(imageVector = itemIcon, contentDescription = item.title) },
                selected = selected,
                onClick = { onNavigate(item) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        if (onKeepScreenOnChange != null) {
            Spacer(modifier = Modifier.weight(1f))
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
