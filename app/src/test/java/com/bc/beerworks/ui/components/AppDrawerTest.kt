package com.bc.beerworks.ui.components

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AppDrawerTest {

    @Test
    fun drawerDestinationItems_areNonNullAndContainsAllDestinations() {
        val items = DrawerDestination.items

        assertThat(items).hasSize(5)
        assertThat(items).doesNotContainNull()
        assertThat(items.map { it.route }).containsExactly(
            "style_list",
            "hops",
            "malt",
            "review_history",
            "settings"
        )
    }
}
