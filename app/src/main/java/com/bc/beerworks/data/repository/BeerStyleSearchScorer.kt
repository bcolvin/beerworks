package com.bc.beerworks.data.repository

import com.bc.beerworks.data.model.BeerStyle

const val WEIGHT_STYLE_NAME = 10
const val WEIGHT_IMPRESSION_OR_COMMERCIAL = 5
const val WEIGHT_OTHER_FIELDS = 1

fun calculateStyleSearchScore(style: BeerStyle, query: String): Int {
    val q = query.trim()
    if (q.isBlank()) return 0

    var score = 0

    // Style name & style number: Weight 10
    val matchesName = style.name.contains(q, ignoreCase = true) ||
            style.name.split(" ", "-", "/").any { it.startsWith(q, ignoreCase = true) }
    val matchesNumber = style.number.equals(q, ignoreCase = true) ||
            style.number.startsWith(q, ignoreCase = true)

    if (matchesName || matchesNumber) {
        score += WEIGHT_STYLE_NAME
        // Exact name or exact number match bonus
        if (style.name.equals(q, ignoreCase = true) || style.number.equals(q, ignoreCase = true)) {
            score += 5
        }
    }

    // Overall impression: Weight 5
    if (style.overallImpression.contains(q, ignoreCase = true)) {
        score += WEIGHT_IMPRESSION_OR_COMMERCIAL
    }

    // Commercial examples: Weight 5
    if (style.commercialExamples.contains(q, ignoreCase = true)) {
        score += WEIGHT_IMPRESSION_OR_COMMERCIAL
    }

    // Everything else: Weight 1
    val otherFields = listOf(
        style.category,
        style.characteristicIngredients,
        style.aroma,
        style.appearance,
        style.flavor,
        style.mouthfeel,
        style.comments,
        style.history,
        style.styleComparison,
        style.tags.joinToString(" ")
    )

    if (otherFields.any { it.contains(q, ignoreCase = true) }) {
        score += WEIGHT_OTHER_FIELDS
    }

    // For multi-word queries, also reward token matches
    val tokens = q.split("\\s+".toRegex()).filter { it.length >= 2 && !it.equals(q, ignoreCase = true) }
    for (token in tokens) {
        if (style.name.contains(token, ignoreCase = true) || style.number.equals(token, ignoreCase = true)) {
            score += WEIGHT_STYLE_NAME
        }
        if (style.overallImpression.contains(token, ignoreCase = true)) {
            score += WEIGHT_IMPRESSION_OR_COMMERCIAL
        }
        if (style.commercialExamples.contains(token, ignoreCase = true)) {
            score += WEIGHT_IMPRESSION_OR_COMMERCIAL
        }
        if (otherFields.any { it.contains(token, ignoreCase = true) }) {
            score += WEIGHT_OTHER_FIELDS
        }
    }

    return score
}
