package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.bc.beerworks.data.model.PomeSensoryProfile
import com.bc.beerworks.data.model.PomeVariety

@Entity(tableName = "pomes")
data class PomeVarietyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val fruitType: String,
    val classification: String,
    val fruitUse: String,
    val botanicalName: String,
    val origin: String,
    val countryCode: String,
    val acidityMalicPercent: Double,
    val tanninPolyphenolPercent: Double,
    val tanninLevel: String,
    val sgMin: Double,
    val sgMax: Double,
    val harvestSeason: String,
    val sorbitolGramsPerLiter: Double?,
    val description: String,
    val blendingGuidance: String,
    val pressingCharacteristics: String,
    val descriptors: String, // comma-separated
    val substituteIds: String, // comma-separated
    val radarAcidity: Float?,
    val radarTannin: Float?,
    val radarFreshFruit: Float?,
    val radarBakedFruit: Float?,
    val radarCitrus: Float?,
    val radarFloral: Float?,
    val radarEarthy: Float?,
    val radarSweet: Float?
) {
    fun toDomainModel(): PomeVariety {
        val radar = if (radarAcidity != null || radarTannin != null || radarFreshFruit != null ||
            radarBakedFruit != null || radarCitrus != null || radarFloral != null ||
            radarEarthy != null || radarSweet != null
        ) {
            PomeSensoryProfile(
                aciditySharpness = radarAcidity ?: 0f,
                tanninBitterness = radarTannin ?: 0f,
                freshFruit = radarFreshFruit ?: 0f,
                bakedFruit = radarBakedFruit ?: 0f,
                citrusTropical = radarCitrus ?: 0f,
                floralHerbal = radarFloral ?: 0f,
                earthyFarmhouse = radarEarthy ?: 0f,
                sweetConfectionary = radarSweet ?: 0f
            )
        } else null

        return PomeVariety(
            id = id,
            name = name,
            fruitType = fruitType,
            classification = classification,
            fruitUse = fruitUse,
            botanicalName = botanicalName,
            origin = origin,
            countryCode = countryCode,
            acidityMalicPercent = acidityMalicPercent,
            tanninPolyphenolPercent = tanninPolyphenolPercent,
            tanninLevel = tanninLevel,
            sgMin = sgMin,
            sgMax = sgMax,
            harvestSeason = harvestSeason,
            sorbitolGramsPerLiter = sorbitolGramsPerLiter,
            description = description,
            blendingGuidance = blendingGuidance,
            pressingCharacteristics = pressingCharacteristics,
            descriptors = descriptors.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            substituteIds = substituteIds.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            radarProfile = radar
        )
    }
}

@Fts4(contentEntity = PomeVarietyEntity::class)
@Entity(tableName = "pomes_fts")
data class PomeVarietyFtsEntity(
    val name: String,
    val fruitType: String,
    val classification: String,
    val botanicalName: String,
    val origin: String,
    val description: String,
    val blendingGuidance: String,
    val pressingCharacteristics: String,
    val descriptors: String
)
