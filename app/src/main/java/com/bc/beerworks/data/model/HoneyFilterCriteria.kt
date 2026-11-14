package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class HoneyFilterCriteria(
    override val query: String = "",
    val selectedClassification: String? = null,
    val selectedUsdaColors: Set<String> = emptySet(),
    val selectedFlavorIntensity: String? = null, // "Delicate", "Moderate", "Robust"
    val selectedCrystallizationTendency: String? = null, // "Slow", "Moderate", "Rapid"
    val selectedStyleId: String? = null
) : IngredientFilterCriteria<HoneyFilterCriteria> {
    override val isFilterActive: Boolean
        get() = query.isNotBlank() ||
                selectedClassification != null ||
                selectedUsdaColors.isNotEmpty() ||
                selectedFlavorIntensity != null ||
                selectedCrystallizationTendency != null ||
                selectedStyleId != null

    val activeFilterCount: Int
        get() {
            var count = 0
            if (selectedClassification != null) count++
            if (selectedUsdaColors.isNotEmpty()) count += selectedUsdaColors.size
            if (selectedFlavorIntensity != null) count++
            if (selectedCrystallizationTendency != null) count++
            if (selectedStyleId != null) count++
            return count
        }

    override fun withQuery(newQuery: String): HoneyFilterCriteria = copy(query = newQuery)

    override fun resetKeepingQuery(): HoneyFilterCriteria = HoneyFilterCriteria(query = query)

    companion object {
        val DEFAULT_CLASSIFICATIONS = listOf("Monofloral", "Polyfloral", "Honeydew")
        val USDA_COLORS = listOf(
            "Water White",
            "Extra White",
            "White",
            "Extra Light Amber",
            "Light Amber",
            "Amber",
            "Dark Amber"
        )
        val FLAVOR_INTENSITIES = listOf("Delicate", "Moderate", "Robust")
        val CRYSTALLIZATION_TENDENCIES = listOf("Slow", "Moderate", "Rapid")
    }
}
