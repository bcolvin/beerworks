package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class PomeSensoryProfile(
    val aciditySharpness: Float = 0f,
    val tanninBitterness: Float = 0f,
    val freshFruit: Float = 0f,
    val bakedFruit: Float = 0f,
    val citrusTropical: Float = 0f,
    val floralHerbal: Float = 0f,
    val earthyFarmhouse: Float = 0f,
    val sweetConfectionary: Float = 0f
) : SensoryProfile {
    override val defaultStrokeColorHex: Long = 0xFF4D7C0F

    override fun hasData(): Boolean {
        return aciditySharpness > 0f || tanninBitterness > 0f || freshFruit > 0f || bakedFruit > 0f ||
                citrusTropical > 0f || floralHerbal > 0f || earthyFarmhouse > 0f || sweetConfectionary > 0f
    }

    override fun toAxisMap(): List<Pair<String, Float>> {
        return listOf(
            "Acidity" to aciditySharpness,
            "Tannin" to tanninBitterness,
            "Fresh Fruit" to freshFruit,
            "Baked Fruit" to bakedFruit,
            "Citrus" to citrusTropical,
            "Floral" to floralHerbal,
            "Earthy" to earthyFarmhouse,
            "Sweet" to sweetConfectionary
        )
    }
}
