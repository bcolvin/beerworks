package com.bc.beerworks.ui.styledetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.Ingredient
import com.bc.beerworks.data.model.StyleIngredients
import com.bc.beerworks.data.model.isBeer
import com.bc.beerworks.ui.components.SrmVerticalScrollbar
import com.bc.beerworks.ui.theme.BeerworksTheme
import com.bc.beerworks.util.SrmColorConverter
import com.bc.beerworks.util.formattedAbvRange
import com.bc.beerworks.util.formattedFgRange
import com.bc.beerworks.util.formattedIbuRange
import com.bc.beerworks.util.formattedOgRange
import com.bc.beerworks.util.formattedSrmRange

@Composable
fun StyleDetailScreen(
    viewModel: StyleDetailViewModel,
    onBackClick: () -> Unit,
    onHopClick: (String) -> Unit,
    onMaltClick: (String) -> Unit,
    onHoneyClick: (String) -> Unit,
    onPomeClick: (String) -> Unit,
    onYeastClick: (String) -> Unit,
    onNavigateToStyle: (BeerStyle) -> Unit
) {
    val style by viewModel.style.collectAsState()
    val nextStyle by viewModel.nextStyle.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()

    style?.let { beerStyle ->
        StyleDetailContent(
            style = beerStyle,
            onBackClick = onBackClick,
            onToggleFavorite = viewModel::toggleFavorite,
            ingredients = ingredients,
            onHopClick = onHopClick,
            onMaltClick = onMaltClick,
            onHoneyClick = onHoneyClick,
            onPomeClick = onPomeClick,
            onYeastClick = onYeastClick,
            nextStyle = nextStyle,
            onSwipeForward = {
                nextStyle?.let(onNavigateToStyle)
            },
            onSwipeBack = onBackClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleDetailContent(
    style: BeerStyle,
    onBackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    ingredients: StyleIngredients = StyleIngredients(),
    onHopClick: (String) -> Unit = {},
    onMaltClick: (String) -> Unit = {},
    onHoneyClick: (String) -> Unit = {},
    onPomeClick: (String) -> Unit = {},
    onYeastClick: (String) -> Unit = {},
    nextStyle: BeerStyle? = null,
    onSwipeForward: () -> Unit = {},
    onSwipeBack: () -> Unit = onBackClick
) {
    val scrollState = rememberScrollState()

    val srmMin = style.srmMin ?: 2.0
    val srmMax = style.srmMax ?: srmMin
    val srmColorMax = SrmColorConverter.srmToColor(srmMax)
    val isDarkBeer = ((srmMin + srmMax) / 2.0) >= 10.0
    val accentTitleColor = if (isDarkBeer) MaterialTheme.colorScheme.primary else srmColorMax

    var totalDragX by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 72.dp.toPx() }

    Scaffold(
        topBar = {
            val fullTitle = "${style.number} ${style.name}"
            TopAppBar(
                expandedHeight = if (fullTitle.length > 26) 76.dp else TopAppBarDefaults.TopAppBarExpandedHeight,
                title = {
                    Column {
                        Text(
                            text = fullTitle,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = style.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (style.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (style.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(nextStyle) {
                    detectHorizontalDragGestures(
                        onDragStart = { totalDragX = 0f },
                        onDragEnd = {
                            if (totalDragX < -swipeThresholdPx && nextStyle != null) {
                                onSwipeForward()
                            } else if (totalDragX > swipeThresholdPx) {
                                onSwipeBack()
                            }
                            totalDragX = 0f
                        },
                        onDragCancel = { totalDragX = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 28.dp, top = 12.dp, bottom = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                if (style.overallImpression.isNotBlank()) {
                    DetailSectionCard(title = "Overall Impression", contentText = style.overallImpression, titleColor = accentTitleColor)
                }
                if (style.aroma.isNotBlank()) {
                    DetailSectionCard(title = "Aroma", contentText = style.aroma, titleColor = accentTitleColor)
                }
                if (style.appearance.isNotBlank()) {
                    DetailSectionCard(title = "Appearance", contentText = style.appearance, titleColor = accentTitleColor)
                }
                if (style.flavor.isNotBlank()) {
                    DetailSectionCard(title = "Flavor", contentText = style.flavor, titleColor = accentTitleColor)
                }
                if (style.mouthfeel.isNotBlank()) {
                    DetailSectionCard(title = "Mouthfeel", contentText = style.mouthfeel, titleColor = accentTitleColor)
                }
                if (style.comments.isNotBlank()) {
                    DetailSectionCard(title = "Comments", contentText = style.comments, titleColor = accentTitleColor)
                }
                if (style.history.isNotBlank()) {
                    DetailSectionCard(title = "History", contentText = style.history, titleColor = accentTitleColor)
                }
                if (style.characteristicIngredients.isNotBlank()) {
                    DetailSectionCard(title = "Characteristic Ingredients", contentText = style.characteristicIngredients, titleColor = accentTitleColor)
                }
                if (style.styleComparison.isNotBlank()) {
                    DetailSectionCard(title = "Style Comparison", contentText = style.styleComparison, titleColor = accentTitleColor)
                }
                if (style.commercialExamples.isNotBlank()) {
                    DetailSectionCard(title = "Commercial Examples", contentText = style.commercialExamples, titleColor = accentTitleColor)
                }

                // Vital Statistics Card (moved below Commercial Examples)
                DetailSectionCard(title = "Vital Statistics", titleColor = accentTitleColor) {
                    Column {
                        StatRow("OG Range", style.formattedOgRange())
                        StatRow("FG Range", style.formattedFgRange())
                        if (style.guidelineType.isBeer) {
                            StatRow("IBU Range", style.formattedIbuRange(suffix = " IBU"))
                            StatRow("SRM Range", style.formattedSrmRange(suffix = " SRM"))
                        }
                        StatRow("ABV Range", style.formattedAbvRange(suffix = "%"))
                        if (!style.guidelineType.isBeer) {
                            style.sweetness?.let { if (it.isNotBlank()) StatRow("Sweetness", it) }
                            style.carbonation?.let { if (it.isNotBlank()) StatRow("Carbonation", it) }
                            style.strength?.let { if (it.isNotBlank()) StatRow("Strength", it) }
                        }
                    }
                }

                if (ingredients.malts.isNotEmpty()) {
                    CollapsibleIngredientSectionCard(
                        title = "Common Malts & Grains",
                        items = ingredients.malts,
                        badgeLabel = { it.category },
                        swatchColor = { SrmColorConverter.srmToColor(it.averageColor) },
                        onItemClick = onMaltClick
                    )
                }

                if (ingredients.hops.isNotEmpty()) {
                    CollapsibleIngredientSectionCard(
                        title = "Common Hops",
                        items = ingredients.hops,
                        badgeLabel = { it.purpose },
                        onItemClick = onHopClick
                    )
                }

                baseStyleDependentIngredientNote(style, ingredients)?.let { note ->
                    BaseStyleIngredientNoteCard(note = note)
                }

                if (ingredients.honeys.isNotEmpty()) {
                    CollapsibleIngredientSectionCard(
                        title = "Recommended Honeys",
                        items = ingredients.honeys,
                        badgeLabel = { it.floralClassification },
                        swatchColor = { it.color },
                        onItemClick = onHoneyClick
                    )
                }

                if (ingredients.pomes.isNotEmpty()) {
                    CollapsibleIngredientSectionCard(
                        title = "Associated Pomes & Varieties",
                        items = ingredients.pomes,
                        badgeLabel = { it.classification },
                        onItemClick = onPomeClick
                    )
                }

                if (ingredients.yeasts.isNotEmpty()) {
                    CollapsibleIngredientSectionCard(
                        title = "Associated Yeast Strains",
                        items = ingredients.yeasts,
                        badgeLabel = { it.classification },
                        onItemClick = onYeastClick
                    )
                }

                if (style.guidelineType.isBeer) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Full-width SRM Horizontal Color Range Banner at the end of the scrollable content
                    SrmBottomBanner(
                        minSrm = style.srmMin,
                        maxSrm = style.srmMax,
                        modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            // Custom Vertical SRM Gradient Scrollbar with Foam Head Indicator
            if (style.guidelineType.isBeer) {
                SrmVerticalScrollbar(
                    scrollState = scrollState,
                    minSrm = style.srmMin,
                    maxSrm = style.srmMax,
                    headDescription = style.appearance,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(top = 8.dp, bottom = 8.dp, end = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SrmBottomBanner(
    minSrm: Double?,
    maxSrm: Double?,
    modifier: Modifier = Modifier
) {
    val minColor = SrmColorConverter.srmToColor(minSrm ?: 2.0)
    val maxColor = SrmColorConverter.srmToColor(maxSrm ?: minSrm ?: 4.0)

    val brush = Brush.horizontalGradient(
        colors = listOf(minColor, maxColor)
    )

    val srmText = when {
        minSrm != null && maxSrm != null -> "${minSrm.toInt()} – ${maxSrm.toInt()} SRM Color Range"
        minSrm != null -> "${minSrm.toInt()} SRM Color"
        else -> "SRM Color Range"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(brush)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.45f)
            )
        ) {
            Text(
                text = srmText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T : Ingredient> CollapsibleIngredientSectionCard(
    title: String,
    items: List<T>,
    badgeLabel: (T) -> String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    swatchColor: ((T) -> Color)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$title (${items.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { item ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .clickable { onItemClick(item.id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (swatchColor != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(swatchColor(item))
                                            .border(
                                                0.5.dp,
                                                Color.White.copy(alpha = 0.5f),
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                }
                                Text(
                                    text = "${item.countryFlag} ${item.name} (${badgeLabel(item)})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun baseStyleDependentIngredientNote(
    style: BeerStyle,
    ingredients: StyleIngredients
): String? {
    if (style.guidelineType != GuidelineType.BJCP_2021) return null
    return when {
        ingredients.malts.isEmpty() && ingredients.hops.isEmpty() ->
            "Common Malts & Hops: Varies depending on the declared base style."

        ingredients.malts.isEmpty() ->
            "Common Malts & Grains: Varies depending on the declared base style."

        ingredients.hops.isEmpty() ->
            "Common Hops: Varies depending on the declared base style."

        else -> null
    }
}

@Composable
private fun BaseStyleIngredientNoteCard(
    note: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = note,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun DetailSectionCard(
    title: String,
    contentText: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    customContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (contentText != null) {
                Text(
                    text = contentText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            customContent?.invoke()
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(text = "$label: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun StyleDetailContentPreview() {
    BeerworksTheme {
        StyleDetailContent(
            style = BeerStyle(
                id = "7B",
                name = "Altbier",
                category = "Amber Bitter European Beer",
                categoryNumber = "7",
                number = "7B",
                overallImpression = "A moderately colored, well-attenuated, bitter beer.",
                aroma = "Malty and rich with grainy characteristics.",
                appearance = "Amber to deep copper color. Brilliant clarity.",
                flavor = "Malt profile similar to the aroma, with an assertive hop bitterness.",
                mouthfeel = "Medium body. Smooth. Medium to medium-high carbonation.",
                comments = "Classic, traditional examples served from casks in Düsseldorf.",
                history = "Developed in the late 19th century in Düsseldorf.",
                characteristicIngredients = "German base malts, Spalt hops.",
                styleComparison = "More bitter and malty than International Amber Lagers.",
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
            onBackClick = {},
            onToggleFavorite = {}
        )
    }
}
