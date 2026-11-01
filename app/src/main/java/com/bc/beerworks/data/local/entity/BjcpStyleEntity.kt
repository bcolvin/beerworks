package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "bjcp_styles")
data class BjcpStyleEntity(
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
)

@Fts4(contentEntity = BjcpStyleEntity::class)
@Entity(tableName = "bjcp_styles_fts")
data class BjcpStyleFtsEntity(
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
    val tags: String
)
