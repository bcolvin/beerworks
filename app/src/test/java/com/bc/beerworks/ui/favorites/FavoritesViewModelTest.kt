package com.bc.beerworks.ui.favorites

import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.FavoriteIngredientEntity
import com.bc.beerworks.data.local.entity.FavoriteStyleEntity
import com.bc.beerworks.data.local.entity.HoneyVarietyEntity
import com.bc.beerworks.data.local.entity.HopVarietyEntity
import com.bc.beerworks.data.local.entity.MaltVarietyEntity
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import com.bc.beerworks.data.local.entity.OffFlavorEntity
import com.bc.beerworks.data.local.entity.PomeVarietyEntity
import com.bc.beerworks.data.local.entity.YeastStrainEntity
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeBaDao
import com.bc.beerworks.data.repository.FakeBjcpDao
import com.bc.beerworks.data.repository.FakeCiderDao
import com.bc.beerworks.data.repository.FakeFavDao
import com.bc.beerworks.data.repository.FakeFavoriteIngredientDao
import com.bc.beerworks.data.repository.FakeHoneyDao
import com.bc.beerworks.data.repository.FakeHoneyStyleCrossRefDao
import com.bc.beerworks.data.repository.FakeHopDao
import com.bc.beerworks.data.repository.FakeHopStyleCrossRefDao
import com.bc.beerworks.data.repository.FakeMaltDao
import com.bc.beerworks.data.repository.FakeMaltStyleCrossRefDao
import com.bc.beerworks.data.repository.FakeMeadDao
import com.bc.beerworks.data.repository.FakeOffFlavorDao
import com.bc.beerworks.data.repository.FakeOffFlavorStyleCrossRefDao
import com.bc.beerworks.data.repository.FakePomeDao
import com.bc.beerworks.data.repository.FakePomeStyleCrossRefDao
import com.bc.beerworks.data.repository.FakePrefsManager
import com.bc.beerworks.data.repository.FakeYeastDao
import com.bc.beerworks.data.repository.FakeYeastStyleCrossRefDao
import com.bc.beerworks.data.repository.HoneyRepository
import com.bc.beerworks.data.repository.HopRepository
import com.bc.beerworks.data.repository.MaltRepository
import com.bc.beerworks.data.repository.OffFlavorRepository
import com.bc.beerworks.data.repository.PomeRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val sampleBjcpStyle = BjcpStyleEntity(
        id = "21A", name = "American IPA", category = "IPA", categoryNumber = "21", number = "21A",
        overallImpression = "Decidedly hoppy and bitter", aroma = "", appearance = "", flavor = "",
        mouthfeel = "", comments = "", history = "", characteristicIngredients = "", styleComparison = "",
        ibuMin = 40.0, ibuMax = 70.0, ogMin = 1.056, ogMax = 1.070, fgMin = 1.008, fgMax = 1.014,
        abvMin = 5.5, abvMax = 7.5, srmMin = 6.0, srmMax = 14.0, commercialExamples = "", tags = "ipa, hoppy"
    )

    private val sampleMeadStyle = MeadStyleEntity(
        id = "M1A", name = "Dry Mead", category = "Traditional Mead", categoryNumber = "M1", number = "M1A",
        overallImpression = "Classic dry mead", aroma = "", appearance = "", flavor = "", mouthfeel = "",
        comments = "", history = "", characteristicIngredients = "", styleComparison = "", ogMin = 1.075,
        ogMax = 1.140, fgMin = 0.999, fgMax = 1.010, abvMin = 3.5, abvMax = 14.0, sweetness = "Dry",
        carbonation = "Still", strength = "Standard", commercialExamples = "", tags = "mead"
    )

    private val sampleHop = HopVarietyEntity(
        id = "citra", name = "Citra", origin = "USA", countryCode = "US", purpose = "Aroma",
        alphaAcidMin = 11.0, alphaAcidMax = 13.0, betaAcidMin = 3.5, betaAcidMax = 4.5,
        sensoryCategories = "Citrus,Tropical Fruit",
        descriptors = "grapefruit,mango,passionfruit",
        description = "Bright citrus and tropical fruit.",
        parentHopIds = "", substituteIds = "",
        radarCitrus = 5f, radarTropical = 5f, radarFloral = 2f, radarHerbal = 1f,
        radarSpicy = 1f, radarPiney = 2f, radarEarthy = 0f, radarGrassy = 1f
    )

    private val sampleMalt = MaltVarietyEntity(
        id = "maris-otter", name = "Maris Otter Pale Malt", category = "Base Malt",
        grainType = "Barley", origin = "United Kingdom", countryCode = "GB",
        colorMin = 2.5, colorMax = 3.5, diastaticPowerLintner = 60,
        extractPotentialDbfg = 81.0, maxGristPercentage = 100,
        description = "Rich biscuit and nutty character.",
        typicalUsage = "Base grist", functionalTags = "Base Grist",
        descriptors = "biscuit,bready,nutty", commercialExamples = "Crisp Maris Otter",
        substituteIds = "", radarSweet = 4.5f, radarBiscuit = 4.5f, radarToasty = 2.5f,
        radarCaramel = 1.0f, radarNutty = 3.5f, radarChocolate = 0.0f, radarRoast = 0.0f, radarHusky = 2.0f
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
    fun uiState_aggregatesFavoritesAcrossStylesAndIngredients_andFiltersByDisciplineAndQuery() = runTest {
        val favStyleDao = FakeFavDao()
        val favIngredientDao = FakeFavoriteIngredientDao()
        val prefsManager = FakePrefsManager()

        favStyleDao.addFavorite(FavoriteStyleEntity("21A", GuidelineType.BJCP_2021.name))
        favStyleDao.addFavorite(FavoriteStyleEntity("M1A", GuidelineType.BJCP_MEAD.name))
        favIngredientDao.addFavorite(FavoriteIngredientEntity("citra", "HOP"))
        favIngredientDao.addFavorite(FavoriteIngredientEntity("maris-otter", "MALT"))

        val viewModel = createViewModel(
            favStyleDao = favStyleDao,
            favIngredientDao = favIngredientDao,
            prefsManager = prefsManager
        )

        val collectJob = backgroundScope.launch {
            viewModel.uiState.collect {}
        }
        runCurrent()

        val initialState = viewModel.uiState.value
        assertThat(initialState.totalFavoritesCount).isEqualTo(4)
        assertThat(initialState.availableDisciplines).containsExactly(
            FavoriteDiscipline.STYLES,
            FavoriteDiscipline.HOPS,
            FavoriteDiscipline.MALTS
        )
        assertThat(initialState.styles.map { it.id }).containsExactly("21A", "M1A")
        assertThat(initialState.hops.map { it.id }).containsExactly("citra")
        assertThat(initialState.malts.map { it.id }).containsExactly("maris-otter")
        assertThat(initialState.honeys).isEmpty()

        // Filter by HOPS discipline chip
        viewModel.onToggleDiscipline(FavoriteDiscipline.HOPS)
        runCurrent()

        val hopFiltered = viewModel.uiState.value
        assertThat(hopFiltered.styles).isEmpty()
        assertThat(hopFiltered.hops).hasSize(1)
        assertThat(hopFiltered.malts).isEmpty()
        assertThat(hopFiltered.availableDisciplines).containsExactly(
            FavoriteDiscipline.STYLES,
            FavoriteDiscipline.HOPS,
            FavoriteDiscipline.MALTS
        )

        // Reset and filter by search query
        viewModel.resetFilters()
        viewModel.onQueryChange("biscuit")
        runCurrent()

        val queryFiltered = viewModel.uiState.value
        assertThat(queryFiltered.styles).isEmpty()
        assertThat(queryFiltered.hops).isEmpty()
        assertThat(queryFiltered.malts.map { it.id }).containsExactly("maris-otter")

        collectJob.cancel()
    }

    @Test
    fun onRequestRemove_withConfirmationEnabled_promptsModalAndRemovesOnConfirm() = runTest {
        val favStyleDao = FakeFavDao()
        val favIngredientDao = FakeFavoriteIngredientDao()
        val prefsManager = FakePrefsManager()

        favIngredientDao.addFavorite(FavoriteIngredientEntity("citra", "HOP"))
        favIngredientDao.addFavorite(FavoriteIngredientEntity("maris-otter", "MALT"))

        val viewModel = createViewModel(
            favStyleDao = favStyleDao,
            favIngredientDao = favIngredientDao,
            prefsManager = prefsManager
        )

        val uiJob = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val hopTarget = FavoriteItemTarget.IngredientItem(
            key = "hop_citra",
            name = "Citra",
            id = "citra",
            type = "HOP"
        )
        val maltTarget = FavoriteItemTarget.IngredientItem(
            key = "malt_maris-otter",
            name = "Maris Otter Pale Malt",
            id = "maris-otter",
            type = "MALT"
        )

        // Request removal -> should set pendingRemoval and NOT delete yet
        viewModel.onRequestRemove(hopTarget)
        runCurrent()

        assertThat(viewModel.pendingRemoval.value).isEqualTo(hopTarget)
        assertThat(viewModel.uiState.value.hops).hasSize(1)

        // Cancel removal -> clears pendingRemoval, item remains
        viewModel.onCancelRemove()
        runCurrent()

        assertThat(viewModel.pendingRemoval.value).isNull()
        assertThat(viewModel.uiState.value.hops).hasSize(1)

        // Request again and confirm with "always remove without asking" = true
        viewModel.onRequestRemove(hopTarget)
        runCurrent()
        viewModel.onConfirmRemove(alwaysRemoveWithoutAsking = true)
        runCurrent()

        assertThat(viewModel.pendingRemoval.value).isNull()
        assertThat(viewModel.uiState.value.hops).isEmpty()
        assertThat(viewModel.confirmFavoriteRemoval.value).isFalse()

        // Next removal should immediately remove without showing modal
        viewModel.onRequestRemove(maltTarget)
        runCurrent()

        assertThat(viewModel.pendingRemoval.value).isNull()
        assertThat(viewModel.uiState.value.malts).isEmpty()

        uiJob.cancel()
    }

    @Test
    fun onRequestRemove_withConfirmationDisabled_removesImmediatelyWithoutModal() = runTest {
        val favStyleDao = FakeFavDao()
        val favIngredientDao = FakeFavoriteIngredientDao()
        val prefsManager = FakePrefsManager()
        prefsManager.setConfirmFavoriteRemoval(false)

        favStyleDao.addFavorite(FavoriteStyleEntity("21A", GuidelineType.BJCP_2021.name))

        val viewModel = createViewModel(
            favStyleDao = favStyleDao,
            favIngredientDao = favIngredientDao,
            prefsManager = prefsManager
        )

        val uiJob = backgroundScope.launch { viewModel.uiState.collect {} }
        val confirmJob = backgroundScope.launch { viewModel.confirmFavoriteRemoval.collect {} }
        runCurrent()

        val style = viewModel.uiState.value.styles.first()
        viewModel.onRequestRemove(FavoriteItemTarget.Style(style))
        runCurrent()

        assertThat(viewModel.pendingRemoval.value).isNull()
        assertThat(viewModel.uiState.value.styles).isEmpty()

        uiJob.cancel()
        confirmJob.cancel()
    }

    private fun createViewModel(
        favStyleDao: FakeFavDao,
        favIngredientDao: FakeFavoriteIngredientDao,
        prefsManager: FakePrefsManager
    ): FavoritesViewModel {
        val bjcpDao = FakeBjcpDao(listOf(sampleBjcpStyle))
        val baDao = FakeBaDao()
        val meadDao = FakeMeadDao(listOf(sampleMeadStyle))
        val ciderDao = FakeCiderDao()

        val beerStyleRepository = BeerStyleRepository(
            bjcpStyleDao = bjcpDao,
            baStyleDao = baDao,
            favoriteStyleDao = favStyleDao,
            preferencesManager = prefsManager,
            defaultDispatcher = testDispatcher,
            meadStyleDao = meadDao,
            ciderStyleDao = ciderDao
        )

        val hopRepository = HopRepository(
            hopDao = FakeHopDao(listOf(sampleHop)),
            hopStyleCrossRefDao = FakeHopStyleCrossRefDao(),
            bjcpStyleDao = bjcpDao,
            baStyleDao = baDao,
            defaultDispatcher = testDispatcher,
            meadStyleDao = meadDao,
            ciderStyleDao = ciderDao,
            favoriteIngredientDao = favIngredientDao
        )

        val maltRepository = MaltRepository(
            maltDao = FakeMaltDao(listOf(sampleMalt)),
            maltStyleCrossRefDao = FakeMaltStyleCrossRefDao(),
            bjcpStyleDao = bjcpDao,
            baStyleDao = baDao,
            defaultDispatcher = testDispatcher,
            meadStyleDao = meadDao,
            ciderStyleDao = ciderDao,
            favoriteIngredientDao = favIngredientDao
        )

        val honeyRepository = HoneyRepository(
            honeyDao = FakeHoneyDao(emptyList<HoneyVarietyEntity>()),
            honeyStyleCrossRefDao = FakeHoneyStyleCrossRefDao(),
            bjcpStyleDao = bjcpDao,
            baStyleDao = baDao,
            meadStyleDao = meadDao,
            ciderStyleDao = ciderDao,
            defaultDispatcher = testDispatcher,
            favoriteIngredientDao = favIngredientDao
        )

        val pomeRepository = PomeRepository(
            pomeDao = FakePomeDao(emptyList<PomeVarietyEntity>()),
            pomeStyleCrossRefDao = FakePomeStyleCrossRefDao(),
            bjcpStyleDao = bjcpDao,
            baStyleDao = baDao,
            meadStyleDao = meadDao,
            ciderStyleDao = ciderDao,
            defaultDispatcher = testDispatcher,
            favoriteIngredientDao = favIngredientDao
        )

        val yeastRepository = YeastRepository(
            yeastDao = FakeYeastDao(emptyList<YeastStrainEntity>()),
            yeastStyleCrossRefDao = FakeYeastStyleCrossRefDao(),
            bjcpStyleDao = bjcpDao,
            baStyleDao = baDao,
            meadStyleDao = meadDao,
            ciderStyleDao = ciderDao,
            defaultDispatcher = testDispatcher,
            favoriteIngredientDao = favIngredientDao
        )

        val offFlavorRepository = OffFlavorRepository(
            offFlavorDao = FakeOffFlavorDao(
                listOf(
                    OffFlavorEntity(
                        id = "diacetyl",
                        name = "Diacetyl",
                        emoji = "🧈",
                        category = "Fermentation & Yeast",
                        chemicalCompound = "2,3-Butanedione",
                        perceptionThreshold = "0.10 ppm",
                        typicalConcentration = "0.02 ppm",
                        meilgaardCode = "0620",
                        detectedIn = "Aroma,Flavor,Mouthfeel",
                        disciplines = "Beer,Mead,Cider",
                        stylePermissibility = "Style-Permissible",
                        description = "Butter and butterscotch.",
                        aromaPerception = "Melted butter.",
                        flavorPerception = "Butterscotch.",
                        mouthfeelPerception = "Slick palate.",
                        descriptors = "Butter,Butterscotch",
                        diagnosticContrastsJson = "[]",
                        typicalOriginsJson = "[]",
                        preventionTipsJson = "[]",
                        whenAppropriate = "",
                        confusedWithIds = ""
                    )
                )
            ),
            offFlavorStyleCrossRefDao = FakeOffFlavorStyleCrossRefDao(),
            bjcpStyleDao = bjcpDao,
            baStyleDao = baDao,
            meadStyleDao = meadDao,
            ciderStyleDao = ciderDao,
            defaultDispatcher = testDispatcher,
            favoriteIngredientDao = favIngredientDao
        )

        return FavoritesViewModel(
            beerStyleRepository = beerStyleRepository,
            hopRepository = hopRepository,
            maltRepository = maltRepository,
            honeyRepository = honeyRepository,
            pomeRepository = pomeRepository,
            yeastRepository = yeastRepository,
            offFlavorRepository = offFlavorRepository,
            defaultDispatcher = testDispatcher
        )
    }

    @Test
    fun uiState_includesFavoritedOffFlavors_andSupportsDisciplineFiltering() = runTest {
        val favStyleDao = FakeFavDao()
        val favIngredientDao = FakeFavoriteIngredientDao()
        val prefsManager = FakePrefsManager()

        favIngredientDao.addFavorite(FavoriteIngredientEntity("diacetyl", OffFlavorRepository.TYPE_OFF_FLAVOR))

        val viewModel = createViewModel(
            favStyleDao = favStyleDao,
            favIngredientDao = favIngredientDao,
            prefsManager = prefsManager
        )

        val collectJob = backgroundScope.launch {
            viewModel.uiState.collect {}
        }
        runCurrent()

        assertThat(viewModel.uiState.value.offFlavors.map { it.id }).containsExactly("diacetyl")
        assertThat(viewModel.uiState.value.disciplineCounts[FavoriteDiscipline.OFF_FLAVORS]).isEqualTo(1)

        viewModel.onToggleDiscipline(FavoriteDiscipline.OFF_FLAVORS)
        runCurrent()
        assertThat(viewModel.uiState.value.offFlavors.map { it.id }).containsExactly("diacetyl")

        collectJob.cancel()
    }
}
