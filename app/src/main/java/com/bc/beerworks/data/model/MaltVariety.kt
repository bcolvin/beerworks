package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable
import java.util.Locale

@Immutable
data class MaltVariety(
    override val id: String,
    override val name: String,
    val category: String, // Base Malt, Kilned & Toasted, Caramel & Crystal, Roasted & Dark, Adjunct & Raw Grain
    val grainType: String, // Barley, Wheat, Rye, Oats, Corn, Rice, Other
    override val origin: String,
    override val countryCode: String,
    val colorMin: Double,
    val colorMax: Double,
    val diastaticPowerLintner: Int,
    val extractPotentialDbfg: Double,
    val maxGristPercentage: Int,
    override val description: String,
    val typicalUsage: String,
    val functionalTags: List<String> = emptyList(),
    override val descriptors: List<String> = emptyList(),
    val commercialExamples: List<String> = emptyList(),
    override val substituteIds: List<String> = emptyList(),
    val radarProfile: MaltSensoryProfile? = null
) : Ingredient {
    override fun matchesQuery(query: String): Boolean =
        matchesBaseQuery(query) ||
                category.contains(query, ignoreCase = true) ||
                grainType.contains(query, ignoreCase = true)

    val colorRangeText: String
        get() {
            val minEbc = (colorMin * 1.97).toInt().coerceAtLeast(1)
            val maxEbc = (colorMax * 1.97).toInt().coerceAtLeast(minEbc)
            val lovText = if (colorMin == colorMax) "${formatDecimal(colorMin)} °L"
            else "${formatDecimal(colorMin)} – ${formatDecimal(colorMax)} °L"
            val ebcText = if (minEbc == maxEbc) "$minEbc EBC" else "$minEbc – $maxEbc EBC"
            return "$lovText ($ebcText)"
        }

    val diastaticTier: String
        get() = when {
            diastaticPowerLintner >= 100 -> "High"
            diastaticPowerLintner >= 30 -> "Moderate"
            else -> "None"
        }

    val averageColor: Double
        get() = (colorMin + colorMax) / 2.0

    private fun formatDecimal(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", value)
        }
    }
}
