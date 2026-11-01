package com.bc.beerworks.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.BaseStyleGroup
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.ui.theme.BeerworksTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryCard(
    group: BaseStyleGroup,
    isExpanded: Boolean,
    onCategoryClick: () -> Unit,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .combinedClickable(
                onClick = onCategoryClick,
                onLongClick = onToggleExpand
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.displayTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleExpand) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryCardPreview() {
    BeerworksTheme {
        CategoryCard(
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
            isExpanded = false,
            onCategoryClick = {},
            onToggleExpand = {}
        )
    }
}
