package com.bc.beerworks.ui.categorydetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.BaseStyleGroup
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.ui.components.StyleCard
import com.bc.beerworks.ui.theme.BeerworksTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailContent(
    group: BaseStyleGroup,
    onBackClick: () -> Unit,
    onStyleClick: (BeerStyle) -> Unit,
    onToggleFavorite: (BeerStyle) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = group.displayTitle,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = group.categoryName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${group.styles.size} Sub-styles included in this base style category.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Specs Summary Row
                        Row(modifier = Modifier.fillMaxWidth()) {
                            group.abvMin?.let { min ->
                                group.abvMax?.let { max ->
                                    StatChip(
                                        label = "ABV",
                                        value = String.format(Locale.US, "%.1f - %.1f%%", min, max)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                            if (group.guidelineType != GuidelineType.BJCP_MEAD) {
                                group.ibuMin?.let { min ->
                                    group.ibuMax?.let { max ->
                                        StatChip(
                                            label = "IBU",
                                            value = String.format(Locale.US, "%.0f - %.0f", min, max)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                }
                                group.srmMin?.let { min ->
                                    group.srmMax?.let { max ->
                                        StatChip(
                                            label = "SRM",
                                            value = String.format(Locale.US, "%.0f - %.0f", min, max)
                                        )
                                    }
                                }
                            } else {
                                group.fgMin?.let { min ->
                                    group.fgMax?.let { max ->
                                        StatChip(
                                            label = "FG",
                                            value = String.format(Locale.US, "%.3f - %.3f", min, max)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sub-styles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            items(group.styles, key = { "${it.guidelineType.name}_${it.id}" }) { style ->
                StyleCard(
                    style = style,
                    onClick = { onStyleClick(style) },
                    onToggleFavorite = { onToggleFavorite(style) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryDetailContentPreview() {
    BeerworksTheme {
        CategoryDetailContent(
            group = BaseStyleGroup(
                categoryNumber = "1",
                categoryName = "Standard American Beer",
                styles = listOf(
                    BeerStyle(
                        id = "1A", name = "American Light Lager", category = "Standard American Beer",
                        categoryNumber = "1", number = "1A", overallImpression = "", aroma = "", appearance = "",
                        flavor = "", mouthfeel = "", comments = "", history = "", characteristicIngredients = "",
                        styleComparison = "", ibuMin = 8.0, ibuMax = 12.0, ogMin = 1.028, ogMax = 1.040,
                        fgMin = 0.998, fgMax = 1.008, abvMin = 2.8, abvMax = 4.2, srmMin = 2.0, srmMax = 3.0,
                        commercialExamples = "Bud Light", tags = emptyList(), guidelineType = GuidelineType.BJCP_2021
                    )
                ),
                guidelineType = GuidelineType.BJCP_2021
            ),
            onBackClick = {},
            onStyleClick = {},
            onToggleFavorite = {}
        )
    }
}
