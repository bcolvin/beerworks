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
    fun scoresheetEntity_roundTripsDomainModelAndIntensityMaps() {
        val original = Scoresheet(
            id = "sheet-1",
            formType = ScoresheetFormType.LONG_FORM,
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
    fun formatForShare_includesTotalScoreTierAndFaultNames() {
        val sheet = Scoresheet(
            formType = ScoresheetFormType.SHORT_FORM,
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

        val text = sheet.formatForShare(mapOf("acetaldehyde" to "Acetaldehyde"))

        assertThat(text).contains("Entry: West Coast Pale")
        assertThat(text).contains("Style: 18B - American Pale Ale")
        assertThat(text).contains("Total Score: 38 / 50 (Excellent)")
        assertThat(text).contains("Off-Flavors: Acetaldehyde (Low)")
        assertThat(text).contains("Overall Comments: Crisp finish.")
    }
}
