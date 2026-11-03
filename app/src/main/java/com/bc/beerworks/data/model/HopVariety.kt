package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class HopVariety(
    val id: String,
    val name: String,
    val origin: String,
    val countryCode: String,
    val purpose: String,
    val alphaAcidMin: Double,
    val alphaAcidMax: Double,
    val betaAcidMin: Double,
    val betaAcidMax: Double,
    val sensoryCategories: List<String> = emptyList(),
    val descriptors: List<String> = emptyList(),
    val description: String = "",
    val parentHopIds: List<String> = emptyList(),
    val substituteIds: List<String> = emptyList(),
    val radarProfile: SensoryRadarProfile? = null
) {
    val countryFlag: String
        get() {
            if (countryCode.length != 2) return "🌍"
            val upper = countryCode.uppercase()
            val firstChar = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6
            val secondChar = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6
            return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
        }

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
