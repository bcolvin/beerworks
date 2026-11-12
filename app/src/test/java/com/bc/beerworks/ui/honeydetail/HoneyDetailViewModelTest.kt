package com.bc.beerworks.ui.honeydetail

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.HoneyVarietyEntity
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
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
class HoneyDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val orangeBlossom = HoneyVarietyEntity(
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

    private val tupelo = HoneyVarietyEntity(
        id = "tupelo",
        name = "Tupelo Honey",
        floralClassification = "Monofloral",
        botanicalSource = "Nyssa ogeche",
        origin = "Florida, United States",
        countryCode = "US",
        pfundMin = 25.0,
        pfundMax = 45.0,
        usdaColor = "White",
        moistureTypicalPercent = 17.0,
        fgRatio = 1.48,
        crystallizationTendency = "Slow",
        extractPotentialSg = 1.037,
        flavorIntensity = "Moderate",
        description = "Queen of honeys.",
        fermentationBehavior = "High fructose stability.",
        pairingNotes = "Stone fruits.",
        descriptors = "Ripe Pear, Cinnamon",
        substituteIds = "orange_blossom",
        radarFloral = 4.5f,
        radarFruity = 4.0f,
        radarHerbal = 1.8f,
        radarSpicy = 2.2f,
        radarSweet = 4.2f,
        radarWoody = 0.8f,
        radarEarthy = 0.5f,
        radarPungent = 0.0f
    )

    private val meadStyle = MeadStyleEntity(
        id = "M1A",
        name = "Dry Mead",
        category = "Traditional Mead",
        categoryNumber = "M1",
        number = "M1A",
        overallImpression = "A subtle, dry fermented honey beverage",
        aroma = "Honey aroma should be subtle",
        appearance = "Water-white to dark amber",
        flavor = "Subtle honey character",
        mouthfeel = "Light to medium body",
        comments = "",
        history = "",
        characteristicIngredients = "Varietal honey",
        styleComparison = "",
        ogMin = 1.035,
        ogMax = 1.080,
        fgMin = 0.998,
        fgMax = 1.008,
        abvMin = 3.5,
        abvMax = 14.0,
        sweetness = "Dry",
        carbonation = "Still, Pétillant, Sparkling",
        strength = "Hydromel, Standard, Sack",
        commercialExamples = "",
        tags = "traditional-mead, dry"
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
        honeyId: String = "orange_blossom"
    ): HoneyDetailViewModel {
        val fakeHoneyDao = FakeHoneyDao(listOf(orangeBlossom, tupelo))
        val fakeCrossRefDao = FakeHoneyStyleCrossRefDao(
            styleIdsForHoneys = mapOf("orange_blossom" to listOf("M1A"))
        )
        val mockBjcp = mock(BjcpStyleDao::class.java)
        val mockBa = mock(BaStyleDao::class.java)
        val fakeMead = FakeMeadDao(listOf(meadStyle))
        val fakeCider = FakeCiderDao()
        val fakeFav = FakeFavDao()
        val prefs = FakePrefsManager()

        val honeyRepo = HoneyRepository(
            fakeHoneyDao,
            fakeCrossRefDao,
            mockBjcp,
            mockBa,
            fakeMead,
            fakeCider,
            testDispatcher
        )
        val styleRepo = BeerStyleRepository(
            mockBjcp,
            mockBa,
            fakeFav,
            prefs,
            testDispatcher,
            fakeMead,
            fakeCider
        )

        return HoneyDetailViewModel(honeyRepo, styleRepo, honeyId)
    }

    @Test
    fun loadsHoneyAndSubstitutesCorrectly() = runTest {
        val viewModel = createViewModel("orange_blossom")

        val honeyJob = launch { viewModel.honey.collect {} }
        val subsJob = launch { viewModel.substituteHoneys.collect {} }
        val stylesJob = launch { viewModel.commonStyles.collect {} }
        runCurrent()

        assertThat(viewModel.honey.value?.name).isEqualTo("Orange Blossom Honey")
        assertThat(viewModel.substituteHoneys.value).hasSize(1)
        assertThat(viewModel.substituteHoneys.value.first().name).isEqualTo("Tupelo Honey")
        assertThat(viewModel.commonStyles.value).hasSize(1)
        assertThat(viewModel.commonStyles.value.first().name).isEqualTo("Dry Mead")

        honeyJob.cancel()
        subsJob.cancel()
        stylesJob.cancel()
    }
}
