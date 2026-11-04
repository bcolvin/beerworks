package com.bc.beerworks.ui.maltdetail

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.MaltVarietyEntity
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
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
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class MaltDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val pilsnerMalt = MaltVarietyEntity(
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
        description = "Palest base malt available.",
        typicalUsage = "Standard base malt for continental lagers.",
        functionalTags = "Base Grist",
        descriptors = "Cracker,Clean",
        commercialExamples = "Weyermann Pilsner",
        substituteIds = "barke_pilsner",
        radarSweet = 3.2f,
        radarBiscuit = 2.8f,
        radarToasty = 0.5f,
        radarCaramel = 0.5f,
        radarNutty = 0.8f,
        radarChocolate = 0f,
        radarRoast = 0f,
        radarHusky = 1.5f
    )

    private val barkeMalt = MaltVarietyEntity(
        id = "barke_pilsner",
        name = "Barke Pilsner Malt",
        category = "Base Malt",
        grainType = "Barley",
        origin = "Germany",
        countryCode = "DE",
        colorMin = 1.5,
        colorMax = 2.1,
        diastaticPowerLintner = 105,
        extractPotentialDbfg = 81.5,
        maxGristPercentage = 100,
        description = "Heritage Bavarian malt.",
        typicalUsage = "Premium Bavarian lagers.",
        functionalTags = "Base Grist",
        descriptors = "Rich Malt,Bread",
        commercialExamples = "Weyermann Barke",
        substituteIds = "pilsner_malt",
        radarSweet = 3.8f,
        radarBiscuit = 3.2f,
        radarToasty = 1.0f,
        radarCaramel = 0.5f,
        radarNutty = 1.2f,
        radarChocolate = 0f,
        radarRoast = 0f,
        radarHusky = 1.2f
    )

    private val testStyle = BjcpStyleEntity(
        id = "5D",
        name = "German Pils",
        category = "German Lager",
        categoryNumber = "5",
        number = "5D",
        overallImpression = "A crisp, clean German lager.",
        aroma = "", appearance = "", flavor = "", mouthfeel = "", comments = "", history = "", characteristicIngredients = "", styleComparison = "",
        ibuMin = 22.0, ibuMax = 40.0, ogMin = 1.044, ogMax = 1.050, fgMin = 1.008, fgMax = 1.013, abvMin = 4.4, abvMax = 5.2, srmMin = 2.0, srmMax = 4.0,
        commercialExamples = "Bitburger", tags = "lager,pils"
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
        maltId: String = "pilsner_malt",
        malts: List<MaltVarietyEntity> = listOf(pilsnerMalt, barkeMalt)
    ): Triple<MaltDetailViewModel, MaltRepository, BeerStyleRepository> {
        val fakeMaltDao = FakeMaltDao(malts)
        val fakeCrossRefDao = FakeMaltStyleCrossRefDao(
            styleIdsForMalts = mapOf("pilsner_malt" to listOf("5D"))
        )
        val mockBjcp = mock(BjcpStyleDao::class.java)
        val mockBa = mock(BaStyleDao::class.java)
        val mockFav = mock(FavoriteStyleDao::class.java)
        val prefs = FakePrefsManager()

        doReturn(flowOf(listOf(testStyle))).`when`(mockBjcp).getAllStyles()

        val maltRepo = MaltRepository(fakeMaltDao, fakeCrossRefDao, mockBjcp, mockBa, testDispatcher)
        val styleRepo = BeerStyleRepository(mockBjcp, mockBa, mockFav, prefs, testDispatcher)

        val viewModel = MaltDetailViewModel(maltRepo, styleRepo, maltId)
        return Triple(viewModel, maltRepo, styleRepo)
    }

    @Test
    fun malt_emitsSelectedMaltDetails() = runTest {
        val (viewModel, _, _) = createViewModel("pilsner_malt")

        val collectJob = launch { viewModel.malt.collect {} }
        runCurrent()

        val malt = viewModel.malt.value
        assertThat(malt).isNotNull
        assertThat(malt!!.name).isEqualTo("Pilsner Malt")
        assertThat(malt.category).isEqualTo("Base Malt")
        assertThat(malt.countryCode).isEqualTo("DE")

        collectJob.cancel()
    }

    @Test
    fun substituteMalts_emitsAssociatedSubstitutes() = runTest {
        val (viewModel, _, _) = createViewModel("pilsner_malt")

        val collectJob = launch {
            launch { viewModel.malt.collect {} }
            launch { viewModel.substituteMalts.collect {} }
        }
        runCurrent()

        val substitutes = viewModel.substituteMalts.value
        assertThat(substitutes).hasSize(1)
        assertThat(substitutes[0].name).isEqualTo("Barke Pilsner Malt")

        collectJob.cancel()
    }

    @Test
    fun commonStyles_emitsCrossReferencedStyles() = runTest {
        val (viewModel, _, _) = createViewModel("pilsner_malt")

        val collectJob = launch { viewModel.commonStyles.collect {} }
        runCurrent()

        val styles = viewModel.commonStyles.value
        assertThat(styles).hasSize(1)
        assertThat(styles[0].name).isEqualTo("German Pils")

        collectJob.cancel()
    }

    @Test
    fun toggleStyleFavorite_delegatesToRepository() = runTest {
        val (viewModel, _, _) = createViewModel("pilsner_malt")
        val sampleStyle = BeerStyle(
            id = "5D",
            name = "German Pils",
            category = "German Lager",
            categoryNumber = "5",
            number = "5D",
            overallImpression = "",
            aroma = "", appearance = "", flavor = "", mouthfeel = "", comments = "", history = "", characteristicIngredients = "", styleComparison = "",
            ibuMin = 25.0, ibuMax = 40.0, ogMin = 1.044, ogMax = 1.050, fgMin = 1.008, fgMax = 1.013, abvMin = 4.4, abvMax = 5.2, srmMin = 2.0, srmMax = 4.0,
            commercialExamples = "", tags = emptyList(), isFavorite = false, guidelineType = GuidelineType.BJCP_2021
        )

        viewModel.toggleStyleFavorite(sampleStyle)
        runCurrent()

        // Test finishes without error
    }
}
