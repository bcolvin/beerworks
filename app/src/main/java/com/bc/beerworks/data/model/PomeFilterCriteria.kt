package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class PomeFilterCriteria(
    val query: String = "",
    val selectedFruitType: String? = null, // "Apple", "Pear"
    val selectedClassification: String? = null, // "Bittersweet", "Bittersharp", "Sharp", "Sweet"
    val selectedHarvestSeason: String? = null, // "Early", "Mid", "Late"
    val selectedTanninLevel: String? = null, // "Low", "Medium", "High"
    val selectedStyleId: String? = null
) {
    val isFilterActive: Boolean
        get() = query.isNotBlank() ||
                selectedFruitType != null ||
                selectedClassification != null ||
                selectedHarvestSeason != null ||
                selectedTanninLevel != null ||
                selectedStyleId != null

    val activeFilterCount: Int
        get() {
            var count = 0
            if (selectedFruitType != null) count++
            if (selectedClassification != null) count++
            if (selectedHarvestSeason != null) count++
            if (selectedTanninLevel != null) count++
            if (selectedStyleId != null) count++
            return count
        }
}
