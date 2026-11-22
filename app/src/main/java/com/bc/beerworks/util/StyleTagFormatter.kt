package com.bc.beerworks.util

import androidx.compose.runtime.Immutable

@Immutable
data class StyleDescriptorChip(
    val key: String,
    val label: String
)

private val tagAliasMap = mapOf(
    "northamerica" to "north-america",
    "north america" to "north-america",
    "britishisles" to "british-isles",
    "centraleurope" to "central-europe",
    "westerneurope" to "western-europe",
    "western europe" to "western-europe",
    "any fermentation" to "any-fermentation",
    "bottom fermented" to "bottom-fermented",
    "bockfamily" to "bock-family",
    "dark-lagerfamily" to "dark-lager-family",
    "specialtybeer" to "specialty-beer",
    "wild-fermented" to "wild-fermentation"
)

private val tagPriorityGroups: List<Set<String>> = listOf(
    setOf("top-fermented", "bottom-fermented", "any-fermentation", "wild-fermentation", "hybrid-style"),
    setOf("lagered", "aged", "wood", "smoke", "smoky", "sour", "tart", "sweet", "dry", "semi-sweet", "sparkling", "keeving", "cryo-extraction"),
    setOf("session-strength", "standard-strength", "high-strength", "very-high-strength", "high-alcohol", "strong"),
    setOf("pale-color", "amber-color", "dark-color"),
    setOf("balanced", "malty", "hoppy", "bitter", "roasty", "wheat", "fruit", "fruity", "spice", "spiced", "herb", "herbal", "botanical", "floral", "crisp", "clean", "refreshing", "rich", "complex", "vinous", "tannic", "delicate", "vibrant", "rustic", "warming", "concentrated", "dessert", "luxury", "colorful", "aromatic", "full-bodied", "dry-hopped"),
    setOf("north-america", "central-europe", "western-europe", "eastern-europe", "british-isles", "irish", "belgian-french", "pacific", "english", "french", "new-england", "new-world", "polish-mead")
)

/**
 * Normalizes a raw style tag into a canonical kebab-case key for consistent matching and display.
 */
fun normalizeStyleTagKey(rawTag: String): String {
    val cleaned = rawTag.trim().lowercase()
    val aliased = tagAliasMap[cleaned] ?: cleaned
    return aliased.replace(Regex("\\s+"), "-")
}

private fun tagSortPriority(normalizedTag: String): Int {
    val index = tagPriorityGroups.indexOfFirst { normalizedTag in it }
    return if (index >= 0) index else tagPriorityGroups.size
}

/**
 * Converts a normalized style tag key into a human-readable display label.
 */
fun formatStyleTagLabel(normalizedKey: String): String {
    return normalizedKey
        .split("-")
        .filter { it.isNotEmpty() }
        .joinToString(" ") { word ->
            if (word == "ipa") "IPA"
            else word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}

/**
 * Normalizes, deduplicates, sorts, and formats raw style tags into [StyleDescriptorChip] items.
 */
fun formatStyleDescriptorChips(tags: List<String>): List<StyleDescriptorChip> {
    return tags
        .map(::normalizeStyleTagKey)
        .filter { it.isNotEmpty() }
        .distinct()
        .sortedBy(::tagSortPriority)
        .map { key -> StyleDescriptorChip(key = key, label = formatStyleTagLabel(key)) }
}
