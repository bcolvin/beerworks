package com.bc.beerworks.ui.components

import com.bc.beerworks.data.model.HoneySensoryProfile
import com.bc.beerworks.data.model.HoneyVariety
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HoneyCardTest {

    private val honey = HoneyVariety(
        id = "orange_blossom",
        name = "Orange Blossom Honey",
        floralClassification = "Monofloral",
        botanicalSource = "Citrus sinensis",
        origin = "California, United States",
        countryCode = "US",
        pfundMin = 20.0,
        pfundMax = 45.0,
        usdaColor = "White",
        moistureTypicalPercent = 16.8,
        fgRatio = 1.25,
        crystallizationTendency = "Moderate",
        extractPotentialSg = 1.037,
        flavorIntensity = "Moderate",
        description = "Bright citrus notes.",
        fermentationBehavior = "Cool fermentation.",
        pairingNotes = "Peaches and oak.",
        descriptors = listOf("Citrus Blossom", "Orange Zest", "Bergamot"),
        substituteIds = listOf("tupelo"),
        radarProfile = HoneySensoryProfile(
            floral = 4.8f,
            fruity = 4.2f,
            herbalFresh = 1.5f,
            spicyWarm = 1.0f,
            sweetConfectionary = 3.8f,
            nuttyWoody = 0.5f,
            earthyMineral = 0.5f,
            animalPungent = 0.0f
        )
    )

    @Test
    fun countryFlag_rendersCorrectFlag() {
        assertThat(honey.countryFlag).isEqualTo("🇺🇸")
    }

    @Test
    fun pfundRangeText_formatsRangeWithUsdaTier() {
        assertThat(honey.pfundRangeText).isEqualTo("20 – 45 mm (White)")
    }

    @Test
    fun averagePfund_calculatesCorrectMidpoint() {
        assertThat(honey.averagePfund).isEqualTo(32.5)
    }

    @Test
    fun sensoryProfile_toAxisMap_containsAllEightAxes() {
        val axes = honey.radarProfile?.toAxisMap()

        assertThat(axes).hasSize(8)
        assertThat(axes?.map { it.first }).containsExactly(
            "Floral",
            "Fruity",
            "Herbal",
            "Spicy",
            "Sweet",
            "Woody",
            "Earthy",
            "Pungent"
        )
        assertThat(axes?.first { it.first == "Floral" }?.second).isEqualTo(4.8f)
    }
}
