package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class MaltFilterCriteria(
    val query: String = "",
    val selectedCategory: String? = null,
    val selectedGrainType: String? = null,
    val selectedDiastaticTier: String? = null, // "High", "Moderate", "None"
    val selectedColorTier: String? = null, // "Pale (<10°L)", "Amber (10–50°L)", "Brown (50–150°L)", "Dark (>150°L)"
    val selectedFunctionalTags: Set<String> = emptySet(),
    val selectedBaseCategory: String? = null
) {
    val isFilterActive: Boolean
        get() = query.isNotBlank() ||
                selectedCategory != null ||
                selectedGrainType != null ||
                selectedDiastaticTier != null ||
                selectedColorTier != null ||
                selectedFunctionalTags.isNotEmpty() ||
                selectedBaseCategory != null
}
