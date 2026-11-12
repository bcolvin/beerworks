package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class BaseStyleGroup(
    val categoryNumber: String,
    val categoryName: String,
    val styles: List<BeerStyle>,
    val guidelineType: GuidelineType
) {
    val displayCategoryName: String
        get() = categoryName.replace(Regex("\\bipa\\b", RegexOption.IGNORE_CASE), "IPA")

    val displayTitle: String
        get() = if (categoryNumber.isNotBlank()) "$categoryNumber. $displayCategoryName" else displayCategoryName

    val abvMin: Double? get() = styles.mapNotNull { it.abvMin }.minOrNull()
    val abvMax: Double? get() = styles.mapNotNull { it.abvMax }.maxOrNull()
    val ibuMin: Double? get() = styles.mapNotNull { it.ibuMin }.minOrNull()
    val ibuMax: Double? get() = styles.mapNotNull { it.ibuMax }.maxOrNull()
    val srmMin: Double? get() = styles.mapNotNull { it.srmMin }.minOrNull()
    val srmMax: Double? get() = styles.mapNotNull { it.srmMax }.maxOrNull()
    val fgMin: Double? get() = styles.mapNotNull { it.fgMin }.minOrNull()
    val fgMax: Double? get() = styles.mapNotNull { it.fgMax }.maxOrNull()
}
