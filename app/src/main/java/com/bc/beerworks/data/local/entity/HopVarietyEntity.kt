package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.model.SensoryRadarProfile

@Entity(tableName = "hops")
data class HopVarietyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val origin: String,
    val countryCode: String,
    val purpose: String,
    val alphaAcidMin: Double,
    val alphaAcidMax: Double,
    val betaAcidMin: Double,
    val betaAcidMax: Double,
    val sensoryCategories: String, // comma-separated
    val descriptors: String, // comma-separated
    val description: String,
    val parentHopIds: String, // comma-separated
    val substituteIds: String, // comma-separated
    val radarCitrus: Float?,
    val radarTropical: Float?,
    val radarFloral: Float?,
    val radarHerbal: Float?,
    val radarSpicy: Float?,
    val radarPiney: Float?,
    val radarEarthy: Float?,
    val radarGrassy: Float?
) {
    fun toDomainModel(): HopVariety {
        val radar = if (radarCitrus != null || radarTropical != null || radarFloral != null ||
            radarHerbal != null || radarSpicy != null || radarPiney != null ||
            radarEarthy != null || radarGrassy != null
        ) {
            SensoryRadarProfile(
                citrus = radarCitrus ?: 0f,
                tropical = radarTropical ?: 0f,
                floral = radarFloral ?: 0f,
                herbal = radarHerbal ?: 0f,
                spicy = radarSpicy ?: 0f,
                piney = radarPiney ?: 0f,
                earthy = radarEarthy ?: 0f,
                grassy = radarGrassy ?: 0f
            )
        } else null

        return HopVariety(
            id = id,
            name = name,
            origin = origin,
            countryCode = countryCode,
            purpose = purpose,
            alphaAcidMin = alphaAcidMin,
            alphaAcidMax = alphaAcidMax,
            betaAcidMin = betaAcidMin,
            betaAcidMax = betaAcidMax,
            sensoryCategories = sensoryCategories.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            descriptors = descriptors.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            description = description,
            parentHopIds = parentHopIds.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            substituteIds = substituteIds.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            radarProfile = radar
        )
    }
}

@Fts4(contentEntity = HopVarietyEntity::class)
@Entity(tableName = "hops_fts")
data class HopVarietyFtsEntity(
    val name: String,
    val origin: String,
    val purpose: String,
    val sensoryCategories: String,
    val descriptors: String,
    val description: String
)
