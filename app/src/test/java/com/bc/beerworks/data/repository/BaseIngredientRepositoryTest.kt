package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.FavoriteIngredientDao
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.Ingredient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

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
    private val styleIdsMap: Map<Pair<String, String>, List<String>>,
    styleLookup: StyleLookup,
    defaultDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(),
    favoriteIngredientDao: FavoriteIngredientDao? = null
) : BaseIngredientRepository<TestEntity, TestDomain, String>(
    styleLookup = styleLookup,
    defaultDispatcher = defaultDispatcher,
    favoriteIngredientDao = favoriteIngredientDao
) {
    override val ingredientType: String = "TEST"
    override fun performSearch(criteria: String): Flow<List<TestEntity>> =
        flowOf(entities.filter { it.name.contains(criteria, ignoreCase = true) })

    override fun fetchById(id: String): Flow<TestEntity?> =
        flowOf(entities.find { it.id == id })

    override suspend fun fetchByIdDirect(id: String): TestEntity? =
        entities.find { it.id == id }

    override fun fetchAll(): Flow<List<TestEntity>> = flowOf(entities)

    override fun fetchStyleIds(id: String, guidelineType: String): Flow<List<String>> =
        flowOf(styleIdsMap[id to guidelineType] ?: emptyList())

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

    private fun testStyle(id: String, name: String, categoryNumber: String, guidelineType: GuidelineType) =
        BeerStyle(
            id = id,
            name = name,
            category = "Category",
            categoryNumber = categoryNumber,
            number = id,
            overallImpression = "",
            aroma = "",
            appearance = "",
            flavor = "",
            mouthfeel = "",
            comments = "",
            history = "",
            characteristicIngredients = "",
            styleComparison = "",
            ibuMin = 0.0,
            ibuMax = 0.0,
            ogMin = 1.0,
            ogMax = 1.0,
            fgMin = 1.0,
            fgMax = 1.0,
            abvMin = 0.0,
            abvMax = 0.0,
            srmMin = 0.0,
            srmMax = 0.0,
            commercialExamples = "",
            tags = emptyList(),
            guidelineType = guidelineType
        )

    private val allTestStyles = listOf(
        testStyle("1A", "American Light Lager", "1", GuidelineType.BJCP_2021),
        testStyle("BA-1", "Juicy or Hazy IPA", "1", GuidelineType.BA_2026),
        testStyle("M1A", "Dry Mead", "M1", GuidelineType.BJCP_MEAD),
        testStyle("C1A", "New World Cider", "C1", GuidelineType.BJCP_CIDER)
    )

    private fun createRepository(
        styleIdsMap: Map<Pair<String, String>, List<String>> = mapOf(
            ("item1" to GuidelineType.BJCP_2021.name) to listOf("1A"),
            ("item1" to GuidelineType.BA_2026.name) to listOf("BA-1"),
            ("item1" to GuidelineType.BJCP_MEAD.name) to listOf("M1A"),
            ("item1" to GuidelineType.BJCP_CIDER.name) to listOf("C1A")
        ),
        defaultDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
    ): ConcreteTestRepository {
        val styleLookup = StyleLookup { idsByGuideline ->
            flowOf(
                allTestStyles.filter { style ->
                    idsByGuideline[style.guidelineType]?.contains(style.id) == true
                }
            )
        }

        return ConcreteTestRepository(
            entities = items,
            styleIdsMap = styleIdsMap,
            styleLookup = styleLookup,
            defaultDispatcher = defaultDispatcher
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
        val repo = createRepository(defaultDispatcher = UnconfinedTestDispatcher(testScheduler))

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

        val allResults = repo.getCommonStylesForItem("item1").first()
        assertThat(allResults.map { it.id }).containsExactly("1A", "BA-1", "M1A", "C1A")
    }

    @Test
    fun buildFtsQuery_tokenizesAndAppendsWildcards() {
        val repo = createRepository()

        assertThat(repo.testFtsQuery("")).isEqualTo("")
        assertThat(repo.testFtsQuery("   ")).isEqualTo("")
        assertThat(repo.testFtsQuery("---")).isEqualTo("")
        assertThat(repo.testFtsQuery("cascade")).isEqualTo("cascade*")
        assertThat(repo.testFtsQuery("citra mosaic simcoe")).isEqualTo("citra* mosaic* simcoe*")
        assertThat(repo.testFtsQuery("  dabinett   apple  ")).isEqualTo("dabinett* apple*")
        assertThat(repo.testFtsQuery("4-VG WLP-001")).isEqualTo("4* VG* WLP* 001*")
    }

    @Test
    fun getItemCount_delegatesCount() = runTest {
        val repo = createRepository()
        assertThat(repo.getItemCount()).isEqualTo(2)
    }
}
