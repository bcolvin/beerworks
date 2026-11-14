package com.bc.beerworks.data.local.seeder

import android.content.res.AssetManager
import com.bc.beerworks.data.local.dao.PomeDao
import com.bc.beerworks.data.local.dao.PomeStyleCrossRefDao
import com.bc.beerworks.data.local.entity.PomeStyleCrossRef
import com.bc.beerworks.data.local.entity.PomeVarietyEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private data class JsonPomeRadarRaw(
    val aciditySharpness: Float?,
    val tanninBitterness: Float?,
    val freshFruit: Float?,
    val bakedFruit: Float?,
    val citrusTropical: Float?,
    val floralHerbal: Float?,
    val earthyFarmhouse: Float?,
    val sweetConfectionary: Float?
)

private data class JsonPomeRaw(
    val id: String?,
    val name: String?,
    val fruitType: String?,
    val classification: String?,
    val fruitUse: String?,
    val botanicalName: String?,
    val origin: String?,
    val countryCode: String?,
    val acidityMalicPercent: Double?,
    val tanninPolyphenolPercent: Double?,
    val tanninLevel: String?,
    val sgMin: Double?,
    val sgMax: Double?,
    val harvestSeason: String?,
    val sorbitolGramsPerLiter: Double?,
    val description: String?,
    val blendingGuidance: String?,
    val pressingCharacteristics: String?,
    val descriptors: List<String>?,
    val substituteIds: List<String>?,
    val commonStyleIds: List<String>?,
    val radarProfile: JsonPomeRadarRaw?
)

class PomeSeeder(
    private val pomeDao: PomeDao,
    private val crossRefDao: PomeStyleCrossRefDao
) : DomainSeeder {

    override suspend fun seedIfNeeded(assets: AssetManager, gson: Gson) {
        if (pomeDao.getPomeCount() > 0) return

        val pomeJson = runCatching {
            assets.open("pomes.json").bufferedReader().use { it.readText() }
        }.getOrNull() ?: return

        if (pomeJson.isBlank()) return

        val pomeType = object : TypeToken<List<JsonPomeRaw>>() {}.type
        val rawPomes: List<JsonPomeRaw> = gson.fromJson(pomeJson, pomeType)

        val pomeEntities = rawPomes.map { raw ->
            val id = raw.id ?: "unknown"
            PomeVarietyEntity(
                id = id,
                name = raw.name ?: "",
                fruitType = raw.fruitType ?: "Apple",
                classification = raw.classification ?: "Bittersweet",
                fruitUse = raw.fruitUse ?: "Heritage Cider/Perry",
                botanicalName = raw.botanicalName ?: "Malus domestica",
                origin = raw.origin ?: "",
                countryCode = raw.countryCode ?: "GB",
                acidityMalicPercent = raw.acidityMalicPercent ?: 0.30,
                tanninPolyphenolPercent = raw.tanninPolyphenolPercent ?: 0.25,
                tanninLevel = raw.tanninLevel ?: "Medium",
                sgMin = raw.sgMin ?: 1.050,
                sgMax = raw.sgMax ?: 1.060,
                harvestSeason = raw.harvestSeason ?: "Mid",
                sorbitolGramsPerLiter = raw.sorbitolGramsPerLiter,
                description = raw.description ?: "",
                blendingGuidance = raw.blendingGuidance ?: "",
                pressingCharacteristics = raw.pressingCharacteristics ?: "",
                descriptors = raw.descriptors?.joinToString(",") ?: "",
                substituteIds = raw.substituteIds?.joinToString(",") ?: "",
                radarAcidity = raw.radarProfile?.aciditySharpness,
                radarTannin = raw.radarProfile?.tanninBitterness,
                radarFreshFruit = raw.radarProfile?.freshFruit,
                radarBakedFruit = raw.radarProfile?.bakedFruit,
                radarCitrus = raw.radarProfile?.citrusTropical,
                radarFloral = raw.radarProfile?.floralHerbal,
                radarEarthy = raw.radarProfile?.earthyFarmhouse,
                radarSweet = raw.radarProfile?.sweetConfectionary
            )
        }
        pomeDao.insertAll(pomeEntities)

        val pomeCrossRefs = rawPomes.flatMap { raw ->
            val pomeId = raw.id ?: return@flatMap emptyList()
            (raw.commonStyleIds ?: emptyList()).map { styleId ->
                PomeStyleCrossRef(
                    pomeId = pomeId,
                    styleId = styleId,
                    guidelineType = StyleGuidelineResolver.inferGuideline(styleId)
                )
            }
        }
        if (pomeCrossRefs.isNotEmpty()) {
            crossRefDao.insertAll(pomeCrossRefs)
        }
    }
}
