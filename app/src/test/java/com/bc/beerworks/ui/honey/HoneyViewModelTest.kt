package com.bc.beerworks.ui.honey

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.HoneyVarietyEntity
import com.bc.beerworks.data.model.HoneyFilterCriteria
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeBaDao
import com.bc.beerworks.data.repository.FakeCiderDao
import com.bc.beerworks.data.repository.FakeFavDao
import com.bc.beerworks.data.repository.FakeHoneyDao
import com.bc.beerworks.data.repository.FakeHoneyStyleCrossRefDao
import com.bc.beerworks.data.repository.FakeMeadDao
import com.bc.beerworks.data.repository.FakePrefsManager
import com.bc.beerworks.data.repository.HoneyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class HoneyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testHoney = HoneyVarietyEntity(
        id = "orange_blossom",
        name = "Orange Blossom Honey",
        floralClassification = "Monofloral",
        botanicalSource = "Citrus sinensis",
        origin = "California, United States",
        countryCode = "US",
        pfundMin = 20.0,
        pfundMax = 45.0,
        usdaColor = "White",
        moistureTypicalPercent = 16.8,
        fgRatio = 1.25,
        crystallizationTendency = "Moderate",
        extractPotentialSg = 1.037,
        flavorIntensity = "Moderate",
        description = "Citrus blossom notes.",
        fermentationBehavior = "Clean cool fermentation.",
        pairingNotes = "Peaches and ginger.",
        descriptors = "Citrus Blossom, Orange Zest",
        substituteIds = "tupelo",
        radarFloral = 4.8f,
        radarFruity = 4.2f,
        radarHerbal = 1.5f,
        radarSpicy = 1.0f,
        radarSweet = 3.8f,
        radarWoody = 0.5f,
        radarEarthy = 0.5f,
        radarPungent = 0.0f
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
        honeys: List<HoneyVarietyEntity> = listOf(testHoney)
    ): HoneyViewModel {
        val fakeHoneyDao = FakeHoneyDao(honeys)
        val fakeCrossRefDao = FakeHoneyStyleCrossRefDao()
        val mockBjcp = mock(BjcpStyleDao::class.java)
        val mockBa = mock(BaStyleDao::class.java)
        val mockMead = mock(MeadStyleDao::class.java)
        val mockCider = mock(CiderStyleDao::class.java)
        val mockFav = mock(FavoriteStyleDao::class.java)
        val prefs = FakePrefsManager()

        doReturn(flowOf(listOf("Traditional Mead", "Fruit Mead"))).`when`(mockMead).getCategories()

        val honeyRepo = HoneyRepository(
            fakeHoneyDao,
            fakeCrossRefDao,
            mockBjcp,
            mockBa,
            mockMead,
            mockCider,
            testDispatcher
        )
        val styleRepo = BeerStyleRepository(
            mockBjcp,
            mockBa,
            mockFav,
            prefs,
            testDispatcher,
            mockMead,
            mockCider
        )

        return HoneyViewModel(honeyRepo, styleRepo)
    }

    @Test
    fun onQueryChange_updatesFilterCriteria() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("Orange")

        assertThat(viewModel.filterCriteria.value.query).isEqualTo("Orange")
    }

    @Test
    fun onClassificationSelected_togglesClassification() = runTest {
        val viewModel = createViewModel()

        viewModel.onClassificationSelected("Monofloral")
        assertThat(viewModel.filterCriteria.value.selectedClassification).isEqualTo("Monofloral")

        viewModel.onClassificationSelected("Monofloral")
        assertThat(viewModel.filterCriteria.value.selectedClassification).isNull()
    }

    @Test
    fun resetFilters_clearsFiltersPreservingQuery() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("Blossom")
        viewModel.onClassificationSelected("Monofloral")
        viewModel.onFlavorIntensitySelected("Moderate")

        viewModel.resetFilters()

        assertThat(viewModel.filterCriteria.value.query).isEqualTo("Blossom")
        assertThat(viewModel.filterCriteria.value.selectedClassification).isNull()
        assertThat(viewModel.filterCriteria.value.selectedFlavorIntensity).isNull()
    }
}
