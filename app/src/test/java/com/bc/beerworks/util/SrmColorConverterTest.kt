package com.bc.beerworks.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SrmColorConverterTest {

    @Test
    fun srmToHex_returnsCorrectHexForVariousSrmValues() {
        assertThat(SrmColorConverter.srmToHex(1.0)).isEqualTo("#FFE699")
        assertThat(SrmColorConverter.srmToHex(3.0)).isEqualTo("#FFCA5A")
        assertThat(SrmColorConverter.srmToHex(5.0)).isEqualTo("#FBB123")
        assertThat(SrmColorConverter.srmToHex(9.0)).isEqualTo("#E58500")
        assertThat(SrmColorConverter.srmToHex(40.0)).isEqualTo("#000000")
        assertThat(SrmColorConverter.srmToHex(null)).isEqualTo("#FFE699")
    }

    @Test
    fun getSrmDescription_returnsCorrectLabels() {
        assertThat(SrmColorConverter.getSrmDescription(1.5)).isEqualTo("Very Light / Pale Straw")
        assertThat(SrmColorConverter.getSrmDescription(5.0)).isEqualTo("Gold")
        assertThat(SrmColorConverter.getSrmDescription(8.0)).isEqualTo("Amber / Light Copper")
        assertThat(SrmColorConverter.getSrmDescription(40.0)).isEqualTo("Very Dark / Black")
    }
}
