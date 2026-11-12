package com.bc.beerworks.data.model

enum class GuidelineType(val displayName: String) {
    BJCP_2021("BJCP 2021"),
    BA_2026("Brewers Association 2026"),
    BJCP_MEAD("BJCP Mead"),
    BJCP_CIDER("BJCP Cider")
}

val GuidelineType.discipline: String
    get() = when (this) {
        GuidelineType.BJCP_2021, GuidelineType.BA_2026 -> "Beer"
        GuidelineType.BJCP_MEAD -> "Mead"
        GuidelineType.BJCP_CIDER -> "Cider"
    }

val GuidelineType.isMead: Boolean
    get() = this == GuidelineType.BJCP_MEAD

val GuidelineType.isCider: Boolean
    get() = this == GuidelineType.BJCP_CIDER

val GuidelineType.isBeer: Boolean
    get() = this == GuidelineType.BJCP_2021 || this == GuidelineType.BA_2026

