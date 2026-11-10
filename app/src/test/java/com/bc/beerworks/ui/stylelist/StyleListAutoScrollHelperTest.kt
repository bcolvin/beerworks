package com.bc.beerworks.ui.stylelist

import com.bc.beerworks.data.model.BaseStyleGroup
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class StyleListAutoScrollHelperTest {

    private fun createStyle(id: String, number: String, categoryNumber: String): BeerStyle {
        return BeerStyle(
            id = id,
            name = "Style $number",
            category = "Category $categoryNumber",
            categoryNumber = categoryNumber,
            number = number,
            overallImpression = "Impression",
            aroma = "",
            appearance = "",
            flavor = "",
            mouthfeel = "",
            comments = "",
            history = "",
            characteristicIngredients = "",
            styleComparison = "",
            ibuMin = null,
            ibuMax = null,
            ogMin = null,
            ogMax = null,
            fgMin = null,
            fgMax = null,
            abvMin = null,
            abvMax = null,
            srmMin = null,
            srmMax = null,
            commercialExamples = "",
            tags = emptyList(),
            guidelineType = GuidelineType.BJCP_2021
        )
    }

    private fun createGroup(categoryNumber: String, styleCount: Int): BaseStyleGroup {
        val styles = (1..styleCount).map { i ->
            createStyle(id = "$categoryNumber$i", number = "$categoryNumber$i", categoryNumber = categoryNumber)
        }
        return BaseStyleGroup(
            categoryNumber = categoryNumber,
            categoryName = "Category $categoryNumber",
            styles = styles,
            guidelineType = GuidelineType.BJCP_2021
        )
    }

    @Test
    fun calculateCategoryItemIndex_returnsCorrectIndicesWhenCollapsed() {
        val groups = listOf(
            createGroup("1", 3),
            createGroup("2", 2),
            createGroup("3", 4)
        )

        // When all collapsed
        assertThat(calculateCategoryItemIndex(groups, "1", null, "")).isEqualTo(0)
        assertThat(calculateCategoryItemIndex(groups, "2", null, "")).isEqualTo(1)
        assertThat(calculateCategoryItemIndex(groups, "3", null, "")).isEqualTo(2)
        assertThat(calculateCategoryItemIndex(groups, "99", null, "")).isEqualTo(-1)
    }

    @Test
    fun calculateCategoryItemIndex_accountsForExpandedPrecedingCategory() {
        val groups = listOf(
            createGroup("1", 3),
            createGroup("2", 2),
            createGroup("3", 4)
        )

        // Category 1 is expanded (adds 3 sub-items after index 0)
        assertThat(calculateCategoryItemIndex(groups, "1", "1", "")).isEqualTo(0)
        // Category 2 category-card is at index 0 + 1 (cat1) + 3 (cat1 styles) = 4
        assertThat(calculateCategoryItemIndex(groups, "2", "1", "")).isEqualTo(4)
        // Category 3 category-card is at 4 + 1 (cat2) = 5
        assertThat(calculateCategoryItemIndex(groups, "3", "1", "")).isEqualTo(5)
    }

    @Test
    fun calculateCategoryItemIndex_accountsForActiveSearchQueryExpandingAllPreceding() {
        val groups = listOf(
            createGroup("1", 3),
            createGroup("2", 2),
            createGroup("3", 4)
        )

        // All groups are expanded during search
        // Category 1: index 0 (styles 1..3)
        assertThat(calculateCategoryItemIndex(groups, "1", null, "ipa")).isEqualTo(0)
        // Category 2: index 0 + 1 + 3 = 4 (styles 5..6)
        assertThat(calculateCategoryItemIndex(groups, "2", null, "ipa")).isEqualTo(4)
        // Category 3: index 4 + 1 + 2 = 7 (styles 8..11)
        assertThat(calculateCategoryItemIndex(groups, "3", null, "ipa")).isEqualTo(7)
    }

    @Test
    fun shouldAutoScrollCategory_returnsFalseForInvalidOrEmptyStyles() {
        val visibleItems = listOf(
            VisibleItemInfoSnapshot(index = 0, offset = 0, size = 100)
        )
        assertThat(shouldAutoScrollCategory(categoryIndex = -1, subStylesCount = 2, visibleItems = visibleItems, viewportStart = 0, viewportEnd = 1000)).isFalse()
        assertThat(shouldAutoScrollCategory(categoryIndex = 0, subStylesCount = 0, visibleItems = visibleItems, viewportStart = 0, viewportEnd = 1000)).isFalse()
    }

    @Test
    fun shouldAutoScrollCategory_singleStyle_returnsFalseWhenFullyVisible() {
        // Category at index 0, single sub-style at index 1
        val visibleItems = listOf(
            VisibleItemInfoSnapshot(index = 0, offset = 0, size = 70),
            VisibleItemInfoSnapshot(index = 1, offset = 70, size = 200)
        )
        // Sub-card 1 ends at 270 <= 1000 viewportEnd -> fully visible
        val shouldScroll = shouldAutoScrollCategory(
            categoryIndex = 0,
            subStylesCount = 1,
            visibleItems = visibleItems,
            viewportStart = 0,
            viewportEnd = 1000
        )
        assertThat(shouldScroll).isFalse()
    }

    @Test
    fun shouldAutoScrollCategory_singleStyle_returnsTrueWhenSubCardCutOff() {
        // Category at index 5, single sub-style at index 6
        // Sub-card starts at 900, size 200 -> ends at 1100 (viewport ends at 1000)
        // Visible height = 100, which is 50% (< 85% requirement)
        val visibleItems = listOf(
            VisibleItemInfoSnapshot(index = 5, offset = 830, size = 70),
            VisibleItemInfoSnapshot(index = 6, offset = 900, size = 200)
        )
        val shouldScroll = shouldAutoScrollCategory(
            categoryIndex = 5,
            subStylesCount = 1,
            visibleItems = visibleItems,
            viewportStart = 0,
            viewportEnd = 1000
        )
        assertThat(shouldScroll).isTrue()
    }

    @Test
    fun shouldAutoScrollCategory_multipleStyles_returnsFalseWhenAtLeastOneAndHalfCardsVisible() {
        // Category at index 2, sub0 at index 3, sub1 at index 4
        // sub0 is fully visible (100 to 300)
        // sub1 starts at 300, size 200, ends at 500 (fully visible >= 50%)
        val visibleItems = listOf(
            VisibleItemInfoSnapshot(index = 2, offset = 30, size = 70),
            VisibleItemInfoSnapshot(index = 3, offset = 100, size = 200),
            VisibleItemInfoSnapshot(index = 4, offset = 300, size = 200)
        )
        val shouldScroll = shouldAutoScrollCategory(
            categoryIndex = 2,
            subStylesCount = 3,
            visibleItems = visibleItems,
            viewportStart = 0,
            viewportEnd = 1000
        )
        assertThat(shouldScroll).isFalse()
    }

    @Test
    fun shouldAutoScrollCategory_multipleStyles_returnsFalseWhenSecondCardExactlyHalfVisible() {
        // sub0 is fully visible (offset 600, size 200, ends at 800)
        // sub1 starts at 800, size 200, ends at 1000 in a 900 viewport (visible height = 100, which is 50%)
        val visibleItems = listOf(
            VisibleItemInfoSnapshot(index = 0, offset = 530, size = 70),
            VisibleItemInfoSnapshot(index = 1, offset = 600, size = 200),
            VisibleItemInfoSnapshot(index = 2, offset = 800, size = 200)
        )
        val shouldScroll = shouldAutoScrollCategory(
            categoryIndex = 0,
            subStylesCount = 2,
            visibleItems = visibleItems,
            viewportStart = 0,
            viewportEnd = 900
        )
        assertThat(shouldScroll).isFalse()
    }

    @Test
    fun shouldAutoScrollCategory_multipleStyles_returnsTrueWhenSecondCardLessThanHalfVisible() {
        // sub0 is fully visible (offset 600, size 200, ends at 800)
        // sub1 starts at 800, size 200, ends at 1000 in an 850 viewport (visible height = 50, 25% < 50%)
        val visibleItems = listOf(
            VisibleItemInfoSnapshot(index = 0, offset = 530, size = 70),
            VisibleItemInfoSnapshot(index = 1, offset = 600, size = 200),
            VisibleItemInfoSnapshot(index = 2, offset = 800, size = 200)
        )
        val shouldScroll = shouldAutoScrollCategory(
            categoryIndex = 0,
            subStylesCount = 2,
            visibleItems = visibleItems,
            viewportStart = 0,
            viewportEnd = 850
        )
        assertThat(shouldScroll).isTrue()
    }

    @Test
    fun shouldAutoScrollCategory_returnsTrueWhenSubCardsNotLaidOutOffscreen() {
        // Last category in list (index 10) is visible near the bottom, but sub-cards are not in visibleItems
        val visibleItems = listOf(
            VisibleItemInfoSnapshot(index = 9, offset = 600, size = 70),
            VisibleItemInfoSnapshot(index = 10, offset = 750, size = 70)
        )
        val shouldScroll = shouldAutoScrollCategory(
            categoryIndex = 10,
            subStylesCount = 2,
            visibleItems = visibleItems,
            viewportStart = 0,
            viewportEnd = 800
        )
        assertThat(shouldScroll).isTrue()
    }

    @Test
    fun calculateAutoScrollDelta_returnsZeroWhenOneAndHalfCardsAlreadyVisible() {
        // sub0 is 600..800, sub1 is 800..1000 in a 900 viewport (half of sub1 visible)
        val visibleItems = listOf(
            VisibleItemInfoSnapshot(index = 0, offset = 530, size = 70),
            VisibleItemInfoSnapshot(index = 1, offset = 600, size = 200),
            VisibleItemInfoSnapshot(index = 2, offset = 800, size = 200)
        )
        val delta = calculateAutoScrollDelta(
            categoryIndex = 0,
            subStylesCount = 2,
            visibleItems = visibleItems,
            viewportStart = 0,
            viewportEnd = 900
        )
        assertThat(delta).isEqualTo(0f)
    }

    @Test
    fun calculateAutoScrollDelta_returnsExactDeltaToRevealHalfOfSecondCard() {
        // sub0 is 600..800, sub1 is 800..1000 in an 850 viewport
        // sub1 halfway line is 900. Target bottom is 850.
        // Scroll delta needed = 900 - 850 = 50px.
        val visibleItems = listOf(
            VisibleItemInfoSnapshot(index = 0, offset = 530, size = 70),
            VisibleItemInfoSnapshot(index = 1, offset = 600, size = 200),
            VisibleItemInfoSnapshot(index = 2, offset = 800, size = 200)
        )
        val delta = calculateAutoScrollDelta(
            categoryIndex = 0,
            subStylesCount = 2,
            visibleItems = visibleItems,
            viewportStart = 0,
            viewportEnd = 850
        )
        assertThat(delta).isEqualTo(50f)
    }

    @Test
    fun calculateAutoScrollDelta_singleCard_returnsExactDeltaToRevealFullCard() {
        // category at 800..870, sub0 at 870..1070 in a 1000 viewport with 16px bottom padding
        // targetBottom = 984. sub0Bottom = 1070.
        // Scroll delta needed = 1070 - 984 = 86px.
        val visibleItems = listOf(
            VisibleItemInfoSnapshot(index = 0, offset = 800, size = 70),
            VisibleItemInfoSnapshot(index = 1, offset = 870, size = 200)
        )
        val delta = calculateAutoScrollDelta(
            categoryIndex = 0,
            subStylesCount = 1,
            visibleItems = visibleItems,
            viewportStart = 0,
            viewportEnd = 1000,
            bottomPadding = 16
        )
        assertThat(delta).isEqualTo(86f)
    }

    @Test
    fun calculateAutoScrollDelta_capsDeltaAtCategoryOffsetToKeepCategoryVisible() {
        // Category is at offset 40 (near top).
        // If raw delta needed was 100px, it should be capped at 40px so category doesn't scroll off top.
        val visibleItems = listOf(
            VisibleItemInfoSnapshot(index = 0, offset = 40, size = 70),
            VisibleItemInfoSnapshot(index = 1, offset = 110, size = 200),
            VisibleItemInfoSnapshot(index = 2, offset = 320, size = 200)
        )
        // Suppose viewportEnd is 350. sub1 half line is 420. raw delta = 420 - 350 = 70.
        // Max allowed scroll = 40 - 0 = 40.
        val delta = calculateAutoScrollDelta(
            categoryIndex = 0,
            subStylesCount = 2,
            visibleItems = visibleItems,
            viewportStart = 0,
            viewportEnd = 350
        )
        assertThat(delta).isEqualTo(40f)
    }
}
