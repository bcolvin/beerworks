package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.BaStyleEntity
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.CiderStyleEntity
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.Ingredient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.doReturn

private data class TestEntity(
    val id: String,
    val name: String,
    val origin: String
)

private data class TestDomain(
    override val id: String,
    override val name: String,
    override val origin: String,
    override val countryCode: String = "US",
    override val description: String = "",
    override val descriptors: List<String> = emptyList(),
    override val substituteIds: List<String> = emptyList(),
    override val countryFlag: String = "🇺🇸"
) : Ingredient

@OptIn(ExperimentalCoroutinesApi::class)
private class ConcreteTestRepository(
    private val entities: List<TestEntity>,
    private val styleIdsMap: Map<String, List<String>>,
    bjcpStyleDao: BjcpStyleDao,
    baStyleDao: BaStyleDao,
    meadStyleDao: MeadStyleDao? = null,
    ciderStyleDao: CiderStyleDao? = null
) : BaseIngredientRepository<TestEntity, TestDomain, String>(
    bjcpStyleDao,
    baStyleDao,
    meadStyleDao,
    ciderStyleDao,
    UnconfinedTestDispatcher()
) {
    override fun performSearch(criteria: String): Flow<List<TestEntity>> =
        flowOf(entities.filter { it.name.contains(criteria, ignoreCase = true) })

    override fun fetchById(id: String): Flow<TestEntity?> =
        flowOf(entities.find { it.id == id })

    override suspend fun fetchByIdDirect(id: String): TestEntity? =
        entities.find { it.id == id }

    override fun fetchAll(): Flow<List<TestEntity>> = flowOf(entities)

    override fun fetchStyleIds(id: String, guidelineType: String): Flow<List<String>> =
        flowOf(styleIdsMap[id] ?: emptyList())

    override suspend fun fetchCount(): Int = entities.size

    override fun getEntityId(entity: TestEntity): String = entity.id

    override fun TestEntity.toDomain(): TestDomain =
        TestDomain(id = id, name = name, origin = origin)

    fun testFtsQuery(query: String): String = buildFtsQuery(query)
}

@OptIn(ExperimentalCoroutinesApi::class)
class BaseIngredientRepositoryTest {

    private val item1 = TestEntity("item1", "Item One", "Origin A")
    private val item2 = TestEntity("item2", "Item Two", "Origin B")
    private val items = listOf(item1, item2)

    private val bjcpStyle = BjcpStyleEntity(
        id = "1A",
        name = "American Light Lager",
        category = "Standard American Beer",
        categoryNumber = "1",
        number = "1A",
        overallImpression = "",
        aroma = "",
        appearance = "",
        flavor = "",
        mouthfeel = "",
        comments = "",
        history = "",
        characteristicIngredients = "",
        styleComparison = "",
        ibuMin = 8.0,
        ibuMax = 12.0,
        srmMin = 2.0,
        srmMax = 3.0,
        ogMin = 1.028,
        ogMax = 1.040,
        fgMin = 0.998,
        fgMax = 1.008,
        abvMin = 2.8,
        abvMax = 4.2,
        commercialExamples = "",
        tags = ""
    )

    private val baStyle = BaStyleEntity(
        id = "BA-1",
        name = "Juicy or Hazy IPA",
        category = "North American Origin Ale Styles",
        categoryNumber = "1",
        number = "1",
        overallImpression = "",
        aroma = "",
        appearance = "",
        flavor = "",
        mouthfeel = "",
        comments = "",
        history = "",
        characteristicIngredients = "",
        styleComparison = "",
        ibuMin = 30.0,
        ibuMax = 65.0,
        srmMin = 4.0,
        srmMax = 7.0,
        ogMin = 1.060,
        ogMax = 1.070,
        fgMin = 1.008,
        fgMax = 1.016,
        abvMin = 6.3,
        abvMax = 7.5,
        commercialExamples = "",
        tags = ""
    )

    private val meadStyle = MeadStyleEntity(
        id = "M1A",
        name = "Dry Mead",
        category = "Traditional Mead",
        categoryNumber = "M1",
        number = "M1A",
        overallImpression = "",
        aroma = "",
        appearance = "",
        flavor = "",
        mouthfeel = "",
        comments = "",
        history = "",
        characteristicIngredients = "",
        styleComparison = "",
        ogMin = 1.075,
        ogMax = 1.120,
        fgMin = 0.990,
        fgMax = 1.008,
        abvMin = 8.5,
        abvMax = 14.0,
        sweetness = "Dry",
        carbonation = "Still",
        strength = "Standard",
        commercialExamples = "",
        tags = ""
    )

    private val ciderStyle = CiderStyleEntity(
        id = "C1A",
        name = "New World Cider",
        category = "Standard Cider and Perry",
        categoryNumber = "C1",
        number = "C1A",
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
        ogMax = 1.065,
        fgMin = 0.995,
        fgMax = 1.015,
        abvMin = 5.0,
        abvMax = 8.0,
        sweetness = "Dry",
        carbonation = "Sparkling",
        strength = "Standard",
        commercialExamples = "",
        tags = ""
    )

    private fun createRepository(
        styleIdsMap: Map<String, List<String>> = mapOf("item1" to listOf("1A", "BA-1", "M1A", "C1A"))
    ): ConcreteTestRepository {
        val bjcpDao = mock(BjcpStyleDao::class.java)
        val baDao = mock(BaStyleDao::class.java)
        val meadDao = mock(MeadStyleDao::class.java)
        val ciderDao = mock(CiderStyleDao::class.java)

        doReturn(flowOf(listOf(bjcpStyle))).`when`(bjcpDao).getAllStyles()
        doReturn(flowOf(listOf(baStyle))).`when`(baDao).getAllStyles()
        doReturn(flowOf(listOf(meadStyle))).`when`(meadDao).getAllStyles()
        doReturn(flowOf(listOf(ciderStyle))).`when`(ciderDao).getAllStyles()

        return ConcreteTestRepository(
            entities = items,
            styleIdsMap = styleIdsMap,
            bjcpStyleDao = bjcpDao,
            baStyleDao = baDao,
            meadStyleDao = meadDao,
            ciderStyleDao = ciderDao
        )
    }

    @Test
    fun getFilteredItems_delegatesAndMapsToDomain() = runTest {
        val repo = createRepository()
        val results = repo.getFilteredItems("One").first()

        assertThat(results).hasSize(1)
        assertThat(results.first().id).isEqualTo("item1")
        assertThat(results.first().name).isEqualTo("Item One")
    }

    @Test
    fun getItemById_delegatesLookup() = runTest {
        val repo = createRepository()
        val result = repo.getItemById("item2").first()

        assertThat(result).isNotNull
        assertThat(result?.name).isEqualTo("Item Two")
    }

    @Test
    fun getItemByIdDirect_returnsItem() = runTest {
        val repo = createRepository()
        val result = repo.getItemByIdDirect("item1")

        assertThat(result).isNotNull
        assertThat(result?.id).isEqualTo("item1")
    }

    @Test
    fun getSubstitutes_returnsMappedMatches() = runTest {
        val repo = createRepository()
        val subs = repo.getSubstitutes(listOf("item2")).first()

        assertThat(subs).hasSize(1)
        assertThat(subs.first().id).isEqualTo("item2")
    }

    @Test
    fun getCommonStylesForItem_resolvesAcrossAllGuidelines() = runTest {
        val repo = createRepository()

        val bjcpResults = repo.getCommonStylesForItem("item1", GuidelineType.BJCP_2021).first()
        assertThat(bjcpResults).hasSize(1)
        assertThat(bjcpResults.first().id).isEqualTo("1A")

        val baResults = repo.getCommonStylesForItem("item1", GuidelineType.BA_2026).first()
        assertThat(baResults).hasSize(1)
        assertThat(baResults.first().id).isEqualTo("BA-1")

        val meadResults = repo.getCommonStylesForItem("item1", GuidelineType.BJCP_MEAD).first()
        assertThat(meadResults).hasSize(1)
        assertThat(meadResults.first().id).isEqualTo("M1A")

        val ciderResults = repo.getCommonStylesForItem("item1", GuidelineType.BJCP_CIDER).first()
        assertThat(ciderResults).hasSize(1)
        assertThat(ciderResults.first().id).isEqualTo("C1A")
    }

    @Test
    fun buildFtsQuery_tokenizesAndAppendsWildcards() {
        val repo = createRepository()

        assertThat(repo.testFtsQuery("")).isEqualTo("")
        assertThat(repo.testFtsQuery("   ")).isEqualTo("")
        assertThat(repo.testFtsQuery("cascade")).isEqualTo("cascade*")
        assertThat(repo.testFtsQuery("citra mosaic simcoe")).isEqualTo("citra* mosaic* simcoe*")
        assertThat(repo.testFtsQuery("  dabinett   apple  ")).isEqualTo("dabinett* apple*")
    }

    @Test
    fun getItemCount_delegatesCount() = runTest {
        val repo = createRepository()
        assertThat(repo.getItemCount()).isEqualTo(2)
    }
}
