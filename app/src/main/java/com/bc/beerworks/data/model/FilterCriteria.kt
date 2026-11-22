package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class FilterCriteria(
    val query: String = "",
    val minSrm: Double = 0.0,
    val maxSrm: Double = 40.0,
    val minAbv: Double = 0.0,
    val maxAbv: Double = 20.0,
    val minIbu: Double = 0.0,
    val maxIbu: Double = 120.0,
    val selectedCategory: String? = null,
    val favoritesOnly: Boolean = false,
    val selectedSweetness: Set<String> = emptySet(),
    val selectedCarbonation: Set<String> = emptySet(),
    val selectedStrength: Set<String> = emptySet(),
    val selectedTags: Set<String> = emptySet()
) {
    val isFilterActive: Boolean
        get() = minSrm > 0.0 || maxSrm < 40.0 ||
                minAbv > 0.0 || maxAbv < 20.0 ||
                minIbu > 0.0 || maxIbu < 120.0 ||
                selectedCategory != null ||
                favoritesOnly ||
                selectedSweetness.isNotEmpty() ||
                selectedCarbonation.isNotEmpty() ||
                selectedStrength.isNotEmpty() ||
                selectedTags.isNotEmpty()
}
