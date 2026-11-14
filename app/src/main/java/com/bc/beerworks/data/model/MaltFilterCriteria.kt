package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class MaltFilterCriteria(
    override val query: String = "",
    val selectedCategory: String? = null,
    val selectedGrainType: String? = null,
    val selectedDiastaticTier: String? = null, // "High", "Moderate", "None"
    val selectedColorTier: String? = null, // "Pale (<10°L)", "Amber (10–50°L)", "Brown (50–150°L)", "Dark (>150°L)"
    val selectedFunctionalTags: Set<String> = emptySet(),
    val selectedBaseCategory: String? = null
) : IngredientFilterCriteria<MaltFilterCriteria> {
    override val isFilterActive: Boolean
        get() = query.isNotBlank() ||
                selectedCategory != null ||
                selectedGrainType != null ||
                selectedDiastaticTier != null ||
                selectedColorTier != null ||
                selectedFunctionalTags.isNotEmpty() ||
                selectedBaseCategory != null

    override fun withQuery(newQuery: String): MaltFilterCriteria = copy(query = newQuery)

    override fun resetKeepingQuery(): MaltFilterCriteria = MaltFilterCriteria(query = query)

    companion object {
        val DEFAULT_CATEGORIES = listOf(
            "Base Malt",
            "Kilned & Toasted",
            "Caramel & Crystal",
            "Roasted & Dark",
            "Adjunct & Raw Grain"
        )
        val DEFAULT_GRAIN_TYPES = listOf("Barley", "Wheat", "Rye", "Oats", "Corn", "Rice")
        val FUNCTIONAL_TAGS = listOf(
            "Base Grist",
            "Head Retention",
            "Body & Mouthfeel",
            "Color Adjustment",
            "Caramel Sweetness",
            "Toasty Aroma",
            "Roast & Coffee",
            "Dark Chocolate",
            "Haze & Cloudiness",
            "Lighten Body",
            "Smoke & Campfire"
        )
    }
}
