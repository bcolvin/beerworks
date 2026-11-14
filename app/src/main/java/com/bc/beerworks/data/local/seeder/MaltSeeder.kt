package com.bc.beerworks.data.local.seeder

import android.content.res.AssetManager
import com.bc.beerworks.data.local.dao.MaltDao
import com.bc.beerworks.data.local.dao.MaltStyleCrossRefDao
import com.bc.beerworks.data.local.entity.MaltStyleCrossRef
import com.bc.beerworks.data.local.entity.MaltVarietyEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private data class JsonMaltRadarRaw(
    val maltySweet: Float?,
    val breadyBiscuit: Float?,
    val toasty: Float?,
    val caramelToffee: Float?,
    val nutty: Float?,
    val chocolate: Float?,
    val roastCoffee: Float?,
    val grainyHusky: Float?
)

private data class JsonMaltRaw(
    val id: String?,
    val name: String?,
    val category: String?,
    val grainType: String?,
    val origin: String?,
    val countryCode: String?,
    val colorMin: Double?,
    val colorMax: Double?,
    val diastaticPowerLintner: Int?,
    val extractPotentialDbfg: Double?,
    val maxGristPercentage: Int?,
    val description: String?,
    val typicalUsage: String?,
    val functionalTags: List<String>?,
    val descriptors: List<String>?,
    val commercialExamples: List<String>?,
    val substituteIds: List<String>?,
    val commonStyleIds: List<String>?,
    val radarProfile: JsonMaltRadarRaw?
)

class MaltSeeder(
    private val maltDao: MaltDao,
    private val crossRefDao: MaltStyleCrossRefDao
) : DomainSeeder {

    override suspend fun seedIfNeeded(assets: AssetManager, gson: Gson) {
        if (maltDao.getMaltCount() > 0) return

        val maltsJson = runCatching {
            assets.open("malts.json").bufferedReader().use { it.readText() }
        }.getOrNull() ?: return

        if (maltsJson.isBlank()) return

        val maltType = object : TypeToken<List<JsonMaltRaw>>() {}.type
        val rawMalts: List<JsonMaltRaw> = gson.fromJson(maltsJson, maltType)

        val maltEntities = rawMalts.map { raw ->
            val id = raw.id ?: raw.name?.lowercase()?.replace(" ", "_") ?: "unknown"
            MaltVarietyEntity(
                id = id,
                name = raw.name ?: "",
                category = raw.category ?: "Base Malt",
                grainType = raw.grainType ?: "Barley",
                origin = raw.origin ?: "",
                countryCode = raw.countryCode ?: "",
                colorMin = raw.colorMin ?: 2.0,
                colorMax = raw.colorMax ?: 2.0,
                diastaticPowerLintner = raw.diastaticPowerLintner ?: 0,
                extractPotentialDbfg = raw.extractPotentialDbfg ?: 75.0,
                maxGristPercentage = raw.maxGristPercentage ?: 100,
                description = raw.description ?: "",
                typicalUsage = raw.typicalUsage ?: "",
                functionalTags = raw.functionalTags?.joinToString(",") ?: "",
                descriptors = raw.descriptors?.joinToString(",") ?: "",
                commercialExamples = raw.commercialExamples?.joinToString(",") ?: "",
                substituteIds = raw.substituteIds?.joinToString(",") ?: "",
                radarSweet = raw.radarProfile?.maltySweet,
                radarBiscuit = raw.radarProfile?.breadyBiscuit,
                radarToasty = raw.radarProfile?.toasty,
                radarCaramel = raw.radarProfile?.caramelToffee,
                radarNutty = raw.radarProfile?.nutty,
                radarChocolate = raw.radarProfile?.chocolate,
                radarRoast = raw.radarProfile?.roastCoffee,
                radarHusky = raw.radarProfile?.grainyHusky
            )
        }
        maltDao.insertAll(maltEntities)

        val crossRefEntities = rawMalts.flatMap { raw ->
            val maltId = raw.id ?: raw.name?.lowercase()?.replace(" ", "_") ?: "unknown"
            (raw.commonStyleIds ?: emptyList()).map { styleId ->
                MaltStyleCrossRef(
                    maltId = maltId,
                    styleId = styleId,
                    guidelineType = StyleGuidelineResolver.inferGuideline(styleId)
                )
            }
        }
        val braggotMaltCrossRefs = BRAGGOT_MALTS.map { maltId ->
            MaltStyleCrossRef(maltId = maltId, styleId = "M4A", guidelineType = "BJCP_MEAD")
        }
        val allCrossRefs = crossRefEntities + braggotMaltCrossRefs
        if (allCrossRefs.isNotEmpty()) {
            crossRefDao.insertAll(allCrossRefs)
        }
    }

    companion object {
        private val BRAGGOT_MALTS = listOf("pale_ale_malt", "pilsner_malt", "munich_malt", "caramel_malt_60l")
    }
}
