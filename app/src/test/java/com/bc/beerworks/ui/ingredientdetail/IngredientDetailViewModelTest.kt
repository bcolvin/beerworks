package com.bc.beerworks.ui.ingredientdetail

import com.bc.beerworks.data.local.dao.HopStyleCrossRefDao
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.CiderStyleEntity
import com.bc.beerworks.data.local.entity.HopStyleCrossRef
import com.bc.beerworks.data.local.entity.HopVarietyEntity
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.model.TemperatureUnit
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeBaDao
import com.bc.beerworks.data.repository.FakeBjcpDao
import com.bc.beerworks.data.repository.FakeCiderDao
import com.bc.beerworks.data.repository.FakeFavDao
import com.bc.beerworks.data.repository.FakeFavoriteIngredientDao
import com.bc.beerworks.data.repository.FakeHopDao
import com.bc.beerworks.data.repository.FakeMeadDao
import com.bc.beerworks.data.repository.FakePrefsManager
import com.bc.beerworks.data.repository.HopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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

private class GuidelineAwareHopStyleCrossRefDao(
    private val styleIdsByHopAndGuideline: Map<Pair<String, String>, List<String>> = emptyMap()
) : HopStyleCrossRefDao {
    override suspend fun insertAll(crossRefs: List<HopStyleCrossRef>) {}

    override fun getStyleIdsForHop(hopId: String, guidelineType: String): Flow<List<String>> =
        flowOf(styleIdsByHopAndGuideline[hopId to guidelineType] ?: emptyList())

    override fun getHopIdsForStyle(styleId: String, guidelineType: String): Flow<List<String>> =
        flowOf(emptyList())
}

@OptIn(ExperimentalCoroutinesApi::class)
class IngredientDetailViewModelTest {

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

    private val bitterStyle = BjcpStyleEntity(
        id = "11A",
        name = "Ordinary Bitter",
        category = "British Bitter",
        categoryNumber = "11",
        number = "11A",
        overallImpression = "",
        aroma = "",
        appearance = "",
        flavor = "",
        mouthfeel = "",
        comments = "",
        history = "",
        characteristicIngredients = "",
        styleComparison = "",
        ibuMin = 25.0,
        ibuMax = 35.0,
        ogMin = 1.030,
        ogMax = 1.039,
        fgMin = 1.007,
        fgMax = 1.011,
        abvMin = 3.2,
        abvMax = 3.8,
        srmMin = 8.0,
        srmMax = 14.0,
        commercialExamples = "Fuller's Chiswick Bitter",
        tags = "bitter, session"
    )

    private val braggotStyle = MeadStyleEntity(
        id = "M4A",
        name = "Braggot",
        category = "Specialty Mead",
        categoryNumber = "M4",
        number = "M4A",
        overallImpression = "",
        aroma = "",
        appearance = "",
        flavor = "",
        mouthfeel = "",
        comments = "",
        history = "",
        characteristicIngredients = "",
        styleComparison = "",
        ogMin = 1.060,
        ogMax = 1.120,
        fgMin = 1.004,
        fgMax = 1.025,
        abvMin = 6.0,
        abvMax = 14.0,
        sweetness = "Varies",
        carbonation = "Varies",
        strength = "Standard",
        commercialExamples = "",
        tags = "braggot"
    )

    private val hoppedCiderStyle = CiderStyleEntity(
        id = "C2E",
        name = "Cider with Herbs/Spices",
        category = "Specialty Cider and Perry",
        categoryNumber = "C2",
        number = "C2E",
        overallImpression = "",
        aroma = "",
        appearance = "",
        flavor = "",
        mouthfeel = "",
        comments = "",
        history = "",
        characteristicIngredients = "",
        styleComparison = "",
        ogMin = 1.045,
        ogMax = 1.070,
        fgMin = 0.995,
        fgMax = 1.020,
        abvMin = 5.0,
        abvMax = 9.0,
        sweetness = "Varies",
        carbonation = "Sparkling",
        strength = "Standard",
        commercialExamples = "",
        tags = "cider"
    )

    private lateinit var prefsManager: FakePrefsManager
    private lateinit var fakeFavStyleDao: FakeFavDao
    private lateinit var fakeFavIngredientDao: FakeFavoriteIngredientDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        prefsManager = FakePrefsManager()
        fakeFavStyleDao = FakeFavDao()
        fakeFavIngredientDao = FakeFavoriteIngredientDao()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        hopId: String = "fuggle"
    ): IngredientDetailViewModel<HopVariety> {
        val fakeHopDao = FakeHopDao(listOf(fuggleHop, cascadeHop, willametteHop))
        val crossRefDao = GuidelineAwareHopStyleCrossRefDao(
            styleIdsByHopAndGuideline = mapOf(
                ("fuggle" to GuidelineType.BJCP_2021.name) to listOf("11A"),
                ("fuggle" to GuidelineType.BJCP_MEAD.name) to listOf("M4A"),
                ("fuggle" to GuidelineType.BJCP_CIDER.name) to listOf("C2E")
            )
        )
        val bjcpDao = FakeBjcpDao(listOf(bitterStyle))
        val baDao = FakeBaDao()
        val meadDao = FakeMeadDao(listOf(braggotStyle))
        val ciderDao = FakeCiderDao(listOf(hoppedCiderStyle))

        val beerStyleRepository = BeerStyleRepository(
            bjcpStyleDao = bjcpDao,
            baStyleDao = baDao,
            favoriteStyleDao = fakeFavStyleDao,
            preferencesManager = prefsManager,
            defaultDispatcher = testDispatcher,
            meadStyleDao = meadDao,
            ciderStyleDao = ciderDao
        )

        val hopRepository = HopRepository(
            hopDao = fakeHopDao,
            hopStyleCrossRefDao = crossRefDao,
            styleLookup = beerStyleRepository,
            defaultDispatcher = testDispatcher,
            favoriteIngredientDao = fakeFavIngredientDao
        )

        return IngredientDetailViewModel(
            repository = hopRepository,
            beerStyleRepository = beerStyleRepository,
            itemId = hopId,
            relatedItemsProvider = hopRepository::getChildHops,
            preferencesManager = prefsManager
        )
    }

    @Test
    fun item_loadsTargetIngredient() = runTest {
        val viewModel = createViewModel("fuggle")
        val collectJob = backgroundScope.launch { viewModel.item.collect {} }
        runCurrent()

        val item = viewModel.item.value
        assertThat(item).isNotNull
        assertThat(item?.id).isEqualTo("fuggle")
        assertThat(item?.name).isEqualTo("Fuggle")
        assertThat(item?.countryFlag).isEqualTo("🇬🇧")

        collectJob.cancel()
    }

    @Test
    fun relatedItems_loadsProgenyViaProvider() = runTest {
        val viewModel = createViewModel("fuggle")
        val collectJob = backgroundScope.launch { viewModel.relatedItems.collect {} }
        runCurrent()

        val children = viewModel.relatedItems.value
        assertThat(children.map { it.name }).containsExactlyInAnyOrder("Cascade", "Willamette")

        collectJob.cancel()
    }

    @Test
    fun substitutes_loadsAlternativeIngredients() = runTest {
        val viewModel = createViewModel("fuggle")
        val itemJob = backgroundScope.launch { viewModel.item.collect {} }
        val subJob = backgroundScope.launch { viewModel.substitutes.collect {} }
        runCurrent()

        val substitutes = viewModel.substitutes.value
        assertThat(substitutes).hasSize(1)
        assertThat(substitutes.first().name).isEqualTo("Willamette")

        itemJob.cancel()
        subJob.cancel()
    }

    @Test
    fun commonStyles_resolvesAcrossMultipleGuidelinesInCanonicalOrder() = runTest {
        val viewModel = createViewModel("fuggle")
        val collectJob = backgroundScope.launch { viewModel.commonStyles.collect {} }
        runCurrent()

        val styles = viewModel.commonStyles.value
        assertThat(styles.map { it.id }).containsExactly("11A", "M4A", "C2E")
        assertThat(styles.map { it.guidelineType }).containsExactly(
            GuidelineType.BJCP_2021,
            GuidelineType.BJCP_MEAD,
            GuidelineType.BJCP_CIDER
        )

        collectJob.cancel()
    }

    @Test
    fun toggleFavorite_togglesIngredientFavoriteState() = runTest {
        val viewModel = createViewModel("fuggle")
        val collectJob = backgroundScope.launch { viewModel.isFavorite.collect {} }
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
    fun toggleStyleFavorite_reactivelyUpdatesCommonStylesFavoriteState() = runTest {
        val viewModel = createViewModel("fuggle")
        val collectJob = backgroundScope.launch { viewModel.commonStyles.collect {} }
        runCurrent()

        val initialStyle = viewModel.commonStyles.value.first { it.id == "11A" }
        assertThat(initialStyle.isFavorite).isFalse()

        viewModel.toggleStyleFavorite(initialStyle)
        runCurrent()

        val favoritedStyle = viewModel.commonStyles.value.first { it.id == "11A" }
        assertThat(favoritedStyle.isFavorite).isTrue()

        viewModel.toggleStyleFavorite(favoritedStyle)
        runCurrent()

        val unfavoritedStyle = viewModel.commonStyles.value.first { it.id == "11A" }
        assertThat(unfavoritedStyle.isFavorite).isFalse()

        collectJob.cancel()
    }

    @Test
    fun temperatureUnit_emitsConfiguredUnitAndReactsToUpdates() = runTest {
        val viewModel = createViewModel("fuggle")
        val collectJob = backgroundScope.launch { viewModel.temperatureUnit.collect {} }
        runCurrent()

        assertThat(viewModel.temperatureUnit.value).isEqualTo(TemperatureUnit.FAHRENHEIT)

        prefsManager.setTemperatureUnit(TemperatureUnit.CELSIUS)
        runCurrent()

        assertThat(viewModel.temperatureUnit.value).isEqualTo(TemperatureUnit.CELSIUS)

        collectJob.cancel()
    }
}
