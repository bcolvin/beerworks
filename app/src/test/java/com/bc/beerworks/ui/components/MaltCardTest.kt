package com.bc.beerworks.ui.components

import com.bc.beerworks.data.model.MaltVariety
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MaltCardTest {

    private val malt = MaltVariety(
        id = "two_row_pale",
        name = "2-Row Pale Malt",
        category = "Base Malt",
        grainType = "Barley",
        origin = "North America",
        countryCode = "US",
        colorMin = 1.7,
        colorMax = 2.2,
        diastaticPowerLintner = 140,
        extractPotentialDbfg = 81.0,
        maxGristPercentage = 100,
        description = "Clean, versatile workhorse base malt.",
        typicalUsage = "Foundation grain.",
        descriptors = listOf("Clean", "Sweet", "Mild Grain", "Cracker")
    )

    @Test
    fun countryFlag_rendersCorrectFlag() {
        assertThat(malt.countryFlag).isEqualTo("🇺🇸")
    }

    @Test
    fun colorRangeText_formatsRangeWithLovibondAndEbc() {
        assertThat(malt.colorRangeText).isEqualTo("1.7 – 2.2 °L (3 – 4 EBC)")
    }

    @Test
    fun diastaticTier_returnsHighForOver100Lintner() {
        assertThat(malt.diastaticTier).isEqualTo("High")
    }

    @Test
    fun descriptors_containsExpectedItems() {
        assertThat(malt.descriptors).containsExactly("Clean", "Sweet", "Mild Grain", "Cracker")
    }
}
