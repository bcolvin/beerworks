package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class HoneyFilterCriteria(
    val query: String = "",
    val selectedClassification: String? = null,
    val selectedUsdaColors: Set<String> = emptySet(),
    val selectedFlavorIntensity: String? = null, // "Delicate", "Moderate", "Robust"
    val selectedCrystallizationTendency: String? = null, // "Slow", "Moderate", "Rapid"
    val selectedStyleId: String? = null
) {
    val isFilterActive: Boolean
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
}
