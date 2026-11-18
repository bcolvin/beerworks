package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Immutable
enum class ScoresheetFormType(val displayName: String) {
    SHORT_FORM("Short-Form"),
    LONG_FORM("Long-Form")
}

@Immutable
enum class IntensityLevel(val label: String, val shortLabel: String) {
    LOW("Low", "Low"),
    MEDIUM("Medium", "Med"),
    HIGH("High", "High");

    companion object {
        fun cycle(current: IntensityLevel?): IntensityLevel? = when (current) {
            null -> LOW
            LOW -> MEDIUM
            MEDIUM -> HIGH
            HIGH -> null
        }
    }
}

@Immutable
enum class BjcpScoringTier(val label: String, val rangeText: String, val description: String) {
    OUTSTANDING("Outstanding", "45–50", "World-class example of style."),
    EXCELLENT("Excellent", "38–44", "Exemplifies style well, requires minor fine-tuning."),
    VERY_GOOD("Very Good", "30–37", "Generally within style parameters, some minor flaws."),
    GOOD("Good", "21–29", "Misses the mark on style and/or minor flaws."),
    FAIR("Fair", "14–20", "Off flavors, aromas, or major style deficiencies."),
    PROBLEMATIC("Problematic", "0–13", "Major off flavors and aromas dominate. Hard to drink.");

    companion object {
        fun fromTotalScore(score: Int): BjcpScoringTier = when {
            score >= 45 -> OUTSTANDING
            score >= 38 -> EXCELLENT
            score >= 30 -> VERY_GOOD
            score >= 21 -> GOOD
            score >= 14 -> FAIR
            else -> PROBLEMATIC
        }
    }
}

@Immutable
enum class ScoresheetSection(val title: String) {
    AROMA("Aroma"),
    APPEARANCE("Appearance"),
    FLAVOR("Flavor"),
    MOUTHFEEL("Mouthfeel")
}

private val LOW_MED_HIGH = listOf("Low", "Med", "High")

@Immutable
enum class ScoresheetDescriptor(
    val key: String,
    val label: String,
    val section: ScoresheetSection,
    val options: List<String> = LOW_MED_HIGH
) {
    // Aroma
    AROMA_MALT("aroma_malt", "Malt", ScoresheetSection.AROMA),
    AROMA_HOPS("aroma_hops", "Hops", ScoresheetSection.AROMA),
    AROMA_FERMENTATION("aroma_fermentation", "Fermentation Character", ScoresheetSection.AROMA),

    // Appearance
    APPEARANCE_COLOR(
        "appearance_color",
        "Color",
        ScoresheetSection.APPEARANCE,
        listOf("Yellow", "Gold", "Amber", "Copper", "Brown", "Black")
    ),
    APPEARANCE_CLARITY(
        "appearance_clarity",
        "Clarity",
        ScoresheetSection.APPEARANCE,
        listOf("Opaque", "Hazy", "Brilliant")
    ),
    APPEARANCE_HEAD_SIZE(
        "appearance_head_size",
        "Head Size",
        ScoresheetSection.APPEARANCE,
        listOf("None", "Small", "Med", "Large")
    ),
    APPEARANCE_HEAD_RETENTION(
        "appearance_head_retention",
        "Head Retention",
        ScoresheetSection.APPEARANCE,
        listOf("Quick", "Moderate", "Long Lasting")
    ),
    APPEARANCE_HEAD_COLOR(
        "appearance_head_color",
        "Head Color",
        ScoresheetSection.APPEARANCE,
        listOf("White", "Ivory", "Beige", "Tan")
    ),

    // Flavor
    FLAVOR_MALT("flavor_malt", "Malt", ScoresheetSection.FLAVOR),
    FLAVOR_HOPS("flavor_hops", "Hops", ScoresheetSection.FLAVOR),
    FLAVOR_BITTERNESS("flavor_bitterness", "Bitterness", ScoresheetSection.FLAVOR),
    FLAVOR_FERMENTATION("flavor_fermentation", "Fermentation Character", ScoresheetSection.FLAVOR),
    FLAVOR_BALANCE(
        "flavor_balance",
        "Balance",
        ScoresheetSection.FLAVOR,
        listOf("Hoppy", "Balanced", "Malty")
    ),
    FLAVOR_FINISH(
        "flavor_finish",
        "Finish / Aftertaste",
        ScoresheetSection.FLAVOR,
        listOf("Dry", "Balanced", "Sweet")
    ),

    // Mouthfeel
    MOUTHFEEL_BODY(
        "mouthfeel_body",
        "Body",
        ScoresheetSection.MOUTHFEEL,
        listOf("Thin", "Med", "Full")
    ),
    MOUTHFEEL_CARBONATION("mouthfeel_carbonation", "Carbonation", ScoresheetSection.MOUTHFEEL),
    MOUTHFEEL_WARMTH("mouthfeel_warmth", "Warmth", ScoresheetSection.MOUTHFEEL),
    MOUTHFEEL_CREAMINESS("mouthfeel_creaminess", "Creaminess", ScoresheetSection.MOUTHFEEL),
    MOUTHFEEL_ASTRINGENCY("mouthfeel_astringency", "Astringency", ScoresheetSection.MOUTHFEEL);

    fun cycle(current: String?): String? {
        if (current == null) return options.firstOrNull()
        val idx = options.indexOf(current)
        return if (idx == -1 || idx + 1 >= options.size) null else options[idx + 1]
    }

    companion object {
        fun forSection(section: ScoresheetSection): List<ScoresheetDescriptor> =
            entries.filter { it.section == section }
    }
}

@Immutable
data class Scoresheet(
    val id: String = UUID.randomUUID().toString(),
    val formType: ScoresheetFormType = ScoresheetFormType.SHORT_FORM,
    val entryTitle: String = "",
    val brewer: String = "",
    val styleId: String? = null,
    val styleName: String = "",
    val styleNumber: String = "",
    val evaluatedAtMillis: Long = System.currentTimeMillis(),
    val aromaScore: Int = 0,
    val appearanceScore: Int = 0,
    val flavorScore: Int = 0,
    val mouthfeelScore: Int = 0,
    val overallScore: Int = 0,
    val aromaNotes: String = "",
    val appearanceNotes: String = "",
    val flavorNotes: String = "",
    val mouthfeelNotes: String = "",
    val overallNotes: String = "",
    val bottleInspectionOk: Boolean = true,
    val bottleInspectionNotes: String = "",
    val stylisticAccuracy: Int = 0,
    val technicalMerit: Int = 0,
    val intangibles: Int = 0,
    val descriptorValues: Map<String, String> = emptyMap(),
    val offFlavorLevels: Map<String, IntensityLevel> = emptyMap(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val totalScore: Int
        get() = aromaScore + appearanceScore + flavorScore + mouthfeelScore + overallScore

    val scoringTier: BjcpScoringTier
        get() = BjcpScoringTier.fromTotalScore(totalScore)
}

fun Scoresheet.formatForShare(offFlavorNames: Map<String, String> = emptyMap()): String {
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(evaluatedAtMillis))
    val styleDisplay = listOf(styleNumber.takeIf { it.isNotBlank() }, styleName.takeIf { it.isNotBlank() })
        .filterNotNull()
        .joinToString(" - ")

    return buildString {
        appendLine("BJCP Beer Scoresheet")
        appendLine("Entry: $entryTitle")
        if (brewer.isNotBlank()) appendLine("Brewer: $brewer")
        if (styleDisplay.isNotBlank()) appendLine("Style: $styleDisplay")
        appendLine("Date: $dateStr")
        if (!bottleInspectionOk || bottleInspectionNotes.isNotBlank()) {
            val status = if (bottleInspectionOk) "OK" else "Issue Noted"
            val extra = bottleInspectionNotes.takeIf { it.isNotBlank() }?.let { " - $it" } ?: ""
            appendLine("Bottle Inspection: $status$extra")
        }
        appendLine()
        appendLine("Total Score: $totalScore / 50 (${scoringTier.label})")
        appendLine("• Aroma: $aromaScore / 12")
        appendLine("• Appearance: $appearanceScore / 3")
        appendLine("• Flavor: $flavorScore / 20")
        appendLine("• Mouthfeel: $mouthfeelScore / 5")
        appendLine("• Overall Impression: $overallScore / 10")
        appendLine()
        appendLine("Stylistic Accuracy: $stylisticAccuracy/10 • Technical Merit: $technicalMerit/10 • Intangibles: $intangibles/10")

        if (descriptorValues.isNotEmpty()) {
            val grouped = ScoresheetSection.entries.mapNotNull { section ->
                val items = ScoresheetDescriptor.forSection(section).mapNotNull { desc ->
                    descriptorValues[desc.key]?.let { value -> "${desc.label}: $value" }
                }
                if (items.isEmpty()) null else "${section.title}: ${items.joinToString(", ")}"
            }
            if (grouped.isNotEmpty()) {
                appendLine()
                grouped.forEach { appendLine(it) }
            }
        }

        if (offFlavorLevels.isNotEmpty()) {
            val faultSummary = offFlavorLevels.entries.map { (id, level) ->
                val name = offFlavorNames[id] ?: id
                "$name (${level.label})"
            }
            appendLine()
            appendLine("Off-Flavors: ${faultSummary.joinToString(", ")}")
        }

        if (aromaNotes.isNotBlank()) appendLine("\nAroma: $aromaNotes")
        if (appearanceNotes.isNotBlank()) appendLine("\nAppearance: $appearanceNotes")
        if (flavorNotes.isNotBlank()) appendLine("\nFlavor: $flavorNotes")
        if (mouthfeelNotes.isNotBlank()) appendLine("\nMouthfeel: $mouthfeelNotes")
        if (overallNotes.isNotBlank()) {
            appendLine("\nOverall Comments: $overallNotes")
        }
    }.trim()
}
