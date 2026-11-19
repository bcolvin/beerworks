package com.bc.beerworks.util

import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class StyleVitalStatsTest {

    private fun sampleStyle(
        ogMin: Double? = 1.044,
        ogMax: Double? = 1.052,
        fgMin: Double? = 1.008,
        fgMax: Double? = 1.014,
        ibuMin: Double? = 25.0,
        ibuMax: Double? = 50.0,
        srmMin: Double? = 11.0,
        srmMax: Double? = 17.0,
        abvMin: Double? = 4.3,
        abvMax: Double? = 5.5
    ) = BeerStyle(
        id = "7B",
        name = "Altbier",
        category = "Amber Bitter European Beer",
        categoryNumber = "7",
        number = "7B",
        overallImpression = "",
        aroma = "",
        appearance = "",
        flavor = "",
        mouthfeel = "",
        comments = "",
        history = "",
        characteristicIngredients = "",
        styleComparison = "",
        ibuMin = ibuMin,
        ibuMax = ibuMax,
        ogMin = ogMin,
        ogMax = ogMax,
        fgMin = fgMin,
        fgMax = fgMax,
        abvMin = abvMin,
        abvMax = abvMax,
        srmMin = srmMin,
        srmMax = srmMax,
        commercialExamples = "",
        tags = emptyList(),
        guidelineType = GuidelineType.BJCP_2021
    )

    @Test
    fun formattedRanges_formatTwoBoundsWithExpectedPrecisionAndSuffixes() {
        val style = sampleStyle()

        assertThat(style.formattedOgRange()).isEqualTo("1.044–1.052")
        assertThat(style.formattedFgRange()).isEqualTo("1.008–1.014")
        assertThat(style.formattedIbuRange()).isEqualTo("25–50")
        assertThat(style.formattedIbuRange(" IBU")).isEqualTo("25–50 IBU")
        assertThat(style.formattedSrmRange(" SRM")).isEqualTo("11–17 SRM")
        assertThat(style.formattedAbvRange()).isEqualTo("4.3–5.5%")
        assertThat(style.formattedAbvRange("% ABV")).isEqualTo("4.3–5.5% ABV")
    }

    @Test
    fun formattedRanges_handleSingleMinBoundAndMissingValuesUniformly() {
        val singleMin = sampleStyle(
            ogMax = null,
            fgMin = null,
            fgMax = null,
            ibuMax = null,
            srmMin = null,
            srmMax = null,
            abvMin = null,
            abvMax = null
        )

        assertThat(singleMin.formattedOgRange()).isEqualTo("1.044")
        assertThat(singleMin.formattedFgRange()).isEqualTo("—")
        assertThat(singleMin.formattedIbuRange(" IBU")).isEqualTo("25 IBU")
        assertThat(singleMin.formattedSrmRange(" SRM")).isEqualTo("—")
        assertThat(singleMin.formattedAbvRange("% ABV")).isEqualTo("—")
    }
}
