package com.bc.beerworks.ui.yeastdetail

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.YeastStrainEntity
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.TemperatureUnit
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeBaDao
import com.bc.beerworks.data.repository.FakeBjcpDao
import com.bc.beerworks.data.repository.FakeFavDao
import com.bc.beerworks.data.repository.FakePrefsManager
import com.bc.beerworks.data.repository.FakeYeastDao
import com.bc.beerworks.data.repository.FakeYeastStyleCrossRefDao
import com.bc.beerworks.data.repository.YeastRepository
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
class YeastDetailViewModelTest {

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
        substituteIds = "california-v",
        radarClean = 4.8f,
        radarFruity = 1.5f,
        radarSpicy = 0.0f,
        radarTart = 0.5f,
        radarFunky = 0.0f,
        radarSulfury = 1.0f,
        radarAlcohol = 1.5f,
        radarMouthfeel = 2.0f
    )

    private val calV = YeastStrainEntity(
        id = "california-v",
        name = "California V",
        scientificName = "Saccharomyces cerevisiae",
        origin = "California, USA",
        countryCode = "US",
        discipline = "Beer",
        classification = "Ale",
        form = "Liquid",
        attenuationMin = 76.0,
        attenuationMax = 83.0,
        flocculation = "High",
        tempMinC = 18.0,
        tempMaxC = 21.0,
        tempMinF = 65.0,
        tempMaxF = 70.0,
        alcoholTolerance = 12.0,
        isDiastaticus = false,
        isPhenolic = false,
        description = "Super clean ale strain.",
        pitchingDynamics = "Fast.",
        descriptors = "clean, crisp",
        commercialEquivalentsJson = "[]",
        substituteIds = "chico-american-ale",
        radarClean = 5.0f,
        radarFruity = 1.0f,
        radarSpicy = 0.0f,
        radarTart = 0.5f,
        radarFunky = 0.0f,
        radarSulfury = 1.0f,
        radarAlcohol = 1.5f,
        radarMouthfeel = 1.5f
    )

    private lateinit var yeastRepository: YeastRepository
    private lateinit var beerStyleRepository: BeerStyleRepository
    private lateinit var prefsManager: FakePrefsManager
    private lateinit var mockBjcp: BjcpStyleDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        prefsManager = FakePrefsManager()
        mockBjcp = mock(BjcpStyleDao::class.java)

        yeastRepository = YeastRepository(
            yeastDao = FakeYeastDao(listOf(chico, calV)),
            yeastStyleCrossRefDao = FakeYeastStyleCrossRefDao(
                styleIdsForYeasts = mapOf("chico-american-ale" to listOf("18B"))
            ),
            bjcpStyleDao = mockBjcp,
            baStyleDao = mock(BaStyleDao::class.java),
            defaultDispatcher = testDispatcher
        )

        beerStyleRepository = BeerStyleRepository(
            FakeBjcpDao(emptyList()),
            FakeBaDao(),
            FakeFavDao(),
            prefsManager,
            testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun yeast_emitsCorrectDetails() = runTest {
        val viewModel = YeastDetailViewModel(
            yeastRepository = yeastRepository,
            beerStyleRepository = beerStyleRepository,
            preferencesManager = prefsManager,
            yeastId = "chico-american-ale"
        )

        val collectJob = backgroundScope.launch {
            viewModel.yeast.collect {}
        }
        runCurrent()

        assertThat(viewModel.yeast.value).isNotNull()
        assertThat(viewModel.yeast.value?.id).isEqualTo("chico-american-ale")
        assertThat(viewModel.yeast.value?.name).isEqualTo("Chico / American Ale")
        collectJob.cancel()
    }

    @Test
    fun temperatureUnit_emitsConfiguredUnit() = runTest {
        val viewModel = YeastDetailViewModel(
            yeastRepository = yeastRepository,
            beerStyleRepository = beerStyleRepository,
            preferencesManager = prefsManager,
            yeastId = "chico-american-ale"
        )

        val collectJob = backgroundScope.launch {
            viewModel.temperatureUnit.collect {}
        }
        runCurrent()

        assertThat(viewModel.temperatureUnit.value).isEqualTo(TemperatureUnit.FAHRENHEIT)

        prefsManager.setTemperatureUnit(TemperatureUnit.CELSIUS)
        runCurrent()

        assertThat(viewModel.temperatureUnit.value).isEqualTo(TemperatureUnit.CELSIUS)
        collectJob.cancel()
    }

    @Test
    fun substituteYeasts_emitsSubstitutes() = runTest {
        val viewModel = YeastDetailViewModel(
            yeastRepository = yeastRepository,
            beerStyleRepository = beerStyleRepository,
            preferencesManager = prefsManager,
            yeastId = "chico-american-ale"
        )

        val collectJob = backgroundScope.launch {
            viewModel.substituteYeasts.collect {}
        }
        runCurrent()

        assertThat(viewModel.substituteYeasts.value).hasSize(1)
        assertThat(viewModel.substituteYeasts.value[0].id).isEqualTo("california-v")
        collectJob.cancel()
    }

    @Test
    fun commonStyles_emitsAssociatedStyles() = runTest {
        val styleEntity = BjcpStyleEntity(
            id = "18B",
            name = "American Pale Ale",
            category = "Pale American Ale",
            categoryNumber = "18",
            number = "18B",
            overallImpression = "",
            aroma = "",
            appearance = "",
            flavor = "",
            mouthfeel = "",
            comments = "",
            history = "",
            characteristicIngredients = "",
            styleComparison = "",
            ibuMin = 30.0,
            ibuMax = 50.0,
            srmMin = 5.0,
            srmMax = 10.0,
            ogMin = 1.045,
            ogMax = 1.060,
            fgMin = 1.010,
            fgMax = 1.015,
            abvMin = 4.5,
            abvMax = 6.2,
            commercialExamples = "",
            tags = ""
        )
        doReturn(flowOf(listOf(styleEntity))).`when`(mockBjcp).getAllStyles()

        val viewModel = YeastDetailViewModel(
            yeastRepository = yeastRepository,
            beerStyleRepository = beerStyleRepository,
            preferencesManager = prefsManager,
            yeastId = "chico-american-ale"
        )

        val collectJob = backgroundScope.launch {
            viewModel.commonStyles.collect {}
        }
        runCurrent()

        assertThat(viewModel.commonStyles.value).hasSize(1)
        assertThat(viewModel.commonStyles.value[0].id).isEqualTo("18B")
        collectJob.cancel()
    }
}
