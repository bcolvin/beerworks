package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class HopVariety(
    override val id: String,
    override val name: String,
    override val origin: String,
    override val countryCode: String,
    val purpose: String,
    val alphaAcidMin: Double,
    val alphaAcidMax: Double,
    val betaAcidMin: Double,
    val betaAcidMax: Double,
    val sensoryCategories: List<String> = emptyList(),
    override val descriptors: List<String> = emptyList(),
    override val description: String = "",
    val parentHopIds: List<String> = emptyList(),
    override val substituteIds: List<String> = emptyList(),
    val radarProfile: SensoryRadarProfile? = null
) : Ingredient {
    override fun matchesQuery(query: String): Boolean =
        matchesBaseQuery(query) ||
                purpose.contains(query, ignoreCase = true) ||
                sensoryCategories.any { it.contains(query, ignoreCase = true) }

    val alphaAcidRangeText: String
        get() = "${formatDecimal(alphaAcidMin)}% – ${formatDecimal(alphaAcidMax)}%"

    val betaAcidRangeText: String
        get() = "${formatDecimal(betaAcidMin)}% – ${formatDecimal(betaAcidMax)}%"

    private fun formatDecimal(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(java.util.Locale.US, "%.1f", value)
        }
    }
}
