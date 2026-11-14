package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.bc.beerworks.util.PfundColorConverter
import java.util.Locale

@Immutable
data class HoneyVariety(
    override val id: String,
    override val name: String,
    val floralClassification: String, // Monofloral, Polyfloral, Honeydew
    val botanicalSource: String, // Latin genus/species or botanical origin
    override val origin: String,
    override val countryCode: String,
    val pfundMin: Double,
    val pfundMax: Double,
    val usdaColor: String, // Water White, Extra White, White, Extra Light Amber, Light Amber, Amber, Dark Amber
    val moistureTypicalPercent: Double,
    val fgRatio: Double,
    val crystallizationTendency: String, // Slow, Moderate, Rapid
    val extractPotentialSg: Double, // e.g. 1.037
    val flavorIntensity: String, // Delicate, Moderate, Robust
    override val description: String,
    val fermentationBehavior: String,
    val pairingNotes: String,
    override val descriptors: List<String> = emptyList(),
    override val substituteIds: List<String> = emptyList(),
    val commonStyleIds: List<String> = emptyList(),
    val radarProfile: HoneySensoryProfile? = null
) : Ingredient {
    override fun matchesQuery(query: String): Boolean =
        matchesBaseQuery(query) ||
                botanicalSource.contains(query, ignoreCase = true) ||
                floralClassification.contains(query, ignoreCase = true)

    val pfundRangeText: String
        get() {
            val rangeStr = if (pfundMin == pfundMax) {
                "${formatDecimal(pfundMin)} mm"
            } else {
                "${formatDecimal(pfundMin)} – ${formatDecimal(pfundMax)} mm"
            }
            return if (usdaColor.isNotBlank()) "$rangeStr ($usdaColor)" else rangeStr
        }

    val averagePfund: Double
        get() = (pfundMin + pfundMax) / 2.0

    val colorHex: String
        get() = PfundColorConverter.pfundToHex(averagePfund)

    val color: Color
        get() = PfundColorConverter.pfundToColor(averagePfund)

    private fun formatDecimal(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", value)
        }
    }
}
