package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class SensoryRadarProfile(
    val citrus: Float = 0f,
    val tropical: Float = 0f,
    val floral: Float = 0f,
    val herbal: Float = 0f,
    val spicy: Float = 0f,
    val piney: Float = 0f,
    val earthy: Float = 0f,
    val grassy: Float = 0f
) {
    fun hasData(): Boolean {
        return citrus > 0f || tropical > 0f || floral > 0f || herbal > 0f ||
                spicy > 0f || piney > 0f || earthy > 0f || grassy > 0f
    }

    fun toAxisMap(): List<Pair<String, Float>> {
        return listOf(
            "Citrus" to citrus,
            "Tropical" to tropical,
            "Floral" to floral,
            "Herbal" to herbal,
            "Spicy" to spicy,
            "Piney" to piney,
            "Earthy" to earthy,
            "Grassy" to grassy
        )
    }
}
