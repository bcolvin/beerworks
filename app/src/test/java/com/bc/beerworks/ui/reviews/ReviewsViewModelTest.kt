package com.bc.beerworks.ui.reviews

import com.bc.beerworks.data.local.dao.ScoresheetDao
import com.bc.beerworks.data.local.entity.ScoresheetEntity
import com.bc.beerworks.data.local.entity.toEntity
import com.bc.beerworks.data.model.BeerScoresheet
import com.bc.beerworks.data.model.CiderScoresheet
import com.bc.beerworks.data.model.MeadScoresheet
import com.bc.beerworks.data.model.OffFlavor
import com.bc.beerworks.data.model.OffFlavorFilterCriteria
import com.bc.beerworks.data.model.Scoresheet
import com.bc.beerworks.data.model.ScoresheetDiscipline
import com.bc.beerworks.data.repository.OffFlavorRepository
import com.bc.beerworks.data.repository.ScoresheetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

private class InMemoryScoresheetDao(
    initial: List<ScoresheetEntity> = emptyList()
) : ScoresheetDao {
    private val items = MutableStateFlow(initial)

    override fun getAllScoresheets(): Flow<List<ScoresheetEntity>> = items

    override suspend fun getScoresheetById(id: String): ScoresheetEntity? =
        items.value.firstOrNull { it.id == id }

    override suspend fun upsert(scoresheet: ScoresheetEntity) {
        items.value = items.value.filterNot { it.id == scoresheet.id } + scoresheet
    }

    override suspend fun deleteById(id: String) {
        items.value = items.value.filterNot { it.id == id }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(sheets: List<Scoresheet>): ReviewsViewModel {
        val dao = InMemoryScoresheetDao(sheets.map { it.toEntity() })
        val scoresheetRepo = ScoresheetRepository(dao, defaultDispatcher = testDispatcher)
        val offFlavorRepo = mock(OffFlavorRepository::class.java)
        doReturn(flowOf(emptyList<OffFlavor>())).`when`(offFlavorRepo).getFilteredItems(OffFlavorFilterCriteria())
        return ReviewsViewModel(scoresheetRepo, offFlavorRepo)
    }

    @Test
    fun disciplineFilter_togglesDisciplinesAndSearchesSpecialIngredients() = runTest {
        val beer = BeerScoresheet(
            id = "1",
            entryTitle = "West Coast IPA",
            styleName = "American IPA",
            brewer = "Alice"
        )
        val mead = MeadScoresheet(
            id = "2",
            entryTitle = "Traditional Reserve",
            styleName = "Dry Mead",
            brewer = "Bob",
            specialIngredients = "Orange Blossom honey, vanilla bean"
        )
        val meadTwo = MeadScoresheet(
            id = "3",
            entryTitle = "West Orchard Cyser",
            styleName = "Cyser",
            brewer = "Carol"
        )
        val cider = CiderScoresheet(
            id = "4",
            entryTitle = "Heritage Bittersweet",
            styleName = "English Cider",
            brewer = "Dave",
            specialIngredients = "Kingston Black, Dabinett"
        )

        val vm = createViewModel(listOf(beer, mead, meadTwo, cider))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.scoresheets.collect()
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.disciplineCounts.collect()
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.totalCount.collect()
        }
        advanceUntilIdle()

        assertThat(vm.totalCount.value).isEqualTo(4)
        assertThat(vm.disciplineCounts.value).containsEntry(ScoresheetDiscipline.BJCP_BEER, 1)
            .containsEntry(ScoresheetDiscipline.BJCP_MEAD, 2)
            .containsEntry(ScoresheetDiscipline.BJCP_CIDER, 1)
        assertThat(vm.scoresheets.value).hasSize(4)

        // Toggle Mead on -> shows only Mead sheets
        vm.toggleDiscipline(ScoresheetDiscipline.BJCP_MEAD)
        advanceUntilIdle()
        assertThat(vm.scoresheets.value.map { it.id }).containsExactlyInAnyOrder("2", "3")

        // Search matches Mead specialIngredients ("Orange Blossom")
        vm.onSearchQueryChange("Orange Blossom")
        advanceUntilIdle()
        assertThat(vm.scoresheets.value.map { it.id }).containsExactly("2")

        // Switch to Cider and search specialIngredients ("Kingston Black")
        vm.onSearchQueryChange("")
        vm.toggleDiscipline(ScoresheetDiscipline.BJCP_MEAD)
        vm.toggleDiscipline(ScoresheetDiscipline.BJCP_CIDER)
        vm.onSearchQueryChange("Kingston Black")
        advanceUntilIdle()
        assertThat(vm.scoresheets.value.map { it.id }).containsExactly("4")

        // Toggle Beer + Mead and search "West" -> matches Beer #1 and Mead #3
        vm.onSearchQueryChange("")
        vm.toggleDiscipline(ScoresheetDiscipline.BJCP_CIDER)
        vm.toggleDiscipline(ScoresheetDiscipline.BJCP_BEER)
        vm.toggleDiscipline(ScoresheetDiscipline.BJCP_MEAD)
        vm.onSearchQueryChange("West")
        advanceUntilIdle()
        assertThat(vm.scoresheets.value.map { it.id }).containsExactlyInAnyOrder("1", "3")
    }

    @Test
    fun sortOption_ordersByDateOrScoreAscendingAndDescending() = runTest {
        val oldestMid = BeerScoresheet(
            id = "oldest-mid",
            entryTitle = "Weissbier A",
            evaluatedAtMillis = 1000L,
            aromaScore = 8,
            appearanceScore = 2,
            flavorScore = 14,
            mouthfeelScore = 3,
            overallScore = 6 // 33
        )
        val midLowest = BeerScoresheet(
            id = "mid-lowest",
            entryTitle = "Pilsner",
            evaluatedAtMillis = 2000L,
            aromaScore = 6,
            appearanceScore = 2,
            flavorScore = 10,
            mouthfeelScore = 3,
            overallScore = 5 // 26
        )
        val newestHigh = MeadScoresheet(
            id = "newest-high",
            entryTitle = "Dry Mead",
            evaluatedAtMillis = 3000L,
            aromaScore = 9,
            appearanceScore = 5,
            flavorScore = 20,
            overallScore = 8 // 42
        )
        val midHighest = CiderScoresheet(
            id = "mid-highest",
            entryTitle = "English Cider",
            evaluatedAtMillis = 2500L,
            aromaScore = 10,
            appearanceScore = 6,
            flavorScore = 22,
            overallScore = 9 // 47
        )

        val vm = createViewModel(listOf(oldestMid, midLowest, newestHigh, midHighest))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.scoresheets.collect()
        }
        advanceUntilIdle()

        // Default: Date descending (newest first)
        assertThat(vm.sortOption.value).isEqualTo(ReviewsSortOption.DATE_DESC)
        assertThat(vm.scoresheets.value.map { it.id })
            .containsExactly("newest-high", "mid-highest", "mid-lowest", "oldest-mid")

        vm.onSortOptionChange(ReviewsSortOption.DATE_ASC)
        advanceUntilIdle()
        assertThat(vm.scoresheets.value.map { it.id })
            .containsExactly("oldest-mid", "mid-lowest", "mid-highest", "newest-high")

        vm.onSortOptionChange(ReviewsSortOption.SCORE_DESC)
        advanceUntilIdle()
        assertThat(vm.scoresheets.value.map { it.id })
            .containsExactly("mid-highest", "newest-high", "oldest-mid", "mid-lowest")

        vm.onSortOptionChange(ReviewsSortOption.SCORE_ASC)
        advanceUntilIdle()
        assertThat(vm.scoresheets.value.map { it.id })
            .containsExactly("mid-lowest", "oldest-mid", "newest-high", "mid-highest")
    }
}
