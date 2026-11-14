package com.bc.beerworks.ui.styledetail

import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.model.StyleIngredients
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeBaDao
import com.bc.beerworks.data.repository.FakeBjcpDao
import com.bc.beerworks.data.repository.FakeFavDao
import com.bc.beerworks.data.repository.FakePrefsManager
import com.bc.beerworks.data.repository.HoneyRepository
import com.bc.beerworks.data.repository.HopRepository
import com.bc.beerworks.data.repository.IngredientCatalogRepository
import com.bc.beerworks.data.repository.MaltRepository
import com.bc.beerworks.data.repository.PomeRepository
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
class StyleDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testEntity1 = BjcpStyleEntity(
        id = "1A", name = "American Light Lager", category = "Standard American Beer",
        categoryNumber = "1", number = "1A", overallImpression = "Clean and crisp", aroma = "Low malt",
        appearance = "Pale yellow", flavor = "Neutral", mouthfeel = "Light", comments = "", history = "",
        characteristicIngredients = "Corn", styleComparison = "", ibuMin = 8.0, ibuMax = 12.0,
        ogMin = 1.028, ogMax = 1.040, fgMin = 0.998, fgMax = 1.008, abvMin = 2.8, abvMax = 4.2,
        srmMin = 2.0, srmMax = 3.0, commercialExamples = "Bud Light", tags = "session, pale"
    )

    private val testEntity2 = BjcpStyleEntity(
        id = "1B", name = "American Lager", category = "Standard American Beer",
        categoryNumber = "1", number = "1B", overallImpression = "Refreshing", aroma = "Grainy",
        appearance = "Gold", flavor = "Crisp", mouthfeel = "Medium-light", comments = "", history = "",
        characteristicIngredients = "Rice", styleComparison = "", ibuMin = 8.0, ibuMax = 18.0,
        ogMin = 1.040, ogMax = 1.050, fgMin = 1.004, fgMax = 1.010, abvMin = 4.2, abvMax = 5.3,
        srmMin = 2.0, srmMax = 3.5, commercialExamples = "Budweiser", tags = "pale, lager"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadStyle_loadsCurrentAndNextStyleInSequentialOrder() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(listOf(testEntity1, testEntity2)),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            testDispatcher
        )

        val viewModel = StyleDetailViewModel(
            repository = repository,
            styleId = "1A",
            guidelineType = GuidelineType.BJCP_2021
        )

        runCurrent()

        val style = viewModel.style.value
        assertThat(style).isNotNull
        assertThat(style?.id).isEqualTo("1A")
        assertThat(style?.name).isEqualTo("American Light Lager")

        val next = viewModel.nextStyle.value
        assertThat(next).isNotNull
        assertThat(next?.id).isEqualTo("1B")
        assertThat(next?.name).isEqualTo("American Lager")
    }

    @Test
    fun loadStyle_nextStyleIsNullWhenAtLastStyle() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(listOf(testEntity1, testEntity2)),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            testDispatcher
        )

        val viewModel = StyleDetailViewModel(
            repository = repository,
            styleId = "1B",
            guidelineType = GuidelineType.BJCP_2021
        )

        runCurrent()

        val style = viewModel.style.value
        assertThat(style).isNotNull
        assertThat(style?.id).isEqualTo("1B")

        val next = viewModel.nextStyle.value
        assertThat(next).isNull()
    }

    @Test
    fun toggleFavorite_togglesFavoriteState() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(listOf(testEntity1)),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            testDispatcher
        )

        val viewModel = StyleDetailViewModel(
            repository = repository,
            styleId = "1A",
            guidelineType = GuidelineType.BJCP_2021
        )

        runCurrent()

        assertThat(viewModel.style.value?.isFavorite).isFalse()

        viewModel.toggleFavorite()
        runCurrent()

        assertThat(viewModel.style.value?.isFavorite).isTrue()

        viewModel.toggleFavorite()
        runCurrent()

        assertThat(viewModel.style.value?.isFavorite).isFalse()
    }

    @Test
    fun ingredients_emitsAggregatedStyleIngredientsFromCatalogRepository() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(listOf(testEntity1)),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            testDispatcher
        )

        val hopRepo = mock(HopRepository::class.java)
        val maltRepo = mock(MaltRepository::class.java)
        val honeyRepo = mock(HoneyRepository::class.java)
        val pomeRepo = mock(PomeRepository::class.java)
        val yeastRepo = mock(YeastRepository::class.java)

        val cascadeHop = HopVariety(
            id = "cascade",
            name = "Cascade",
            origin = "United States",
            countryCode = "US",
            purpose = "Dual-Purpose",
            alphaAcidMin = 4.5,
            alphaAcidMax = 7.0,
            betaAcidMin = 4.5,
            betaAcidMax = 7.0,
            sensoryCategories = listOf("Citrus"),
            descriptors = listOf("Grapefruit"),
            description = "Classic American hop",
            parentHopIds = emptyList(),
            substituteIds = emptyList(),
            radarProfile = null
        )

        doReturn(flowOf(listOf(cascadeHop))).`when`(hopRepo).getItemsForStyle("1A", GuidelineType.BJCP_2021)
        doReturn(flowOf(emptyList<Any>())).`when`(maltRepo).getItemsForStyle("1A", GuidelineType.BJCP_2021)
        doReturn(flowOf(emptyList<Any>())).`when`(honeyRepo).getItemsForStyle("1A", GuidelineType.BJCP_2021)
        doReturn(flowOf(emptyList<Any>())).`when`(pomeRepo).getItemsForStyle("1A", GuidelineType.BJCP_2021)
        doReturn(flowOf(emptyList<Any>())).`when`(yeastRepo).getItemsForStyle("1A", GuidelineType.BJCP_2021)

        val catalogRepository = IngredientCatalogRepository(
            hopRepository = hopRepo,
            maltRepository = maltRepo,
            honeyRepository = honeyRepo,
            pomeRepository = pomeRepo,
            yeastRepository = yeastRepo,
            defaultDispatcher = testDispatcher
        )

        val viewModel = StyleDetailViewModel(
            repository = repository,
            ingredientCatalogRepository = catalogRepository,
            styleId = "1A",
            guidelineType = GuidelineType.BJCP_2021
        )

        backgroundScope.launch { viewModel.ingredients.collect {} }
        runCurrent()

        assertThat(viewModel.ingredients.value).isEqualTo(
            StyleIngredients(hops = listOf(cascadeHop))
        )
    }
}
