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

    @Test
    fun onFilterCriteriaChange_updatesFilterCriteriaDirectly() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(emptyList()),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            testDispatcher
        )

        val viewModel = StyleListViewModel(repository)

        val updated = com.bc.beerworks.data.model.FilterCriteria(
            minSrm = 12.0,
            maxSrm = 24.0,
            minAbv = 5.0,
            maxAbv = 8.5,
            minIbu = 30.0,
            maxIbu = 70.0,
            favoritesOnly = true,
            selectedSweetness = setOf("Dry"),
            selectedCarbonation = setOf("Sparkling"),
            selectedStrength = setOf("Standard")
        )

        viewModel.onFilterCriteriaChange(updated)

        assertThat(viewModel.filterCriteria.value).isEqualTo(updated)
    }

    @Test
    fun filterCriteria_isFilterActive_detectsActiveFiltersCorrectly() {
        val defaultCriteria = com.bc.beerworks.data.model.FilterCriteria()
        assertThat(defaultCriteria.isFilterActive).isFalse()

        assertThat(defaultCriteria.copy(minSrm = 5.0).isFilterActive).isTrue()
        assertThat(defaultCriteria.copy(maxSrm = 30.0).isFilterActive).isTrue()
        assertThat(defaultCriteria.copy(minAbv = 4.0).isFilterActive).isTrue()
        assertThat(defaultCriteria.copy(maxAbv = 10.0).isFilterActive).isTrue()
        assertThat(defaultCriteria.copy(minIbu = 20.0).isFilterActive).isTrue()
        assertThat(defaultCriteria.copy(maxIbu = 80.0).isFilterActive).isTrue()
        assertThat(defaultCriteria.copy(selectedCategory = "IPA").isFilterActive).isTrue()
        assertThat(defaultCriteria.copy(favoritesOnly = true).isFilterActive).isTrue()
    }

    @Test
    fun resetFilters_preservesQueryAndResetsOtherFilters() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(emptyList()),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            testDispatcher
        )

        val viewModel = StyleListViewModel(repository)

        viewModel.onQueryChange("IPA")
        viewModel.onFilterCriteriaChange(
            com.bc.beerworks.data.model.FilterCriteria(
                query = "IPA",
                minSrm = 10.0,
                maxSrm = 25.0,
                favoritesOnly = true
            )
        )

        assertThat(viewModel.filterCriteria.value.isFilterActive).isTrue()

        viewModel.resetFilters()

        assertThat(viewModel.filterCriteria.value.query).isEqualTo("IPA")
        assertThat(viewModel.filterCriteria.value.isFilterActive).isFalse()
        assertThat(viewModel.filterCriteria.value.minSrm).isEqualTo(0.0)
        assertThat(viewModel.filterCriteria.value.maxSrm).isEqualTo(40.0)
        assertThat(viewModel.filterCriteria.value.favoritesOnly).isFalse()
    }

    @Test
    fun onGuidelineChange_updatesSelectedGuideline() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(emptyList()),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            testDispatcher
        )

        val viewModel = StyleListViewModel(repository)
        val collectJob = backgroundScope.launch {
            viewModel.selectedGuideline.collect {}
        }

        viewModel.onGuidelineChange(com.bc.beerworks.data.model.GuidelineType.BA_2026)
        runCurrent()

        assertThat(viewModel.selectedGuideline.value).isEqualTo(com.bc.beerworks.data.model.GuidelineType.BA_2026)

        viewModel.onGuidelineChange(com.bc.beerworks.data.model.GuidelineType.BJCP_CIDER)
        runCurrent()

        assertThat(viewModel.selectedGuideline.value).isEqualTo(com.bc.beerworks.data.model.GuidelineType.BJCP_CIDER)
        collectJob.cancel()
    }
}
