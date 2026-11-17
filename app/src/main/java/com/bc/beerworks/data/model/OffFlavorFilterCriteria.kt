package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class OffFlavorFilterCriteria(
    override val query: String = "",
    val selectedCategory: String? = null,
    val selectedModality: String? = null,
    val selectedDiscipline: String? = null,
    val selectedPermissibility: String? = null
) : IngredientFilterCriteria<OffFlavorFilterCriteria> {

    override val isFilterActive: Boolean
        get() = query.isNotBlank() ||
                selectedCategory != null ||
                selectedModality != null ||
                selectedDiscipline != null ||
                selectedPermissibility != null

    val activeFilterCount: Int
        get() {
            var count = 0
            if (selectedCategory != null) count++
            if (selectedModality != null) count++
            if (selectedDiscipline != null) count++
            if (selectedPermissibility != null) count++
            return count
        }

    override fun withQuery(newQuery: String): OffFlavorFilterCriteria = copy(query = newQuery)

    override fun resetKeepingQuery(): OffFlavorFilterCriteria = OffFlavorFilterCriteria(query = query)

    companion object {
        val DEFAULT_CATEGORIES = listOf(
            "Fermentation & Yeast",
            "Mashing & Boil",
            "Sanitation & Microbial",
            "Oxidation & Aging",
            "Water & Packaging"
        )
        val MODALITIES = listOf("Aroma", "Flavor", "Mouthfeel")
        val DISCIPLINES = listOf("Beer", "Mead", "Cider")
        val PERMISSIBILITIES = listOf("Always a Fault", "Style-Permissible")
    }
}
