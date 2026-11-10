package com.bc.beerworks.data.repository

import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BeerStyleSearchScorerTest {

    private fun createStyle(
        name: String = "Test Style",
        number: String = "1A",
        category: String = "Test Category",
        overallImpression: String = "",
        commercialExamples: String = "",
        aroma: String = "",
        flavor: String = "",
        comments: String = ""
    ): BeerStyle {
        return BeerStyle(
            id = number,
            name = name,
            category = category,
            categoryNumber = "1",
            number = number,
            overallImpression = overallImpression,
            aroma = aroma,
            appearance = "",
            flavor = flavor,
            mouthfeel = "",
            comments = comments,
            history = "",
            characteristicIngredients = "",
            styleComparison = "",
            ibuMin = null,
            ibuMax = null,
            ogMin = null,
            ogMax = null,
            fgMin = null,
            fgMax = null,
            abvMin = null,
            abvMax = null,
            srmMin = null,
            srmMax = null,
            commercialExamples = commercialExamples,
            tags = emptyList(),
            guidelineType = GuidelineType.BJCP_2021
        )
    }

    @Test
    fun calculateStyleSearchScore_returnsZeroForBlankQuery() {
        val style = createStyle(name = "American IPA")
        assertThat(calculateStyleSearchScore(style, "")).isEqualTo(0)
        assertThat(calculateStyleSearchScore(style, "   ")).isEqualTo(0)
    }

    @Test
    fun calculateStyleSearchScore_nameMatchScoresHighest() {
        val nameMatchStyle = createStyle(name = "American IPA", number = "21A")
        val impressionMatchStyle = createStyle(name = "English Golden Ale", number = "12A", overallImpression = "Refreshing IPA-like bitterness")
        val commentMatchStyle = createStyle(name = "American Light Lager", number = "1A", comments = "Lower bitterness than an IPA")

        val nameScore = calculateStyleSearchScore(nameMatchStyle, "IPA")
        val impressionScore = calculateStyleSearchScore(impressionMatchStyle, "IPA")
        val commentScore = calculateStyleSearchScore(commentMatchStyle, "IPA")

        assertThat(nameScore).isGreaterThanOrEqualTo(WEIGHT_STYLE_NAME)
        assertThat(impressionScore).isEqualTo(WEIGHT_IMPRESSION_OR_COMMERCIAL)
        assertThat(commentScore).isEqualTo(WEIGHT_OTHER_FIELDS)

        assertThat(nameScore).isGreaterThan(impressionScore)
        assertThat(impressionScore).isGreaterThan(commentScore)
    }

    @Test
    fun calculateStyleSearchScore_commercialExampleMatchesFivePoints() {
        val style = createStyle(name = "Irish Extra Stout", commercialExamples = "Guinness Extra Stout")
        val score = calculateStyleSearchScore(style, "Guinness")
        assertThat(score).isEqualTo(WEIGHT_IMPRESSION_OR_COMMERCIAL)
    }

    @Test
    fun calculateStyleSearchScore_numberMatchScoresTenPlus() {
        val style = createStyle(name = "American IPA", number = "21A")
        val score = calculateStyleSearchScore(style, "21A")
        assertThat(score).isGreaterThanOrEqualTo(WEIGHT_STYLE_NAME)
    }

    @Test
    fun calculateStyleSearchScore_accumulatesAcrossMultipleFields() {
        val multiMatchStyle = createStyle(
            name = "Double IPA",
            overallImpression = "A very strong, intensely bitter IPA",
            commercialExamples = "Russian River Pliny the Elder (double IPA)"
        )
        val score = calculateStyleSearchScore(multiMatchStyle, "IPA")
        // Name (10) + Impression (5) + Commercial (5)
        assertThat(score).isGreaterThanOrEqualTo(20)
    }
}
