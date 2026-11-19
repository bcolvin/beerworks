package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bc.beerworks.data.model.IntensityLevel
import com.bc.beerworks.data.model.Scoresheet
import com.bc.beerworks.data.model.ScoresheetDiscipline

@Entity(tableName = "scoresheets")
data class ScoresheetEntity(
    @PrimaryKey val id: String,
    val formType: String,
    val entryTitle: String,
    val brewer: String,
    val styleId: String?,
    val styleName: String,
    val styleNumber: String,
    val evaluatedAtMillis: Long,
    val aromaScore: Int,
    val appearanceScore: Int,
    val flavorScore: Int,
    val mouthfeelScore: Int,
    val overallScore: Int,
    val aromaNotes: String,
    val appearanceNotes: String,
    val flavorNotes: String,
    val mouthfeelNotes: String,
    val overallNotes: String,
    val bottleInspectionOk: Boolean,
    val bottleInspectionNotes: String,
    val stylisticAccuracy: Int,
    val technicalMerit: Int,
    val intangibles: Int,
    val descriptorValues: String,
    val offFlavorLevels: String,
    val updatedAtMillis: Long
) {
    fun toDomainModel(): Scoresheet = Scoresheet(
        id = id,
        discipline = ScoresheetDiscipline.fromStorageValue(formType),
        entryTitle = entryTitle,
        brewer = brewer,
        styleId = styleId,
        styleName = styleName,
        styleNumber = styleNumber,
        evaluatedAtMillis = evaluatedAtMillis,
        aromaScore = aromaScore,
        appearanceScore = appearanceScore,
        flavorScore = flavorScore,
        mouthfeelScore = mouthfeelScore,
        overallScore = overallScore,
        aromaNotes = aromaNotes,
        appearanceNotes = appearanceNotes,
        flavorNotes = flavorNotes,
        mouthfeelNotes = mouthfeelNotes,
        overallNotes = overallNotes,
        bottleInspectionOk = bottleInspectionOk,
        bottleInspectionNotes = bottleInspectionNotes,
        stylisticAccuracy = stylisticAccuracy,
        technicalMerit = technicalMerit,
        intangibles = intangibles,
        descriptorValues = decodeStringMap(descriptorValues),
        offFlavorLevels = decodeIntensityMap(offFlavorLevels),
        updatedAtMillis = updatedAtMillis
    )
}

fun Scoresheet.toEntity(): ScoresheetEntity = ScoresheetEntity(
    id = id,
    formType = discipline.name,
    entryTitle = entryTitle,
    brewer = brewer,
    styleId = styleId,
    styleName = styleName,
    styleNumber = styleNumber,
    evaluatedAtMillis = evaluatedAtMillis,
    aromaScore = aromaScore,
    appearanceScore = appearanceScore,
    flavorScore = flavorScore,
    mouthfeelScore = mouthfeelScore,
    overallScore = overallScore,
    aromaNotes = aromaNotes,
    appearanceNotes = appearanceNotes,
    flavorNotes = flavorNotes,
    mouthfeelNotes = mouthfeelNotes,
    overallNotes = overallNotes,
    bottleInspectionOk = bottleInspectionOk,
    bottleInspectionNotes = bottleInspectionNotes,
    stylisticAccuracy = stylisticAccuracy,
    technicalMerit = technicalMerit,
    intangibles = intangibles,
    descriptorValues = encodeStringMap(descriptorValues),
    offFlavorLevels = encodeIntensityMap(offFlavorLevels),
    updatedAtMillis = updatedAtMillis
)

internal fun encodeStringMap(map: Map<String, String>): String =
    map.entries.joinToString(";") { (k, v) -> "$k=$v" }

internal fun decodeStringMap(raw: String): Map<String, String> {
    if (raw.isBlank()) return emptyMap()
    return raw.split(";")
        .mapNotNull { entry ->
            val parts = entry.split("=", limit = 2)
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                parts[0] to parts[1]
            } else {
                null
            }
        }
        .toMap()
}

internal fun encodeIntensityMap(map: Map<String, IntensityLevel>): String =
    map.entries.joinToString(";") { (k, v) -> "$k=${v.name}" }

internal fun decodeIntensityMap(raw: String): Map<String, IntensityLevel> {
    if (raw.isBlank()) return emptyMap()
    return raw.split(";")
        .mapNotNull { entry ->
            val parts = entry.split("=", limit = 2)
            if (parts.size == 2) {
                val level = runCatching { IntensityLevel.valueOf(parts[1]) }.getOrNull()
                if (level != null) parts[0] to level else null
            } else {
                null
            }
        }
        .toMap()
}
