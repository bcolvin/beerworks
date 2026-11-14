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
        tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        guidelineType = GuidelineType.BA_2026,
        isFavorite = isFavorite
    )
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
