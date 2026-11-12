package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.HoneyDao
import com.bc.beerworks.data.local.dao.HoneyStyleCrossRefDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.HoneyStyleCrossRef
import com.bc.beerworks.data.local.entity.HoneyVarietyEntity
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HoneyFilterCriteria
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock

class FakeHoneyDao(
    private var honeys: List<HoneyVarietyEntity> = emptyList()
) : HoneyDao {
    override suspend fun insertAll(honeys: List<HoneyVarietyEntity>) { this.honeys = honeys }
    override fun getAllHoneys(): Flow<List<HoneyVarietyEntity>> = flowOf(honeys)
    override fun getHoneyById(id: String): Flow<HoneyVarietyEntity?> = flowOf(honeys.find { it.id == id })
    override suspend fun getHoneyByIdDirect(id: String): HoneyVarietyEntity? = honeys.find { it.id == id }
    override fun getAllClassifications(): Flow<List<String>> =
        flowOf(honeys.map { it.floralClassification }.distinct().sorted())
    override fun getAllUsdaColors(): Flow<List<String>> =
        flowOf(honeys.map { it.usdaColor }.distinct().sorted())

    override fun searchAndFilterHoneys(
        ftsQuery: String,
        classification: String?,
        flavorIntensity: String?,
        crystallizationTendency: String?,
        styleId: String?
    ): Flow<List<HoneyVarietyEntity>> {
        var filtered = honeys
        if (classification != null) {
            filtered = filtered.filter { it.floralClassification == classification }
        }
        if (flavorIntensity != null) {
            filtered = filtered.filter { it.flavorIntensity == flavorIntensity }
        }
        if (crystallizationTendency != null) {
            filtered = filtered.filter { it.crystallizationTendency == crystallizationTendency }
        }
        return flowOf(filtered)
    }

    override fun getHoneysForStyle(styleId: String, guidelineType: String): Flow<List<HoneyVarietyEntity>> =
        flowOf(honeys)

    override fun getHoneysForStyles(styleIds: List<String>, guidelineType: String): Flow<List<HoneyVarietyEntity>> =
        flowOf(honeys)

    override suspend fun getHoneyCount(): Int = honeys.size
}

class FakeHoneyStyleCrossRefDao(
    private val styleIdsForHoneys: Map<String, List<String>> = emptyMap(),
    private val honeyIdsForStyles: Map<String, List<String>> = emptyMap()
) : HoneyStyleCrossRefDao {
    override suspend fun insertAll(crossRefs: List<HoneyStyleCrossRef>) {}
    override fun getStyleIdsForHoney(honeyId: String, guidelineType: String): Flow<List<String>> =
        flowOf(styleIdsForHoneys[honeyId] ?: emptyList())
    override fun getHoneyIdsForStyle(styleId: String, guidelineType: String): Flow<List<String>> =
        flowOf(honeyIdsForStyles[styleId] ?: emptyList())
}

@OptIn(ExperimentalCoroutinesApi::class)
class HoneyRepositoryTest {

    private val orangeBlossomEntity = HoneyVarietyEntity(
        id = "orange_blossom",
        name = "Orange Blossom Honey",
        floralClassification = "Monofloral",
        botanicalSource = "Citrus sinensis",
        origin = "California, United States",
        countryCode = "US",
        pfundMin = 20.0,
        pfundMax = 45.0,
        usdaColor = "White",
        moistureTypicalPercent = 16.8,
        fgRatio = 1.25,
        crystallizationTendency = "Moderate",
        extractPotentialSg = 1.037,
        flavorIntensity = "Moderate",
        description = "The quintessential varietal honey for traditional meads.",
        fermentationBehavior = "Retains citrus blossom aromas when fermented cool.",
        pairingNotes = "Superb with stone fruits and ginger.",
        descriptors = "Citrus Blossom, Orange Zest, Floral",
        substituteIds = "tupelo, sweet_clover",
        radarFloral = 4.8f,
        radarFruity = 4.2f,
        radarHerbal = 1.5f,
        radarSpicy = 1.0f,
        radarSweet = 3.8f,
        radarWoody = 0.5f,
        radarEarthy = 0.5f,
        radarPungent = 0.0f
    )

    private val buckwheatEntity = HoneyVarietyEntity(
        id = "buckwheat_eastern",
        name = "Buckwheat Honey (Eastern)",
        floralClassification = "Monofloral",
        botanicalSource = "Fagopyrum esculentum",
        origin = "New York, United States",
        countryCode = "US",
        pfundMin = 120.0,
        pfundMax = 140.0,
        usdaColor = "Dark Amber",
        moistureTypicalPercent = 18.2,
        fgRatio = 1.05,
        crystallizationTendency = "Rapid",
        extractPotentialSg = 1.036,
        flavorIntensity = "Robust",
        description = "Deep blackish-amber honey packed with iron and molasses.",
        fermentationBehavior = "Mellows with aging into leather and dark cherry.",
        pairingNotes = "Ideal for Braggot and dark metheglins.",
        descriptors = "Molasses, Barnyard, Malt",
        substituteIds = "avocado",
        radarFloral = 1.0f,
        radarFruity = 2.0f,
        radarHerbal = 0.5f,
        radarSpicy = 2.8f,
        radarSweet = 3.2f,
        radarWoody = 3.5f,
        radarEarthy = 4.8f,
        radarPungent = 4.5f
    )

    private val wildflowerEntity = HoneyVarietyEntity(
        id = "wildflower_northeast",
        name = "Northeastern Wildflower Honey",
        floralClassification = "Polyfloral",
        botanicalSource = "Mixed Flora",
        origin = "New England, United States",
        countryCode = "US",
        pfundMin = 50.0,
        pfundMax = 85.0,
        usdaColor = "Light Amber",
        moistureTypicalPercent = 17.8,
        fgRatio = 1.12,
        crystallizationTendency = "Moderate",
        extractPotentialSg = 1.037,
        flavorIntensity = "Moderate",
        description = "Complex autumn wildflower blend.",
        fermentationBehavior = "Consistent fermentation.",
        pairingNotes = "Great with apples and berries.",
        descriptors = "Autumn Wildflower, Stone Fruit",
        substituteIds = "wildflower_pnw",
        radarFloral = 3.2f,
        radarFruity = 3.5f,
        radarHerbal = 2.8f,
        radarSpicy = 2.2f,
        radarSweet = 3.6f,
        radarWoody = 2.0f,
        radarEarthy = 2.2f,
        radarPungent = 1.0f
    )

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun createRepository(
        honeys: List<HoneyVarietyEntity> = listOf(orangeBlossomEntity, buckwheatEntity, wildflowerEntity),
        styleIdsForHoneys: Map<String, List<String>> = emptyMap(),
        honeyIdsForStyles: Map<String, List<String>> = emptyMap(),
        meadStyles: List<MeadStyleEntity> = emptyList()
    ): HoneyRepository {
        val honeyDao = FakeHoneyDao(honeys)
        val crossRefDao = FakeHoneyStyleCrossRefDao(styleIdsForHoneys, honeyIdsForStyles)
        val bjcpDao = mock(BjcpStyleDao::class.java)
        val baDao = mock(BaStyleDao::class.java)
        val meadDao = mock(MeadStyleDao::class.java)
        val ciderDao = mock(CiderStyleDao::class.java)

        return HoneyRepository(
            honeyDao = honeyDao,
            honeyStyleCrossRefDao = crossRefDao,
            bjcpStyleDao = bjcpDao,
            baStyleDao = baDao,
            meadStyleDao = meadDao,
            ciderStyleDao = ciderDao,
            defaultDispatcher = testDispatcher
        )
    }

    @Test
    fun getFilteredHoneys_returnsAllWhenNoCriteria() = runTest(testDispatcher) {
        val repo = createRepository()

        val results = repo.getFilteredHoneys(HoneyFilterCriteria()).first()

        assertThat(results).hasSize(3)
        assertThat(results.map { it.id }).containsExactly("orange_blossom", "buckwheat_eastern", "wildflower_northeast")
    }

    @Test
    fun getFilteredHoneys_filtersByClassification() = runTest(testDispatcher) {
        val repo = createRepository()

        val results = repo.getFilteredHoneys(HoneyFilterCriteria(selectedClassification = "Polyfloral")).first()

        assertThat(results).hasSize(1)
        assertThat(results.first().id).isEqualTo("wildflower_northeast")
    }

    @Test
    fun getFilteredHoneys_filtersByFlavorIntensity() = runTest(testDispatcher) {
        val repo = createRepository()

        val results = repo.getFilteredHoneys(HoneyFilterCriteria(selectedFlavorIntensity = "Robust")).first()

        assertThat(results).hasSize(1)
        assertThat(results.first().id).isEqualTo("buckwheat_eastern")
    }

    @Test
    fun getFilteredHoneys_filtersByUsdaColors() = runTest(testDispatcher) {
        val repo = createRepository()

        val results = repo.getFilteredHoneys(
            HoneyFilterCriteria(selectedUsdaColors = setOf("White", "Dark Amber"))
        ).first()

        assertThat(results).hasSize(2)
        assertThat(results.map { it.id }).containsExactlyInAnyOrder("orange_blossom", "buckwheat_eastern")
    }

    @Test
    fun getHoneyById_returnsCorrectHoney() = runTest(testDispatcher) {
        val repo = createRepository()

        val honey = repo.getHoneyById("orange_blossom").first()

        assertThat(honey).isNotNull
        assertThat(honey?.name).isEqualTo("Orange Blossom Honey")
    }

    @Test
    fun getSubstituteHoneys_returnsMatchingSubstitutes() = runTest(testDispatcher) {
        val repo = createRepository()

        val subs = repo.getSubstituteHoneys(listOf("buckwheat_eastern")).first()

        assertThat(subs).hasSize(1)
        assertThat(subs.first().id).isEqualTo("buckwheat_eastern")
    }
}
