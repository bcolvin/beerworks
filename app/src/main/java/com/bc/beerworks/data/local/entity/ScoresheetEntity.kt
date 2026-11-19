package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bc.beerworks.data.model.BeerScoresheet
import com.bc.beerworks.data.model.IntensityLevel
import com.bc.beerworks.data.model.MeadScoresheet
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
    val updatedAtMillis: Long,
    val declaredCarbonation: String = "",
    val declaredSweetness: String = "",
    val declaredStrength: String = "",
    val specialIngredients: String = ""
) {
    fun toDomainModel(): Scoresheet {
        val resolvedDiscipline = ScoresheetDiscipline.fromStorageValue(formType)
        val decodedDescriptors = decodeStringMap(descriptorValues)
        val decodedOffFlavors = decodeIntensityMap(offFlavorLevels)

        return when (resolvedDiscipline) {
            ScoresheetDiscipline.BJCP_MEAD -> MeadScoresheet(
                id = id,
                entryTitle = entryTitle,
                brewer = brewer,
                styleId = styleId,
                styleName = styleName,
                styleNumber = styleNumber,
                evaluatedAtMillis = evaluatedAtMillis,
                aromaScore = aromaScore,
                appearanceScore = appearanceScore,
                flavorScore = flavorScore,
                overallScore = overallScore,
                aromaNotes = aromaNotes,
                appearanceNotes = appearanceNotes,
                flavorNotes = flavorNotes,
                overallNotes = overallNotes,
                declaredCarbonation = declaredCarbonation.takeIf { it.isNotBlank() },
                declaredSweetness = declaredSweetness.takeIf { it.isNotBlank() },
                declaredStrength = declaredStrength.takeIf { it.isNotBlank() },
                specialIngredients = specialIngredients,
                bottleInspectionOk = bottleInspectionOk,
                bottleInspectionNotes = bottleInspectionNotes,
                stylisticAccuracy = stylisticAccuracy,
                technicalMerit = technicalMerit,
                intangibles = intangibles,
                descriptorValues = decodedDescriptors,
                offFlavorLevels = decodedOffFlavors,
                updatedAtMillis = updatedAtMillis
            )
            ScoresheetDiscipline.BJCP_BEER,
            ScoresheetDiscipline.BJCP_CIDER -> BeerScoresheet(
                id = id,
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
                descriptorValues = decodedDescriptors,
                offFlavorLevels = decodedOffFlavors,
                updatedAtMillis = updatedAtMillis
            )
        }
    }
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
    mouthfeelScore = if (this is BeerScoresheet) mouthfeelScore else 0,
    overallScore = overallScore,
    aromaNotes = aromaNotes,
    appearanceNotes = appearanceNotes,
    flavorNotes = flavorNotes,
    mouthfeelNotes = if (this is BeerScoresheet) mouthfeelNotes else "",
    overallNotes = overallNotes,
    bottleInspectionOk = bottleInspectionOk,
    bottleInspectionNotes = bottleInspectionNotes,
    stylisticAccuracy = stylisticAccuracy,
    technicalMerit = technicalMerit,
    intangibles = intangibles,
    descriptorValues = encodeStringMap(descriptorValues),
    offFlavorLevels = encodeIntensityMap(offFlavorLevels),
    updatedAtMillis = updatedAtMillis,
    declaredCarbonation = if (this is MeadScoresheet) declaredCarbonation.orEmpty() else "",
    declaredSweetness = if (this is MeadScoresheet) declaredSweetness.orEmpty() else "",
    declaredStrength = if (this is MeadScoresheet) declaredStrength.orEmpty() else "",
    specialIngredients = if (this is MeadScoresheet) specialIngredients else ""
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
