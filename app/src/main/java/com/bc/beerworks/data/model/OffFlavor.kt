package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class OffFlavor(
    override val id: String,
    override val name: String,
    val emoji: String,
    val category: String, // "Fermentation & Yeast", "Mashing & Boil", "Sanitation & Microbial", "Oxidation & Aging", "Water & Packaging"
    val chemicalCompound: String,
    val perceptionThreshold: String,
    val typicalConcentration: String,
    val meilgaardCode: String,
    val detectedIn: List<String> = emptyList(), // "Aroma", "Flavor", "Mouthfeel"
    val disciplines: List<String> = emptyList(), // "Beer", "Mead", "Cider"
    val stylePermissibility: String, // "Always a Fault", "Style-Permissible"
    override val description: String,
    val aromaPerception: String,
    val flavorPerception: String,
    val mouthfeelPerception: String,
    override val descriptors: List<String> = emptyList(),
    val diagnosticContrasts: List<String> = emptyList(),
    val typicalOrigins: List<String> = emptyList(),
    val preventionTips: List<String> = emptyList(),
    val whenAppropriate: String,
    val confusedWithIds: List<String> = emptyList(),
    val permissibleStyleIds: List<String> = emptyList()
) : Ingredient {
    override val origin: String
        get() = perceptionThreshold

    override val countryCode: String
        get() = ""

    override val countryFlag: String
        get() = emoji

    override val substituteIds: List<String>
        get() = confusedWithIds

    val cardSubtitle: String
        get() = "$chemicalCompound • Threshold: $perceptionThreshold"

    override fun matchesQuery(query: String): Boolean =
        matchesBaseQuery(query) ||
                chemicalCompound.contains(query, ignoreCase = true) ||
                category.contains(query, ignoreCase = true) ||
                meilgaardCode.contains(query, ignoreCase = true) ||
                diagnosticContrasts.any { it.contains(query, ignoreCase = true) } ||
                typicalOrigins.any { it.contains(query, ignoreCase = true) } ||
                preventionTips.any { it.contains(query, ignoreCase = true) } ||
                whenAppropriate.contains(query, ignoreCase = true)
}
