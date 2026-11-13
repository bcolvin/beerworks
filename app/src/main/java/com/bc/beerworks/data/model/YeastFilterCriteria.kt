package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class YeastFilterCriteria(
    val query: String = "",
    val selectedClassification: String? = null,
    val selectedFlocculation: String? = null,
    val minAttenuation: Double? = null,
    val minAlcoholTolerance: Double? = null,
    val selectedStyleId: String? = null
) {
    val isFilterActive: Boolean
        get() = query.isNotBlank() ||
                selectedClassification != null ||
                selectedFlocculation != null ||
                minAttenuation != null ||
                minAlcoholTolerance != null ||
                selectedStyleId != null

    val activeFilterCount: Int
        get() {
            var count = 0
            if (selectedClassification != null) count++
            if (selectedFlocculation != null) count++
            if (minAttenuation != null) count++
            if (minAlcoholTolerance != null) count++
            if (selectedStyleId != null) count++
            return count
        }
}
