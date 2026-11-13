package com.bc.beerworks.util

import com.bc.beerworks.data.model.TemperatureUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TemperatureConverterTest {

    @Test
    fun celsiusToFahrenheit_convertsAccurately() {
        assertThat(TemperatureConverter.celsiusToFahrenheit(0.0)).isEqualTo(32.0)
        assertThat(TemperatureConverter.celsiusToFahrenheit(20.0)).isEqualTo(68.0)
        assertThat(TemperatureConverter.celsiusToFahrenheit(100.0)).isEqualTo(212.0)
    }

    @Test
    fun fahrenheitToCelsius_convertsAccurately() {
        assertThat(TemperatureConverter.fahrenheitToCelsius(32.0)).isEqualTo(0.0)
        assertThat(TemperatureConverter.fahrenheitToCelsius(68.0)).isEqualTo(20.0)
        assertThat(TemperatureConverter.fahrenheitToCelsius(212.0)).isEqualTo(100.0)
    }

    @Test
    fun formatRange_fahrenheit_formatsCorrectly() {
        val range = TemperatureConverter.formatRange(
            tempMinC = 18.0,
            tempMaxC = 22.0,
            tempMinF = 64.0,
            tempMaxF = 72.0,
            unit = TemperatureUnit.FAHRENHEIT
        )
        assertThat(range).isEqualTo("64°F – 72°F")
    }

    @Test
    fun formatRange_celsius_formatsCorrectly() {
        val range = TemperatureConverter.formatRange(
            tempMinC = 18.0,
            tempMaxC = 22.0,
            tempMinF = 64.0,
            tempMaxF = 72.0,
            unit = TemperatureUnit.CELSIUS
        )
        assertThat(range).isEqualTo("18°C – 22°C")
    }

    @Test
    fun formatRange_singleValue_formatsWithoutDash() {
        val rangeF = TemperatureConverter.formatRange(
            tempMinC = 20.0,
            tempMaxC = 20.0,
            tempMinF = 68.0,
            tempMaxF = 68.0,
            unit = TemperatureUnit.FAHRENHEIT
        )
        assertThat(rangeF).isEqualTo("68°F")

        val rangeC = TemperatureConverter.formatRange(
            tempMinC = 20.0,
            tempMaxC = 20.0,
            tempMinF = 68.0,
            tempMaxF = 68.0,
            unit = TemperatureUnit.CELSIUS
        )
        assertThat(rangeC).isEqualTo("20°C")
    }
}
