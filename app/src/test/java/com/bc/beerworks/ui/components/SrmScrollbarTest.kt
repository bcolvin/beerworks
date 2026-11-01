package com.bc.beerworks.ui.components

import androidx.compose.ui.graphics.Color
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SrmScrollbarTest {

    @Test
    fun determineFoamColor_returnsWhiteForWhiteFrothyHead() {
        val color = determineFoamColor("White, frothy head seldom persists.")
        assertThat(color).isEqualTo(Color(0xFFFFFFFF))
    }

    @Test
    fun determineFoamColor_returnsCreamyOffWhiteForOffWhiteHead() {
        val color = determineFoamColor("Thick, creamy, long-lasting off-white head.")
        assertThat(color).isEqualTo(Color(0xFFFFFDD0))
    }

    @Test
    fun determineFoamColor_returnsTanForTanHead() {
        val color = determineFoamColor("Persistent tan head.")
        assertThat(color).isEqualTo(Color(0xFFE6C280))
    }

    @Test
    fun determineFoamColor_returnsDarkTanForDarkTanHead() {
        val color = determineFoamColor("Thick, creamy, dark tan to brown head.")
        assertThat(color).isEqualTo(Color(0xFFC19A6B))
    }
}
