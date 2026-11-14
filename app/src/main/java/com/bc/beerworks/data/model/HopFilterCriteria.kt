package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class HopFilterCriteria(
    override val query: String = "",
    val selectedPurpose: String? = null,
    val selectedSensoryCategory: String? = null,
    val selectedOrigins: Set<String> = emptySet(),
    val minAlphaAcid: Double = 0.0,
    val maxAlphaAcid: Double = 25.0,
    val selectedBaseCategory: String? = null
) : IngredientFilterCriteria<HopFilterCriteria> {
    val isAlphaFiltered: Boolean
        get() = minAlphaAcid > 0.0 || maxAlphaAcid < 25.0

    override val isFilterActive: Boolean
        get() = query.isNotBlank() ||
                selectedPurpose != null ||
                selectedSensoryCategory != null ||
                selectedOrigins.isNotEmpty() ||
                isAlphaFiltered ||
                selectedBaseCategory != null

    override fun withQuery(newQuery: String): HopFilterCriteria = copy(query = newQuery)

    override fun resetKeepingQuery(): HopFilterCriteria = HopFilterCriteria(query = query)
}
