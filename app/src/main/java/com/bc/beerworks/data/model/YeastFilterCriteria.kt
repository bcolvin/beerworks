package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class YeastFilterCriteria(
    override val query: String = "",
    val selectedClassification: String? = null,
    val selectedFlocculation: String? = null,
    val minAttenuation: Double? = null,
    val minAlcoholTolerance: Double? = null,
    val selectedStyleId: String? = null
) : IngredientFilterCriteria<YeastFilterCriteria> {
    override val isFilterActive: Boolean
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

    override fun withQuery(newQuery: String): YeastFilterCriteria = copy(query = newQuery)

    override fun resetKeepingQuery(): YeastFilterCriteria = YeastFilterCriteria(query = query)

    companion object {
        val DEFAULT_CLASSIFICATIONS = listOf(
            "Ale",
            "Lager",
            "Wheat",
            "Kveik",
            "Wild / Brett",
            "Bacteria",
            "Wine / Mead / Cider"
        )
        val FLOCCULATIONS = listOf("Low", "Medium", "Medium-High", "High", "Very High")
    }
}
