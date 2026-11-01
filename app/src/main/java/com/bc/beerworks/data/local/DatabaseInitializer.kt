package com.bc.beerworks.data.local

import android.content.Context
import com.bc.beerworks.data.local.entity.BaStyleEntity
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    }
}
