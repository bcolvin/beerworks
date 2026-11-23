package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable
import com.bc.beerworks.data.local.entity.HopVarietyEntity

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

/**
 * Defines the canonical hop sensory filter options and matches hops against their 8-axis
 * radar scores (at >= [RADAR_THRESHOLD]) or non-8-axis descriptors for `"Other"`.
 */
object HopSensoryProfileFilter {
    const val RADAR_THRESHOLD = 2.5f

    val OPTIONS: List<String> = listOf(
        "Citrus",
        "Tropical",
        "Floral",
        "Herbal",
        "Spicy",
        "Piney",
        "Earthy",
        "Grassy",
        "Other"
    )

    private val OTHER_DESCRIPTOR_REGEX = Regex(
        pattern = listOf(
            "stone fruit", "apricot", "peach",
            "berr(?:y|ies)", "strawberry", "blueberry", "raspberry", "loganberry",
            "blackcurrant", "redcurrant", "cassis", "dark fruit", "cherry",
            "melon", "cantaloupe", "honeydew", "watermelon",
            "dank", "resin(?:ous)?",
            "wood(?:y)?", "cedar",
            "wine", "pinot", "gooseberry", "grape",
            "tea",
            "mint", "peppermint", "menthol", "eucalyptus",
            "coconut", "bubblegum", "candy", "candied", "lollipop", "fruity pebbles",
            "apple", "pear", "honey", "anise", "dill", "green pepper",
            "bitterness", "bittering"
        ).joinToString(separator = "|", prefix = "\\b(?:", postfix = ")\\b"),
        option = RegexOption.IGNORE_CASE
    )

    fun matches(entity: HopVarietyEntity, sensoryOption: String): Boolean {
        return when (sensoryOption) {
            "Citrus" -> (entity.radarCitrus ?: 0f) >= RADAR_THRESHOLD
            "Tropical" -> (entity.radarTropical ?: 0f) >= RADAR_THRESHOLD
            "Floral" -> (entity.radarFloral ?: 0f) >= RADAR_THRESHOLD
            "Herbal" -> (entity.radarHerbal ?: 0f) >= RADAR_THRESHOLD
            "Spicy" -> (entity.radarSpicy ?: 0f) >= RADAR_THRESHOLD
            "Piney" -> (entity.radarPiney ?: 0f) >= RADAR_THRESHOLD
            "Earthy" -> (entity.radarEarthy ?: 0f) >= RADAR_THRESHOLD
            "Grassy" -> (entity.radarGrassy ?: 0f) >= RADAR_THRESHOLD
            "Other" -> OTHER_DESCRIPTOR_REGEX.containsMatchIn(entity.descriptors)
            else -> entity.sensoryCategories.contains(sensoryOption, ignoreCase = true)
        }
    }
}
