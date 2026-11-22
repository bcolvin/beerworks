package com.bc.beerworks.ui.components

import androidx.compose.ui.graphics.Color
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class StyleCardTest {

    private val testStyle = BeerStyle(
        id = "1A",
        name = "American Light Lager",
        category = "Standard American Beer",
        categoryNumber = "1",
        number = "1A",
        overallImpression = "Refreshing",
        aroma = "",
        appearance = "",
        flavor = "",
        mouthfeel = "",
        comments = "",
        history = "",
        characteristicIngredients = "Two-row lager malt and corn.",
        styleComparison = "",
        ibuMin = 8.0,
        ibuMax = 12.0,
        ogMin = 1.028,
        ogMax = 1.040,
        fgMin = 0.998,
        fgMax = 1.008,
        abvMin = 2.8,
        abvMax = 4.2,
        srmMin = 2.0,
        srmMax = 3.0,
        commercialExamples = "Bud Light, Coors Light, Miller Lite",
        tags = listOf("session", "pale"),
        guidelineType = GuidelineType.BJCP_2021
    )

    @Test
    fun getSearchMatchSnippet_returnsNullForBlankQuery() {
        val snippet = getSearchMatchSnippet(testStyle, "", Color.Blue)
        assertThat(snippet).isNull()
    }

    @Test
    fun getSearchMatchSnippet_returnsNullWhenTitleMatches() {
        val snippet = getSearchMatchSnippet(testStyle, "American", Color.Blue)
        assertThat(snippet).isNull()
    }

    @Test
    fun getSearchMatchSnippet_returnsExampleSnippetWhenCommercialExamplesMatch() {
        val snippet = getSearchMatchSnippet(testStyle, "Bud Light", Color.Blue)
        assertThat(snippet).isNotNull
        assertThat(snippet?.categoryLabel).isEqualTo("Example")
        assertThat(snippet?.snippetText?.text).contains("Bud Light")
    }

    @Test
    fun getSearchMatchSnippet_returnsIngredientSnippetWhenIngredientsMatch() {
        val snippet = getSearchMatchSnippet(testStyle, "corn", Color.Blue)
        assertThat(snippet).isNotNull
        assertThat(snippet?.categoryLabel).isEqualTo("Ingredient")
        assertThat(snippet?.snippetText?.text).contains("corn")
    }

    @Test
    fun getMaxOverallImpressionLines_returnsTenForActiveSearchQuery() {
        assertThat(getMaxOverallImpressionLines("IPA")).isEqualTo(10)
        assertThat(getMaxOverallImpressionLines("lager")).isEqualTo(10)
    }

    @Test
    fun getMaxOverallImpressionLines_returnsTenEvenForBlankOrWhitespaceQuery() {
        assertThat(getMaxOverallImpressionLines("")).isEqualTo(10)
        assertThat(getMaxOverallImpressionLines("   ")).isEqualTo(10)
    }

    @Test
    fun maxOverallImpressionLines_constantIsTen() {
        assertThat(MAX_OVERALL_IMPRESSION_LINES).isEqualTo(10)
    }

    @Test
    fun formatStyleDescriptorTags_ordersFermentationAndConditioningBeforeStrengthAndColor() {
        val result = formatStyleDescriptorTags(
            listOf(
                "standard-strength",
                "pale-color",
                "bottom-fermented",
                "lagered",
                "central-europe",
                "traditional-style",
                "pilsner-family",
                "balanced",
                "hoppy"
            )
        )

        assertThat(result).containsExactly(
            "Bottom Fermented",
            "Lagered",
            "Standard Strength",
            "Pale Color",
            "Balanced",
            "Hoppy",
            "Central Europe",
            "Traditional Style",
            "Pilsner Family"
        )
    }

    @Test
    fun formatStyleDescriptorTags_preservesStandardAndSpecialtyAndNormalizesAliasesAndIpa() {
        val result = formatStyleDescriptorTags(
            listOf(
                "specialty-beer",
                "northamerica",
                "britishisles",
                "any fermentation",
                "ipa-family",
                "standard-strength"
            )
        )

        assertThat(result).containsExactly(
            "Any Fermentation",
            "Standard Strength",
            "North America",
            "British Isles",
            "Specialty Beer",
            "IPA Family"
        )
    }
}
