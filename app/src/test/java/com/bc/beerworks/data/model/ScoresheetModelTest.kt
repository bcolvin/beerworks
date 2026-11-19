package com.bc.beerworks.data.model

import com.bc.beerworks.data.local.entity.decodeStringMap
import com.bc.beerworks.data.local.entity.encodeStringMap
import com.bc.beerworks.data.local.entity.toEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ScoresheetModelTest {

    @Test
    fun intensityLevel_cyclesThroughFourStates() {
        val first = IntensityLevel.cycle(null)
        val second = IntensityLevel.cycle(first)
        val third = IntensityLevel.cycle(second)
        val fourth = IntensityLevel.cycle(third)

        assertThat(first).isEqualTo(IntensityLevel.LOW)
        assertThat(second).isEqualTo(IntensityLevel.MEDIUM)
        assertThat(third).isEqualTo(IntensityLevel.HIGH)
        assertThat(fourth).isNull()
    }

    @Test
    fun bjcpScoringTier_mapsAllScoreBandsAccurately() {
        assertThat(BjcpScoringTier.fromTotalScore(50)).isEqualTo(BjcpScoringTier.OUTSTANDING)
        assertThat(BjcpScoringTier.fromTotalScore(45)).isEqualTo(BjcpScoringTier.OUTSTANDING)
        assertThat(BjcpScoringTier.fromTotalScore(44)).isEqualTo(BjcpScoringTier.EXCELLENT)
        assertThat(BjcpScoringTier.fromTotalScore(38)).isEqualTo(BjcpScoringTier.EXCELLENT)
        assertThat(BjcpScoringTier.fromTotalScore(37)).isEqualTo(BjcpScoringTier.VERY_GOOD)
        assertThat(BjcpScoringTier.fromTotalScore(30)).isEqualTo(BjcpScoringTier.VERY_GOOD)
        assertThat(BjcpScoringTier.fromTotalScore(29)).isEqualTo(BjcpScoringTier.GOOD)
        assertThat(BjcpScoringTier.fromTotalScore(21)).isEqualTo(BjcpScoringTier.GOOD)
        assertThat(BjcpScoringTier.fromTotalScore(20)).isEqualTo(BjcpScoringTier.FAIR)
        assertThat(BjcpScoringTier.fromTotalScore(14)).isEqualTo(BjcpScoringTier.FAIR)
        assertThat(BjcpScoringTier.fromTotalScore(13)).isEqualTo(BjcpScoringTier.PROBLEMATIC)
        assertThat(BjcpScoringTier.fromTotalScore(0)).isEqualTo(BjcpScoringTier.PROBLEMATIC)
    }

    @Test
    fun beerScoresheetEntity_roundTripsDomainModelAndIntensityMaps() {
        val original = BeerScoresheet(
            id = "sheet-1",
            entryTitle = "Entry #42",
            brewer = "Jane Brewer",
            styleId = "21A",
            styleName = "American IPA",
            styleNumber = "21A",
            evaluatedAtMillis = 1_700_000_000_000L,
            aromaScore = 10,
            appearanceScore = 3,
            flavorScore = 16,
            mouthfeelScore = 4,
            overallScore = 8,
            aromaNotes = "Bright grapefruit and pine",
            descriptorValues = mapOf(
                ScoresheetDescriptor.AROMA_HOPS.key to "High",
                ScoresheetDescriptor.MOUTHFEEL_BODY.key to "Full"
            ),
            offFlavorLevels = mapOf("diacetyl" to IntensityLevel.LOW)
        )

        val roundTripped = original.toEntity().toDomainModel()

        assertThat(roundTripped).isEqualTo(original)
        assertThat(roundTripped.totalScore).isEqualTo(41)
        assertThat(roundTripped.scoringTier).isEqualTo(BjcpScoringTier.EXCELLENT)
        assertThat(decodeStringMap(encodeStringMap(original.descriptorValues))).isEqualTo(original.descriptorValues)
    }

    @Test
    fun meadScoresheetEntity_roundTripsDomainModelAndAdditionalInfo() {
        val original = FourSectionScoresheet(
            discipline = ScoresheetDiscipline.BJCP_MEAD,
            id = "mead-1",
            entryTitle = "Orange Blossom Reserve",
            brewer = "Alex Meadmaker",
            styleId = "M1A",
            styleName = "Dry Mead",
            styleNumber = "M1A",
            evaluatedAtMillis = 1_700_000_000_000L,
            aromaScore = 9,
            appearanceScore = 5,
            flavorScore = 21,
            overallScore = 9,
            aromaNotes = "Delicate citrus blossom",
            declaredCarbonation = "Still",
            declaredSweetness = "Dry",
            declaredStrength = "Standard",
            specialIngredients = "Florida Orange Blossom honey",
            descriptorValues = mapOf(
                ScoresheetDescriptor.MEAD_AROMA_HONEY.key to "Med",
                ScoresheetDescriptor.MEAD_FLAVOR_BALANCE.key to "Balanced"
            ),
            offFlavorLevels = mapOf("thp" to IntensityLevel.LOW)
        )

        val roundTripped = original.toEntity().toDomainModel()

        assertThat(roundTripped).isEqualTo(original)
        assertThat(roundTripped.totalScore).isEqualTo(44)
        assertThat(roundTripped.scoringTier).isEqualTo(BjcpScoringTier.EXCELLENT)
        assertThat((roundTripped as FourSectionScoresheet).hasAdditionalInfo).isTrue()
    }

    @Test
    fun ciderScoresheetEntity_roundTripsDomainModelAndAdditionalInfo() {
        val original = FourSectionScoresheet(
            discipline = ScoresheetDiscipline.BJCP_CIDER,
            id = "cider-1",
            entryTitle = "Heritage Kingston",
            brewer = "Orchard House Cidery",
            styleId = "C1B",
            styleName = "English Cider",
            styleNumber = "C1B",
            evaluatedAtMillis = 1_700_000_000_000L,
            aromaScore = 9,
            appearanceScore = 5,
            flavorScore = 22,
            overallScore = 9,
            flavorNotes = "Crisp malic balance with firm bittersweet tannin",
            declaredCarbonation = "Pétillant",
            declaredSweetness = "Medium-Dry",
            declaredStrength = "Standard",
            specialIngredients = "Kingston Black, Dabinett",
            descriptorValues = mapOf(
                ScoresheetDescriptor.CIDER_AROMA_APPLE_PEAR.key to "High",
                ScoresheetDescriptor.CIDER_FLAVOR_SWEETNESS.key to "Medium-Dry"
            ),
            offFlavorLevels = mapOf("thp" to IntensityLevel.LOW)
        )

        val roundTripped = original.toEntity().toDomainModel()

        assertThat(roundTripped).isEqualTo(original)
        assertThat(roundTripped.totalScore).isEqualTo(45)
        assertThat(roundTripped.scoringTier).isEqualTo(BjcpScoringTier.OUTSTANDING)
        assertThat((roundTripped as FourSectionScoresheet).hasAdditionalInfo).isTrue()
    }

    @Test
    fun formatForShare_beerAndMeadIncludeDisciplineSectionsAndOptionalFields() {
        val beer = BeerScoresheet(
            entryTitle = "West Coast Pale",
            styleNumber = "18B",
            styleName = "American Pale Ale",
            aromaScore = 9,
            appearanceScore = 3,
            flavorScore = 15,
            mouthfeelScore = 4,
            overallScore = 7,
            descriptorValues = mapOf(ScoresheetDescriptor.FLAVOR_HOPS.key to "Med"),
            offFlavorLevels = mapOf("acetaldehyde" to IntensityLevel.LOW),
            overallNotes = "Crisp finish."
        )

        val beerText = beer.formatForShare(mapOf("acetaldehyde" to "Acetaldehyde"))
        assertThat(beerText).contains("BJCP Beer Scoresheet")
        assertThat(beerText).contains("• Aroma: 9 / 12")
        assertThat(beerText).contains("• Mouthfeel: 4 / 5")
        assertThat(beerText).contains("Total Score: 38 / 50 (Excellent)")
        assertThat(beerText).contains("Off-Flavors: Acetaldehyde (Low)")

        val meadMinimal = FourSectionScoresheet(
            discipline = ScoresheetDiscipline.BJCP_MEAD,
            entryTitle = "Traditional Dry",
            styleNumber = "M1A",
            styleName = "Dry Mead",
            aromaScore = 8,
            appearanceScore = 5,
            flavorScore = 20,
            overallScore = 8
        )
        val meadMinimalText = meadMinimal.formatForShare()
        assertThat(meadMinimalText).contains("BJCP Mead Scoresheet")
        assertThat(meadMinimalText).contains("• Aroma: 8 / 10")
        assertThat(meadMinimalText).contains("• Appearance: 5 / 6")
        assertThat(meadMinimalText).contains("• Flavor: 20 / 24")
        assertThat(meadMinimalText).contains("• Overall Impression: 8 / 10")
        assertThat(meadMinimalText).doesNotContain("Mouthfeel:")
        assertThat(meadMinimalText).doesNotContain("Carbonation:")
        assertThat(meadMinimalText).doesNotContain("Special Ingredients:")

        val meadFull = meadMinimal.copy(
            brewer = "Honey Bee Meadery",
            declaredCarbonation = "Pétillant",
            declaredSweetness = "Semi-Sweet",
            declaredStrength = "Standard",
            specialIngredients = "Tart Montmorency cherries"
        )
        val meadFullText = meadFull.formatForShare()
        assertThat(meadFullText).contains("Meadmaker: Honey Bee Meadery")
        assertThat(meadFullText).contains("Carbonation: Pétillant")
        assertThat(meadFullText).contains("Sweetness: Semi-Sweet")
        assertThat(meadFullText).contains("Strength: Standard")
        assertThat(meadFullText).contains("Special Ingredients: Tart Montmorency cherries")

        val cider = FourSectionScoresheet(
            discipline = ScoresheetDiscipline.BJCP_CIDER,
            entryTitle = "Dry Bittersweet",
            brewer = "Orchard Cidery",
            styleNumber = "C1B",
            styleName = "English Cider",
            aromaScore = 8,
            appearanceScore = 6,
            flavorScore = 21,
            overallScore = 9,
            declaredCarbonation = "Still",
            declaredSweetness = "Medium-Dry",
            declaredStrength = "Standard",
            specialIngredients = "Dabinett, Yarlington Mill"
        )
        val ciderText = cider.formatForShare()
        assertThat(ciderText).contains("BJCP Cider Scoresheet")
        assertThat(ciderText).contains("Cidermaker: Orchard Cidery")
        assertThat(ciderText).contains("• Aroma: 8 / 10")
        assertThat(ciderText).contains("• Appearance: 6 / 6")
        assertThat(ciderText).contains("• Flavor: 21 / 24")
        assertThat(ciderText).contains("• Overall Impression: 9 / 10")
        assertThat(ciderText).contains("Sweetness: Medium-Dry")
        assertThat(ciderText).contains("Special Ingredients: Dabinett, Yarlington Mill")
        assertThat(ciderText).doesNotContain("Mouthfeel:")
    }

    @Test
    fun scoresheetDiscipline_resolvesStorageValuesAndDefaultsUnknownToBeer() {
        assertThat(ScoresheetDiscipline.fromStorageValue("BJCP_BEER")).isEqualTo(ScoresheetDiscipline.BJCP_BEER)
        assertThat(ScoresheetDiscipline.fromStorageValue("BJCP_MEAD")).isEqualTo(ScoresheetDiscipline.BJCP_MEAD)
        assertThat(ScoresheetDiscipline.fromStorageValue("BJCP_CIDER")).isEqualTo(ScoresheetDiscipline.BJCP_CIDER)
        assertThat(ScoresheetDiscipline.fromStorageValue("UNKNOWN")).isEqualTo(ScoresheetDiscipline.BJCP_BEER)
        assertThat(ScoresheetDiscipline.fromStorageValue(null)).isEqualTo(ScoresheetDiscipline.BJCP_BEER)
    }

    @Test
    fun scoresheetDiscipline_exposesSectionBoundsAndClassificationOptions() {
        assertThat(ScoresheetDiscipline.BJCP_BEER.aromaMax).isEqualTo(12)
        assertThat(ScoresheetDiscipline.BJCP_BEER.appearanceMax).isEqualTo(3)
        assertThat(ScoresheetDiscipline.BJCP_BEER.flavorMax).isEqualTo(20)
        assertThat(ScoresheetDiscipline.BJCP_BEER.mouthfeelMax).isEqualTo(5)
        assertThat(ScoresheetDiscipline.BJCP_BEER.hasMouthfeelSection).isTrue()

        assertThat(ScoresheetDiscipline.BJCP_MEAD.aromaMax).isEqualTo(10)
        assertThat(ScoresheetDiscipline.BJCP_MEAD.appearanceMax).isEqualTo(6)
        assertThat(ScoresheetDiscipline.BJCP_MEAD.flavorMax).isEqualTo(24)
        assertThat(ScoresheetDiscipline.BJCP_MEAD.hasMouthfeelSection).isFalse()
        assertThat(ScoresheetDiscipline.BJCP_MEAD.sweetnessOptions).containsExactly("Dry", "Semi-Sweet", "Sweet")

        assertThat(ScoresheetDiscipline.BJCP_CIDER.aromaMax).isEqualTo(10)
        assertThat(ScoresheetDiscipline.BJCP_CIDER.appearanceMax).isEqualTo(6)
        assertThat(ScoresheetDiscipline.BJCP_CIDER.flavorMax).isEqualTo(24)
        assertThat(ScoresheetDiscipline.BJCP_CIDER.hasMouthfeelSection).isFalse()
        assertThat(ScoresheetDiscipline.BJCP_CIDER.sweetnessOptions)
            .containsExactly("Dry", "Medium-Dry", "Medium", "Medium-Sweet", "Sweet")
    }

    @Test
    fun scoresheetDescriptor_forSectionFiltersByDiscipline() {
        val beerAroma = ScoresheetDescriptor.forSection(ScoresheetSection.AROMA, ScoresheetDiscipline.BJCP_BEER)
        val meadAroma = ScoresheetDescriptor.forSection(ScoresheetSection.AROMA, ScoresheetDiscipline.BJCP_MEAD)
        val ciderAroma = ScoresheetDescriptor.forSection(ScoresheetSection.AROMA, ScoresheetDiscipline.BJCP_CIDER)

        assertThat(beerAroma).contains(ScoresheetDescriptor.AROMA_MALT, ScoresheetDescriptor.AROMA_HOPS)
        assertThat(beerAroma).doesNotContain(ScoresheetDescriptor.MEAD_AROMA_HONEY, ScoresheetDescriptor.CIDER_AROMA_APPLE_PEAR)

        assertThat(meadAroma).contains(
            ScoresheetDescriptor.MEAD_AROMA_HONEY,
            ScoresheetDescriptor.MEAD_AROMA_FLORAL_FRUITY,
            ScoresheetDescriptor.MEAD_AROMA_SPICES
        )
        assertThat(meadAroma).doesNotContain(ScoresheetDescriptor.AROMA_MALT, ScoresheetDescriptor.CIDER_AROMA_APPLE_PEAR)

        assertThat(ciderAroma).contains(
            ScoresheetDescriptor.CIDER_AROMA_APPLE_PEAR,
            ScoresheetDescriptor.CIDER_AROMA_OAK,
            ScoresheetDescriptor.CIDER_AROMA_FRUITY_ESTERS
        )
        assertThat(ciderAroma).doesNotContain(ScoresheetDescriptor.AROMA_MALT, ScoresheetDescriptor.MEAD_AROMA_HONEY)
    }
}
