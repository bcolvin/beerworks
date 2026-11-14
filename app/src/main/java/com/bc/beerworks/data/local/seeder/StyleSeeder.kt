package com.bc.beerworks.data.local.seeder

import android.content.res.AssetManager
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.BaStyleEntity
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.CiderStyleEntity
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import com.bc.beerworks.data.local.entity.normalizeIpa
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first

private data class JsonStyleRaw(
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

private data class JsonMeadStyleRaw(
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

class StyleSeeder(
    private val bjcpDao: BjcpStyleDao,
    private val baDao: BaStyleDao,
    private val meadDao: MeadStyleDao,
    private val ciderDao: CiderStyleDao
) : DomainSeeder {

    override suspend fun seedIfNeeded(assets: AssetManager, gson: Gson) {
        val styleListType = object : TypeToken<List<JsonStyleRaw>>() {}.type
        val meadListType = object : TypeToken<List<JsonMeadStyleRaw>>() {}.type

        if (bjcpDao.getAllStyles().first().isEmpty()) {
            val bjcpJson = runCatching {
                assets.open("bjcp_styles.json").bufferedReader().use { it.readText() }
            }.getOrNull()
            if (!bjcpJson.isNullOrBlank()) {
                val rawBjcp: List<JsonStyleRaw> = gson.fromJson(bjcpJson, styleListType)
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
            }
        }

        if (baDao.getAllStyles().first().isEmpty()) {
            val baJson = runCatching {
                assets.open("ba_styles.json").bufferedReader().use { it.readText() }
            }.getOrNull()
            if (!baJson.isNullOrBlank()) {
                val rawBa: List<JsonStyleRaw> = gson.fromJson(baJson, styleListType)
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
            }
        }

        if (meadDao.getAllStyles().first().isEmpty()) {
            val meadJson = runCatching {
                assets.open("mead_styles.json").bufferedReader().use { it.readText() }
            }.getOrNull()
            if (!meadJson.isNullOrBlank()) {
                val rawMeads: List<JsonMeadStyleRaw> = gson.fromJson(meadJson, meadListType)
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
            }
        }

        if (ciderDao.getAllStyles().first().isEmpty()) {
            val ciderJson = runCatching {
                assets.open("cider_styles.json").bufferedReader().use { it.readText() }
            }.getOrNull()
            if (!ciderJson.isNullOrBlank()) {
                val rawCiders: List<JsonMeadStyleRaw> = gson.fromJson(ciderJson, meadListType)
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
        }
    }
}
