package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable
import java.util.Locale

@Immutable
data class PomeVariety(
    override val id: String,
    override val name: String,
    val fruitType: String, // "Apple", "Pear"
    val classification: String, // "Bittersweet", "Bittersharp", "Sharp", "Sweet"
    val fruitUse: String, // "Heritage Cider/Perry", "Culinary / Multi-Purpose"
    val botanicalName: String, // "Malus domestica" or "Pyrus communis"
    override val origin: String,
    override val countryCode: String,
    val acidityMalicPercent: Double,
    val tanninPolyphenolPercent: Double,
    val tanninLevel: String, // "Low", "Medium", "High"
    val sgMin: Double,
    val sgMax: Double,
    val harvestSeason: String, // "Early", "Mid", "Late"
    val sorbitolGramsPerLiter: Double? = null,
    override val description: String,
    val blendingGuidance: String,
    val pressingCharacteristics: String,
    override val descriptors: List<String> = emptyList(),
    override val substituteIds: List<String> = emptyList(),
    val commonStyleIds: List<String> = emptyList(),
    val radarProfile: PomeSensoryProfile? = null
) : Ingredient {
    override val countryFlag: String
        get() {
            if (countryCode.length != 2) return "🌍"
            val upper = countryCode.uppercase()
            val firstChar = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6
            val secondChar = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6
            return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
        }

    val sgRangeText: String
        get() {
            return if (sgMin == sgMax) {
                String.format(Locale.US, "%.3f SG", sgMin)
            } else {
                String.format(Locale.US, "%.3f – %.3f SG", sgMin, sgMax)
            }
        }

    val acidityText: String
        get() = String.format(Locale.US, "%.2f%%", acidityMalicPercent)

    val tanninText: String
        get() = String.format(Locale.US, "%.2f%% (%s)", tanninPolyphenolPercent, tanninLevel)
}
