package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType

@Entity(tableName = "mead_styles")
data class MeadStyleEntity(
    @PrimaryKey val id: String,
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
    val ogMin: Double?,
    val ogMax: Double?,
    val fgMin: Double?,
    val fgMax: Double?,
    val abvMin: Double?,
    val abvMax: Double?,
    val sweetness: String,
    val carbonation: String,
    val strength: String,
    val commercialExamples: String,
    val tags: String
) {
    fun toDomainModel(isFavorite: Boolean = false): BeerStyle {
        return BeerStyle(
            id = id,
            name = name,
            category = category,
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
            ibuMin = null,
            ibuMax = null,
            ogMin = ogMin,
            ogMax = ogMax,
            fgMin = fgMin,
            fgMax = fgMax,
            abvMin = abvMin,
            abvMax = abvMax,
            srmMin = null,
            srmMax = null,
            commercialExamples = commercialExamples,
            tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            guidelineType = GuidelineType.BJCP_MEAD,
            isFavorite = isFavorite,
            sweetness = sweetness,
            carbonation = carbonation,
            strength = strength
        )
    }
}

@Fts4(contentEntity = MeadStyleEntity::class)
@Entity(tableName = "mead_styles_fts")
data class MeadStyleFtsEntity(
    val number: String,
    val name: String,
    val category: String,
    val overallImpression: String,
    val aroma: String,
    val appearance: String,
    val flavor: String,
    val mouthfeel: String,
    val comments: String,
    val characteristicIngredients: String,
    val commercialExamples: String,
    val tags: String,
    val sweetness: String,
    val carbonation: String,
    val strength: String
)
