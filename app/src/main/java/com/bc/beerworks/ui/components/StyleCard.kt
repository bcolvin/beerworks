package com.bc.beerworks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.ui.theme.BeerworksTheme
import com.bc.beerworks.util.formatStyleDescriptorChips
import com.bc.beerworks.util.formattedAbvRange
import com.bc.beerworks.util.formattedIbuRange

data class MatchSnippet(
    val categoryLabel: String,
    val snippetText: AnnotatedString
)

fun getSearchMatchSnippet(
    style: BeerStyle,
    query: String,
    primaryColor: Color
): MatchSnippet? {
    val q = query.trim()
    if (q.isBlank()) return null

    if (style.name.contains(q, ignoreCase = true) || style.number.contains(q, ignoreCase = true)) {
        return null
    }

    val fields = listOf(
        Pair("Example", style.commercialExamples),
        Pair("Ingredient", style.characteristicIngredients),
        Pair("Aroma", style.aroma),
        Pair("Flavor", style.flavor),
        Pair("Comments", style.comments)
    )

    for ((label, text) in fields) {
        val index = text.indexOf(q, ignoreCase = true)
        if (index != -1) {
            val start = (index - 20).coerceAtLeast(0)
            val end = (index + q.length + 30).coerceAtMost(text.length)

            val prefix = if (start > 0) "..." else ""
            val suffix = if (end < text.length) "..." else ""
            val rawSnippet = prefix + text.substring(start, end) + suffix

            val annotated = buildAnnotatedString {
                val matchIndexInSnippet = rawSnippet.indexOf(q, ignoreCase = true)
                if (matchIndexInSnippet != -1) {
                    append(rawSnippet.substring(0, matchIndexInSnippet))
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                        append(rawSnippet.substring(matchIndexInSnippet, matchIndexInSnippet + q.length))
                    }
                    append(rawSnippet.substring(matchIndexInSnippet + q.length))
                } else {
                    append(rawSnippet)
                }
            }

            return MatchSnippet(
                categoryLabel = label,
                snippetText = annotated
            )
        }
    }

    return null
}

const val MAX_OVERALL_IMPRESSION_LINES = 10

fun getMaxOverallImpressionLines(searchQuery: String = ""): Int {
    return MAX_OVERALL_IMPRESSION_LINES
}

/**
 * Formats and orders style tags consistently for display on [StyleCard].
 */
fun formatStyleDescriptorTags(tags: List<String>): List<String> =
    formatStyleDescriptorChips(tags).map { it.label }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleCard(
    style: BeerStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleFavorite: (() -> Unit)? = null,
    searchQuery: String = "",
    maxOverallImpressionLines: Int = MAX_OVERALL_IMPRESSION_LINES,
    selectedTags: Set<String> = emptySet(),
    onTagClick: ((String) -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val matchSnippet = remember(style, searchQuery, primaryColor) {
        getSearchMatchSnippet(style, searchQuery, primaryColor)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Style Number Badge
                SuggestionChip(
                    onClick = {},
                    label = { Text(text = style.number, fontWeight = FontWeight.Bold) }
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Style Name & Category
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = style.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = style.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Overall Impression Preview / Full text in search results
            if (style.overallImpression.isNotBlank()) {
                Text(
                    text = style.overallImpression,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = maxOverallImpressionLines,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (style.guidelineType == GuidelineType.BJCP_CIDER) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (!style.strength.isNullOrBlank()) Arrangement.SpaceBetween else Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!style.strength.isNullOrBlank()) {
                        Text(
                            text = "⚡ ${style.strength}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    val abvText = style.formattedAbvRange(suffix = "% ABV")

                    Text(
                        text = abvText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (style.guidelineType != GuidelineType.BJCP_MEAD) {
                // Vital Stats Summary Row (Beer)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SrmRangeSwatch(minSrm = style.srmMin, maxSrm = style.srmMax)

                    val abvText = style.formattedAbvRange(suffix = "% ABV")
                    val ibuText = style.formattedIbuRange(suffix = " IBU")

                    Text(
                        text = "$abvText  •  $ibuText",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Match Context Snippet Chip
            if (matchSnippet != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔍 ${matchSnippet.categoryLabel}: ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = matchSnippet.snippetText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Flavor / Character Descriptor Chips
            val descriptorChips = remember(style.tags) {
                formatStyleDescriptorChips(style.tags)
            }
            if (descriptorChips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    descriptorChips.forEach { chip ->
                        DescriptorChip(
                            text = chip.label,
                            selected = chip.key in selectedTags,
                            onClick = onTagClick?.let { callback -> { callback(chip.key) } }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StyleCardPreview() {
    BeerworksTheme {
        StyleCard(
            style = BeerStyle(
                id = "7B",
                name = "Altbier",
                category = "Amber Bitter European Beer",
                categoryNumber = "7",
                number = "7B",
                overallImpression = "A moderately colored, well-attenuated, bitter beer with a rich maltiness balancing a strong bitterness.",
                aroma = "",
                appearance = "",
                flavor = "",
                mouthfeel = "",
                comments = "",
                history = "",
                characteristicIngredients = "",
                styleComparison = "",
                ibuMin = 25.0,
                ibuMax = 50.0,
                ogMin = 1.044,
                ogMax = 1.052,
                fgMin = 1.008,
                fgMax = 1.014,
                abvMin = 4.3,
                abvMax = 5.5,
                srmMin = 9.0,
                srmMax = 17.0,
                commercialExamples = "Uerige Altbier, Füchschen Alt",
                tags = listOf("amber", "bitter"),
                guidelineType = GuidelineType.BJCP_2021,
                isFavorite = true
            ),
            onClick = {},
            onToggleFavorite = {},
            searchQuery = "Uerige",
            modifier = Modifier.padding(16.dp)
        )
    }
}
