package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
interface Ingredient {
    val id: String
    val name: String
    val origin: String
    val countryCode: String
    val description: String
    val descriptors: List<String>
    val substituteIds: List<String>

    val countryFlag: String
        get() {
            if (countryCode.length != 2) return "🌍"
            val upper = countryCode.uppercase()
            val firstChar = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6
            val secondChar = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6
            return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
        }

    fun matchesBaseQuery(query: String): Boolean =
        name.contains(query, ignoreCase = true) ||
                origin.contains(query, ignoreCase = true) ||
                description.contains(query, ignoreCase = true) ||
                descriptors.any { it.contains(query, ignoreCase = true) }

    fun matchesQuery(query: String): Boolean = matchesBaseQuery(query)
}
