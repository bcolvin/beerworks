package com.bc.beerworks.ui.stylelist

import androidx.compose.foundation.lazy.LazyListLayoutInfo
import com.bc.beerworks.data.model.BaseStyleGroup

data class VisibleItemInfoSnapshot(
    val index: Int,
    val offset: Int,
    val size: Int
)

fun calculateCategoryItemIndex(
    groupedStyles: List<BaseStyleGroup>,
    targetCategoryNumber: String,
    expandedCategoryNumber: String?,
    searchQuery: String
): Int {
    var currentIndex = 0
    for (group in groupedStyles) {
        if (group.categoryNumber == targetCategoryNumber) {
            return currentIndex
        }
        currentIndex++ // Category card
        val isExpanded = (expandedCategoryNumber == group.categoryNumber) || searchQuery.isNotBlank()
        if (isExpanded) {
            currentIndex += group.styles.size
        }
    }
    return -1
}

fun calculateAutoScrollDelta(
    categoryIndex: Int,
    subStylesCount: Int,
    visibleItems: List<VisibleItemInfoSnapshot>,
    viewportStart: Int,
    viewportEnd: Int,
    spacing: Int = 0,
    bottomPadding: Int = 0
): Float {
    if (subStylesCount <= 0 || categoryIndex < 0) return 0f

    val catItem = visibleItems.firstOrNull { it.index == categoryIndex } ?: return 0f

    val sub0 = visibleItems.firstOrNull { it.index == categoryIndex + 1 }
    val defaultCardHeight = (viewportEnd - viewportStart).toFloat() * 0.22f
    val styleCardHeight = sub0?.size?.toFloat()
        ?: visibleItems.firstOrNull { it.index > categoryIndex }?.size?.toFloat()
        ?: defaultCardHeight

    val targetBottom = (viewportEnd - bottomPadding).toFloat()

    val rawDelta = if (subStylesCount == 1) {
        // Rule: "(or 1 if only 1 exists)" -> sub-card 0 must be fully visible
        val sub0Bottom = if (sub0 != null) {
            (sub0.offset + sub0.size).toFloat()
        } else {
            (catItem.offset + catItem.size + spacing).toFloat() + styleCardHeight
        }
        sub0Bottom - targetBottom
    } else {
        // Rule: "at least 1.5 sub cards" -> sub-card 0 fully visible, sub-card 1 at least 50% visible
        val sub1 = visibleItems.firstOrNull { it.index == categoryIndex + 2 }
        val sub1HalfLine = if (sub1 != null) {
            (sub1.offset + (sub1.size / 2)).toFloat()
        } else if (sub0 != null) {
            (sub0.offset + sub0.size + spacing).toFloat() + (styleCardHeight / 2f)
        } else {
            (catItem.offset + catItem.size + spacing).toFloat() + styleCardHeight + spacing.toFloat() + (styleCardHeight / 2f)
        }
        sub1HalfLine - targetBottom
    }

    if (rawDelta <= 0f) {
        return 0f
    }

    // Never scroll category card off the top of the viewport
    val maxAllowedScroll = (catItem.offset - viewportStart).coerceAtLeast(0).toFloat()
    return rawDelta.coerceAtMost(maxAllowedScroll)
}

fun calculateAutoScrollDelta(
    categoryIndex: Int,
    subStylesCount: Int,
    layoutInfo: LazyListLayoutInfo,
    spacing: Int = 0,
    bottomPadding: Int = 0
): Float {
    val items = layoutInfo.visibleItemsInfo.map {
        VisibleItemInfoSnapshot(index = it.index, offset = it.offset, size = it.size)
    }
    return calculateAutoScrollDelta(
        categoryIndex = categoryIndex,
        subStylesCount = subStylesCount,
        visibleItems = items,
        viewportStart = layoutInfo.viewportStartOffset,
        viewportEnd = layoutInfo.viewportEndOffset,
        spacing = spacing,
        bottomPadding = bottomPadding
    )
}

fun shouldAutoScrollCategory(
    categoryIndex: Int,
    subStylesCount: Int,
    visibleItems: List<VisibleItemInfoSnapshot>,
    viewportStart: Int,
    viewportEnd: Int,
    spacing: Int = 0,
    bottomPadding: Int = 0
): Boolean {
    return calculateAutoScrollDelta(
        categoryIndex = categoryIndex,
        subStylesCount = subStylesCount,
        visibleItems = visibleItems,
        viewportStart = viewportStart,
        viewportEnd = viewportEnd,
        spacing = spacing,
        bottomPadding = bottomPadding
    ) > 0f
}

fun shouldAutoScrollCategory(
    categoryIndex: Int,
    subStylesCount: Int,
    layoutInfo: LazyListLayoutInfo,
    spacing: Int = 0,
    bottomPadding: Int = 0
): Boolean {
    return calculateAutoScrollDelta(
        categoryIndex = categoryIndex,
        subStylesCount = subStylesCount,
        layoutInfo = layoutInfo,
        spacing = spacing,
        bottomPadding = bottomPadding
    ) > 0f
}
