package com.bc.beerworks.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Nature
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AppDrawerTest {

    @Test
    fun drawerDestinationItems_areNonNullAndContainsAllDestinations() {
        val items = DrawerDestination.items

        assertThat(items).hasSize(7)
        assertThat(items).doesNotContainNull()
        assertThat(items.map { it.route }).containsExactly(
            "style_list",
            "hops",
            "malt",
            "honey",
            "pomes",
            "review_history",
            "settings"
        )
        assertThat(DrawerDestination.Malt.icon).isEqualTo(Icons.Default.Grass)
        assertThat(DrawerDestination.Pomes.icon).isEqualTo(Icons.Default.Nature)
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
