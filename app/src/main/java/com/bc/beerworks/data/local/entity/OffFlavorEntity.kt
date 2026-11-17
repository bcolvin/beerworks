package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.bc.beerworks.data.model.OffFlavor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "off_flavors")
data class OffFlavorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val emoji: String,
    val category: String,
    val chemicalCompound: String,
    val perceptionThreshold: String,
    val typicalConcentration: String,
    val meilgaardCode: String,
    val detectedIn: String, // comma-separated
    val disciplines: String, // comma-separated
    val stylePermissibility: String,
    val description: String,
    val aromaPerception: String,
    val flavorPerception: String,
    val mouthfeelPerception: String,
    val descriptors: String, // comma-separated
    val diagnosticContrastsJson: String, // JSON array of strings
    val typicalOriginsJson: String, // JSON array of strings
    val preventionTipsJson: String, // JSON array of strings
    val whenAppropriate: String,
    val confusedWithIds: String // comma-separated
) {
    fun toDomainModel(): OffFlavor {
        return OffFlavor(
            id = id,
            name = name,
            emoji = emoji,
            category = category,
            chemicalCompound = chemicalCompound,
            perceptionThreshold = perceptionThreshold,
            typicalConcentration = typicalConcentration,
            meilgaardCode = meilgaardCode,
            detectedIn = detectedIn.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            disciplines = disciplines.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            stylePermissibility = stylePermissibility,
            description = description,
            aromaPerception = aromaPerception,
            flavorPerception = flavorPerception,
            mouthfeelPerception = mouthfeelPerception,
            descriptors = descriptors.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            diagnosticContrasts = parseStringList(diagnosticContrastsJson),
            typicalOrigins = parseStringList(typicalOriginsJson),
            preventionTips = parseStringList(preventionTipsJson),
            whenAppropriate = whenAppropriate,
            confusedWithIds = confusedWithIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        )
    }

    private fun parseStringList(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, listType)
        }.getOrDefault(emptyList())
    }

    companion object {
        private val gson = Gson()
    }
}

@Fts4(contentEntity = OffFlavorEntity::class)
@Entity(tableName = "off_flavors_fts")
data class OffFlavorFtsEntity(
    val name: String,
    val chemicalCompound: String,
    val category: String,
    val meilgaardCode: String,
    val description: String,
    val descriptors: String,
    val diagnosticContrastsJson: String,
    val typicalOriginsJson: String,
    val preventionTipsJson: String,
    val whenAppropriate: String
)
