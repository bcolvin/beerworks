package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class YeastSensoryProfile(
    val cleanNeutral: Float = 0f,
    val fruityEsters: Float = 0f,
    val spicyPhenols: Float = 0f,
    val tartAcidity: Float = 0f,
    val funkyEarthy: Float = 0f,
    val sulfuryCrisp: Float = 0f,
    val alcoholWarmth: Float = 0f,
    val mouthfeelSweetness: Float = 0f
) {
    fun hasData(): Boolean {
        return cleanNeutral > 0f || fruityEsters > 0f || spicyPhenols > 0f || tartAcidity > 0f ||
                funkyEarthy > 0f || sulfuryCrisp > 0f || alcoholWarmth > 0f || mouthfeelSweetness > 0f
    }

    fun toAxisMap(): List<Pair<String, Float>> {
        return listOf(
            "Clean" to cleanNeutral,
            "Fruity" to fruityEsters,
            "Spicy" to spicyPhenols,
            "Tart" to tartAcidity,
            "Funky" to funkyEarthy,
            "Sulfury" to sulfuryCrisp,
            "Alcohol" to alcoholWarmth,
            "Mouthfeel" to mouthfeelSweetness
        )
    }
}
