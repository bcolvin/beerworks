package com.bc.beerworks.ui.yeasts

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.entity.YeastStrainEntity
import com.bc.beerworks.data.model.TemperatureUnit
import com.bc.beerworks.data.model.YeastFilterCriteria
import com.bc.beerworks.data.repository.FakePrefsManager
import com.bc.beerworks.data.repository.FakeYeastDao
import com.bc.beerworks.data.repository.FakeYeastStyleCrossRefDao
import com.bc.beerworks.data.repository.YeastRepository
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
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class YeastsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val chico = YeastStrainEntity(
        id = "chico-american-ale",
        name = "Chico / American Ale",
        scientificName = "Saccharomyces cerevisiae",
        origin = "Chico, California, USA",
        countryCode = "US",
        discipline = "Beer",
        classification = "Ale",
        form = "Both",
        attenuationMin = 73.0,
        attenuationMax = 80.0,
        flocculation = "Medium",
        tempMinC = 18.0,
        tempMaxC = 22.0,
        tempMinF = 64.0,
        tempMaxF = 72.0,
        alcoholTolerance = 11.0,
        isDiastaticus = false,
        isPhenolic = false,
        description = "Clean American ale.",
        pitchingDynamics = "Rapid start.",
        descriptors = "clean, crisp",
        commercialEquivalentsJson = "[]",
        substituteIds = "",
        radarClean = 4.8f,
        radarFruity = 1.5f,
        radarSpicy = 0.0f,
        radarTart = 0.5f,
        radarFunky = 0.0f,
        radarSulfury = 1.0f,
        radarAlcohol = 1.5f,
        radarMouthfeel = 2.0f
    )

    private val ec1118 = YeastStrainEntity(
        id = "lalvin-ec-1118",
        name = "Montpellier / Lalvin EC-1118",
        scientificName = "Saccharomyces cerevisiae",
        origin = "Montpellier, France",
        countryCode = "FR",
        discipline = "Multi-Discipline",
        classification = "Wine / Mead / Cider",
        form = "Dry",
        attenuationMin = 90.0,
        attenuationMax = 100.0,
        flocculation = "High",
        tempMinC = 10.0,
        tempMaxC = 30.0,
        tempMinF = 50.0,
        tempMaxF = 86.0,
        alcoholTolerance = 18.0,
        isDiastaticus = false,
        isPhenolic = false,
        description = "Champagne strain.",
        pitchingDynamics = "Vigorous killer factor.",
        descriptors = "neutral, crisp",
        commercialEquivalentsJson = "[]",
        substituteIds = "",
        radarClean = 5.0f,
        radarFruity = 1.0f,
        radarSpicy = 0.0f,
        radarTart = 1.5f,
        radarFunky = 0.0f,
        radarSulfury = 2.2f,
        radarAlcohol = 3.8f,
        radarMouthfeel = 0.2f
    )

    private lateinit var yeastRepository: YeastRepository
    private lateinit var prefsManager: FakePrefsManager
    private lateinit var viewModel: YeastsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        prefsManager = FakePrefsManager()
        yeastRepository = YeastRepository(
            yeastDao = FakeYeastDao(listOf(chico, ec1118)),
            yeastStyleCrossRefDao = FakeYeastStyleCrossRefDao(),
            bjcpStyleDao = mock(BjcpStyleDao::class.java),
            baStyleDao = mock(BaStyleDao::class.java),
            defaultDispatcher = testDispatcher
        )
        viewModel = YeastsViewModel(yeastRepository, prefsManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsAllYeastsAndDefaults() = runTest {
        val collectJob = backgroundScope.launch {
            viewModel.yeasts.collect {}
        }
        runCurrent()

        assertThat(viewModel.yeasts.value).hasSize(2)
        assertThat(viewModel.filterCriteria.value).isEqualTo(YeastFilterCriteria())
        assertThat(viewModel.temperatureUnit.value).isEqualTo(TemperatureUnit.FAHRENHEIT)
        collectJob.cancel()
    }

    @Test
    fun onQueryChange_updatesQueryInCriteria() = runTest {
        viewModel.onQueryChange("Chico")
        runCurrent()

        assertThat(viewModel.filterCriteria.value.query).isEqualTo("Chico")
    }

    @Test
    fun onClassificationSelected_togglesClassification() = runTest {
        viewModel.onClassificationSelected("Ale")
        runCurrent()
        assertThat(viewModel.filterCriteria.value.selectedClassification).isEqualTo("Ale")

        // Toggling same classification clears it
        viewModel.onClassificationSelected("Ale")
        runCurrent()
        assertThat(viewModel.filterCriteria.value.selectedClassification).isNull()
    }

    @Test
    fun onFlocculationSelected_togglesFlocculation() = runTest {
        viewModel.onFlocculationSelected("High")
        runCurrent()
        assertThat(viewModel.filterCriteria.value.selectedFlocculation).isEqualTo("High")

        viewModel.onFlocculationSelected("High")
        runCurrent()
        assertThat(viewModel.filterCriteria.value.selectedFlocculation).isNull()
    }

    @Test
    fun onMinAttenuationChange_and_onMinAlcoholToleranceChange_updateCriteria() = runTest {
        viewModel.onMinAttenuationChange(80.0)
        viewModel.onMinAlcoholToleranceChange(12.0)
        runCurrent()

        assertThat(viewModel.filterCriteria.value.minAttenuation).isEqualTo(80.0)
        assertThat(viewModel.filterCriteria.value.minAlcoholTolerance).isEqualTo(12.0)
    }

    @Test
    fun resetFilters_preservesQueryAndClearsOtherCriteria() = runTest {
        viewModel.onQueryChange("search")
        viewModel.onClassificationSelected("Ale")
        viewModel.onMinAttenuationChange(75.0)
        runCurrent()

        viewModel.resetFilters()
        runCurrent()

        assertThat(viewModel.filterCriteria.value).isEqualTo(YeastFilterCriteria(query = "search"))
        assertThat(viewModel.filterCriteria.value.selectedClassification).isNull()
        assertThat(viewModel.filterCriteria.value.minAttenuation).isNull()
    }

    @Test
    fun temperatureUnit_reactsToPreferencesChange() = runTest {
        val collectJob = backgroundScope.launch {
            viewModel.temperatureUnit.collect {}
        }
        runCurrent()

        prefsManager.setTemperatureUnit(TemperatureUnit.CELSIUS)
        runCurrent()

        assertThat(viewModel.temperatureUnit.value).isEqualTo(TemperatureUnit.CELSIUS)
        collectJob.cancel()
    }
}
