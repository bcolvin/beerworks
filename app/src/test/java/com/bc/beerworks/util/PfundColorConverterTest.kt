package com.bc.beerworks.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PfundColorConverterTest {

    @Test
    fun pfundToUsdaClassification_mapsCorrectlyAcrossAllSevenTiers() {
        assertThat(PfundColorConverter.pfundToUsdaClassification(4.0)).isEqualTo("Water White")
        assertThat(PfundColorConverter.pfundToUsdaClassification(8.0)).isEqualTo("Water White")
        assertThat(PfundColorConverter.pfundToUsdaClassification(9.0)).isEqualTo("Extra White")
        assertThat(PfundColorConverter.pfundToUsdaClassification(17.0)).isEqualTo("Extra White")
        assertThat(PfundColorConverter.pfundToUsdaClassification(25.0)).isEqualTo("White")
        assertThat(PfundColorConverter.pfundToUsdaClassification(34.0)).isEqualTo("White")
        assertThat(PfundColorConverter.pfundToUsdaClassification(40.0)).isEqualTo("Extra Light Amber")
        assertThat(PfundColorConverter.pfundToUsdaClassification(50.0)).isEqualTo("Extra Light Amber")
        assertThat(PfundColorConverter.pfundToUsdaClassification(65.0)).isEqualTo("Light Amber")
        assertThat(PfundColorConverter.pfundToUsdaClassification(85.0)).isEqualTo("Light Amber")
        assertThat(PfundColorConverter.pfundToUsdaClassification(100.0)).isEqualTo("Amber")
        assertThat(PfundColorConverter.pfundToUsdaClassification(114.0)).isEqualTo("Amber")
        assertThat(PfundColorConverter.pfundToUsdaClassification(120.0)).isEqualTo("Dark Amber")
    }

    @Test
    fun pfundToHex_returnsExpectedHexCodes() {
        assertThat(PfundColorConverter.pfundToHex(5.0)).isEqualTo("#FFFDE7")
        assertThat(PfundColorConverter.pfundToHex(15.0)).isEqualTo("#FFF9C4")
        assertThat(PfundColorConverter.pfundToHex(25.0)).isEqualTo("#FFF176")
        assertThat(PfundColorConverter.pfundToHex(45.0)).isEqualTo("#FFD54F")
        assertThat(PfundColorConverter.pfundToHex(65.0)).isEqualTo("#FFB300")
        assertThat(PfundColorConverter.pfundToHex(95.0)).isEqualTo("#FB8C00")
        assertThat(PfundColorConverter.pfundToHex(130.0)).isEqualTo("#4E2616")
    }

    @Test
    fun usdaClassificationToColor_resolvesCorrectly() {
        val color = PfundColorConverter.usdaClassificationToColor("Water White")
        assertThat(color).isNotNull()
    }
}
