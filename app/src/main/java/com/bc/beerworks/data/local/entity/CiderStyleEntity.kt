package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType

@Entity(tableName = "cider_styles")
data class CiderStyleEntity(
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
) : StyleEntity {
    override fun toDomainModel(isFavorite: Boolean): BeerStyle {
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
            guidelineType = GuidelineType.BJCP_CIDER,
            isFavorite = isFavorite,
            sweetness = sweetness,
            carbonation = carbonation,
            strength = strength
        )
    }
}

@Fts4(contentEntity = CiderStyleEntity::class)
@Entity(tableName = "cider_styles_fts")
data class CiderStyleFtsEntity(
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
