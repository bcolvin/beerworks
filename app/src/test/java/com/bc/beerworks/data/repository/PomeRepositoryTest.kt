package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.dao.PomeDao
import com.bc.beerworks.data.local.dao.PomeStyleCrossRefDao
import com.bc.beerworks.data.local.entity.CiderStyleEntity
import com.bc.beerworks.data.local.entity.PomeStyleCrossRef
import com.bc.beerworks.data.local.entity.PomeVarietyEntity
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.PomeFilterCriteria
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock

class FakePomeDao(
    private var pomes: List<PomeVarietyEntity> = emptyList()
) : PomeDao {
    override suspend fun insertAll(pomes: List<PomeVarietyEntity>) { this.pomes = pomes }
    override fun getAllPomes(): Flow<List<PomeVarietyEntity>> = flowOf(pomes)
    override fun getPomeById(id: String): Flow<PomeVarietyEntity?> = flowOf(pomes.find { it.id == id })
    override suspend fun getPomeByIdDirect(id: String): PomeVarietyEntity? = pomes.find { it.id == id }
    override fun getAllFruitTypes(): Flow<List<String>> =
        flowOf(pomes.map { it.fruitType }.distinct().sorted())
    override fun getAllClassifications(): Flow<List<String>> =
        flowOf(pomes.map { it.classification }.distinct().sorted())

    override fun searchAndFilterPomes(
        ftsQuery: String,
        fruitType: String?,
        classification: String?,
        harvestSeason: String?,
        tanninLevel: String?,
        styleId: String?
    ): Flow<List<PomeVarietyEntity>> {
        var filtered = pomes
        if (fruitType != null) {
            filtered = filtered.filter { it.fruitType == fruitType }
        }
        if (classification != null) {
            filtered = filtered.filter { it.classification == classification }
        }
        if (harvestSeason != null) {
            filtered = filtered.filter { it.harvestSeason == harvestSeason }
        }
        if (tanninLevel != null) {
            filtered = filtered.filter { it.tanninLevel == tanninLevel }
        }
        return flowOf(filtered)
    }

    override fun getPomesForStyle(styleId: String, guidelineType: String): Flow<List<PomeVarietyEntity>> =
        flowOf(pomes)

    override fun getPomesForStyles(styleIds: List<String>, guidelineType: String): Flow<List<PomeVarietyEntity>> =
        flowOf(pomes)

    override suspend fun getPomeCount(): Int = pomes.size
}

class FakePomeStyleCrossRefDao(
    private val styleIdsForPomes: Map<String, List<String>> = emptyMap(),
    private val pomeIdsForStyles: Map<String, List<String>> = emptyMap()
) : PomeStyleCrossRefDao {
    override suspend fun insertAll(crossRefs: List<PomeStyleCrossRef>) {}
    override fun getStyleIdsForPome(pomeId: String, guidelineType: String): Flow<List<String>> =
        flowOf(styleIdsForPomes[pomeId] ?: emptyList())
    override fun getPomeIdsForStyle(styleId: String, guidelineType: String): Flow<List<String>> =
        flowOf(pomeIdsForStyles[styleId] ?: emptyList())
}

@OptIn(ExperimentalCoroutinesApi::class)
class PomeRepositoryTest {

    private val dabinettEntity = PomeVarietyEntity(
        id = "dabinett",
        name = "Dabinett",
        fruitType = "Apple",
        classification = "Bittersweet",
        fruitUse = "Heritage Cider/Perry",
        botanicalName = "Malus domestica",
        origin = "Somerset, England",
        countryCode = "GB",
        acidityMalicPercent = 0.18,
        tanninPolyphenolPercent = 0.35,
        tanninLevel = "High",
        sgMin = 1.050,
        sgMax = 1.058,
        harvestSeason = "Late",
        sorbitolGramsPerLiter = null,
        description = "Widely planted English bittersweet apple.",
        blendingGuidance = "Outstanding as a single-varietal cider.",
        pressingCharacteristics = "Presses easily with good juice yields.",
        descriptors = "Soft Tannin, Baked Apple, Vanilla",
        substituteIds = "yarlington-mill, chisel-jersey",
        radarAcidity = 1.5f,
        radarTannin = 3.8f,
        radarFreshFruit = 3.0f,
        radarBakedFruit = 4.0f,
        radarCitrus = 1.2f,
        radarFloral = 2.2f,
        radarEarthy = 2.8f,
        radarSweet = 3.5f
    )

    private val thornEntity = PomeVarietyEntity(
        id = "thorn",
        name = "Thorn",
        fruitType = "Pear",
        classification = "Bittersharp",
        fruitUse = "Heritage Cider/Perry",
        botanicalName = "Pyrus communis",
        origin = "Gloucestershire, England",
        countryCode = "GB",
        acidityMalicPercent = 0.50,
        tanninPolyphenolPercent = 0.22,
        tanninLevel = "Medium",
        sgMin = 1.055,
        sgMax = 1.065,
        harvestSeason = "Mid",
        sorbitolGramsPerLiter = 32.0,
        description = "Premier English perry pear.",
        blendingGuidance = "Exquisite single-varietal perry.",
        pressingCharacteristics = "Narrow harvest window, rapid browning.",
        descriptors = "Elderflower, Citrus, Ripe Pear",
        substituteIds = "yellow-huffcap, hendre-huffcap",
        radarAcidity = 3.5f,
        radarTannin = 3.0f,
        radarFreshFruit = 4.4f,
        radarBakedFruit = 3.2f,
        radarCitrus = 3.4f,
        radarFloral = 4.5f,
        radarEarthy = 2.0f,
        radarSweet = 3.5f
    )

    private val honeycrispEntity = PomeVarietyEntity(
        id = "honeycrisp",
        name = "Honeycrisp",
        fruitType = "Apple",
        classification = "Sweet",
        fruitUse = "Culinary / Multi-Purpose",
        botanicalName = "Malus domestica",
        origin = "Minnesota, USA",
        countryCode = "US",
        acidityMalicPercent = 0.40,
        tanninPolyphenolPercent = 0.06,
        tanninLevel = "Low",
        sgMin = 1.050,
        sgMax = 1.060,
        harvestSeason = "Mid",
        sorbitolGramsPerLiter = null,
        description = "Crisp, sweet dessert apple.",
        blendingGuidance = "Produces light, clean, sparkling New World ciders.",
        pressingCharacteristics = "High juice yield with low tannins.",
        descriptors = "Crisp Honey, Fresh Apple, Sweet",
        substituteIds = "golden-delicious",
        radarAcidity = 2.2f,
        radarTannin = 0.8f,
        radarFreshFruit = 4.6f,
        radarBakedFruit = 2.0f,
        radarCitrus = 2.4f,
        radarFloral = 3.0f,
        radarEarthy = 0.6f,
        radarSweet = 3.6f
    )

    private val englishCiderStyle = CiderStyleEntity(
        id = "C1B",
        name = "English Cider",
        category = "Standard Cider and Perry",
        categoryNumber = "C1",
        number = "C1B",
        overallImpression = "A dry to medium-sweet cider with full bittersweet tannin structure.",
        aroma = "Rich, fruity, bittersweet apple.",
        appearance = "Golden amber to deep copper.",
        flavor = "Substantial tannin grip and dried fruit.",
        mouthfeel = "Full-bodied with pleasant astringency.",
        comments = "Traditional English West Country style.",
        history = "Historic cider made in Somerset, Herefordshire, and Devon.",
        characteristicIngredients = "English bittersweet and bittersharp apples (Dabinett, Kingston Black, Yarlington Mill).",
        styleComparison = "More tannic and less acidic than New World Cider.",
        ogMin = 1.050,
        ogMax = 1.065,
        fgMin = 0.995,
        fgMax = 1.015,
        abvMin = 6.0,
        abvMax = 8.5,
        sweetness = "Dry, Medium-Dry, Medium",
        carbonation = "Still, Pétillant, Sparkling",
        strength = "Standard",
        commercialExamples = "Dunkertons Black Fox, Sheppy's Vintage",
        tags = "traditional-cider, tannic"
    )

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun createRepository(
        pomes: List<PomeVarietyEntity> = listOf(dabinettEntity, thornEntity, honeycrispEntity),
        styleIdsForPomes: Map<String, List<String>> = mapOf("dabinett" to listOf("C1B")),
        styles: List<CiderStyleEntity> = listOf(englishCiderStyle)
    ): PomeRepository {
        val ciderStyleDao = mock(CiderStyleDao::class.java)
        org.mockito.Mockito.doReturn(flowOf(styles)).`when`(ciderStyleDao).getAllStyles()

        return PomeRepository(
            pomeDao = FakePomeDao(pomes),
            pomeStyleCrossRefDao = FakePomeStyleCrossRefDao(styleIdsForPomes),
            bjcpStyleDao = mock(BjcpStyleDao::class.java),
            baStyleDao = mock(BaStyleDao::class.java),
            meadStyleDao = mock(MeadStyleDao::class.java),
            ciderStyleDao = ciderStyleDao,
            defaultDispatcher = testDispatcher
        )
    }

    @Test
    fun getFilteredPomes_unfiltered_returnsAllPomes() = runTest {
        val repo = createRepository()
        val results = repo.getFilteredPomes(PomeFilterCriteria()).first()
        assertThat(results).hasSize(3)
        assertThat(results.map { it.id }).containsExactly("dabinett", "thorn", "honeycrisp")
    }

    @Test
    fun getFilteredPomes_filterByFruitType_returnsOnlyMatches() = runTest {
        val repo = createRepository()
        val results = repo.getFilteredPomes(PomeFilterCriteria(selectedFruitType = "Pear")).first()
        assertThat(results).hasSize(1)
        assertThat(results.first().id).isEqualTo("thorn")
        assertThat(results.first().fruitType).isEqualTo("Pear")
    }

    @Test
    fun getFilteredPomes_filterByClassification_returnsOnlyMatches() = runTest {
        val repo = createRepository()
        val results = repo.getFilteredPomes(PomeFilterCriteria(selectedClassification = "Bittersweet")).first()
        assertThat(results).hasSize(1)
        assertThat(results.first().id).isEqualTo("dabinett")
        assertThat(results.first().classification).isEqualTo("Bittersweet")
    }

    @Test
    fun getFilteredPomes_filterByTanninLevel_returnsOnlyMatches() = runTest {
        val repo = createRepository()
        val results = repo.getFilteredPomes(PomeFilterCriteria(selectedTanninLevel = "High")).first()
        assertThat(results).hasSize(1)
        assertThat(results.first().id).isEqualTo("dabinett")
    }

    @Test
    fun getFilteredPomes_filterByHarvestSeason_returnsOnlyMatches() = runTest {
        val repo = createRepository()
        val results = repo.getFilteredPomes(PomeFilterCriteria(selectedHarvestSeason = "Late")).first()
        assertThat(results).hasSize(1)
        assertThat(results.first().id).isEqualTo("dabinett")
    }

    @Test
    fun getPomeById_existingId_returnsMappedDomainModel() = runTest {
        val repo = createRepository()
        val pome = repo.getPomeById("thorn").first()
        assertThat(pome).isNotNull
        assertThat(pome?.name).isEqualTo("Thorn")
        assertThat(pome?.sorbitolGramsPerLiter).isEqualTo(32.0)
        assertThat(pome?.countryFlag).isEqualTo("🇬🇧")
        assertThat(pome?.acidityText).isEqualTo("0.50%")
        assertThat(pome?.tanninText).isEqualTo("0.22% (Medium)")
    }

    @Test
    fun getSubstitutePomes_matchingIds_returnsMappedSubstitutes() = runTest {
        val repo = createRepository()
        val substitutes = repo.getSubstitutePomes(listOf("thorn")).first()
        assertThat(substitutes).hasSize(1)
        assertThat(substitutes.first().id).isEqualTo("thorn")
    }

    @Test
    fun getCommonStylesForPome_ciderStyle_returnsMappedCiderStyle() = runTest {
        val repo = createRepository()
        val styles = repo.getCommonStylesForPome("dabinett", GuidelineType.BJCP_CIDER).first()
        assertThat(styles).hasSize(1)
        assertThat(styles.first().id).isEqualTo("C1B")
        assertThat(styles.first().name).isEqualTo("English Cider")
    }

    @Test
    fun getAllFruitTypes_returnsDistinctSortedTypes() = runTest {
        val repo = createRepository()
        val types = repo.getAllFruitTypes().first()
        assertThat(types).containsExactly("Apple", "Pear")
    }

    @Test
    fun getAllClassifications_returnsDistinctSortedClassifications() = runTest {
        val repo = createRepository()
        val classifications = repo.getAllClassifications().first()
        assertThat(classifications).containsExactly("Bittersharp", "Bittersweet", "Sweet")
    }
}
