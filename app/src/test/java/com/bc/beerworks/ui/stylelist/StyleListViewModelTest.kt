package com.bc.beerworks.ui.stylelist

import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeBaDao
import com.bc.beerworks.data.repository.FakeBjcpDao
import com.bc.beerworks.data.repository.FakeFavDao
import com.bc.beerworks.data.repository.FakePrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class StyleListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onQueryChange_updatesFilterCriteria() = runTest {
        val testEntity = BjcpStyleEntity(
            id = "1A",
            name = "American Light Lager",
            category = "Standard American Beer",
            categoryNumber = "1",
            number = "1A",
            overallImpression = "Refreshing",
            aroma = "Low malt",
            appearance = "Pale yellow",
            flavor = "Neutral",
            mouthfeel = "Watery",
            comments = "",
            history = "",
            characteristicIngredients = "Corn",
            styleComparison = "",
            ibuMin = 8.0,
            ibuMax = 12.0,
            ogMin = 1.028,
            ogMax = 1.040,
            fgMin = 0.998,
            fgMax = 1.008,
            abvMin = 2.8,
            abvMax = 4.2,
            srmMin = 2.0,
            srmMax = 3.0,
            commercialExamples = "Bud Light",
            tags = "session, pale"
        )

        val repository = BeerStyleRepository(
            FakeBjcpDao(listOf(testEntity)),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            testDispatcher
        )

        val viewModel = StyleListViewModel(repository)

        viewModel.onQueryChange("Lager")

        assertThat(viewModel.filterCriteria.value.query).isEqualTo("Lager")
    }

    @Test
    fun toggleCategoryExpansion_togglesCategoryCorrectly() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(emptyList()),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            testDispatcher
        )

        val viewModel = StyleListViewModel(repository)

        assertThat(viewModel.expandedCategoryNumber.value).isNull()

        viewModel.toggleCategoryExpansion("1")
        assertThat(viewModel.expandedCategoryNumber.value).isEqualTo("1")

        viewModel.toggleCategoryExpansion("2")
        assertThat(viewModel.expandedCategoryNumber.value).isEqualTo("2")

        viewModel.toggleCategoryExpansion("2")
        assertThat(viewModel.expandedCategoryNumber.value).isNull()
    }

    @Test
    fun groupedStyles_groupsBeerStylesByCategory() = runTest {
        val testEntity1 = BjcpStyleEntity(
            id = "1A", name = "American Light Lager", category = "Standard American Beer",
            categoryNumber = "1", number = "1A", overallImpression = "", aroma = "", appearance = "",
            flavor = "", mouthfeel = "", comments = "", history = "", characteristicIngredients = "",
            styleComparison = "", ibuMin = 8.0, ibuMax = 12.0, ogMin = 1.028, ogMax = 1.040,
            fgMin = 0.998, fgMax = 1.008, abvMin = 2.8, abvMax = 4.2, srmMin = 2.0, srmMax = 3.0,
            commercialExamples = "Bud Light", tags = "session, pale"
        )

        val testEntity2 = BjcpStyleEntity(
            id = "1B", name = "American Lager", category = "Standard American Beer",
            categoryNumber = "1", number = "1B", overallImpression = "", aroma = "", appearance = "",
            flavor = "", mouthfeel = "", comments = "", history = "", characteristicIngredients = "",
            styleComparison = "", ibuMin = 8.0, ibuMax = 18.0, ogMin = 1.040, ogMax = 1.050,
            fgMin = 1.004, fgMax = 1.010, abvMin = 4.2, abvMax = 5.3, srmMin = 2.0, srmMax = 3.5,
            commercialExamples = "Budweiser", tags = "pale, lager"
        )

        val repository = BeerStyleRepository(
            FakeBjcpDao(listOf(testEntity1, testEntity2)),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            testDispatcher
        )

        val viewModel = StyleListViewModel(repository)
        val collectJob = backgroundScope.launch {
            viewModel.groupedStyles.collect {}
        }
        runCurrent()

        val groups = viewModel.groupedStyles.value
        assertThat(groups).hasSize(1)
        assertThat(groups[0].categoryNumber).isEqualTo("1")
        assertThat(groups[0].styles).hasSize(2)

        collectJob.cancel()
    }
}
