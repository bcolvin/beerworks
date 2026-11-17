package com.bc.beerworks.data.local.seeder

import android.content.res.AssetManager
import com.bc.beerworks.data.local.dao.OffFlavorDao
import com.bc.beerworks.data.local.dao.OffFlavorStyleCrossRefDao
import com.bc.beerworks.data.local.entity.OffFlavorEntity
import com.bc.beerworks.data.local.entity.OffFlavorStyleCrossRef
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private data class JsonOffFlavorRaw(
    val id: String?,
    val name: String?,
    val emoji: String?,
    val category: String?,
    val chemicalCompound: String?,
    val perceptionThreshold: String?,
    val typicalConcentration: String?,
    val meilgaardCode: String?,
    val detectedIn: List<String>?,
    val disciplines: List<String>?,
    val stylePermissibility: String?,
    val description: String?,
    val aromaPerception: String?,
    val flavorPerception: String?,
    val mouthfeelPerception: String?,
    val descriptors: List<String>?,
    val diagnosticContrasts: List<String>?,
    val typicalOrigins: List<String>?,
    val preventionTips: List<String>?,
    val whenAppropriate: String?,
    val confusedWithIds: List<String>?,
    val permissibleStyleIds: List<String>?
)

class OffFlavorSeeder(
    private val offFlavorDao: OffFlavorDao,
    private val crossRefDao: OffFlavorStyleCrossRefDao
) : DomainSeeder {

    override suspend fun seedIfNeeded(assets: AssetManager, gson: Gson) {
        if (offFlavorDao.getOffFlavorCount() > 0) return

        val offFlavorJson = runCatching {
            assets.open("off_flavors.json").bufferedReader().use { it.readText() }
        }.getOrNull() ?: return

        if (offFlavorJson.isBlank()) return

        val listType = object : TypeToken<List<JsonOffFlavorRaw>>() {}.type
        val rawItems: List<JsonOffFlavorRaw> = gson.fromJson(offFlavorJson, listType)

        val entities = rawItems.map { raw ->
            OffFlavorEntity(
                id = raw.id ?: "unknown",
                name = raw.name ?: "",
                emoji = raw.emoji ?: "🧪",
                category = raw.category ?: "Fermentation & Yeast",
                chemicalCompound = raw.chemicalCompound ?: "",
                perceptionThreshold = raw.perceptionThreshold ?: "",
                typicalConcentration = raw.typicalConcentration ?: "",
                meilgaardCode = raw.meilgaardCode ?: "",
                detectedIn = raw.detectedIn?.joinToString(",") ?: "",
                disciplines = raw.disciplines?.joinToString(",") ?: "Beer",
                stylePermissibility = raw.stylePermissibility ?: "Always a Fault",
                description = raw.description ?: "",
                aromaPerception = raw.aromaPerception ?: "",
                flavorPerception = raw.flavorPerception ?: "",
                mouthfeelPerception = raw.mouthfeelPerception ?: "",
                descriptors = raw.descriptors?.joinToString(",") ?: "",
                diagnosticContrastsJson = gson.toJson(raw.diagnosticContrasts ?: emptyList<String>()),
                typicalOriginsJson = gson.toJson(raw.typicalOrigins ?: emptyList<String>()),
                preventionTipsJson = gson.toJson(raw.preventionTips ?: emptyList<String>()),
                whenAppropriate = raw.whenAppropriate ?: "",
                confusedWithIds = raw.confusedWithIds?.joinToString(",") ?: ""
            )
        }
        offFlavorDao.insertAll(entities)

        val crossRefs = rawItems.flatMap { raw ->
            val offFlavorId = raw.id ?: return@flatMap emptyList()
            (raw.permissibleStyleIds ?: emptyList()).map { styleId ->
                OffFlavorStyleCrossRef(
                    offFlavorId = offFlavorId,
                    styleId = styleId,
                    guidelineType = StyleGuidelineResolver.inferGuideline(styleId)
                )
            }
        }
        if (crossRefs.isNotEmpty()) {
            crossRefDao.insertAll(crossRefs)
        }
    }
}
