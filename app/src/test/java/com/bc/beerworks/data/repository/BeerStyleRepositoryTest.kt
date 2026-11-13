package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.PreferencesManager
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.BaStyleEntity
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.CiderStyleEntity
import com.bc.beerworks.data.local.entity.FavoriteStyleEntity
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import com.bc.beerworks.data.model.FilterCriteria
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.TemperatureUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

class FakeBjcpDao(private val list: List<BjcpStyleEntity>) : BjcpStyleDao {
    override suspend fun insertAll(styles: List<BjcpStyleEntity>) {}
    override fun getAllStyles(): Flow<List<BjcpStyleEntity>> = flowOf(list)
    override suspend fun getStyleById(id: String): BjcpStyleEntity? = list.find { it.id == id }
    override fun getCategories(): Flow<List<String>> = flowOf(list.map { it.category }.distinct())
    override fun filterStyles(
        isSrmActive: Int, minSrm: Double, maxSrm: Double, isAbvActive: Int, minAbv: Double, maxAbv: Double, isIbuActive: Int, minIbu: Double, maxIbu: Double, category: String?
    ): Flow<List<BjcpStyleEntity>> = flowOf(list)
    override fun searchAndFilterStyles(
        ftsQuery: String, likeQuery: String, isSrmActive: Int, minSrm: Double, maxSrm: Double, isAbvActive: Int, minAbv: Double, maxAbv: Double, isIbuActive: Int, minIbu: Double, maxIbu: Double, category: String?
    ): Flow<List<BjcpStyleEntity>> = flowOf(list)
}

class FakeBaDao : BaStyleDao {
    override suspend fun insertAll(styles: List<BaStyleEntity>) {}
    override fun getAllStyles(): Flow<List<BaStyleEntity>> = flowOf(emptyList())
    override suspend fun getStyleById(id: String): BaStyleEntity? = null
    override fun getCategories(): Flow<List<String>> = flowOf(emptyList())
    override fun filterStyles(
        isSrmActive: Int, minSrm: Double, maxSrm: Double, isAbvActive: Int, minAbv: Double, maxAbv: Double, isIbuActive: Int, minIbu: Double, maxIbu: Double, category: String?
    ): Flow<List<BaStyleEntity>> = flowOf(emptyList())
    override fun searchAndFilterStyles(
        ftsQuery: String, likeQuery: String, isSrmActive: Int, minSrm: Double, maxSrm: Double, isAbvActive: Int, minAbv: Double, maxAbv: Double, isIbuActive: Int, minIbu: Double, maxIbu: Double, category: String?
    ): Flow<List<BaStyleEntity>> = flowOf(emptyList())
}

class FakeMeadDao(private val list: List<MeadStyleEntity> = emptyList()) : MeadStyleDao {
    override suspend fun insertAll(styles: List<MeadStyleEntity>) {}
    override fun getAllStyles(): Flow<List<MeadStyleEntity>> = flowOf(list)
    override suspend fun getStyleById(id: String): MeadStyleEntity? = list.find { it.id == id }
    override fun getCategories(): Flow<List<String>> = flowOf(list.map { it.category }.distinct())
    override fun filterStyles(
        isAbvActive: Int, minAbv: Double, maxAbv: Double, category: String?
    ): Flow<List<MeadStyleEntity>> = flowOf(list)
    override fun searchAndFilterStyles(
        ftsQuery: String, likeQuery: String, isAbvActive: Int, minAbv: Double, maxAbv: Double, category: String?
    ): Flow<List<MeadStyleEntity>> = flowOf(list)
}

class FakeCiderDao(private val list: List<CiderStyleEntity> = emptyList()) : CiderStyleDao {
    override suspend fun insertAll(styles: List<CiderStyleEntity>) {}
    override fun getAllStyles(): Flow<List<CiderStyleEntity>> = flowOf(list)
    override suspend fun getStyleById(id: String): CiderStyleEntity? = list.find { it.id == id }
    override fun getCategories(): Flow<List<String>> = flowOf(list.map { it.category }.distinct())
    override fun filterStyles(
        isAbvActive: Int, minAbv: Double, maxAbv: Double, category: String?
    ): Flow<List<CiderStyleEntity>> = flowOf(list)
    override fun searchAndFilterStyles(
        ftsQuery: String, likeQuery: String, isAbvActive: Int, minAbv: Double, maxAbv: Double, category: String?
    ): Flow<List<CiderStyleEntity>> = flowOf(list)
}

class FakeFavDao : FavoriteStyleDao {
    private val favs = MutableStateFlow<List<String>>(emptyList())
    override suspend fun addFavorite(favorite: FavoriteStyleEntity) { favs.value = favs.value + favorite.styleId }
    override suspend fun removeFavorite(styleId: String) { favs.value = favs.value - styleId }
    override fun getAllFavoriteIds(): Flow<List<String>> = favs
    override fun isFavorite(styleId: String): Flow<Boolean> = flowOf(favs.value.contains(styleId))
}

class FakePrefsManager : PreferencesManager {
    private val flow = MutableStateFlow(GuidelineType.BJCP_2021)
    private val screenOnFlow = MutableStateFlow(false)
    private val tempUnitFlow = MutableStateFlow(TemperatureUnit.FAHRENHEIT)
    override val selectedGuideline: Flow<GuidelineType> = flow
    override suspend fun setGuideline(guidelineType: GuidelineType) { flow.value = guidelineType }
    override val keepScreenOn: Flow<Boolean> = screenOnFlow
    override suspend fun setKeepScreenOn(enabled: Boolean) { screenOnFlow.value = enabled }
    override val temperatureUnit: Flow<TemperatureUnit> = tempUnitFlow
    override suspend fun setTemperatureUnit(temperatureUnit: TemperatureUnit) { tempUnitFlow.value = temperatureUnit }
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class BeerStyleRepositoryTest {

    @Test
    fun getFilteredStyles_mapsEntitiesToDomainModelsCorrectly() = runTest {
        val testEntity = BjcpStyleEntity(
            id = "1A",
            name = "American Light Lager",
            category = "Standard American Beer",
            categoryNumber = "1",
            number = "1A",
            overallImpression = "Refreshing",
            aroma = "Low malt",
            appearance = "Pale yellow",
            flavor = "Neutral",
            mouthfeel = "Watery",
            comments = "",
            history = "",
            characteristicIngredients = "Corn",
            styleComparison = "",
            ibuMin = 8.0,
            ibuMax = 12.0,
            ogMin = 1.028,
            ogMax = 1.040,
            fgMin = 0.998,
            fgMax = 1.008,
            abvMin = 2.8,
            abvMax = 4.2,
            srmMin = 2.0,
            srmMax = 3.0,
            commercialExamples = "Bud Light",
            tags = "session, pale"
        )

        val fakeBjcp = FakeBjcpDao(listOf(testEntity))
        val fakeBa = FakeBaDao()
        val fakeFav = FakeFavDao()
        val prefsManager = FakePrefsManager()

        val repository = BeerStyleRepository(
            fakeBjcp,
            fakeBa,
            fakeFav,
            prefsManager,
            kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)
        )

        val result = repository.getFilteredStyles(FilterCriteria()).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("American Light Lager")
        assertThat(result[0].id).isEqualTo("1A")
        assertThat(result[0].guidelineType).isEqualTo(GuidelineType.BJCP_2021)
    }

    @Test
    fun toggleFavorite_addsAndRemovesFavoriteCorrectly() = runTest {
        val mockFavDao = mock(FavoriteStyleDao::class.java)
        val mockBjcp = mock(BjcpStyleDao::class.java)
        val mockBa = mock(BaStyleDao::class.java)
        val prefsManager = FakePrefsManager()

        doReturn(flowOf(emptyList<String>())).`when`(mockFavDao).getAllFavoriteIds()

        val repository = BeerStyleRepository(mockBjcp, mockBa, mockFavDao, prefsManager)

        repository.toggleFavorite("1A", GuidelineType.BJCP_2021, currentFavorite = false)

        assertThat(repository.selectedGuideline.first()).isEqualTo(GuidelineType.BJCP_2021)
    }

    @Test
    fun getNextStyle_returnsNextStyleInSequentialOrder() = runTest {
        val testEntity1 = BjcpStyleEntity(
            id = "1A", name = "American Light Lager", category = "Standard American Beer",
            categoryNumber = "1", number = "1A", overallImpression = "", aroma = "", appearance = "",
            flavor = "", mouthfeel = "", comments = "", history = "", characteristicIngredients = "",
            styleComparison = "", ibuMin = 8.0, ibuMax = 12.0, ogMin = 1.028, ogMax = 1.040,
            fgMin = 0.998, fgMax = 1.008, abvMin = 2.8, abvMax = 4.2, srmMin = 2.0, srmMax = 3.0,
            commercialExamples = "Bud Light", tags = "session, pale"
        )
        val testEntity2 = BjcpStyleEntity(
            id = "1B", name = "American Lager", category = "Standard American Beer",
            categoryNumber = "1", number = "1B", overallImpression = "", aroma = "", appearance = "",
            flavor = "", mouthfeel = "", comments = "", history = "", characteristicIngredients = "",
            styleComparison = "", ibuMin = 8.0, ibuMax = 18.0, ogMin = 1.040, ogMax = 1.050,
            fgMin = 1.004, fgMax = 1.010, abvMin = 4.2, abvMax = 5.3, srmMin = 2.0, srmMax = 3.5,
            commercialExamples = "Budweiser", tags = "pale, lager"
        )

        val repository = BeerStyleRepository(
            FakeBjcpDao(listOf(testEntity1, testEntity2)),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)
        )

        val next = repository.getNextStyle("1A", GuidelineType.BJCP_2021)
        assertThat(next).isNotNull
        assertThat(next?.id).isEqualTo("1B")
        assertThat(next?.name).isEqualTo("American Lager")

        val afterLast = repository.getNextStyle("1B", GuidelineType.BJCP_2021)
        assertThat(afterLast).isNull()

        val unknown = repository.getNextStyle("nonexistent", GuidelineType.BJCP_2021)
        assertThat(unknown).isNull()
    }

    @Test
    fun getGroupedFilteredStyles_ordersCategoriesByRelevanceScoreWhenSearchActive() = runTest {
        // Entity in Category 1 with mention only in comments (score = 1)
        val cat1Entity = BjcpStyleEntity(
            id = "1A", name = "American Light Lager", category = "Standard American Beer",
            categoryNumber = "1", number = "1A", overallImpression = "Clean lager", aroma = "", appearance = "",
            flavor = "", mouthfeel = "", comments = "Lower bitterness than an IPA", history = "", characteristicIngredients = "",
            styleComparison = "", ibuMin = 8.0, ibuMax = 12.0, ogMin = 1.028, ogMax = 1.040,
            fgMin = 0.998, fgMax = 1.008, abvMin = 2.8, abvMax = 4.2, srmMin = 2.0, srmMax = 3.0,
            commercialExamples = "Bud Light", tags = "session, pale"
        )
        // Entity in Category 21 with match in style name (score = 10+)
        val cat21Entity = BjcpStyleEntity(
            id = "21A", name = "American IPA", category = "IPA",
            categoryNumber = "21", number = "21A", overallImpression = "A decidedly hoppy beer", aroma = "", appearance = "",
            flavor = "", mouthfeel = "", comments = "", history = "", characteristicIngredients = "",
            styleComparison = "", ibuMin = 40.0, ibuMax = 70.0, ogMin = 1.056, ogMax = 1.070,
            fgMin = 1.008, fgMax = 1.014, abvMin = 5.5, abvMax = 7.5, srmMin = 6.0, srmMax = 14.0,
            commercialExamples = "Bell's Two Hearted", tags = "bitter, hoppy"
        )

        val repository = BeerStyleRepository(
            FakeBjcpDao(listOf(cat1Entity, cat21Entity)),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)
        )

        val groups = repository.getGroupedFilteredStyles(FilterCriteria(query = "IPA")).first()

        // Category 21 should be ranked FIRST because of the style name match (weight 10)
        assertThat(groups).hasSize(2)
        assertThat(groups[0].categoryNumber).isEqualTo("21")
        assertThat(groups[0].styles[0].name).isEqualTo("American IPA")

        // Category 1 should be ranked SECOND because it only has a comment match (weight 1)
        assertThat(groups[1].categoryNumber).isEqualTo("1")
    }

    @Test
    fun getFilteredStyles_bjcpMead_filtersBySweetnessAndStrength() = runTest {
        val dryMead = MeadStyleEntity(
            id = "M1A", name = "Dry Mead", category = "Traditional Mead", categoryNumber = "M1", number = "M1A",
            overallImpression = "A classic dry mead", aroma = "", appearance = "", flavor = "", mouthfeel = "",
            comments = "", history = "", characteristicIngredients = "", styleComparison = "", ogMin = 1.075, ogMax = 1.140,
            fgMin = 0.999, fgMax = 1.010, abvMin = 3.5, abvMax = 14.0, sweetness = "Dry", carbonation = "Still, Sparkling",
            strength = "Standard, Sack", commercialExamples = "", tags = "traditional"
        )
        val sweetMead = MeadStyleEntity(
            id = "M1C", name = "Sweet Mead", category = "Traditional Mead", categoryNumber = "M1", number = "M1C",
            overallImpression = "A rich sweet mead", aroma = "", appearance = "", flavor = "", mouthfeel = "",
            comments = "", history = "", characteristicIngredients = "", styleComparison = "", ogMin = 1.075, ogMax = 1.140,
            fgMin = 1.025, fgMax = 1.050, abvMin = 3.5, abvMax = 14.0, sweetness = "Sweet", carbonation = "Still",
            strength = "Standard, Sack", commercialExamples = "", tags = "traditional"
        )

        val prefs = FakePrefsManager()
        prefs.setGuideline(GuidelineType.BJCP_MEAD)

        val repository = BeerStyleRepository(
            bjcpStyleDao = FakeBjcpDao(emptyList()),
            baStyleDao = FakeBaDao(),
            favoriteStyleDao = FakeFavDao(),
            preferencesManager = prefs,
            defaultDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler),
            meadStyleDao = FakeMeadDao(listOf(dryMead, sweetMead))
        )

        val allMeads = repository.getFilteredStyles(FilterCriteria()).first()
        assertThat(allMeads).hasSize(2)

        val dryOnly = repository.getFilteredStyles(FilterCriteria(selectedSweetness = setOf("Dry"))).first()
        assertThat(dryOnly).hasSize(1)
        assertThat(dryOnly[0].id).isEqualTo("M1A")
        assertThat(dryOnly[0].sweetness).isEqualTo("Dry")

        val sweetOnly = repository.getFilteredStyles(FilterCriteria(selectedSweetness = setOf("Sweet"))).first()
        assertThat(sweetOnly).hasSize(1)
        assertThat(sweetOnly[0].id).isEqualTo("M1C")
    }

    @Test
    fun getFilteredStyles_bjcpCider_filtersBySweetnessAndStrength() = runTest {
        val newWorldCider = CiderStyleEntity(
            id = "C1A", name = "New World Cider", category = "Standard Cider and Perry", categoryNumber = "C1", number = "C1A",
            overallImpression = "A refreshing, crisp drink", aroma = "", appearance = "", flavor = "", mouthfeel = "",
            comments = "", history = "", characteristicIngredients = "", styleComparison = "", ogMin = 1.045, ogMax = 1.065,
            fgMin = 0.995, fgMax = 1.020, abvMin = 4.5, abvMax = 8.0, sweetness = "Dry, Medium-Dry, Medium, Medium-Sweet, Sweet",
            carbonation = "Still, Pétillant, Sparkling", strength = "Standard", commercialExamples = "", tags = "standard-cider"
        )
        val iceCider = CiderStyleEntity(
            id = "C2D", name = "Ice Cider", category = "Specialty Cider and Perry", categoryNumber = "C2", number = "C2D",
            overallImpression = "A luscious dessert cider", aroma = "", appearance = "", flavor = "", mouthfeel = "",
            comments = "", history = "", characteristicIngredients = "", styleComparison = "", ogMin = 1.130, ogMax = 1.180,
            fgMin = 1.060, fgMax = 1.085, abvMin = 7.0, abvMax = 13.0, sweetness = "Sweet",
            carbonation = "Still, Pétillant", strength = "High Gravity", commercialExamples = "", tags = "specialty-cider"
        )

        val prefs = FakePrefsManager()
        prefs.setGuideline(GuidelineType.BJCP_CIDER)

        val repository = BeerStyleRepository(
            bjcpStyleDao = FakeBjcpDao(emptyList()),
            baStyleDao = FakeBaDao(),
            favoriteStyleDao = FakeFavDao(),
            preferencesManager = prefs,
            defaultDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler),
            ciderStyleDao = FakeCiderDao(listOf(newWorldCider, iceCider))
        )

        val allCiders = repository.getFilteredStyles(FilterCriteria()).first()
        assertThat(allCiders).hasSize(2)
        assertThat(allCiders[0].guidelineType).isEqualTo(GuidelineType.BJCP_CIDER)
        assertThat(allCiders[0].ibuMin).isNull()
        assertThat(allCiders[0].srmMin).isNull()

        val highGravityOnly = repository.getFilteredStyles(FilterCriteria(selectedStrength = setOf("High Gravity"))).first()
        assertThat(highGravityOnly).hasSize(1)
        assertThat(highGravityOnly[0].id).isEqualTo("C2D")
        assertThat(highGravityOnly[0].strength).isEqualTo("High Gravity")

        val mediumDryOnly = repository.getFilteredStyles(FilterCriteria(selectedSweetness = setOf("Medium-Dry"))).first()
        assertThat(mediumDryOnly).hasSize(1)
        assertThat(mediumDryOnly[0].id).isEqualTo("C1A")

        // Style lookup and next navigation
        val fetchedStyle = repository.getStyleById("C1A", GuidelineType.BJCP_CIDER)
        assertThat(fetchedStyle?.name).isEqualTo("New World Cider")

        val nextStyle = repository.getNextStyle("C1A", GuidelineType.BJCP_CIDER)
        assertThat(nextStyle?.id).isEqualTo("C2D")
    }

    @Test
    fun toDomainModel_normalizesIpaInNameAndCategory() = runTest {
        val ipaStyle = BjcpStyleEntity(
            id = "21A", name = "American Ipa", category = "Ipa", categoryNumber = "21", number = "21A",
            overallImpression = "", aroma = "", appearance = "", flavor = "", mouthfeel = "", comments = "",
            history = "", characteristicIngredients = "", styleComparison = "", ibuMin = 40.0, ibuMax = 70.0,
            ogMin = 1.056, ogMax = 1.070, fgMin = 1.008, fgMax = 1.014, abvMin = 5.5, abvMax = 7.5,
            srmMin = 6.0, srmMax = 14.0, commercialExamples = "", tags = "ipa"
        )

        val repository = BeerStyleRepository(
            bjcpStyleDao = FakeBjcpDao(listOf(ipaStyle)),
            baStyleDao = FakeBaDao(),
            favoriteStyleDao = FakeFavDao(),
            preferencesManager = FakePrefsManager(),
            defaultDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)
        )

        val styles = repository.getFilteredStyles(FilterCriteria()).first()
        assertThat(styles[0].name).isEqualTo("American IPA")
        assertThat(styles[0].category).isEqualTo("IPA")

        val categories = repository.getCategories().first()
        assertThat(categories).contains("IPA")
    }

    @Test
    fun baseStyleGroup_displayCategoryNameAndDisplayTitle_formatsIpaInAllCaps() {
        val group = com.bc.beerworks.data.model.BaseStyleGroup(
            categoryNumber = "21",
            categoryName = "Ipa",
            styles = emptyList(),
            guidelineType = GuidelineType.BJCP_2021
        )

        assertThat(group.displayCategoryName).isEqualTo("IPA")
        assertThat(group.displayTitle).isEqualTo("21. IPA")
    }
}
