package com.bc.beerworks.util

import com.bc.beerworks.data.model.BeerStyle
import kotlin.math.roundToInt

private const val MISSING_VITAL_STAT = "—"

fun formatDecimalRange(
    min: Double?,
    max: Double?,
    decimals: Int,
    suffix: String = "",
    emptyText: String = MISSING_VITAL_STAT
): String {
    val pattern = "%.${decimals}f"
    return when {
        min != null && max != null -> "${pattern.format(min)}–${pattern.format(max)}$suffix"
        min != null -> "${pattern.format(min)}$suffix"
        else -> emptyText
    }
}

fun formatWholeNumberRange(
    min: Double?,
    max: Double?,
    suffix: String = "",
    emptyText: String = MISSING_VITAL_STAT
): String = when {
    min != null && max != null -> "${min.roundToInt()}–${max.roundToInt()}$suffix"
    min != null -> "${min.roundToInt()}$suffix"
    else -> emptyText
}

fun BeerStyle.formattedOgRange(): String = formatDecimalRange(ogMin, ogMax, decimals = 3)

fun BeerStyle.formattedFgRange(): String = formatDecimalRange(fgMin, fgMax, decimals = 3)

fun BeerStyle.formattedIbuRange(suffix: String = ""): String = formatWholeNumberRange(ibuMin, ibuMax, suffix = suffix)

fun BeerStyle.formattedSrmRange(suffix: String = ""): String = formatWholeNumberRange(srmMin, srmMax, suffix = suffix)

fun BeerStyle.formattedAbvRange(suffix: String = "%"): String = formatDecimalRange(abvMin, abvMax, decimals = 1, suffix = suffix)
