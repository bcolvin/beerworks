package com.bc.beerworks.ui.components

import com.bc.beerworks.data.model.FilterCriteria
import com.bc.beerworks.data.model.GuidelineType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FilterScrollResetTrackerTest {

    @Test
    fun initialCompositionAndReEntry_doNotTriggerScrollToTop() {
        val activeKey = FilterCriteria(query = "IPA") to GuidelineType.BJCP_2021
        val tracker = FilterScrollResetTracker(initialFilterKey = activeKey)

        tracker.onFilterKeyObserved(activeKey)

        assertThat(tracker.pendingScrollToTop).isFalse()
        assertThat(tracker.consumePendingScrollToTop(hasItems = true)).isFalse()
    }

    @Test
    fun filterChange_triggersScrollToTopOnceItemsBecomeAvailable() {
        val initialKey = FilterCriteria() to GuidelineType.BJCP_2021
        val updatedKey = FilterCriteria(query = "Lager") to GuidelineType.BJCP_2021
        val tracker = FilterScrollResetTracker(initialFilterKey = initialKey)

        tracker.onFilterKeyObserved(updatedKey)
        assertThat(tracker.pendingScrollToTop).isTrue()

        // Does not consume while list is still empty
        assertThat(tracker.consumePendingScrollToTop(hasItems = false)).isFalse()
        assertThat(tracker.pendingScrollToTop).isTrue()

        // Consumes once non-empty items arrive and clears pending flag
        assertThat(tracker.consumePendingScrollToTop(hasItems = true)).isTrue()
        assertThat(tracker.pendingScrollToTop).isFalse()
        assertThat(tracker.consumePendingScrollToTop(hasItems = true)).isFalse()
    }

    @Test
    fun guidelineChange_triggersScrollToTop() {
        val tracker =
            FilterScrollResetTracker(initialFilterKey = FilterCriteria() to GuidelineType.BJCP_2021)

        tracker.onFilterKeyObserved(FilterCriteria() to GuidelineType.BA_2026)

        assertThat(tracker.consumePendingScrollToTop(hasItems = true)).isTrue()
    }
}
