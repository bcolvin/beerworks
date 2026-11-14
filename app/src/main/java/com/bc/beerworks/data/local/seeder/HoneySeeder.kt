package com.bc.beerworks.data.local.seeder

import android.content.res.AssetManager
import com.bc.beerworks.data.local.dao.HoneyDao
import com.bc.beerworks.data.local.dao.HoneyStyleCrossRefDao
import com.bc.beerworks.data.local.entity.HoneyStyleCrossRef
import com.bc.beerworks.data.local.entity.HoneyVarietyEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private data class JsonHoneyRadarRaw(
    val floral: Float?,
    val fruity: Float?,
    val herbalFresh: Float?,
    val spicyWarm: Float?,
    val sweetConfectionary: Float?,
    val nuttyWoody: Float?,
    val earthyMineral: Float?,
    val animalPungent: Float?
)

private data class JsonHoneyRaw(
    val id: String?,
    val name: String?,
    val floralClassification: String?,
    val botanicalSource: String?,
    val origin: String?,
    val countryCode: String?,
    val pfundMin: Double?,
    val pfundMax: Double?,
    val usdaColor: String?,
    val moistureTypicalPercent: Double?,
    val fgRatio: Double?,
    val crystallizationTendency: String?,
    val extractPotentialSg: Double?,
    val flavorIntensity: String?,
    val description: String?,
    val fermentationBehavior: String?,
    val pairingNotes: String?,
    val descriptors: List<String>?,
    val substituteIds: List<String>?,
    val commonStyleIds: List<String>?,
    val radarProfile: JsonHoneyRadarRaw?
)

class HoneySeeder(
    private val honeyDao: HoneyDao,
    private val crossRefDao: HoneyStyleCrossRefDao
) : DomainSeeder {

    override suspend fun seedIfNeeded(assets: AssetManager, gson: Gson) {
        if (honeyDao.getHoneyCount() > 0) return

        val honeyJson = runCatching {
            assets.open("honeys.json").bufferedReader().use { it.readText() }
        }.getOrNull() ?: return

        if (honeyJson.isBlank()) return

        val honeyType = object : TypeToken<List<JsonHoneyRaw>>() {}.type
        val rawHoneys: List<JsonHoneyRaw> = gson.fromJson(honeyJson, honeyType)

        val honeyEntities = rawHoneys.map { raw ->
            val id = raw.id ?: "unknown"
            HoneyVarietyEntity(
                id = id,
                name = raw.name ?: "",
                floralClassification = raw.floralClassification ?: "Monofloral",
                botanicalSource = raw.botanicalSource ?: "",
                origin = raw.origin ?: "",
                countryCode = raw.countryCode ?: "US",
                pfundMin = raw.pfundMin ?: 15.0,
                pfundMax = raw.pfundMax ?: 30.0,
                usdaColor = raw.usdaColor ?: "White",
                moistureTypicalPercent = raw.moistureTypicalPercent ?: 17.0,
                fgRatio = raw.fgRatio ?: 1.15,
                crystallizationTendency = raw.crystallizationTendency ?: "Moderate",
                extractPotentialSg = raw.extractPotentialSg ?: 1.037,
                flavorIntensity = raw.flavorIntensity ?: "Moderate",
                description = raw.description ?: "",
                fermentationBehavior = raw.fermentationBehavior ?: "",
                pairingNotes = raw.pairingNotes ?: "",
                descriptors = raw.descriptors?.joinToString(",") ?: "",
                substituteIds = raw.substituteIds?.joinToString(",") ?: "",
                radarFloral = raw.radarProfile?.floral,
                radarFruity = raw.radarProfile?.fruity,
                radarHerbal = raw.radarProfile?.herbalFresh,
                radarSpicy = raw.radarProfile?.spicyWarm,
                radarSweet = raw.radarProfile?.sweetConfectionary,
                radarWoody = raw.radarProfile?.nuttyWoody,
                radarEarthy = raw.radarProfile?.earthyMineral,
                radarPungent = raw.radarProfile?.animalPungent
            )
        }
        honeyDao.insertAll(honeyEntities)

        val honeyCrossRefs = rawHoneys.flatMap { raw ->
            val honeyId = raw.id ?: return@flatMap emptyList()
            (raw.commonStyleIds ?: emptyList()).map { styleId ->
                HoneyStyleCrossRef(
                    honeyId = honeyId,
                    styleId = styleId,
                    guidelineType = StyleGuidelineResolver.inferGuideline(styleId)
                )
            }
        }
        if (honeyCrossRefs.isNotEmpty()) {
            crossRefDao.insertAll(honeyCrossRefs)
        }
    }
}
