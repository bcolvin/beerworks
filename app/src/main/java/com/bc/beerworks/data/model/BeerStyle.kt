package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class BeerStyle(
    val id: String,
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
    val tags: List<String>,
    val guidelineType: GuidelineType,
    val isFavorite: Boolean = false,
    val sweetness: String? = null,
    val carbonation: String? = null,
    val strength: String? = null
)
