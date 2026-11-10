package com.bc.beerworks.data.model

enum class GuidelineType(val displayName: String) {
    BJCP_2021("BJCP 2021"),
    BA_2026("Brewers Association 2026"),
    BJCP_MEAD("BJCP Mead")
}

val GuidelineType.discipline: String
    get() = when (this) {
        GuidelineType.BJCP_2021, GuidelineType.BA_2026 -> "Beer"
        GuidelineType.BJCP_MEAD -> "Mead"
    }

val GuidelineType.isMead: Boolean
    get() = this == GuidelineType.BJCP_MEAD
