package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable
import com.bc.beerworks.data.local.entity.YeastStrainEntity

@Immutable
data class YeastFilterCriteria(
    override val query: String = "",
    val selectedClassification: String? = null,
    val selectedFlocculation: String? = null,
    val selectedEsterProfile: String? = null,
    val minAttenuation: Double? = null,
    val minAlcoholTolerance: Double? = null,
    val selectedStyleId: String? = null
) : IngredientFilterCriteria<YeastFilterCriteria> {
    override val isFilterActive: Boolean
        get() = query.isNotBlank() ||
                selectedClassification != null ||
                selectedFlocculation != null ||
                selectedEsterProfile != null ||
                minAttenuation != null ||
                minAlcoholTolerance != null ||
                selectedStyleId != null

    val activeFilterCount: Int
        get() {
            var count = 0
            if (selectedClassification != null) count++
            if (selectedFlocculation != null) count++
            if (selectedEsterProfile != null) count++
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

/**
 * Classifies yeast strains by ester and fermentation character using their radar profile
 * and phenolic (`POF+`) status.
 */
object YeastEsterProfileFilter {
    const val CLEAN_NEUTRAL = "Clean / Neutral"
    const val SUBTLE_ESTERS = "Subtle Esters"
    const val FRUITY_HIGH_ESTERS = "Fruity / High Esters"
    const val SPICY_PHENOLIC = "Spicy / Phenolic"
    const val TART_SOUR = "Tart / Sour"
    const val FUNKY_WILD = "Funky / Wild"

    val OPTIONS: List<String> = listOf(
        CLEAN_NEUTRAL,
        SUBTLE_ESTERS,
        FRUITY_HIGH_ESTERS,
        SPICY_PHENOLIC,
        TART_SOUR,
        FUNKY_WILD
    )

    fun matches(entity: YeastStrainEntity, esterProfile: String): Boolean {
        val clean = entity.radarClean ?: 0f
        val fruity = entity.radarFruity ?: 0f
        val spicy = entity.radarSpicy ?: 0f
        val tart = entity.radarTart ?: 0f
        val funky = entity.radarFunky ?: 0f

        return when (esterProfile) {
            CLEAN_NEUTRAL -> clean >= 3.5f && fruity < 2.5f
            SUBTLE_ESTERS -> fruity in 1.8f..3.0f && spicy < 2.5f
            FRUITY_HIGH_ESTERS -> fruity >= 3.2f
            SPICY_PHENOLIC -> entity.isPhenolic || spicy >= 2.5f
            TART_SOUR -> tart >= 2.5f
            FUNKY_WILD -> funky >= 2.5f
            else -> false
        }
    }
}
