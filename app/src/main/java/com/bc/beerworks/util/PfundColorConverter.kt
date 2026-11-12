package com.bc.beerworks.util

import androidx.compose.ui.graphics.Color

object PfundColorConverter {

    fun pfundToUsdaClassification(pfund: Double): String {
        return when {
            pfund <= 8.0 -> "Water White"
            pfund <= 17.0 -> "Extra White"
            pfund <= 34.0 -> "White"
            pfund <= 50.0 -> "Extra Light Amber"
            pfund <= 85.0 -> "Light Amber"
            pfund <= 114.0 -> "Amber"
            else -> "Dark Amber"
        }
    }

    fun pfundToHex(pfund: Double): String {
        return when {
            pfund <= 8.0 -> "#FFFDE7"
            pfund <= 17.0 -> "#FFF9C4"
            pfund <= 34.0 -> "#FFF176"
            pfund <= 50.0 -> "#FFD54F"
            pfund <= 85.0 -> "#FFB300"
            pfund <= 114.0 -> "#FB8C00"
            else -> "#4E2616"
        }
    }

    fun pfundToColor(pfund: Double): Color {
        val hex = pfundToHex(pfund).removePrefix("#")
        val argb = 0xFF000000.toInt() or hex.toInt(16)
        return Color(argb)
    }

    fun usdaClassificationToColor(usda: String): Color {
        val hex = when (usda.trim().lowercase()) {
            "water white" -> "#FFFDE7"
            "extra white" -> "#FFF9C4"
            "white" -> "#FFF176"
            "extra light amber" -> "#FFD54F"
            "light amber" -> "#FFB300"
            "amber" -> "#FB8C00"
            "dark amber" -> "#4E2616"
            else -> "#FFD54F"
        }.removePrefix("#")
        val argb = 0xFF000000.toInt() or hex.toInt(16)
        return Color(argb)
    }
}
