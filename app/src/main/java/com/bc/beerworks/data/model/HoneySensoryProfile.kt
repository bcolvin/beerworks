package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class HoneySensoryProfile(
    val floral: Float = 0f,
    val fruity: Float = 0f,
    val herbalFresh: Float = 0f,
    val spicyWarm: Float = 0f,
    val sweetConfectionary: Float = 0f,
    val nuttyWoody: Float = 0f,
    val earthyMineral: Float = 0f,
    val animalPungent: Float = 0f
) {
    fun hasData(): Boolean {
        return floral > 0f || fruity > 0f || herbalFresh > 0f || spicyWarm > 0f ||
                sweetConfectionary > 0f || nuttyWoody > 0f || earthyMineral > 0f || animalPungent > 0f
    }

    fun toAxisMap(): List<Pair<String, Float>> {
        return listOf(
            "Floral" to floral,
            "Fruity" to fruity,
            "Herbal" to herbalFresh,
            "Spicy" to spicyWarm,
            "Sweet" to sweetConfectionary,
            "Woody" to nuttyWoody,
            "Earthy" to earthyMineral,
            "Pungent" to animalPungent
        )
    }
}
