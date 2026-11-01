package com.bc.beerworks.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

sealed class DrawerDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object StyleGuide : DrawerDestination("style_list", "Style Guide", Icons.Default.Book)
    data object Hops : DrawerDestination("hops", "Hops", Icons.Default.LocalFlorist)
    data object Malt : DrawerDestination("malt", "Malt", Icons.Default.Grain)
    data object ReviewHistory : DrawerDestination("review_history", "Review History", Icons.Default.History)
    data object Settings : DrawerDestination("settings", "Settings", Icons.Default.Settings)

    companion object {
        val items: List<DrawerDestination>
            get() = listOf(StyleGuide, Hops, Malt, ReviewHistory, Settings)
    }
}

@Composable
fun AppDrawer(
    currentRoute: String,
    onNavigate: (DrawerDestination) -> Unit,
    modifier: Modifier = Modifier
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
            NavigationDrawerItem(
                label = { Text(text = item.title) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                selected = selected,
                onClick = { onNavigate(item) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}
