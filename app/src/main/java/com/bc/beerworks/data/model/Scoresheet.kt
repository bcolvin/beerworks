package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Immutable
enum class ScoresheetDiscipline(
    val displayName: String,
    val shortLabel: String,
    val emoji: String,
    val isSupported: Boolean
) {
    BJCP_BEER("BJCP Beer", "Beer", "🍺", isSupported = true),
    BJCP_MEAD("BJCP Mead", "Mead", "🐝", isSupported = true),
    BJCP_CIDER("BJCP Cider", "Cider", "🍏", isSupported = false);

    companion object {
        fun fromStorageValue(raw: String?): ScoresheetDiscipline = when (raw) {
            BJCP_MEAD.name -> BJCP_MEAD
            BJCP_CIDER.name -> BJCP_CIDER
            // BJCP_BEER plus legacy "SHORT_FORM" / "LONG_FORM" Beer values all normalize to BJCP_BEER
            else -> BJCP_BEER
        }
    }
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
private val MEAD_CARBONATION_VALUES = listOf("Still", "Pétillant", "Sparkling")
private val MEAD_SWEETNESS_VALUES = listOf("Dry", "Semi-Sweet", "Sweet")
val MEAD_STRENGTH_VALUES = listOf("Hydromel", "Standard", "Sack")

/** Cycles through [options] sequentially, returning null after the final option to unset. */
fun cycleOption(current: String?, options: List<String>): String? {
    if (current == null) return options.firstOrNull()
    val idx = options.indexOf(current)
    return if (idx == -1 || idx + 1 >= options.size) null else options[idx + 1]
}

@Immutable
enum class ScoresheetDescriptor(
    val key: String,
    val label: String,
    val section: ScoresheetSection,
    val discipline: ScoresheetDiscipline,
    val options: List<String> = LOW_MED_HIGH
) {
    // Beer — Aroma
    AROMA_MALT("aroma_malt", "Malt", ScoresheetSection.AROMA, ScoresheetDiscipline.BJCP_BEER),
    AROMA_HOPS("aroma_hops", "Hops", ScoresheetSection.AROMA, ScoresheetDiscipline.BJCP_BEER),
    AROMA_FERMENTATION("aroma_fermentation", "Fermentation Character", ScoresheetSection.AROMA, ScoresheetDiscipline.BJCP_BEER),

    // Beer — Appearance
    APPEARANCE_COLOR(
        "appearance_color",
        "Color",
        ScoresheetSection.APPEARANCE,
        ScoresheetDiscipline.BJCP_BEER,
        listOf("Yellow", "Gold", "Amber", "Copper", "Brown", "Black")
    ),
    APPEARANCE_CLARITY(
        "appearance_clarity",
        "Clarity",
        ScoresheetSection.APPEARANCE,
        ScoresheetDiscipline.BJCP_BEER,
        listOf("Opaque", "Hazy", "Brilliant")
    ),
    APPEARANCE_HEAD_SIZE(
        "appearance_head_size",
        "Head Size",
        ScoresheetSection.APPEARANCE,
        ScoresheetDiscipline.BJCP_BEER,
        listOf("None", "Small", "Med", "Large")
    ),
    APPEARANCE_HEAD_RETENTION(
        "appearance_head_retention",
        "Head Retention",
        ScoresheetSection.APPEARANCE,
        ScoresheetDiscipline.BJCP_BEER,
        listOf("Quick", "Moderate", "Long Lasting")
    ),
    APPEARANCE_HEAD_COLOR(
        "appearance_head_color",
        "Head Color",
        ScoresheetSection.APPEARANCE,
        ScoresheetDiscipline.BJCP_BEER,
        listOf("White", "Ivory", "Beige", "Tan")
    ),

    // Beer — Flavor
    FLAVOR_MALT("flavor_malt", "Malt", ScoresheetSection.FLAVOR, ScoresheetDiscipline.BJCP_BEER),
    FLAVOR_HOPS("flavor_hops", "Hops", ScoresheetSection.FLAVOR, ScoresheetDiscipline.BJCP_BEER),
    FLAVOR_BITTERNESS("flavor_bitterness", "Bitterness", ScoresheetSection.FLAVOR, ScoresheetDiscipline.BJCP_BEER),
    FLAVOR_FERMENTATION("flavor_fermentation", "Fermentation Character", ScoresheetSection.FLAVOR, ScoresheetDiscipline.BJCP_BEER),
    FLAVOR_BALANCE(
        "flavor_balance",
        "Balance",
        ScoresheetSection.FLAVOR,
        ScoresheetDiscipline.BJCP_BEER,
        listOf("Hoppy", "Balanced", "Malty")
    ),
    FLAVOR_FINISH(
        "flavor_finish",
        "Finish / Aftertaste",
        ScoresheetSection.FLAVOR,
        ScoresheetDiscipline.BJCP_BEER,
        listOf("Dry", "Balanced", "Sweet")
    ),

    // Beer — Mouthfeel
    MOUTHFEEL_BODY(
        "mouthfeel_body",
        "Body",
        ScoresheetSection.MOUTHFEEL,
        ScoresheetDiscipline.BJCP_BEER,
        listOf("Thin", "Med", "Full")
    ),
    MOUTHFEEL_CARBONATION("mouthfeel_carbonation", "Carbonation", ScoresheetSection.MOUTHFEEL, ScoresheetDiscipline.BJCP_BEER),
    MOUTHFEEL_WARMTH("mouthfeel_warmth", "Warmth", ScoresheetSection.MOUTHFEEL, ScoresheetDiscipline.BJCP_BEER),
    MOUTHFEEL_CREAMINESS("mouthfeel_creaminess", "Creaminess", ScoresheetSection.MOUTHFEEL, ScoresheetDiscipline.BJCP_BEER),
    MOUTHFEEL_ASTRINGENCY("mouthfeel_astringency", "Astringency", ScoresheetSection.MOUTHFEEL, ScoresheetDiscipline.BJCP_BEER),

    // Mead — Aroma
    MEAD_AROMA_HONEY("mead_aroma_honey", "Honey", ScoresheetSection.AROMA, ScoresheetDiscipline.BJCP_MEAD),
    MEAD_AROMA_FLORAL_FRUITY("mead_aroma_floral_fruity", "Floral / Fruity", ScoresheetSection.AROMA, ScoresheetDiscipline.BJCP_MEAD),
    MEAD_AROMA_SPICES("mead_aroma_spices", "Spices / Adjuncts", ScoresheetSection.AROMA, ScoresheetDiscipline.BJCP_MEAD),
    MEAD_AROMA_ALCOHOL("mead_aroma_alcohol", "Alcohol", ScoresheetSection.AROMA, ScoresheetDiscipline.BJCP_MEAD),
    MEAD_AROMA_FERMENTATION("mead_aroma_fermentation", "Fermentation", ScoresheetSection.AROMA, ScoresheetDiscipline.BJCP_MEAD),

    // Mead — Appearance
    MEAD_APPEARANCE_COLOR(
        "mead_appearance_color",
        "Color",
        ScoresheetSection.APPEARANCE,
        ScoresheetDiscipline.BJCP_MEAD,
        listOf("Straw", "Gold", "Amber", "Dark", "Pink", "Red", "Purple")
    ),
    MEAD_APPEARANCE_CLARITY(
        "mead_appearance_clarity",
        "Clarity",
        ScoresheetSection.APPEARANCE,
        ScoresheetDiscipline.BJCP_MEAD,
        listOf("Opaque", "Hazy", "Brilliant")
    ),
    MEAD_APPEARANCE_LEGS(
        "mead_appearance_legs",
        "Legs / Viscosity",
        ScoresheetSection.APPEARANCE,
        ScoresheetDiscipline.BJCP_MEAD,
        listOf("Light", "Med", "Heavy")
    ),
    MEAD_APPEARANCE_CARBONATION(
        "mead_appearance_carbonation",
        "Carbonation",
        ScoresheetSection.APPEARANCE,
        ScoresheetDiscipline.BJCP_MEAD,
        MEAD_CARBONATION_VALUES
    ),

    // Mead — Flavor (encompasses palate & mouthfeel)
    MEAD_FLAVOR_HONEY("mead_flavor_honey", "Honey", ScoresheetSection.FLAVOR, ScoresheetDiscipline.BJCP_MEAD),
    MEAD_FLAVOR_SPICES("mead_flavor_spices", "Spices / Adjuncts", ScoresheetSection.FLAVOR, ScoresheetDiscipline.BJCP_MEAD),
    MEAD_FLAVOR_SWEETNESS(
        "mead_flavor_sweetness",
        "Sweetness",
        ScoresheetSection.FLAVOR,
        ScoresheetDiscipline.BJCP_MEAD,
        MEAD_SWEETNESS_VALUES
    ),
    MEAD_FLAVOR_ACIDITY("mead_flavor_acidity", "Acidity", ScoresheetSection.FLAVOR, ScoresheetDiscipline.BJCP_MEAD),
    MEAD_FLAVOR_TANNIN("mead_flavor_tannin", "Tannin / Astringency", ScoresheetSection.FLAVOR, ScoresheetDiscipline.BJCP_MEAD),
    MEAD_FLAVOR_BODY(
        "mead_flavor_body",
        "Body",
        ScoresheetSection.FLAVOR,
        ScoresheetDiscipline.BJCP_MEAD,
        listOf("Light", "Med", "Full")
    ),
    MEAD_FLAVOR_CARBONATION(
        "mead_flavor_carbonation",
        "Carbonation",
        ScoresheetSection.FLAVOR,
        ScoresheetDiscipline.BJCP_MEAD,
        MEAD_CARBONATION_VALUES
    ),
    MEAD_FLAVOR_ALCOHOL("mead_flavor_alcohol", "Alcohol Warmth", ScoresheetSection.FLAVOR, ScoresheetDiscipline.BJCP_MEAD),
    MEAD_FLAVOR_BALANCE(
        "mead_flavor_balance",
        "Balance",
        ScoresheetSection.FLAVOR,
        ScoresheetDiscipline.BJCP_MEAD,
        listOf("Sweet", "Balanced", "Acidic")
    ),
    MEAD_FLAVOR_FINISH(
        "mead_flavor_finish",
        "Finish",
        ScoresheetSection.FLAVOR,
        ScoresheetDiscipline.BJCP_MEAD,
        MEAD_SWEETNESS_VALUES.let { listOf("Dry", "Balanced", "Sweet") }
    );

    fun cycle(current: String?): String? = cycleOption(current, options)

    companion object {
        fun forSection(
            section: ScoresheetSection,
            discipline: ScoresheetDiscipline = ScoresheetDiscipline.BJCP_BEER
        ): List<ScoresheetDescriptor> = entries.filter { it.section == section && it.discipline == discipline }
    }
}

/**
 * Polymorphic BJCP sensory evaluation record supporting discipline-specific scoring sections
 * and entrant metadata while sharing a common 50-point scale and database table.
 */
@Immutable
sealed interface Scoresheet {
    val id: String
    val discipline: ScoresheetDiscipline
    val entryTitle: String
    val brewer: String
    val styleId: String?
    val styleName: String
    val styleNumber: String
    val evaluatedAtMillis: Long
    val aromaScore: Int
    val appearanceScore: Int
    val flavorScore: Int
    val overallScore: Int
    val aromaNotes: String
    val appearanceNotes: String
    val flavorNotes: String
    val overallNotes: String
    val bottleInspectionOk: Boolean
    val bottleInspectionNotes: String
    val stylisticAccuracy: Int
    val technicalMerit: Int
    val intangibles: Int
    val descriptorValues: Map<String, String>
    val offFlavorLevels: Map<String, IntensityLevel>
    val updatedAtMillis: Long

    val totalScore: Int
    val scoringTier: BjcpScoringTier
        get() = BjcpScoringTier.fromTotalScore(totalScore)

    companion object {
        fun createBlank(
            discipline: ScoresheetDiscipline,
            id: String = UUID.randomUUID().toString(),
            entryTitle: String = "",
            brewer: String = "",
            evaluatedAtMillis: Long = System.currentTimeMillis(),
            bottleInspectionOk: Boolean = true,
            bottleInspectionNotes: String = ""
        ): Scoresheet = when (discipline) {
            ScoresheetDiscipline.BJCP_MEAD -> MeadScoresheet(
                id = id,
                entryTitle = entryTitle,
                brewer = brewer,
                evaluatedAtMillis = evaluatedAtMillis,
                bottleInspectionOk = bottleInspectionOk,
                bottleInspectionNotes = bottleInspectionNotes
            )
            ScoresheetDiscipline.BJCP_BEER,
            ScoresheetDiscipline.BJCP_CIDER -> BeerScoresheet(
                id = id,
                entryTitle = entryTitle,
                brewer = brewer,
                evaluatedAtMillis = evaluatedAtMillis,
                bottleInspectionOk = bottleInspectionOk,
                bottleInspectionNotes = bottleInspectionNotes
            )
        }
    }
}

@Immutable
data class BeerScoresheet(
    override val id: String = UUID.randomUUID().toString(),
    override val entryTitle: String = "",
    override val brewer: String = "",
    override val styleId: String? = null,
    override val styleName: String = "",
    override val styleNumber: String = "",
    override val evaluatedAtMillis: Long = System.currentTimeMillis(),
    override val aromaScore: Int = 0,
    override val appearanceScore: Int = 0,
    override val flavorScore: Int = 0,
    val mouthfeelScore: Int = 0,
    override val overallScore: Int = 0,
    override val aromaNotes: String = "",
    override val appearanceNotes: String = "",
    override val flavorNotes: String = "",
    val mouthfeelNotes: String = "",
    override val overallNotes: String = "",
    override val bottleInspectionOk: Boolean = true,
    override val bottleInspectionNotes: String = "",
    override val stylisticAccuracy: Int = 0,
    override val technicalMerit: Int = 0,
    override val intangibles: Int = 0,
    override val descriptorValues: Map<String, String> = emptyMap(),
    override val offFlavorLevels: Map<String, IntensityLevel> = emptyMap(),
    override val updatedAtMillis: Long = System.currentTimeMillis()
) : Scoresheet {
    override val discipline: ScoresheetDiscipline
        get() = ScoresheetDiscipline.BJCP_BEER

    override val totalScore: Int
        get() = aromaScore + appearanceScore + flavorScore + mouthfeelScore + overallScore
}

@Immutable
data class MeadScoresheet(
    override val id: String = UUID.randomUUID().toString(),
    override val entryTitle: String = "",
    override val brewer: String = "",
    override val styleId: String? = null,
    override val styleName: String = "",
    override val styleNumber: String = "",
    override val evaluatedAtMillis: Long = System.currentTimeMillis(),
    override val aromaScore: Int = 0,
    override val appearanceScore: Int = 0,
    override val flavorScore: Int = 0,
    override val overallScore: Int = 0,
    override val aromaNotes: String = "",
    override val appearanceNotes: String = "",
    override val flavorNotes: String = "",
    override val overallNotes: String = "",
    val declaredCarbonation: String? = null,
    val declaredSweetness: String? = null,
    val declaredStrength: String? = null,
    val specialIngredients: String = "",
    override val bottleInspectionOk: Boolean = true,
    override val bottleInspectionNotes: String = "",
    override val stylisticAccuracy: Int = 0,
    override val technicalMerit: Int = 0,
    override val intangibles: Int = 0,
    override val descriptorValues: Map<String, String> = emptyMap(),
    override val offFlavorLevels: Map<String, IntensityLevel> = emptyMap(),
    override val updatedAtMillis: Long = System.currentTimeMillis()
) : Scoresheet {
    override val discipline: ScoresheetDiscipline
        get() = ScoresheetDiscipline.BJCP_MEAD

    override val totalScore: Int
        get() = aromaScore + appearanceScore + flavorScore + overallScore

    val hasAdditionalInfo: Boolean
        get() = declaredCarbonation != null ||
            declaredSweetness != null ||
            declaredStrength != null ||
            specialIngredients.isNotBlank()

    companion object {
        val CARBONATION_OPTIONS: List<String> = MEAD_CARBONATION_VALUES
        val SWEETNESS_OPTIONS: List<String> = MEAD_SWEETNESS_VALUES
        val STRENGTH_OPTIONS: List<String> = MEAD_STRENGTH_VALUES
    }
}

fun Scoresheet.formatForShare(offFlavorNames: Map<String, String> = emptyMap()): String {
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(evaluatedAtMillis))
    val styleDisplay = listOf(styleNumber.takeIf { it.isNotBlank() }, styleName.takeIf { it.isNotBlank() })
        .filterNotNull()
        .joinToString(" - ")
    val brewerLabel = if (this is MeadScoresheet) "Meadmaker" else "Brewer"

    return buildString {
        appendLine("${discipline.displayName} Scoresheet")
        appendLine("Entry: $entryTitle")
        if (brewer.isNotBlank()) appendLine("$brewerLabel: $brewer")
        if (styleDisplay.isNotBlank()) appendLine("Style: $styleDisplay")
        appendLine("Date: $dateStr")
        if (this@formatForShare is MeadScoresheet) {
            declaredCarbonation?.let { appendLine("Carbonation: $it") }
            declaredSweetness?.let { appendLine("Sweetness: $it") }
            declaredStrength?.let { appendLine("Strength: $it") }
            if (specialIngredients.isNotBlank()) appendLine("Special Ingredients: $specialIngredients")
        }
        if (!bottleInspectionOk || bottleInspectionNotes.isNotBlank()) {
            val status = if (bottleInspectionOk) "OK" else "Issue Noted"
            val extra = bottleInspectionNotes.takeIf { it.isNotBlank() }?.let { " - $it" } ?: ""
            appendLine("Bottle Inspection: $status$extra")
        }
        appendLine()
        appendLine("Total Score: $totalScore / 50 (${scoringTier.label})")
        when (this@formatForShare) {
            is BeerScoresheet -> {
                appendLine("• Aroma: $aromaScore / 12")
                appendLine("• Appearance: $appearanceScore / 3")
                appendLine("• Flavor: $flavorScore / 20")
                appendLine("• Mouthfeel: $mouthfeelScore / 5")
                appendLine("• Overall Impression: $overallScore / 10")
            }
            is MeadScoresheet -> {
                appendLine("• Aroma: $aromaScore / 10")
                appendLine("• Appearance: $appearanceScore / 6")
                appendLine("• Flavor: $flavorScore / 24")
                appendLine("• Overall Impression: $overallScore / 10")
            }
        }
        appendLine()
        appendLine("Stylistic Accuracy: $stylisticAccuracy/10 • Technical Merit: $technicalMerit/10 • Intangibles: $intangibles/10")

        if (descriptorValues.isNotEmpty()) {
            val grouped = ScoresheetSection.entries.mapNotNull { section ->
                val items = ScoresheetDescriptor.forSection(section, discipline).mapNotNull { desc ->
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
        if (this@formatForShare is BeerScoresheet && mouthfeelNotes.isNotBlank()) {
            appendLine("\nMouthfeel: $mouthfeelNotes")
        }
        if (overallNotes.isNotBlank()) {
            appendLine("\nOverall Comments: $overallNotes")
        }
    }.trim()
}
