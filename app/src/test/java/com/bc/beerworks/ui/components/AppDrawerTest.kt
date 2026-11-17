package com.bc.beerworks.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Science
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AppDrawerTest {

    @Test
    fun drawerDestinationItems_areNonNullAndContainsAllDestinations() {
        val items = DrawerDestination.items

        assertThat(items).hasSize(10)
        assertThat(items).doesNotContainNull()
        assertThat(items.map { it.route }).containsExactly(
            "style_list",
            "hops",
            "malt",
            "honey",
            "pomes",
            "yeasts",
            "off_flavors",
            "favorites",
            "review_history",
            "settings"
        )
        assertThat(DrawerDestination.Hops.icon).isEqualTo(Icons.Default.Grass)
        assertThat(DrawerDestination.Malt.icon).isEqualTo(Icons.Default.CandlestickChart)
        assertThat(DrawerDestination.Malt.title).isEqualTo("Malts")
        assertThat(DrawerDestination.Pomes.icon).isEqualTo(Icons.Default.Nature)
        assertThat(DrawerDestination.Yeast.icon).isEqualTo(Icons.Default.BubbleChart)
        assertThat(DrawerDestination.Yeast.title).isEqualTo("Yeast")
        assertThat(DrawerDestination.OffFlavors.icon).isEqualTo(Icons.Default.Science)
        assertThat(DrawerDestination.OffFlavors.title).isEqualTo("Off-Flavors")
        assertThat(DrawerDestination.Favorites.icon).isEqualTo(Icons.Default.Favorite)
        assertThat(DrawerDestination.Favorites.title).isEqualTo("Favorites")
    }

    @Test
    fun getDisciplineIcon_and_emoji_returnsCorrectGlyphs() {
        assertThat(getDisciplineEmoji(com.bc.beerworks.data.model.GuidelineType.BJCP_2021)).isEqualTo("🍺")
        assertThat(getDisciplineEmoji(com.bc.beerworks.data.model.GuidelineType.BA_2026)).isEqualTo("🍺")
        assertThat(getDisciplineEmoji(com.bc.beerworks.data.model.GuidelineType.BJCP_MEAD)).isEqualTo("🐝")
        assertThat(getDisciplineEmoji(com.bc.beerworks.data.model.GuidelineType.BJCP_CIDER)).isEqualTo("🍏")
        assertThat(getDisciplineIcon(com.bc.beerworks.data.model.GuidelineType.BJCP_CIDER)).isNotNull()
    }
}
