package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.bc.beerworks.data.model.CommercialYeastEquivalent
import com.bc.beerworks.data.model.YeastSensoryProfile
import com.bc.beerworks.data.model.YeastStrain
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "yeasts")
data class YeastStrainEntity(
    @PrimaryKey val id: String,
    val name: String,
    val scientificName: String,
    val origin: String,
    val countryCode: String,
    val discipline: String,
    val classification: String,
    val form: String,
    val attenuationMin: Double,
    val attenuationMax: Double,
    val flocculation: String,
    val tempMinC: Double,
    val tempMaxC: Double,
    val tempMinF: Double,
    val tempMaxF: Double,
    val alcoholTolerance: Double,
    val isDiastaticus: Boolean,
    val isPhenolic: Boolean,
    val description: String,
    val pitchingDynamics: String,
    val descriptors: String, // comma-separated
    val commercialEquivalentsJson: String, // JSON string
    val substituteIds: String, // comma-separated
    val radarClean: Float?,
    val radarFruity: Float?,
    val radarSpicy: Float?,
    val radarTart: Float?,
    val radarFunky: Float?,
    val radarSulfury: Float?,
    val radarAlcohol: Float?,
    val radarMouthfeel: Float?
) {
    fun toDomainModel(): YeastStrain {
        val radar = if (radarClean != null || radarFruity != null || radarSpicy != null ||
            radarTart != null || radarFunky != null || radarSulfury != null ||
            radarAlcohol != null || radarMouthfeel != null
        ) {
            YeastSensoryProfile(
                cleanNeutral = radarClean ?: 0f,
                fruityEsters = radarFruity ?: 0f,
                spicyPhenols = radarSpicy ?: 0f,
                tartAcidity = radarTart ?: 0f,
                funkyEarthy = radarFunky ?: 0f,
                sulfuryCrisp = radarSulfury ?: 0f,
                alcoholWarmth = radarAlcohol ?: 0f,
                mouthfeelSweetness = radarMouthfeel ?: 0f
            )
        } else null

        val commercialList = if (commercialEquivalentsJson.isNotBlank()) {
            runCatching {
                val listType = object : TypeToken<List<CommercialYeastEquivalent>>() {}.type
                Gson().fromJson<List<CommercialYeastEquivalent>>(commercialEquivalentsJson, listType)
            }.getOrDefault(emptyList())
        } else emptyList()

        return YeastStrain(
            id = id,
            name = name,
            scientificName = scientificName,
            origin = origin,
            countryCode = countryCode,
            discipline = discipline,
            classification = classification,
            form = form,
            attenuationMin = attenuationMin,
            attenuationMax = attenuationMax,
            flocculation = flocculation,
            tempMinC = tempMinC,
            tempMaxC = tempMaxC,
            tempMinF = tempMinF,
            tempMaxF = tempMaxF,
            alcoholTolerance = alcoholTolerance,
            isDiastaticus = isDiastaticus,
            isPhenolic = isPhenolic,
            description = description,
            pitchingDynamics = pitchingDynamics,
            descriptors = descriptors.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            commercialEquivalents = commercialList,
            substituteIds = substituteIds.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            radarProfile = radar
        )
    }
}

@Fts4(contentEntity = YeastStrainEntity::class)
@Entity(tableName = "yeasts_fts")
data class YeastStrainFtsEntity(
    val name: String,
    val scientificName: String,
    val origin: String,
    val description: String,
    val pitchingDynamics: String,
    val descriptors: String,
    val commercialEquivalentsJson: String
)
