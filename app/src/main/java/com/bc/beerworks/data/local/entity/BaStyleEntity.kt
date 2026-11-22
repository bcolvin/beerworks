package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType

@Entity(tableName = "ba_styles")
data class BaStyleEntity(
    @PrimaryKey override val id: String,
    val name: String,
    val category: String,
    val categoryNumber: String,
    val number: String,
    val overallImpression: String,
    val aroma: String,
    val appearance: String,
    val flavor: String,
    val mouthfeel: String,
    val comments: String,
    val history: String,
    val characteristicIngredients: String,
    val styleComparison: String,
    val ibuMin: Double?,
    val ibuMax: Double?,
    val ogMin: Double?,
    val ogMax: Double?,
    val fgMin: Double?,
    val fgMax: Double?,
    val abvMin: Double?,
    val abvMax: Double?,
    val srmMin: Double?,
    val srmMax: Double?,
    val commercialExamples: String,
    val tags: String
) : StyleEntity {
    override fun toDomainModel(isFavorite: Boolean): BeerStyle = BeerStyle(
        id = id,
        name = name.normalizeIpa(),
        category = category.normalizeIpa(),
        categoryNumber = categoryNumber,
        number = number,
        overallImpression = overallImpression,
        aroma = aroma,
        appearance = appearance,
        flavor = flavor,
        mouthfeel = mouthfeel,
        comments = comments,
        history = history,
        characteristicIngredients = characteristicIngredients,
        styleComparison = styleComparison,
        ibuMin = ibuMin,
        ibuMax = ibuMax,
        ogMin = ogMin,
        ogMax = ogMax,
        fgMin = fgMin,
        fgMax = fgMax,
        abvMin = abvMin,
        abvMax = abvMax,
        srmMin = srmMin,
        srmMax = srmMax,
        commercialExamples = commercialExamples,
        tags = deriveBaDescriptorTags(),
        guidelineType = GuidelineType.BA_2026,
        isFavorite = isFavorite
    )

    private fun deriveBaDescriptorTags(): List<String> {
        val catLower = category.trim().lowercase()
        val nameLower = name.lowercase()
        val result = linkedSetOf<String>()

        // Preserve any custom tags beyond the redundant "ba" and parent category name
        tags.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.equals("ba", ignoreCase = true) && !it.equals(category.trim(), ignoreCase = true) }
            .forEach { result.add(it) }

        // Strength
        val maxAbv = abvMax ?: 0.0
        val minAbv = abvMin ?: 0.0
        if (maxAbv > 0.0) {
            when {
                maxAbv <= 4.2 -> result.add("session-strength")
                minAbv >= 7.5 || maxAbv >= 8.5 -> result.add("high-strength")
                else -> result.add("standard-strength")
            }
        }

        // Color
        val maxSrm = srmMax ?: 0.0
        val minSrm = srmMin ?: 0.0
        if (maxSrm > 0.0) {
            val avgSrm = if (minSrm > 0.0) (minSrm + maxSrm) / 2.0 else maxSrm
            when {
                avgSrm <= 8.0 -> result.add("pale-color")
                avgSrm <= 18.0 -> result.add("amber-color")
                else -> result.add("dark-color")
            }
        }

        // Fermentation / style group
        when {
            "hybrid" in catLower || "mixed" in catLower -> result.add("hybrid-style")
            "lager" in catLower -> {
                result.add("bottom-fermented")
                result.add("lagered")
            }
            "ale" in catLower -> result.add("top-fermented")
        }

        // Origin
        when {
            "british" in catLower -> result.add("british-isles")
            "irish" in catLower -> result.add("irish")
            "north american" in catLower -> result.add("north-america")
            "german" in catLower -> result.add("central-europe")
            "belgian" in catLower -> result.add("belgian-french")
        }

        // Character descriptors
        if ("ipa" in nameLower || (ibuMax ?: 0.0) >= 50.0) {
            result.add("hoppy")
            result.add("bitter")
        }
        if ("stout" in nameLower || "porter" in nameLower || "schwarz" in nameLower) {
            result.add("roasty")
        }
        if ("wheat" in nameLower || "weizen" in nameLower || "witbier" in nameLower) {
            result.add("wheat")
        }
        if ("sour" in nameLower || "lambic" in nameLower || "gose" in nameLower || "berliner" in nameLower || "wild" in nameLower) {
            result.add("sour")
        }
        if ("smoke" in nameLower || "rauch" in nameLower) {
            result.add("smoky")
        }
        if ("fruit" in nameLower) {
            result.add("fruity")
        }
        if ("spice" in nameLower || "herb" in nameLower) {
            result.add("spiced")
        }

        return result.toList()
    }
}

@Fts4(contentEntity = BaStyleEntity::class)
@Entity(tableName = "ba_styles_fts")
data class BaStyleFtsEntity(
    val number: String,
    val name: String,
    val category: String,
    val overallImpression: String,
    val aroma: String,
    val appearance: String,
    val flavor: String,
    val mouthfeel: String,
    val comments: String,
    val history: String,
    val characteristicIngredients: String,
    val commercialExamples: String,
    val tags: String
)
