package com.bc.beerworks.data.local

import android.content.Context
import com.bc.beerworks.data.local.entity.BaStyleEntity
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.CiderStyleEntity
import com.bc.beerworks.data.local.entity.HoneyStyleCrossRef
import com.bc.beerworks.data.local.entity.HoneyVarietyEntity
import com.bc.beerworks.data.local.entity.HopStyleCrossRef
import com.bc.beerworks.data.local.entity.HopVarietyEntity
import com.bc.beerworks.data.local.entity.MaltStyleCrossRef
import com.bc.beerworks.data.local.entity.MaltVarietyEntity
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import com.bc.beerworks.data.local.entity.PomeStyleCrossRef
import com.bc.beerworks.data.local.entity.PomeVarietyEntity
import com.bc.beerworks.data.local.entity.YeastStrainEntity
import com.bc.beerworks.data.local.entity.YeastStyleCrossRef
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

data class JsonHoneyRadarRaw(
    val floral: Float?,
    val fruity: Float?,
    val herbalFresh: Float?,
    val spicyWarm: Float?,
    val sweetConfectionary: Float?,
    val nuttyWoody: Float?,
    val earthyMineral: Float?,
    val animalPungent: Float?
)

data class JsonHoneyRaw(
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

data class JsonPomeRadarRaw(
    val aciditySharpness: Float?,
    val tanninBitterness: Float?,
    val freshFruit: Float?,
    val bakedFruit: Float?,
    val citrusTropical: Float?,
    val floralHerbal: Float?,
    val earthyFarmhouse: Float?,
    val sweetConfectionary: Float?
)

data class JsonPomeRaw(
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

data class JsonCommercialEquivalentRaw(
    val laboratory: String?,
    val productCode: String?,
    val productName: String?,
    val form: String?
)

data class JsonYeastRadarRaw(
    val cleanNeutral: Float?,
    val fruityEsters: Float?,
    val spicyPhenols: Float?,
    val tartAcidity: Float?,
    val funkyEarthy: Float?,
    val sulfuryCrisp: Float?,
    val alcoholWarmth: Float?,
    val mouthfeelSweetness: Float?
)

data class JsonYeastRaw(
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
    private fun String.normalizeIpa(): String =
        this.replace(Regex("\\bipa\\b", RegexOption.IGNORE_CASE), "IPA")

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
                name = (raw.name ?: "").normalizeIpa(),
                category = (raw.category ?: "").normalizeIpa(),
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
                name = (raw.name ?: "").normalizeIpa(),
                category = (raw.category ?: "").normalizeIpa(),
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

        val ciderDao = database.ciderStyleDao()
        val ciderJson = runCatching {
            context.assets.open("cider_styles.json").bufferedReader().use { it.readText() }
        }.getOrNull()

        if (!ciderJson.isNullOrBlank()) {
            val ciderType = object : TypeToken<List<JsonMeadStyleRaw>>() {}.type
            val rawCiders: List<JsonMeadStyleRaw> = gson.fromJson(ciderJson, ciderType)

            val ciderEntities = rawCiders.map { raw ->
                val id = raw.id ?: raw.number ?: "unknown"
                CiderStyleEntity(
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
            ciderDao.insertAll(ciderEntities)
        }

        val honeyDao = database.honeyDao()
        val honeyCrossRefDao = database.honeyStyleCrossRefDao()
        val honeyJson = runCatching {
            context.assets.open("honeys.json").bufferedReader().use { it.readText() }
        }.getOrNull()

        if (!honeyJson.isNullOrBlank()) {
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
                    val guidelineType = when {
                        styleId.startsWith("M") -> "BJCP_MEAD"
                        styleId.startsWith("C") -> "BJCP_CIDER"
                        else -> "BJCP_2021"
                    }
                    HoneyStyleCrossRef(
                        honeyId = honeyId,
                        styleId = styleId,
                        guidelineType = guidelineType
                    )
                }
            }
            if (honeyCrossRefs.isNotEmpty()) {
                honeyCrossRefDao.insertAll(honeyCrossRefs)
            }
        }

        val pomeDao = database.pomeDao()
        val pomeCrossRefDao = database.pomeStyleCrossRefDao()
        val pomeJson = runCatching {
            context.assets.open("pomes.json").bufferedReader().use { it.readText() }
        }.getOrNull()

        if (!pomeJson.isNullOrBlank()) {
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
                    val guidelineType = when {
                        styleId.startsWith("C") -> "BJCP_CIDER"
                        styleId.startsWith("M") -> "BJCP_MEAD"
                        else -> "BJCP_2021"
                    }
                    PomeStyleCrossRef(
                        pomeId = pomeId,
                        styleId = styleId,
                        guidelineType = guidelineType
                    )
                }
            }
            if (pomeCrossRefs.isNotEmpty()) {
                pomeCrossRefDao.insertAll(pomeCrossRefs)
            }
        }

        val yeastDao = database.yeastDao()
        val yeastCrossRefDao = database.yeastStyleCrossRefDao()
        val yeastJson = runCatching {
            context.assets.open("yeasts.json").bufferedReader().use { it.readText() }
        }.getOrNull()

        if (!yeastJson.isNullOrBlank()) {
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
                    val guidelineType = when {
                        styleId.startsWith("C") -> "BJCP_CIDER"
                        styleId.startsWith("M") -> "BJCP_MEAD"
                        else -> "BJCP_2021"
                    }
                    YeastStyleCrossRef(
                        yeastId = yeastId,
                        styleId = styleId,
                        guidelineType = guidelineType
                    )
                }
            }
            if (yeastCrossRefs.isNotEmpty()) {
                yeastCrossRefDao.insertAll(yeastCrossRefs)
            }
        }
    }
}
