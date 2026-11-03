package com.bc.beerworks.ui.hops

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.HopDao
import com.bc.beerworks.data.local.dao.HopStyleCrossRefDao
import com.bc.beerworks.data.local.entity.HopVarietyEntity
import com.bc.beerworks.data.model.HopFilterCriteria
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeHopDao
import com.bc.beerworks.data.repository.FakeHopStyleCrossRefDao
import com.bc.beerworks.data.repository.FakePrefsManager
import com.bc.beerworks.data.repository.HopRepository
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
class HopsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testHop = HopVarietyEntity(
        id = "citra",
        name = "Citra",
        origin = "United States",
        countryCode = "US",
        purpose = "Dual-Purpose",
        alphaAcidMin = 11.0,
        alphaAcidMax = 14.0,
        betaAcidMin = 3.0,
        betaAcidMax = 4.5,
        sensoryCategories = "Citrus,Tropical Fruit",
        descriptors = "Grapefruit,Lime",
        description = "Intense citrus.",
        parentHopIds = "",
        substituteIds = "mosaic",
        radarCitrus = 4.9f,
        radarTropical = 4.7f,
        radarFloral = 2.2f,
        radarHerbal = 1.0f,
        radarSpicy = 0.8f,
        radarPiney = 2.1f,
        radarEarthy = 0.7f,
        radarGrassy = 1.0f
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
        hops: List<HopVarietyEntity> = listOf(testHop)
    ): Pair<HopsViewModel, HopRepository> {
        val fakeHopDao = FakeHopDao(hops)
        val fakeCrossRefDao = FakeHopStyleCrossRefDao()
        val mockBjcp = mock(BjcpStyleDao::class.java)
        val mockBa = mock(BaStyleDao::class.java)
        val mockFav = mock(FavoriteStyleDao::class.java)
        val prefs = FakePrefsManager()

        doReturn(flowOf(listOf("IPA", "Czech Lager"))).`when`(mockBjcp).getCategories()

        val hopRepo = HopRepository(fakeHopDao, fakeCrossRefDao, mockBjcp, mockBa, testDispatcher)
        val styleRepo = BeerStyleRepository(mockBjcp, mockBa, mockFav, prefs, testDispatcher)

        val viewModel = HopsViewModel(hopRepo, styleRepo)
        return viewModel to hopRepo
    }

    @Test
    fun onQueryChange_updatesFilterCriteria() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.onQueryChange("Citra")

        assertThat(viewModel.filterCriteria.value.query).isEqualTo("Citra")
    }

    @Test
    fun onPurposeSelected_togglesPurpose() = runTest {
        val (viewModel, _) = createViewModel()

        assertThat(viewModel.filterCriteria.value.selectedPurpose).isNull()

        viewModel.onPurposeSelected("Aroma")
        assertThat(viewModel.filterCriteria.value.selectedPurpose).isEqualTo("Aroma")

        viewModel.onPurposeSelected("Aroma")
        assertThat(viewModel.filterCriteria.value.selectedPurpose).isNull()
    }

    @Test
    fun onSensoryCategorySelected_togglesCategory() = runTest {
        val (viewModel, _) = createViewModel()

        assertThat(viewModel.filterCriteria.value.selectedSensoryCategory).isNull()

        viewModel.onSensoryCategorySelected("Citrus")
        assertThat(viewModel.filterCriteria.value.selectedSensoryCategory).isEqualTo("Citrus")

        viewModel.onSensoryCategorySelected("Citrus")
        assertThat(viewModel.filterCriteria.value.selectedSensoryCategory).isNull()
    }

    @Test
    fun onFilterCriteriaChange_updatesCriteria() = runTest {
        val (viewModel, _) = createViewModel()

        val newCriteria = HopFilterCriteria(
            query = "test",
            selectedPurpose = "Bittering",
            minAlphaAcid = 10.0,
            maxAlphaAcid = 15.0
        )
        viewModel.onFilterCriteriaChange(newCriteria)

        assertThat(viewModel.filterCriteria.value).isEqualTo(newCriteria)
    }

    @Test
    fun resetFilters_preservesQueryAndResetsOtherFilters() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.onFilterCriteriaChange(
            HopFilterCriteria(
                query = "Citra",
                selectedPurpose = "Dual-Purpose",
                selectedSensoryCategory = "Citrus",
                minAlphaAcid = 8.0,
                maxAlphaAcid = 14.0
            )
        )

        viewModel.resetFilters()

        val reset = viewModel.filterCriteria.value
        assertThat(reset.query).isEqualTo("Citra")
        assertThat(reset.selectedPurpose).isNull()
        assertThat(reset.selectedSensoryCategory).isNull()
        assertThat(reset.minAlphaAcid).isEqualTo(0.0)
        assertThat(reset.maxAlphaAcid).isEqualTo(25.0)
    }

    @Test
    fun hops_emitsFilteredHops() = runTest {
        val (viewModel, _) = createViewModel()

        val collectJob = backgroundScope.launch {
            viewModel.hops.collect {}
        }
        runCurrent()

        assertThat(viewModel.hops.value).hasSize(1)
        assertThat(viewModel.hops.value[0].name).isEqualTo("Citra")

        collectJob.cancel()
    }
}
