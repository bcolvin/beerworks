package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class MaltSensoryProfile(
    val maltySweet: Float = 0f,
    val breadyBiscuit: Float = 0f,
    val toasty: Float = 0f,
    val caramelToffee: Float = 0f,
    val nutty: Float = 0f,
    val chocolate: Float = 0f,
    val roastCoffee: Float = 0f,
    val grainyHusky: Float = 0f
) {
    fun hasData(): Boolean {
        return maltySweet > 0f || breadyBiscuit > 0f || toasty > 0f || caramelToffee > 0f ||
                nutty > 0f || chocolate > 0f || roastCoffee > 0f || grainyHusky > 0f
    }

    fun toAxisMap(): List<Pair<String, Float>> {
        return listOf(
            "Sweet" to maltySweet,
            "Biscuit" to breadyBiscuit,
            "Toasty" to toasty,
            "Caramel" to caramelToffee,
            "Nutty" to nutty,
            "Chocolate" to chocolate,
            "Roast" to roastCoffee,
            "Husky" to grainyHusky
        )
    }
}
