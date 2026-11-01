package com.bc.beerworks.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.ui.theme.BeerworksTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidelineSelector(
    selectedGuideline: GuidelineType,
    onGuidelineSelected: (GuidelineType) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = GuidelineType.values()

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = selectedGuideline == option,
                onClick = { onGuidelineSelected(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text(text = option.displayName)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GuidelineSelectorPreview() {
    BeerworksTheme {
        GuidelineSelector(
            selectedGuideline = GuidelineType.BJCP_2021,
            onGuidelineSelected = {}
        )
    }
}
