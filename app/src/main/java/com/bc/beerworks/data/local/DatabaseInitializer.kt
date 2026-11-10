package com.bc.beerworks.data.local

import android.content.Context
import com.bc.beerworks.data.local.entity.BaStyleEntity
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.HopStyleCrossRef
import com.bc.beerworks.data.local.entity.HopVarietyEntity
import com.bc.beerworks.data.local.entity.MaltStyleCrossRef
import com.bc.beerworks.data.local.entity.MaltVarietyEntity
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class JsonMeadStyleRaw(
    val id: String?,
    val name: String?,
    val number: String?,
    val category: String?,
    val categorynumber: String?,
    val overallimpression: String?,
    val aroma: String?,
    val appearance: String?,
    val flavor: String?,
    val mouthfeel: String?,
    val comments: String?,
    val history: String?,
    val characteristicingredients: String?,
    val stylecomparison: String?,
    val ogmin: String?,
    val ogmax: String?,
    val fgmin: String?,
    val fgmax: String?,
    val abvmin: String?,
    val abvmax: String?,
    val commercialexamples: String?,
    val tags: String?,
    val sweetness: String?,
    val carbonation: String?,
    val strength: String?
)

data class JsonMaltRadarRaw(
    val maltySweet: Float?,
    val breadyBiscuit: Float?,
    val toasty: Float?,
    val caramelToffee: Float?,
    val nutty: Float?,
    val chocolate: Float?,
    val roastCoffee: Float?,
    val grainyHusky: Float?
)

data class JsonMaltRaw(
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

data class JsonHopRadarRaw(
    val citrus: Float?,
    val tropical: Float?,
    val floral: Float?,
    val herbal: Float?,
    val spicy: Float?,
    val piney: Float?,
    val earthy: Float?,
    val grassy: Float?
)

data class JsonHopRaw(
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

data class JsonStyleRaw(
    val name: String?,
    val number: String?,
    val category: String?,
    val categorynumber: String?,
    val overallimpression: String?,
    val aroma: String?,
    val appearance: String?,
    val flavor: String?,
    val mouthfeel: String?,
    val comments: String?,
    val history: String?,
    val characteristicingredients: String?,
    val stylecomparison: String?,
    val ibumin: String?,
    val ibumax: String?,
    val ogmin: String?,
    val ogmax: String?,
    val fgmin: String?,
    val fgmax: String?,
    val abvmin: String?,
    val abvmax: String?,
    val srmmin: String?,
    val srmmax: String?,
    val commercialexamples: String?,
    val tags: String?
)

object DatabaseInitializer {
    suspend fun initializeIfNeeded(context: Context, database: BeerworksDatabase) = withContext(Dispatchers.IO) {
        val gson = Gson()
        val bjcpDao = database.bjcpStyleDao()
        val baDao = database.baStyleDao()

        val bjcpJson = context.assets.open("bjcp_styles.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<JsonStyleRaw>>() {}.type
        val rawBjcp: List<JsonStyleRaw> = gson.fromJson(bjcpJson, type)

        val bjcpEntities = rawBjcp.map { raw ->
            val num = raw.number ?: "0"
            val id = if (num.isNotBlank()) num else raw.name ?: "Unknown"
            BjcpStyleEntity(
                id = id,
                name = raw.name ?: "",
                category = raw.category ?: "",
                categoryNumber = raw.categorynumber ?: "0",
                number = num,
                overallImpression = raw.overallimpression ?: "",
                aroma = raw.aroma ?: "",
                appearance = raw.appearance ?: "",
                flavor = raw.flavor ?: "",
                mouthfeel = raw.mouthfeel ?: "",
                comments = raw.comments ?: "",
                history = raw.history ?: "",
                characteristicIngredients = raw.characteristicingredients ?: "",
                styleComparison = raw.stylecomparison ?: "",
                ibuMin = raw.ibumin?.toDoubleOrNull(),
                ibuMax = raw.ibumax?.toDoubleOrNull(),
                ogMin = raw.ogmin?.toDoubleOrNull(),
                ogMax = raw.ogmax?.toDoubleOrNull(),
                fgMin = raw.fgmin?.toDoubleOrNull(),
                fgMax = raw.fgmax?.toDoubleOrNull(),
                abvMin = raw.abvmin?.toDoubleOrNull(),
                abvMax = raw.abvmax?.toDoubleOrNull(),
                srmMin = raw.srmmin?.toDoubleOrNull(),
                srmMax = raw.srmmax?.toDoubleOrNull(),
                commercialExamples = raw.commercialexamples ?: "",
                tags = raw.tags ?: ""
            )
        }
        bjcpDao.insertAll(bjcpEntities)

        val baJson = context.assets.open("ba_styles.json").bufferedReader().use { it.readText() }
        val rawBa: List<JsonStyleRaw> = gson.fromJson(baJson, type)

        val baEntities = rawBa.map { raw ->
            val num = raw.number ?: "0"
            val id = if (num.isNotBlank()) num else raw.name ?: "Unknown"
            BaStyleEntity(
                id = id,
                name = raw.name ?: "",
                category = raw.category ?: "",
                categoryNumber = raw.categorynumber ?: "0",
                number = num,
                overallImpression = raw.overallimpression ?: "",
                aroma = raw.aroma ?: "",
                appearance = raw.appearance ?: "",
                flavor = raw.flavor ?: "",
                mouthfeel = raw.mouthfeel ?: "",
                comments = raw.comments ?: "",
                history = raw.history ?: "",
                characteristicIngredients = raw.characteristicingredients ?: "",
                styleComparison = raw.stylecomparison ?: "",
                ibuMin = raw.ibumin?.toDoubleOrNull(),
                ibuMax = raw.ibumax?.toDoubleOrNull(),
                ogMin = raw.ogmin?.toDoubleOrNull(),
                ogMax = raw.ogmax?.toDoubleOrNull(),
                fgMin = raw.fgmin?.toDoubleOrNull(),
                fgMax = raw.fgmax?.toDoubleOrNull(),
                abvMin = raw.abvmin?.toDoubleOrNull(),
                abvMax = raw.abvmax?.toDoubleOrNull(),
                srmMin = raw.srmmin?.toDoubleOrNull(),
                srmMax = raw.srmmax?.toDoubleOrNull(),
                commercialExamples = raw.commercialexamples ?: "",
                tags = raw.tags ?: ""
            )
        }
        baDao.insertAll(baEntities)

        val hopDao = database.hopDao()
        val crossRefDao = database.hopStyleCrossRefDao()

        val hopsJson = runCatching {
            context.assets.open("hops.json").bufferedReader().use { it.readText() }
        }.getOrNull()

        if (!hopsJson.isNullOrBlank()) {
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
                        guidelineType = "BJCP_2021"
                    )
                }
            }
            if (crossRefEntities.isNotEmpty()) {
                crossRefDao.insertAll(crossRefEntities)
            }
        }

        val maltDao = database.maltDao()
        val maltCrossRefDao = database.maltStyleCrossRefDao()

        val maltsJson = runCatching {
            context.assets.open("malts.json").bufferedReader().use { it.readText() }
        }.getOrNull()

        if (!maltsJson.isNullOrBlank()) {
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
                        guidelineType = "BJCP_2021"
                    )
                }
            }
            if (crossRefEntities.isNotEmpty()) {
                maltCrossRefDao.insertAll(crossRefEntities)
            }
        }

        val meadDao = database.meadStyleDao()
        val meadJson = runCatching {
            context.assets.open("mead_styles.json").bufferedReader().use { it.readText() }
        }.getOrNull()

        if (!meadJson.isNullOrBlank()) {
            val meadType = object : TypeToken<List<JsonMeadStyleRaw>>() {}.type
            val rawMeads: List<JsonMeadStyleRaw> = gson.fromJson(meadJson, meadType)

            val meadEntities = rawMeads.map { raw ->
                val id = raw.id ?: raw.number ?: "unknown"
                MeadStyleEntity(
                    id = id,
                    name = raw.name ?: "",
                    category = raw.category ?: "",
                    categoryNumber = raw.categorynumber ?: "",
                    number = raw.number ?: "",
                    overallImpression = raw.overallimpression ?: "",
                    aroma = raw.aroma ?: "",
                    appearance = raw.appearance ?: "",
                    flavor = raw.flavor ?: "",
                    mouthfeel = raw.mouthfeel ?: "",
                    comments = raw.comments ?: "",
                    history = raw.history ?: "",
                    characteristicIngredients = raw.characteristicingredients ?: "",
                    styleComparison = raw.stylecomparison ?: "",
                    ogMin = raw.ogmin?.toDoubleOrNull(),
                    ogMax = raw.ogmax?.toDoubleOrNull(),
                    fgMin = raw.fgmin?.toDoubleOrNull(),
                    fgMax = raw.fgmax?.toDoubleOrNull(),
                    abvMin = raw.abvmin?.toDoubleOrNull(),
                    abvMax = raw.abvmax?.toDoubleOrNull(),
                    sweetness = raw.sweetness ?: "",
                    carbonation = raw.carbonation ?: "",
                    strength = raw.strength ?: "",
                    commercialExamples = raw.commercialexamples ?: "",
                    tags = raw.tags ?: ""
                )
            }
            meadDao.insertAll(meadEntities)

            // Seed Braggot cross references (M4A)
            val braggotHops = listOf("cascade", "centennial", "hallertau_mittelfruh", "east_kent_goldings")
            val braggotMalts = listOf("pale_ale_malt", "pilsner_malt", "munich_malt", "caramel_malt_60l")

            val hopCrossRefs = braggotHops.map { hopId ->
                HopStyleCrossRef(hopId = hopId, styleId = "M4A", guidelineType = "BJCP_MEAD")
            }
            crossRefDao.insertAll(hopCrossRefs)

            val maltCrossRefs = braggotMalts.map { maltId ->
                MaltStyleCrossRef(maltId = maltId, styleId = "M4A", guidelineType = "BJCP_MEAD")
            }
            maltCrossRefDao.insertAll(maltCrossRefs)
        }
    }
}
