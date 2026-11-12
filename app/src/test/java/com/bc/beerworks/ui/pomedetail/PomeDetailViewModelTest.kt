package com.bc.beerworks.ui.pomedetail

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.CiderStyleEntity
import com.bc.beerworks.data.local.entity.PomeVarietyEntity
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeBaDao
import com.bc.beerworks.data.repository.FakeCiderDao
import com.bc.beerworks.data.repository.FakeFavDao
import com.bc.beerworks.data.repository.FakeMeadDao
import com.bc.beerworks.data.repository.FakePomeDao
import com.bc.beerworks.data.repository.FakePomeStyleCrossRefDao
import com.bc.beerworks.data.repository.FakePrefsManager
import com.bc.beerworks.data.repository.PomeRepository
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
class PomeDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dabinett = PomeVarietyEntity(
        id = "dabinett",
        name = "Dabinett",
        fruitType = "Apple",
        classification = "Bittersweet",
        fruitUse = "Heritage Cider/Perry",
        botanicalName = "Malus domestica",
        origin = "Somerset, England",
        countryCode = "GB",
        acidityMalicPercent = 0.18,
        tanninPolyphenolPercent = 0.35,
        tanninLevel = "High",
        sgMin = 1.050,
        sgMax = 1.058,
        harvestSeason = "Late",
        sorbitolGramsPerLiter = null,
        description = "English bittersweet apple.",
        blendingGuidance = "Single varietal cider.",
        pressingCharacteristics = "Presses cleanly.",
        descriptors = "Soft Tannin, Baked Apple",
        substituteIds = "yarlington-mill",
        radarAcidity = 1.5f,
        radarTannin = 3.8f,
        radarFreshFruit = 3.0f,
        radarBakedFruit = 4.0f,
        radarCitrus = 1.2f,
        radarFloral = 2.2f,
        radarEarthy = 2.8f,
        radarSweet = 3.5f
    )

    private val yarlingtonMill = PomeVarietyEntity(
        id = "yarlington-mill",
        name = "Yarlington Mill",
        fruitType = "Apple",
        classification = "Bittersweet",
        fruitUse = "Heritage Cider/Perry",
        botanicalName = "Malus domestica",
        origin = "Somerset, England",
        countryCode = "GB",
        acidityMalicPercent = 0.22,
        tanninPolyphenolPercent = 0.32,
        tanninLevel = "High",
        sgMin = 1.050,
        sgMax = 1.056,
        harvestSeason = "Late",
        sorbitolGramsPerLiter = null,
        description = "Smooth bittersweet cider apple.",
        blendingGuidance = "Single varietal or blend.",
        pressingCharacteristics = "High yield.",
        descriptors = "Caramel, Licorice",
        substituteIds = "dabinett",
        radarAcidity = 1.8f,
        radarTannin = 3.5f,
        radarFreshFruit = 2.8f,
        radarBakedFruit = 4.2f,
        radarCitrus = 1.0f,
        radarFloral = 2.5f,
        radarEarthy = 2.2f,
        radarSweet = 3.8f
    )

    private val englishCider = CiderStyleEntity(
        id = "C1B",
        name = "English Cider",
        category = "Standard Cider and Perry",
        categoryNumber = "C1",
        number = "C1B",
        overallImpression = "Traditional English cider.",
        aroma = "Bittersweet apple.",
        appearance = "Golden amber.",
        flavor = "Bittersweet tannins.",
        mouthfeel = "Full body.",
        comments = "West Country.",
        history = "Historic.",
        characteristicIngredients = "Bittersweet apples.",
        styleComparison = "More tannic.",
        ogMin = 1.050,
        ogMax = 1.065,
        fgMin = 0.995,
        fgMax = 1.015,
        abvMin = 6.0,
        abvMax = 8.5,
        sweetness = "Dry",
        carbonation = "Sparkling",
        strength = "Standard",
        commercialExamples = "Dunkertons",
        tags = "traditional-cider"
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
        pomeId: String = "dabinett",
        pomes: List<PomeVarietyEntity> = listOf(dabinett, yarlingtonMill),
        styleIdsForPome: List<String> = listOf("C1B")
    ): Pair<PomeDetailViewModel, FakeFavDao> {
        val fakePomeDao = FakePomeDao(pomes)
        val fakeCrossRefDao = FakePomeStyleCrossRefDao(
            styleIdsForPomes = mapOf(pomeId to styleIdsForPome)
        )
        val mockBjcp = mock(BjcpStyleDao::class.java)
        val mockBa = mock(BaStyleDao::class.java)
        val mockMead = mock(MeadStyleDao::class.java)
        val mockCider = mock(CiderStyleDao::class.java)
        val fakeFavDao = FakeFavDao()
        val prefs = FakePrefsManager()

        doReturn(flowOf(listOf(englishCider))).`when`(mockCider).getAllStyles()

        val pomeRepo = PomeRepository(
            fakePomeDao,
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
            fakeFavDao,
            prefs,
            testDispatcher,
            mockMead,
            mockCider
        )

        val viewModel = PomeDetailViewModel(pomeRepo, styleRepo, pomeId)
        return viewModel to fakeFavDao
    }

    @Test
    fun pome_loadsMatchingVariety() = runTest {
        val (viewModel, _) = createViewModel("dabinett")
        val job = launch { viewModel.pome.collect {} }
        runCurrent()

        val pome = viewModel.pome.value
        assertThat(pome).isNotNull
        assertThat(pome?.name).isEqualTo("Dabinett")
        assertThat(pome?.classification).isEqualTo("Bittersweet")
        assertThat(pome?.acidityText).isEqualTo("0.18%")

        job.cancel()
    }

    @Test
    fun substitutePomes_loadsMatchingSubstitutes() = runTest {
        val (viewModel, _) = createViewModel("dabinett")
        val pomeJob = launch { viewModel.pome.collect {} }
        val subJob = launch { viewModel.substitutePomes.collect {} }
        runCurrent()

        val subs = viewModel.substitutePomes.value
        assertThat(subs).hasSize(1)
        assertThat(subs.first().id).isEqualTo("yarlington-mill")

        pomeJob.cancel()
        subJob.cancel()
    }

    @Test
    fun commonStyles_loadsReferencedStyles() = runTest {
        val (viewModel, _) = createViewModel("dabinett", styleIdsForPome = listOf("C1B"))
        val job = launch { viewModel.commonStyles.collect {} }
        runCurrent()

        val styles = viewModel.commonStyles.value
        assertThat(styles).hasSize(1)
        assertThat(styles.first().id).isEqualTo("C1B")
        assertThat(styles.first().name).isEqualTo("English Cider")

        job.cancel()
    }
}
