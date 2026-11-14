package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable
import com.bc.beerworks.util.TemperatureConverter
import java.util.Locale
import kotlin.math.roundToInt

@Immutable
data class YeastStrain(
    override val id: String,
    override val name: String,
    val scientificName: String,
    override val origin: String,
    override val countryCode: String,
    val discipline: String, // "Beer", "Mead", "Cider", "Multi-Discipline"
    val classification: String, // "Ale", "Lager", "Wheat", "Kveik", "Wild / Brett", "Bacteria", "Wine / Mead / Cider"
    val form: String, // "Liquid", "Dry", "Both"
    val attenuationMin: Double,
    val attenuationMax: Double,
    val flocculation: String, // "Low", "Medium", "Medium-High", "High", "Very High"
    val tempMinC: Double,
    val tempMaxC: Double,
    val tempMinF: Double,
    val tempMaxF: Double,
    val alcoholTolerance: Double,
    val isDiastaticus: Boolean,
    val isPhenolic: Boolean,
    override val description: String,
    val pitchingDynamics: String,
    override val descriptors: List<String> = emptyList(),
    val commercialEquivalents: List<CommercialYeastEquivalent> = emptyList(),
    override val substituteIds: List<String> = emptyList(),
    val commonStyleIds: List<String> = emptyList(),
    val radarProfile: YeastSensoryProfile? = null
) : Ingredient {
    override fun matchesQuery(query: String): Boolean =
        matchesBaseQuery(query) ||
                scientificName.contains(query, ignoreCase = true) ||
                classification.contains(query, ignoreCase = true) ||
                commercialEquivalents.any {
                    it.productCode.contains(query, ignoreCase = true) ||
                            it.productName.contains(query, ignoreCase = true) ||
                            it.laboratory.contains(query, ignoreCase = true)
                }

    val attenuationText: String
        get() {
            val min = attenuationMin.roundToInt()
            val max = attenuationMax.roundToInt()
            return if (min == max) "$min%" else "$min% – $max%"
        }

    val alcoholToleranceText: String
        get() = String.format(Locale.US, "%.1f%% ABV", alcoholTolerance).replace(".0%", "%")

    val sta1Text: String
        get() = if (isDiastaticus) "STA1+" else "STA1-"

    val pofText: String
        get() = if (isPhenolic) "POF+" else "POF-"

    fun formattedTemperatureRange(unit: TemperatureUnit): String {
        return TemperatureConverter.formatRange(tempMinC, tempMaxC, tempMinF, tempMaxF, unit)
    }
}
