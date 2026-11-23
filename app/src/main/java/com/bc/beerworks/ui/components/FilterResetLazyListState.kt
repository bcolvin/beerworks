package com.bc.beerworks.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

/**
 * Pure state machine tracking whether a list should reset its scroll position to index 0.
 *
 * Initialized with the current [initialFilterKey] on each composition entry so navigating back
 * to a list screen restores the saved [LazyListState] without jumping to the top, while genuine
 * in-screen filter/search/guideline changes still scroll back to the top once items are non-empty.
 */
internal class FilterScrollResetTracker(initialFilterKey: Any?) {
    private var previousFilterKey: Any? = initialFilterKey
    var pendingScrollToTop: Boolean = false
        private set

    fun onFilterKeyObserved(filterKey: Any?) {
        if (filterKey != previousFilterKey) {
            previousFilterKey = filterKey
            pendingScrollToTop = true
        }
    }

    fun consumePendingScrollToTop(hasItems: Boolean): Boolean {
        if (!pendingScrollToTop || !hasItems) return false
        pendingScrollToTop = false
        return true
    }
}

/**
 * Returns a [LazyListState] that preserves scroll position across navigation backstack entries
 * via [rememberLazyListState], and resets to item 0 only when [filterKey] changes while active.
 */
@Composable
fun rememberFilterResetLazyListState(
    filterKey: Any?,
    hasItems: Boolean
): LazyListState {
    val listState = rememberLazyListState()
    val tracker = remember { FilterScrollResetTracker(filterKey) }

    LaunchedEffect(filterKey) {
        tracker.onFilterKeyObserved(filterKey)
    }

    LaunchedEffect(filterKey, hasItems) {
        if (tracker.consumePendingScrollToTop(hasItems)) {
            listState.scrollToItem(0)
        }
    }

    return listState
}
