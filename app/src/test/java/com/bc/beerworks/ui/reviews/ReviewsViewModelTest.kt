package com.bc.beerworks.ui.reviews

import com.bc.beerworks.data.local.dao.ScoresheetDao
import com.bc.beerworks.data.local.entity.ScoresheetEntity
import com.bc.beerworks.data.local.entity.toEntity
import com.bc.beerworks.data.model.BeerScoresheet
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
        val scoresheetRepo = ScoresheetRepository(dao)
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

        val vm = createViewModel(listOf(beer, mead, meadTwo))
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

        assertThat(vm.totalCount.value).isEqualTo(3)
        assertThat(vm.disciplineCounts.value).containsEntry(ScoresheetDiscipline.BJCP_BEER, 1)
            .containsEntry(ScoresheetDiscipline.BJCP_MEAD, 2)
            .containsEntry(ScoresheetDiscipline.BJCP_CIDER, 0)
        assertThat(vm.scoresheets.value).hasSize(3)

        // Toggle Mead on -> shows only Mead sheets
        vm.toggleDiscipline(ScoresheetDiscipline.BJCP_MEAD)
        advanceUntilIdle()
        assertThat(vm.scoresheets.value.map { it.id }).containsExactlyInAnyOrder("2", "3")

        // Search matches Mead specialIngredients ("Orange Blossom")
        vm.onSearchQueryChange("Orange Blossom")
        advanceUntilIdle()
        assertThat(vm.scoresheets.value.map { it.id }).containsExactly("2")

        // Toggle Beer on too and search "West" -> matches Beer #1 and Mead #3
        vm.toggleDiscipline(ScoresheetDiscipline.BJCP_BEER)
        vm.onSearchQueryChange("West")
        advanceUntilIdle()
        assertThat(vm.scoresheets.value.map { it.id }).containsExactlyInAnyOrder("1", "3")

        // Clear filters restores all 3
        vm.clearFilters()
        advanceUntilIdle()
        assertThat(vm.scoresheets.value).hasSize(3)
    }
}
