package com.bc.beerworks.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.SportsBar
import androidx.compose.ui.graphics.vector.ImageVector
import com.bc.beerworks.data.model.GuidelineType

fun getDisciplineIcon(guidelineType: GuidelineType): ImageVector {
    return when (guidelineType) {
        GuidelineType.BJCP_2021, GuidelineType.BA_2026 -> Icons.Default.SportsBar
        GuidelineType.BJCP_MEAD -> Icons.Default.EmojiNature
    }
}

fun getDisciplineEmoji(guidelineType: GuidelineType): String {
    return when (guidelineType) {
        GuidelineType.BJCP_2021, GuidelineType.BA_2026 -> "🍺"
        GuidelineType.BJCP_MEAD -> "🐝"
    }
}
