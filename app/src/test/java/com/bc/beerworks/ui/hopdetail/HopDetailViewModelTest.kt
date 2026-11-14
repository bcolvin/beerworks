package com.bc.beerworks.ui.hopdetail

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.FavoriteIngredientDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.HopVarietyEntity
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeFavoriteIngredientDao
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
class HopDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fuggleHop = HopVarietyEntity(
        id = "fuggle",
        name = "Fuggle",
        origin = "United Kingdom",
        countryCode = "GB",
        purpose = "Aroma",
        alphaAcidMin = 4.0,
        alphaAcidMax = 5.5,
        betaAcidMin = 2.0,
        betaAcidMax = 3.0,
        sensoryCategories = "Earthy/Woody,Herbal",
        descriptors = "Earthy,Woody",
        description = "Classic English aroma hop.",
        parentHopIds = "",
        substituteIds = "willamette",
        radarCitrus = 0.8f,
        radarTropical = 0.2f,
        radarFloral = 3.2f,
        radarHerbal = 3.9f,
        radarSpicy = 2.8f,
        radarPiney = 1.2f,
        radarEarthy = 4.8f,
        radarGrassy = 3.0f
    )

    private val cascadeHop = HopVarietyEntity(
        id = "cascade",
        name = "Cascade",
        origin = "United States",
        countryCode = "US",
        purpose = "Aroma",
        alphaAcidMin = 4.5,
        alphaAcidMax = 7.5,
        betaAcidMin = 4.5,
        betaAcidMax = 7.0,
        sensoryCategories = "Citrus,Floral",
        descriptors = "Grapefruit,Floral",
        description = "Classic American hop.",
        parentHopIds = "fuggle",
        substituteIds = "centennial",
        radarCitrus = 4.5f,
        radarTropical = 1.8f,
        radarFloral = 4.2f,
        radarHerbal = 2.0f,
        radarSpicy = 2.6f,
        radarPiney = 2.8f,
        radarEarthy = 1.5f,
        radarGrassy = 1.8f
    )

    private val willametteHop = HopVarietyEntity(
        id = "willamette",
        name = "Willamette",
        origin = "United States",
        countryCode = "US",
        purpose = "Aroma",
        alphaAcidMin = 4.0,
        alphaAcidMax = 6.0,
        betaAcidMin = 3.0,
        betaAcidMax = 4.5,
        sensoryCategories = "Floral,Herbal",
        descriptors = "Floral,Spice",
        description = "US Fuggle derivative.",
        parentHopIds = "fuggle",
        substituteIds = "fuggle",
        radarCitrus = 1.2f,
        radarTropical = 0.5f,
        radarFloral = 4.2f,
        radarHerbal = 3.8f,
        radarSpicy = 3.4f,
        radarPiney = 1.5f,
        radarEarthy = 4.1f,
        radarGrassy = 2.2f
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
        hopId: String,
        fakeFavIngredientDao: FavoriteIngredientDao = FakeFavoriteIngredientDao()
    ): Pair<HopDetailViewModel, HopRepository> {
        val fakeHopDao = FakeHopDao(listOf(fuggleHop, cascadeHop, willametteHop))
        val fakeCrossRefDao = FakeHopStyleCrossRefDao(
            styleIdsForHops = mapOf("fuggle" to listOf("11A"))
        )
        val mockBjcp = mock(BjcpStyleDao::class.java)
        val mockBa = mock(BaStyleDao::class.java)
        val mockFav = mock(FavoriteStyleDao::class.java)
        val prefs = FakePrefsManager()

        val styleEntity = BjcpStyleEntity(
            id = "11A", name = "Ordinary Bitter", category = "British Bitter",
            categoryNumber = "11", number = "11A", overallImpression = "", aroma = "", appearance = "",
            flavor = "", mouthfeel = "", comments = "", history = "", characteristicIngredients = "",
            styleComparison = "", ibuMin = 25.0, ibuMax = 35.0, ogMin = 1.030, ogMax = 1.039,
            fgMin = 1.007, fgMax = 1.011, abvMin = 3.2, abvMax = 3.8, srmMin = 8.0, srmMax = 14.0,
            commercialExamples = "Fuller's Chiswick Bitter", tags = "bitter, session"
        )
        doReturn(flowOf(listOf(styleEntity))).`when`(mockBjcp).getAllStyles()

        val hopRepo = HopRepository(
            fakeHopDao,
            fakeCrossRefDao,
            mockBjcp,
            mockBa,
            testDispatcher,
            favoriteIngredientDao = fakeFavIngredientDao
        )
        val styleRepo = BeerStyleRepository(mockBjcp, mockBa, mockFav, prefs, testDispatcher)

        val viewModel = HopDetailViewModel(hopRepo, styleRepo, hopId)
        return viewModel to hopRepo
    }

    @Test
    fun toggleFavorite_togglesFavoriteState() = runTest {
        val fakeFav = FakeFavoriteIngredientDao()
        val (viewModel, _) = createViewModel("fuggle", fakeFav)

        val collectJob = backgroundScope.launch {
            viewModel.isFavorite.collect {}
        }
        runCurrent()

        assertThat(viewModel.isFavorite.value).isFalse()

        viewModel.toggleFavorite()
        runCurrent()
        assertThat(viewModel.isFavorite.value).isTrue()

        viewModel.toggleFavorite()
        runCurrent()
        assertThat(viewModel.isFavorite.value).isFalse()

        collectJob.cancel()
    }

    @Test
    fun hop_loadsCorrectHopProfile() = runTest {
        val (viewModel, _) = createViewModel("fuggle")

        val collectJob = backgroundScope.launch {
            viewModel.hop.collect {}
        }
        runCurrent()

        val hop = viewModel.hop.value
        assertThat(hop).isNotNull
        assertThat(hop?.name).isEqualTo("Fuggle")
        assertThat(hop?.countryFlag).isEqualTo("🇬🇧")

        collectJob.cancel()
    }

    @Test
    fun childHops_loadsDescendantsOfTargetHop() = runTest {
        val (viewModel, _) = createViewModel("fuggle")

        val collectJob = backgroundScope.launch {
            viewModel.childHops.collect {}
        }
        runCurrent()

        val children = viewModel.childHops.value
        assertThat(children).hasSize(2)
        val childNames = children.map { it.name }
        assertThat(childNames).containsExactlyInAnyOrder("Cascade", "Willamette")

        collectJob.cancel()
    }

    @Test
    fun substituteHops_loadsAlternatives() = runTest {
        val (viewModel, _) = createViewModel("fuggle")

        val collectHopJob = backgroundScope.launch {
            viewModel.hop.collect {}
        }
        val collectSubJob = backgroundScope.launch {
            viewModel.substituteHops.collect {}
        }
        runCurrent()

        val subs = viewModel.substituteHops.value
        assertThat(subs).hasSize(1)
        assertThat(subs[0].name).isEqualTo("Willamette")

        collectHopJob.cancel()
        collectSubJob.cancel()
    }

    @Test
    fun commonStyles_loadsAssociatedBeerStyles() = runTest {
        val (viewModel, _) = createViewModel("fuggle")

        val collectJob = backgroundScope.launch {
            viewModel.commonStyles.collect {}
        }
        runCurrent()

        val styles = viewModel.commonStyles.value
        assertThat(styles).hasSize(1)
        assertThat(styles[0].name).isEqualTo("Ordinary Bitter")

        collectJob.cancel()
    }
}
