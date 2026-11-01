package com.bc.beerworks.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.FilterCriteria
import com.bc.beerworks.ui.theme.BeerworksTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    criteria: FilterCriteria,
    categories: List<String>,
    onCriteriaChange: (FilterCriteria) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Filter Styles",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SRM Color Range Slider
            Text(
                text = "Color (SRM): ${criteria.minSrm.roundToInt()} – ${criteria.maxSrm.roundToInt()} SRM",
                style = MaterialTheme.typography.bodyMedium
            )
            RangeSlider(
                value = criteria.minSrm.toFloat()..criteria.maxSrm.toFloat(),
                onValueChange = { range ->
                    onCriteriaChange(
                        criteria.copy(
                            minSrm = range.start.toDouble(),
                            maxSrm = range.endInclusive.toDouble()
                        )
                    )
                },
                valueRange = 0f..40f
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ABV Range Slider
            Text(
                text = "Alcohol (ABV): ${"%.1f".format(criteria.minAbv)}% – ${"%.1f".format(criteria.maxAbv)}%",
                style = MaterialTheme.typography.bodyMedium
            )
            RangeSlider(
                value = criteria.minAbv.toFloat()..criteria.maxAbv.toFloat(),
                onValueChange = { range ->
                    onCriteriaChange(
                        criteria.copy(
                            minAbv = range.start.toDouble(),
                            maxAbv = range.endInclusive.toDouble()
                        )
                    )
                },
                valueRange = 0f..20f
            )

            Spacer(modifier = Modifier.height(12.dp))

            // IBU Bitterness Range Slider
            Text(
                text = "Bitterness (IBU): ${criteria.minIbu.roundToInt()} – ${criteria.maxIbu.roundToInt()} IBU",
                style = MaterialTheme.typography.bodyMedium
            )
            RangeSlider(
                value = criteria.minIbu.toFloat()..criteria.maxIbu.toFloat(),
                onValueChange = { range ->
                    onCriteriaChange(
                        criteria.copy(
                            minIbu = range.start.toDouble(),
                            maxIbu = range.endInclusive.toDouble()
                        )
                    )
                },
                valueRange = 0f..120f
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Favorites Only Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bookmarked Favorites Only",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = criteria.favoritesOnly,
                    onCheckedChange = { isChecked ->
                        onCriteriaChange(criteria.copy(favoritesOnly = isChecked))
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun FilterSheetPreview() {
    BeerworksTheme {
        FilterSheet(
            criteria = FilterCriteria(),
            categories = listOf("Standard American Beer", "Amber Ale"),
            onCriteriaChange = {},
            onReset = {},
            onDismiss = {}
        )
    }
}
