package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.bc.beerworks.data.model.MaltSensoryProfile
import com.bc.beerworks.data.model.MaltVariety

@Entity(tableName = "malts")
data class MaltVarietyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val grainType: String,
    val origin: String,
    val countryCode: String,
    val colorMin: Double,
    val colorMax: Double,
    val diastaticPowerLintner: Int,
    val extractPotentialDbfg: Double,
    val maxGristPercentage: Int,
    val description: String,
    val typicalUsage: String,
    val functionalTags: String, // comma-separated
    val descriptors: String, // comma-separated
    val commercialExamples: String, // comma-separated
    val substituteIds: String, // comma-separated
    val radarSweet: Float?,
    val radarBiscuit: Float?,
    val radarToasty: Float?,
    val radarCaramel: Float?,
    val radarNutty: Float?,
    val radarChocolate: Float?,
    val radarRoast: Float?,
    val radarHusky: Float?
) {
    fun toDomainModel(): MaltVariety {
        val radar = if (radarSweet != null || radarBiscuit != null || radarToasty != null ||
            radarCaramel != null || radarNutty != null || radarChocolate != null ||
            radarRoast != null || radarHusky != null
        ) {
            MaltSensoryProfile(
                maltySweet = radarSweet ?: 0f,
                breadyBiscuit = radarBiscuit ?: 0f,
                toasty = radarToasty ?: 0f,
                caramelToffee = radarCaramel ?: 0f,
                nutty = radarNutty ?: 0f,
                chocolate = radarChocolate ?: 0f,
                roastCoffee = radarRoast ?: 0f,
                grainyHusky = radarHusky ?: 0f
            )
        } else null

        return MaltVariety(
            id = id,
            name = name,
            category = category,
            grainType = grainType,
            origin = origin,
            countryCode = countryCode,
            colorMin = colorMin,
            colorMax = colorMax,
            diastaticPowerLintner = diastaticPowerLintner,
            extractPotentialDbfg = extractPotentialDbfg,
            maxGristPercentage = maxGristPercentage,
            description = description,
            typicalUsage = typicalUsage,
            functionalTags = functionalTags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            descriptors = descriptors.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            commercialExamples = commercialExamples.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            substituteIds = substituteIds.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            radarProfile = radar
        )
    }
}

@Fts4(contentEntity = MaltVarietyEntity::class)
@Entity(tableName = "malts_fts")
data class MaltVarietyFtsEntity(
    val name: String,
    val category: String,
    val origin: String,
    val description: String,
    val typicalUsage: String,
    val functionalTags: String,
    val descriptors: String,
    val commercialExamples: String
)
