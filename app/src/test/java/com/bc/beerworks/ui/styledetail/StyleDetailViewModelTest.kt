package com.bc.beerworks.ui.styledetail

import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.model.MaltVariety
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
import com.bc.beerworks.ui.ingredientdetail.shouldExpandAssociatedStylesByDefault
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

    @Test
    fun baseStyleDependentIngredientNote_returnsExpectedMessageOnlyForBjcpStylesWithMissingHopsOrMalts() {
        val bjcpStyle = BeerStyle(
            id = "29A", name = "Fruit Beer", category = "Fruit Beer", categoryNumber = "29",
            number = "29A", overallImpression = "", aroma = "", appearance = "", flavor = "",
            mouthfeel = "", comments = "", history = "", characteristicIngredients = "",
            styleComparison = "", ibuMin = null, ibuMax = null, ogMin = null, ogMax = null,
            fgMin = null, fgMax = null, abvMin = null, abvMax = null, srmMin = null, srmMax = null,
            commercialExamples = "", tags = emptyList(), guidelineType = GuidelineType.BJCP_2021
        )
        val baStyle = bjcpStyle.copy(guidelineType = GuidelineType.BA_2026)
        val hop = HopVariety(
            id = "cascade",
            name = "Cascade",
            origin = "US",
            countryCode = "US",
            purpose = "Aroma",
            alphaAcidMin = 4.5,
            alphaAcidMax = 7.0,
            betaAcidMin = 4.5,
            betaAcidMax = 7.0,
            sensoryCategories = emptyList(),
            descriptors = emptyList(),
            description = "",
            parentHopIds = emptyList(),
            substituteIds = emptyList(),
            radarProfile = null
        )
        val malt = MaltVariety(
            id = "malted_rye",
            name = "Malted Rye",
            category = "Base Malt",
            grainType = "Rye",
            origin = "DE",
            countryCode = "DE",
            colorMin = 3.0,
            colorMax = 4.0,
            diastaticPowerLintner = 100,
            extractPotentialDbfg = 80.0,
            maxGristPercentage = 40,
            description = "",
            typicalUsage = "",
            functionalTags = emptyList(),
            descriptors = emptyList(),
            commercialExamples = emptyList(),
            substituteIds = emptyList(),
            radarProfile = null
        )

        assertThat(baseStyleDependentIngredientNote(bjcpStyle, StyleIngredients()))
            .isEqualTo("Common Malts & Hops: Varies depending on the declared base style.")
        assertThat(
            baseStyleDependentIngredientNote(
                bjcpStyle,
                StyleIngredients(malts = listOf(malt))
            )
        )
            .isEqualTo("Common Hops: Varies depending on the declared base style.")
        assertThat(
            baseStyleDependentIngredientNote(
                bjcpStyle,
                StyleIngredients(hops = listOf(hop))
            )
        )
            .isEqualTo("Common Malts & Grains: Varies depending on the declared base style.")
        assertThat(
            baseStyleDependentIngredientNote(
                bjcpStyle,
                StyleIngredients(hops = listOf(hop), malts = listOf(malt))
            )
        )
            .isNull()
        assertThat(baseStyleDependentIngredientNote(baStyle, StyleIngredients()))
            .isNull()
    }

    @Test
    fun shouldExpandAssociatedStylesByDefault_collapsesAboveThreshold() {
        assertThat(shouldExpandAssociatedStylesByDefault(1)).isTrue()
        assertThat(shouldExpandAssociatedStylesByDefault(4)).isTrue()
        assertThat(shouldExpandAssociatedStylesByDefault(5)).isFalse()
        assertThat(shouldExpandAssociatedStylesByDefault(25)).isFalse()
    }
}
