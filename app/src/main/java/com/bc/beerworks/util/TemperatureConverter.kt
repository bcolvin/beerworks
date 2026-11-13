package com.bc.beerworks.util

import com.bc.beerworks.data.model.TemperatureUnit
import kotlin.math.roundToInt

object TemperatureConverter {

    fun celsiusToFahrenheit(celsius: Double): Double {
        return (celsius * 9.0 / 5.0) + 32.0
    }

    fun fahrenheitToCelsius(fahrenheit: Double): Double {
        return (fahrenheit - 32.0) * 5.0 / 9.0
    }

    fun formatRange(
        tempMinC: Double,
        tempMaxC: Double,
        tempMinF: Double,
        tempMaxF: Double,
        unit: TemperatureUnit
    ): String {
        return when (unit) {
            TemperatureUnit.FAHRENHEIT -> {
                val min = tempMinF.roundToInt()
                val max = tempMaxF.roundToInt()
                if (min == max) "$min°F" else "$min°F – $max°F"
            }
            TemperatureUnit.CELSIUS -> {
                val min = tempMinC.roundToInt()
                val max = tempMaxC.roundToInt()
                if (min == max) "$min°C" else "$min°C – $max°C"
            }
        }
    }
}
