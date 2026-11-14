package com.bc.beerworks.data.local.seeder

import android.content.res.AssetManager
import com.bc.beerworks.data.local.dao.YeastDao
import com.bc.beerworks.data.local.dao.YeastStyleCrossRefDao
import com.bc.beerworks.data.local.entity.YeastStrainEntity
import com.bc.beerworks.data.local.entity.YeastStyleCrossRef
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private data class JsonCommercialEquivalentRaw(
    val laboratory: String?,
    val productCode: String?,
    val productName: String?,
    val form: String?
)

private data class JsonYeastRadarRaw(
    val cleanNeutral: Float?,
    val fruityEsters: Float?,
    val spicyPhenols: Float?,
    val tartAcidity: Float?,
    val funkyEarthy: Float?,
    val sulfuryCrisp: Float?,
    val alcoholWarmth: Float?,
    val mouthfeelSweetness: Float?
)

private data class JsonYeastRaw(
    val id: String?,
    val name: String?,
    val scientificName: String?,
    val origin: String?,
    val countryCode: String?,
    val discipline: String?,
    val classification: String?,
    val form: String?,
    val attenuationMin: Double?,
    val attenuationMax: Double?,
    val flocculation: String?,
    val tempMinC: Double?,
    val tempMaxC: Double?,
    val tempMinF: Double?,
    val tempMaxF: Double?,
    val alcoholTolerance: Double?,
    val isDiastaticus: Boolean?,
    val isPhenolic: Boolean?,
    val description: String?,
    val pitchingDynamics: String?,
    val descriptors: List<String>?,
    val commercialEquivalents: List<JsonCommercialEquivalentRaw>?,
    val substituteIds: List<String>?,
    val commonStyleIds: List<String>?,
    val radarProfile: JsonYeastRadarRaw?
)

class YeastSeeder(
    private val yeastDao: YeastDao,
    private val crossRefDao: YeastStyleCrossRefDao
) : DomainSeeder {

    override suspend fun seedIfNeeded(assets: AssetManager, gson: Gson) {
        if (yeastDao.getYeastCount() > 0) return

        val yeastJson = runCatching {
            assets.open("yeasts.json").bufferedReader().use { it.readText() }
        }.getOrNull() ?: return

        if (yeastJson.isBlank()) return

        val yeastType = object : TypeToken<List<JsonYeastRaw>>() {}.type
        val rawYeasts: List<JsonYeastRaw> = gson.fromJson(yeastJson, yeastType)

        val yeastEntities = rawYeasts.map { raw ->
            val id = raw.id ?: "unknown"
            val commercialJson = raw.commercialEquivalents?.let { gson.toJson(it) } ?: "[]"
            YeastStrainEntity(
                id = id,
                name = raw.name ?: "",
                scientificName = raw.scientificName ?: "Saccharomyces cerevisiae",
                origin = raw.origin ?: "",
                countryCode = raw.countryCode ?: "US",
                discipline = raw.discipline ?: "Beer",
                classification = raw.classification ?: "Ale",
                form = raw.form ?: "Both",
                attenuationMin = raw.attenuationMin ?: 75.0,
                attenuationMax = raw.attenuationMax ?: 80.0,
                flocculation = raw.flocculation ?: "Medium",
                tempMinC = raw.tempMinC ?: 18.0,
                tempMaxC = raw.tempMaxC ?: 22.0,
                tempMinF = raw.tempMinF ?: 64.0,
                tempMaxF = raw.tempMaxF ?: 72.0,
                alcoholTolerance = raw.alcoholTolerance ?: 10.0,
                isDiastaticus = raw.isDiastaticus ?: false,
                isPhenolic = raw.isPhenolic ?: false,
                description = raw.description ?: "",
                pitchingDynamics = raw.pitchingDynamics ?: "",
                descriptors = raw.descriptors?.joinToString(",") ?: "",
                commercialEquivalentsJson = commercialJson,
                substituteIds = raw.substituteIds?.joinToString(",") ?: "",
                radarClean = raw.radarProfile?.cleanNeutral,
                radarFruity = raw.radarProfile?.fruityEsters,
                radarSpicy = raw.radarProfile?.spicyPhenols,
                radarTart = raw.radarProfile?.tartAcidity,
                radarFunky = raw.radarProfile?.funkyEarthy,
                radarSulfury = raw.radarProfile?.sulfuryCrisp,
                radarAlcohol = raw.radarProfile?.alcoholWarmth,
                radarMouthfeel = raw.radarProfile?.mouthfeelSweetness
            )
        }
        yeastDao.insertAll(yeastEntities)

        val yeastCrossRefs = rawYeasts.flatMap { raw ->
            val yeastId = raw.id ?: return@flatMap emptyList()
            (raw.commonStyleIds ?: emptyList()).map { styleId ->
                YeastStyleCrossRef(
                    yeastId = yeastId,
                    styleId = styleId,
                    guidelineType = StyleGuidelineResolver.inferGuideline(styleId)
                )
            }
        }
        if (yeastCrossRefs.isNotEmpty()) {
            crossRefDao.insertAll(yeastCrossRefs)
        }
    }
}
