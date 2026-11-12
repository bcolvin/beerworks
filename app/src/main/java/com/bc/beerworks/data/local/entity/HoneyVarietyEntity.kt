package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.bc.beerworks.data.model.HoneySensoryProfile
import com.bc.beerworks.data.model.HoneyVariety

@Entity(tableName = "honeys")
data class HoneyVarietyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val floralClassification: String,
    val botanicalSource: String,
    val origin: String,
    val countryCode: String,
    val pfundMin: Double,
    val pfundMax: Double,
    val usdaColor: String,
    val moistureTypicalPercent: Double,
    val fgRatio: Double,
    val crystallizationTendency: String,
    val extractPotentialSg: Double,
    val flavorIntensity: String,
    val description: String,
    val fermentationBehavior: String,
    val pairingNotes: String,
    val descriptors: String, // comma-separated
    val substituteIds: String, // comma-separated
    val radarFloral: Float?,
    val radarFruity: Float?,
    val radarHerbal: Float?,
    val radarSpicy: Float?,
    val radarSweet: Float?,
    val radarWoody: Float?,
    val radarEarthy: Float?,
    val radarPungent: Float?
) {
    fun toDomainModel(): HoneyVariety {
        val radar = if (radarFloral != null || radarFruity != null || radarHerbal != null ||
            radarSpicy != null || radarSweet != null || radarWoody != null ||
            radarEarthy != null || radarPungent != null
        ) {
            HoneySensoryProfile(
                floral = radarFloral ?: 0f,
                fruity = radarFruity ?: 0f,
                herbalFresh = radarHerbal ?: 0f,
                spicyWarm = radarSpicy ?: 0f,
                sweetConfectionary = radarSweet ?: 0f,
                nuttyWoody = radarWoody ?: 0f,
                earthyMineral = radarEarthy ?: 0f,
                animalPungent = radarPungent ?: 0f
            )
        } else null

        return HoneyVariety(
            id = id,
            name = name,
            floralClassification = floralClassification,
            botanicalSource = botanicalSource,
            origin = origin,
            countryCode = countryCode,
            pfundMin = pfundMin,
            pfundMax = pfundMax,
            usdaColor = usdaColor,
            moistureTypicalPercent = moistureTypicalPercent,
            fgRatio = fgRatio,
            crystallizationTendency = crystallizationTendency,
            extractPotentialSg = extractPotentialSg,
            flavorIntensity = flavorIntensity,
            description = description,
            fermentationBehavior = fermentationBehavior,
            pairingNotes = pairingNotes,
            descriptors = descriptors.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            substituteIds = substituteIds.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            radarProfile = radar
        )
    }
}

@Fts4(contentEntity = HoneyVarietyEntity::class)
@Entity(tableName = "honeys_fts")
data class HoneyVarietyFtsEntity(
    val name: String,
    val floralClassification: String,
    val botanicalSource: String,
    val origin: String,
    val description: String,
    val fermentationBehavior: String,
    val pairingNotes: String,
    val descriptors: String
)
