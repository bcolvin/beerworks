package com.bc.beerworks.ui.ingredientlist

import com.bc.beerworks.data.model.HopFilterCriteria
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.repository.IngredientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class IngredientListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val citra = HopVariety(
        id = "citra",
        name = "Citra",
        origin = "United States",
        countryCode = "US",
        purpose = "Aroma",
        alphaAcidMin = 11.0,
        alphaAcidMax = 13.0,
        betaAcidMin = 3.5,
        betaAcidMax = 4.5
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun items_reactsToQueryAndFilterCriteriaChangesAndPreservesQueryOnReset() = runTest {
        @Suppress("UNCHECKED_CAST")
        val repository = mock(IngredientRepository::class.java) as IngredientRepository<HopVariety, HopFilterCriteria>

        val defaultCriteria = HopFilterCriteria()
        val queryCriteria = HopFilterCriteria(query = "Citra")
        val filteredCriteria = HopFilterCriteria(query = "Citra", selectedPurpose = "Aroma")

        doReturn(flowOf(listOf(citra))).`when`(repository).getFilteredItems(defaultCriteria)
        doReturn(flowOf(listOf(citra))).`when`(repository).getFilteredItems(queryCriteria)
        doReturn(flowOf(emptyList<HopVariety>())).`when`(repository).getFilteredItems(filteredCriteria)

        val viewModel = IngredientListViewModel(
            repository = repository,
            initialCriteria = defaultCriteria
        )

        val job = backgroundScope.launch { viewModel.items.collect {} }
        runCurrent()

        assertThat(viewModel.items.value).containsExactly(citra)

        viewModel.onFilterCriteriaChange(filteredCriteria)
        runCurrent()
        assertThat(viewModel.filterCriteria.value).isEqualTo(filteredCriteria)
        assertThat(viewModel.items.value).isEmpty()

        viewModel.resetFilters()
        runCurrent()
        assertThat(viewModel.filterCriteria.value).isEqualTo(queryCriteria)
        assertThat(viewModel.items.value).containsExactly(citra)

        job.cancel()
    }
}
