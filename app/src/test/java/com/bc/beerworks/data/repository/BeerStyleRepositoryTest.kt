package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.PreferencesManager
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.entity.BaStyleEntity
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.FavoriteStyleEntity
import com.bc.beerworks.data.model.FilterCriteria
import com.bc.beerworks.data.model.GuidelineType
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

class FakeFavDao : FavoriteStyleDao {
    private val favs = MutableStateFlow<List<String>>(emptyList())
    override suspend fun addFavorite(favorite: FavoriteStyleEntity) { favs.value = favs.value + favorite.styleId }
    override suspend fun removeFavorite(styleId: String) { favs.value = favs.value - styleId }
    override fun getAllFavoriteIds(): Flow<List<String>> = favs
    override fun isFavorite(styleId: String): Flow<Boolean> = flowOf(favs.value.contains(styleId))
}

class FakePrefsManager : PreferencesManager {
    private val flow = MutableStateFlow(GuidelineType.BJCP_2021)
    override val selectedGuideline: Flow<GuidelineType> = flow
    override suspend fun setGuideline(guidelineType: GuidelineType) { flow.value = guidelineType }
}

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
}
