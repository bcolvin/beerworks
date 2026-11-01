package com.bc.beerworks.data.model

data class BaseStyleGroup(
    val categoryNumber: String,
    val categoryName: String,
    val styles: List<BeerStyle>,
    val guidelineType: GuidelineType
) {
    val displayTitle: String
        get() = if (categoryNumber.isNotBlank()) "$categoryNumber. $categoryName" else categoryName

    val abvMin: Double? get() = styles.mapNotNull { it.abvMin }.minOrNull()
    val abvMax: Double? get() = styles.mapNotNull { it.abvMax }.maxOrNull()
    val ibuMin: Double? get() = styles.mapNotNull { it.ibuMin }.minOrNull()
    val ibuMax: Double? get() = styles.mapNotNull { it.ibuMax }.maxOrNull()
    val srmMin: Double? get() = styles.mapNotNull { it.srmMin }.minOrNull()
    val srmMax: Double? get() = styles.mapNotNull { it.srmMax }.maxOrNull()
}
