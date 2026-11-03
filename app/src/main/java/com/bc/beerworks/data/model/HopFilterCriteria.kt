package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class HopFilterCriteria(
    val query: String = "",
    val selectedPurpose: String? = null,
    val selectedSensoryCategory: String? = null,
    val selectedOrigins: Set<String> = emptySet(),
    val minAlphaAcid: Double = 0.0,
    val maxAlphaAcid: Double = 25.0,
    val selectedBaseCategory: String? = null
) {
    val isAlphaFiltered: Boolean
        get() = minAlphaAcid > 0.0 || maxAlphaAcid < 25.0

    val isFilterActive: Boolean
        get() = query.isNotBlank() ||
                selectedPurpose != null ||
                selectedSensoryCategory != null ||
                selectedOrigins.isNotEmpty() ||
                isAlphaFiltered ||
                selectedBaseCategory != null
}
