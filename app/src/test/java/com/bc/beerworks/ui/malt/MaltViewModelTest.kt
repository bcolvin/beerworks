package com.bc.beerworks.ui.malt

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.entity.MaltVarietyEntity
import com.bc.beerworks.data.model.MaltFilterCriteria
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeMaltDao
import com.bc.beerworks.data.repository.FakeMaltStyleCrossRefDao
import com.bc.beerworks.data.repository.FakePrefsManager
import com.bc.beerworks.data.repository.MaltRepository
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.doReturn

@OptIn(ExperimentalCoroutinesApi::class)
class MaltViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testMalt = MaltVarietyEntity(
        id = "pilsner_malt",
        name = "Pilsner Malt",
        category = "Base Malt",
        grainType = "Barley",
        origin = "Germany",
        countryCode = "DE",
        colorMin = 1.4,
        colorMax = 2.0,
        diastaticPowerLintner = 110,
        extractPotentialDbfg = 82.0,
        maxGristPercentage = 100,
        description = "Pale malt.",
        typicalUsage = "Base for continental lagers.",
        functionalTags = "Base Grist",
        descriptors = "Clean,Bready",
        commercialExamples = "Weyermann",
        substituteIds = "",
        radarSweet = 3.0f,
        radarBiscuit = 2.0f,
        radarToasty = 0.5f,
        radarCaramel = 0f,
        radarNutty = 0.5f,
        radarChocolate = 0f,
        radarRoast = 0f,
        radarHusky = 1.0f
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        malts: List<MaltVarietyEntity> = listOf(testMalt)
    ): MaltViewModel {
        val fakeMaltDao = FakeMaltDao(malts)
        val fakeCrossRefDao = FakeMaltStyleCrossRefDao()
        val mockBjcp = mock(BjcpStyleDao::class.java)
        val mockBa = mock(BaStyleDao::class.java)
        val mockFav = mock(FavoriteStyleDao::class.java)
        val prefs = FakePrefsManager()

        doReturn(flowOf(listOf("German Lager", "Czech Lager"))).`when`(mockBjcp).getCategories()

        val styleRepo = BeerStyleRepository(mockBjcp, mockBa, mockFav, prefs, testDispatcher)
        val maltRepo = MaltRepository(fakeMaltDao, fakeCrossRefDao, styleRepo, testDispatcher)

        return MaltViewModel(maltRepo, styleRepo)
    }

    @Test
    fun onQueryChange_updatesFilterCriteria() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("Pilsner")

        assertThat(viewModel.filterCriteria.value.query).isEqualTo("Pilsner")
    }

    @Test
    fun onCategorySelected_togglesCategory() = runTest {
        val viewModel = createViewModel()

        viewModel.onCategorySelected("Base Malt")
        assertThat(viewModel.filterCriteria.value.selectedCategory).isEqualTo("Base Malt")

        viewModel.onCategorySelected("Base Malt")
        assertThat(viewModel.filterCriteria.value.selectedCategory).isNull()
    }

    @Test
    fun onGrainTypeSelected_togglesGrainType() = runTest {
        val viewModel = createViewModel()

        viewModel.onGrainTypeSelected("Wheat")
        assertThat(viewModel.filterCriteria.value.selectedGrainType).isEqualTo("Wheat")

        viewModel.onGrainTypeSelected("Wheat")
        assertThat(viewModel.filterCriteria.value.selectedGrainType).isNull()
    }

    @Test
    fun onFilterCriteriaChange_updatesEntireCriteria() = runTest {
        val viewModel = createViewModel()

        val newCriteria = MaltFilterCriteria(
            query = "Munich",
            selectedCategory = "Base Malt",
            selectedDiastaticTier = "High"
        )
        viewModel.onFilterCriteriaChange(newCriteria)

        assertThat(viewModel.filterCriteria.value).isEqualTo(newCriteria)
    }

    @Test
    fun resetFilters_preservesQueryAndResetsOtherFilters() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("Caramel")
        viewModel.onFilterCriteriaChange(
            MaltFilterCriteria(
                query = "Caramel",
                selectedCategory = "Caramel & Crystal",
                selectedColorTier = "Amber (10–50°L)",
                selectedDiastaticTier = "None",
                selectedFunctionalTags = setOf("Caramel Sweetness")
            )
        )

        assertThat(viewModel.filterCriteria.value.isFilterActive).isTrue()

        viewModel.resetFilters()

        assertThat(viewModel.filterCriteria.value.query).isEqualTo("Caramel")
        assertThat(viewModel.filterCriteria.value.selectedCategory).isNull()
        assertThat(viewModel.filterCriteria.value.selectedColorTier).isNull()
        assertThat(viewModel.filterCriteria.value.selectedDiastaticTier).isNull()
        assertThat(viewModel.filterCriteria.value.selectedFunctionalTags).isEmpty()
    }

    @Test
    fun malts_emitsFilteredResults() = runTest {
        val viewModel = createViewModel()

        val collectJob = launch { viewModel.malts.collect {} }
        runCurrent()

        assertThat(viewModel.malts.value).hasSize(1)
        assertThat(viewModel.malts.value[0].name).isEqualTo("Pilsner Malt")

        collectJob.cancel()
    }
}
