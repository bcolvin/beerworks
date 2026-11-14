package com.bc.beerworks.data.local.seeder

import android.content.res.AssetManager
import com.bc.beerworks.data.local.dao.HopDao
import com.bc.beerworks.data.local.dao.HopStyleCrossRefDao
import com.bc.beerworks.data.local.entity.HopStyleCrossRef
import com.bc.beerworks.data.local.entity.HopVarietyEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private data class JsonHopRadarRaw(
    val citrus: Float?,
    val tropical: Float?,
    val floral: Float?,
    val herbal: Float?,
    val spicy: Float?,
    val piney: Float?,
    val earthy: Float?,
    val grassy: Float?
)

private data class JsonHopRaw(
    val id: String?,
    val name: String?,
    val origin: String?,
    val countryCode: String?,
    val purpose: String?,
    val alphaAcidMin: Double?,
    val alphaAcidMax: Double?,
    val betaAcidMin: Double?,
    val betaAcidMax: Double?,
    val sensoryCategories: List<String>?,
    val descriptors: List<String>?,
    val description: String?,
    val parentHopIds: List<String>?,
    val substituteIds: List<String>?,
    val commonStyleIds: List<String>?,
    val radarProfile: JsonHopRadarRaw?
)

class HopSeeder(
    private val hopDao: HopDao,
    private val crossRefDao: HopStyleCrossRefDao
) : DomainSeeder {

    override suspend fun seedIfNeeded(assets: AssetManager, gson: Gson) {
        if (hopDao.getHopCount() > 0) return

        val hopsJson = runCatching {
            assets.open("hops.json").bufferedReader().use { it.readText() }
        }.getOrNull() ?: return

        if (hopsJson.isBlank()) return

        val hopType = object : TypeToken<List<JsonHopRaw>>() {}.type
        val rawHops: List<JsonHopRaw> = gson.fromJson(hopsJson, hopType)

        val hopEntities = rawHops.map { raw ->
            val id = raw.id ?: raw.name?.lowercase()?.replace(" ", "_") ?: "unknown"
            HopVarietyEntity(
                id = id,
                name = raw.name ?: "",
                origin = raw.origin ?: "",
                countryCode = raw.countryCode ?: "",
                purpose = raw.purpose ?: "Dual-Purpose",
                alphaAcidMin = raw.alphaAcidMin ?: 0.0,
                alphaAcidMax = raw.alphaAcidMax ?: 0.0,
                betaAcidMin = raw.betaAcidMin ?: 0.0,
                betaAcidMax = raw.betaAcidMax ?: 0.0,
                sensoryCategories = raw.sensoryCategories?.joinToString(",") ?: "",
                descriptors = raw.descriptors?.joinToString(",") ?: "",
                description = raw.description ?: "",
                parentHopIds = raw.parentHopIds?.joinToString(",") ?: "",
                substituteIds = raw.substituteIds?.joinToString(",") ?: "",
                radarCitrus = raw.radarProfile?.citrus,
                radarTropical = raw.radarProfile?.tropical,
                radarFloral = raw.radarProfile?.floral,
                radarHerbal = raw.radarProfile?.herbal,
                radarSpicy = raw.radarProfile?.spicy,
                radarPiney = raw.radarProfile?.piney,
                radarEarthy = raw.radarProfile?.earthy,
                radarGrassy = raw.radarProfile?.grassy
            )
        }
        hopDao.insertAll(hopEntities)

        val crossRefEntities = rawHops.flatMap { raw ->
            val hopId = raw.id ?: raw.name?.lowercase()?.replace(" ", "_") ?: "unknown"
            (raw.commonStyleIds ?: emptyList()).map { styleId ->
                HopStyleCrossRef(
                    hopId = hopId,
                    styleId = styleId,
                    guidelineType = StyleGuidelineResolver.inferGuideline(styleId)
                )
            }
        }
        val braggotHopCrossRefs = BRAGGOT_HOPS.map { hopId ->
            HopStyleCrossRef(hopId = hopId, styleId = "M4A", guidelineType = "BJCP_MEAD")
        }
        val allCrossRefs = crossRefEntities + braggotHopCrossRefs
        if (allCrossRefs.isNotEmpty()) {
            crossRefDao.insertAll(allCrossRefs)
        }
    }

    companion object {
        private val BRAGGOT_HOPS = listOf("cascade", "centennial", "hallertau_mittelfruh", "east_kent_goldings")
    }
}
