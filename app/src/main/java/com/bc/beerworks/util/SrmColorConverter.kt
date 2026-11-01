package com.bc.beerworks.util

import androidx.compose.ui.graphics.Color

object SrmColorConverter {
    fun srmToHex(srm: Double?): String {
        if (srm == null || srm <= 0.0) return "#FFE699"
        val value = srm.coerceIn(1.0, 40.0)
        return when {
            value <= 1.5 -> "#FFE699"
            value <= 2.5 -> "#FFD878"
            value <= 3.5 -> "#FFCA5A"
            value <= 4.5 -> "#FFBF42"
            value <= 5.5 -> "#FBB123"
            value <= 6.5 -> "#F8A600"
            value <= 7.5 -> "#F39C00"
            value <= 8.5 -> "#EA8F00"
            value <= 9.5 -> "#E58500"
            value <= 11.0 -> "#DE7C00"
            value <= 13.0 -> "#CF6900"
            value <= 15.0 -> "#B84E00"
            value <= 18.0 -> "#A13300"
            value <= 22.0 -> "#881B00"
            value <= 30.0 -> "#5C0000"
            value <= 38.0 -> "#2B0000"
            else -> "#000000"
        }
    }

    fun srmToColor(srm: Double?): Color {
        val hex = srmToHex(srm).removePrefix("#")
        val argb = 0xFF000000.toInt() or hex.toInt(16)
        return Color(argb)
    }

    fun getSrmDescription(srm: Double?): String {
        if (srm == null || srm <= 0.0) return "Unknown"
        return when {
            srm <= 2.0 -> "Very Light / Pale Straw"
            srm <= 4.0 -> "Straw to Pale Gold"
            srm <= 6.0 -> "Gold"
            srm <= 9.0 -> "Amber / Light Copper"
            srm <= 14.0 -> "Copper / Garnet"
            srm <= 18.0 -> "Dark Copper / Light Brown"
            srm <= 24.0 -> "Brown / Chestnut"
            srm <= 35.0 -> "Dark Brown"
            else -> "Very Dark / Black"
        }
    }
}
