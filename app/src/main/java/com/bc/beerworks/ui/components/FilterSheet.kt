package com.bc.beerworks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.FilterCriteria
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.isBeer
import com.bc.beerworks.data.model.isCider
import com.bc.beerworks.data.model.isMead
import com.bc.beerworks.ui.theme.BeerworksTheme
import com.bc.beerworks.util.SrmColorConverter
import kotlin.math.roundToInt

data class SrmPreset(
    val name: String,
    val minSrm: Double,
    val maxSrm: Double,
    val sampleSrm: Double
) {
    companion object {
        val presets = listOf(
            SrmPreset("Straw", 2.0, 4.0, 3.0),
            SrmPreset("Gold", 5.0, 9.0, 7.0),
            SrmPreset("Amber", 10.0, 18.0, 14.0),
            SrmPreset("Brown", 19.0, 29.0, 24.0),
            SrmPreset("Black", 30.0, 40.0, 35.0)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    criteria: FilterCriteria,
    categories: List<String>,
    onCriteriaChange: (FilterCriteria) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    selectedGuideline: GuidelineType = GuidelineType.BJCP_2021,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val titleText = when {
                    selectedGuideline.isMead -> "Filter Meads"
                    selectedGuideline.isCider -> "Filter Ciders"
                    else -> "Filter Styles"
                }
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onReset) {
                    Text("Reset All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedGuideline == GuidelineType.BJCP_MEAD) {
                // Sweetness Toggle Chips
                MeadFilterChipSection(
                    title = "Sweetness",
                    options = listOf("Dry", "Semi-Sweet", "Sweet"),
                    selectedOptions = criteria.selectedSweetness,
                    onToggle = { option ->
                        val next = if (criteria.selectedSweetness.contains(option)) {
                            criteria.selectedSweetness - option
                        } else {
                            criteria.selectedSweetness + option
                        }
                        onCriteriaChange(criteria.copy(selectedSweetness = next))
                    }
                )

                // Carbonation Toggle Chips
                MeadFilterChipSection(
                    title = "Carbonation",
                    options = listOf("Still", "Pétillant", "Sparkling"),
                    selectedOptions = criteria.selectedCarbonation,
                    onToggle = { option ->
                        val next = if (criteria.selectedCarbonation.contains(option)) {
                            criteria.selectedCarbonation - option
                        } else {
                            criteria.selectedCarbonation + option
                        }
                        onCriteriaChange(criteria.copy(selectedCarbonation = next))
                    }
                )

                // Strength Toggle Chips
                MeadFilterChipSection(
                    title = "Strength",
                    options = listOf("Hydromel", "Standard", "Sack"),
                    selectedOptions = criteria.selectedStrength,
                    onToggle = { option ->
                        val next = if (criteria.selectedStrength.contains(option)) {
                            criteria.selectedStrength - option
                        } else {
                            criteria.selectedStrength + option
                        }
                        onCriteriaChange(criteria.copy(selectedStrength = next))
                    }
                )
            } else if (selectedGuideline.isCider) {
                // Sweetness Toggle Chips
                MeadFilterChipSection(
                    title = "Sweetness",
                    options = listOf("Dry", "Medium-Dry", "Medium", "Medium-Sweet", "Sweet"),
                    selectedOptions = criteria.selectedSweetness,
                    onToggle = { option ->
                        val next = if (criteria.selectedSweetness.contains(option)) {
                            criteria.selectedSweetness - option
                        } else {
                            criteria.selectedSweetness + option
                        }
                        onCriteriaChange(criteria.copy(selectedSweetness = next))
                    }
                )

                // Carbonation Toggle Chips
                MeadFilterChipSection(
                    title = "Carbonation",
                    options = listOf("Still", "Pétillant", "Sparkling"),
                    selectedOptions = criteria.selectedCarbonation,
                    onToggle = { option ->
                        val next = if (criteria.selectedCarbonation.contains(option)) {
                            criteria.selectedCarbonation - option
                        } else {
                            criteria.selectedCarbonation + option
                        }
                        onCriteriaChange(criteria.copy(selectedCarbonation = next))
                    }
                )

                // Strength Toggle Chips
                MeadFilterChipSection(
                    title = "Strength",
                    options = listOf("Session", "Standard", "High Gravity"),
                    selectedOptions = criteria.selectedStrength,
                    onToggle = { option ->
                        val next = if (criteria.selectedStrength.contains(option)) {
                            criteria.selectedStrength - option
                        } else {
                            criteria.selectedStrength + option
                        }
                        onCriteriaChange(criteria.copy(selectedStrength = next))
                    }
                )
            } else {
                // SRM Color Range Header
                Text(
                    text = "Color (SRM): ${criteria.minSrm.roundToInt()} – ${criteria.maxSrm.roundToInt()} SRM",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Visual SRM Palette Presets Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SrmPreset.presets.forEach { preset ->
                        val isSelected = (criteria.minSrm == preset.minSrm && criteria.maxSrm == preset.maxSrm)
                        val chipColor = SrmColorConverter.srmToColor(preset.sampleSrm)
                        val textColor = if (preset.sampleSrm < 10.0) Color.Black else Color.White

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(chipColor)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    if (isSelected) {
                                        onCriteriaChange(criteria.copy(minSrm = 0.0, maxSrm = 40.0))
                                    } else {
                                        onCriteriaChange(criteria.copy(minSrm = preset.minSrm, maxSrm = preset.maxSrm))
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = textColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(1.dp))
                                }
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SRM Range Slider
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

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ABV Range Slider
            Text(
                text = "Alcohol (ABV): ${"%.1f".format(criteria.minAbv)}% – ${"%.1f".format(criteria.maxAbv)}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
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

            if (selectedGuideline.isBeer) {
                Spacer(modifier = Modifier.height(16.dp))

                // IBU Bitterness Range Slider
                Text(
                    text = "Bitterness (IBU): ${criteria.minIbu.roundToInt()} – ${criteria.maxIbu.roundToInt()} IBU",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Favorites Only Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bookmarked Favorites Only",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeadFilterChipSection(
    title: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onToggle: (String) -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedOptions.contains(option)
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(option) },
                label = { Text(option) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
    Spacer(modifier = Modifier.height(14.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun FilterSheetPreview() {
    BeerworksTheme {
        FilterSheet(
            criteria = FilterCriteria(),
            categories = listOf("Standard American Beer", "IPA"),
            onCriteriaChange = {},
            onReset = {},
            onDismiss = {}
        )
    }
}
