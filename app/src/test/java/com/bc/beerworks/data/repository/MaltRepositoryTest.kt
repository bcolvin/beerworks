package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.MaltDao
import com.bc.beerworks.data.local.dao.MaltStyleCrossRefDao
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.MaltStyleCrossRef
import com.bc.beerworks.data.local.entity.MaltVarietyEntity
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.MaltFilterCriteria
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FakeMaltDao(
    private var malts: List<MaltVarietyEntity> = emptyList()
) : MaltDao {
    override suspend fun insertAll(malts: List<MaltVarietyEntity>) { this.malts = malts }
    override fun getAllMalts(): Flow<List<MaltVarietyEntity>> = flowOf(malts)
    override fun getMaltById(id: String): Flow<MaltVarietyEntity?> = flowOf(malts.find { it.id == id })
    override suspend fun getMaltByIdDirect(id: String): MaltVarietyEntity? = malts.find { it.id == id }
    override fun getAllCategories(): Flow<List<String>> =
        flowOf(malts.map { it.category }.distinct().sorted())
    override fun getAllGrainTypes(): Flow<List<String>> =
        flowOf(malts.map { it.grainType }.distinct().sorted())

    override fun searchAndFilterMalts(
        ftsQuery: String,
        category: String?,
        grainType: String?,
        diastaticTier: String?,
        colorTier: String?,
        baseCategory: String?
    ): Flow<List<MaltVarietyEntity>> {
        var filtered = malts
        if (category != null) {
            filtered = filtered.filter { it.category == category }
        }
        if (grainType != null) {
            filtered = filtered.filter { it.grainType == grainType }
        }
        if (diastaticTier != null) {
            filtered = filtered.filter {
                when (diastaticTier) {
                    "High" -> it.diastaticPowerLintner >= 100
                    "Moderate" -> it.diastaticPowerLintner in 30..99
                    else -> it.diastaticPowerLintner < 30
                }
            }
        }
        if (colorTier != null) {
            filtered = filtered.filter {
                when (colorTier) {
                    "Pale" -> it.colorMin < 10.0
                    "Amber" -> it.colorMax >= 10.0 && it.colorMin <= 50.0
                    "Brown" -> it.colorMax >= 50.0 && it.colorMin <= 150.0
                    "Dark" -> it.colorMax >= 150.0
                    else -> true
                }
            }
        }
        return flowOf(filtered)
    }

    override fun getMaltsForStyle(styleId: String, guidelineType: String): Flow<List<MaltVarietyEntity>> =
        flowOf(malts)

    override suspend fun getMaltCount(): Int = malts.size
}

class FakeMaltStyleCrossRefDao(
    private val styleIdsForMalts: Map<String, List<String>> = emptyMap(),
    private val maltIdsForStyles: Map<String, List<String>> = emptyMap()
) : MaltStyleCrossRefDao {
    override suspend fun insertAll(crossRefs: List<MaltStyleCrossRef>) {}
    override fun getStyleIdsForMalt(maltId: String, guidelineType: String): Flow<List<String>> =
        flowOf(styleIdsForMalts[maltId] ?: emptyList())
    override fun getMaltIdsForStyle(styleId: String, guidelineType: String): Flow<List<String>> =
        flowOf(maltIdsForStyles[styleId] ?: emptyList())
}

@OptIn(ExperimentalCoroutinesApi::class)
class MaltRepositoryTest {

    private val pilsnerEntity = MaltVarietyEntity(
        id = "pilsner_malt",
        name = "Pilsner Malt",
        category = "Base Malt",
        grainType = "Barley",
        origin = "Germany",
        countryCode = "DE",
        colorMin = 1.4,
        colorMax = 2.0,
        diastaticPowerLintner = 110,
        extractPotentialDbfg = 82.0,
        maxGristPercentage = 100,
        description = "Palest base malt available.",
        typicalUsage = "Standard base malt for continental lagers.",
        functionalTags = "Base Grist,Crisp Malt Sweetness,Pale Color",
        descriptors = "Cracker,Bread Dough,Clean",
        commercialExamples = "Weyermann Pilsner,Dingemans Pilsen",
        substituteIds = "two_row_pale",
        radarSweet = 3.2f,
        radarBiscuit = 2.8f,
        radarToasty = 0.5f,
        radarCaramel = 0.5f,
        radarNutty = 0.8f,
        radarChocolate = 0f,
        radarRoast = 0f,
        radarHusky = 1.5f
    )

    private val wheatEntity = MaltVarietyEntity(
        id = "pale_wheat_malt",
        name = "Pale Wheat Malt",
        category = "Adjunct & Raw Grain",
        grainType = "Wheat",
        origin = "Germany",
        countryCode = "DE",
        colorMin = 1.8,
        colorMax = 2.8,
        diastaticPowerLintner = 130,
        extractPotentialDbfg = 83.0,
        maxGristPercentage = 70,
        description = "Malted wheat kernel.",
        typicalUsage = "50-70% in Weissbier.",
        functionalTags = "Head Retention,Body & Mouthfeel",
        descriptors = "Bready Dough,Tart Wheat",
        commercialExamples = "Weyermann Pale Wheat",
        substituteIds = "white_wheat_malt",
        radarSweet = 3.0f,
        radarBiscuit = 3.8f,
        radarToasty = 0.8f,
        radarCaramel = 0.2f,
        radarNutty = 1.2f,
        radarChocolate = 0f,
        radarRoast = 0f,
        radarHusky = 1.0f
    )

    private val chocolateEntity = MaltVarietyEntity(
        id = "chocolate_malt",
        name = "Chocolate Malt",
        category = "Roasted & Dark",
        grainType = "Barley",
        origin = "United Kingdom",
        countryCode = "GB",
        colorMin = 350.0,
        colorMax = 450.0,
        diastaticPowerLintner = 0,
        extractPotentialDbfg = 70.0,
        maxGristPercentage = 10,
        description = "Dark roasted malt.",
        typicalUsage = "3-10% in porters and stouts.",
        functionalTags = "Roast & Coffee,Dark Chocolate",
        descriptors = "Dark Chocolate,Bittersweet Cocoa",
        commercialExamples = "Crisp Chocolate",
        substituteIds = "pale_chocolate_malt",
        radarSweet = 1.2f,
        radarBiscuit = 1.0f,
        radarToasty = 3.8f,
        radarCaramel = 1.0f,
        radarNutty = 3.0f,
        radarChocolate = 4.8f,
        radarRoast = 4.0f,
        radarHusky = 1.5f
    )

    private val testDispatcher = UnconfinedTestDispatcher()
    private val emptyStyleLookup = StyleLookup { flowOf(emptyList()) }

    @Test
    fun getMaltById_returnsMappedDomainModel() = runTest(testDispatcher) {
        val maltDao = FakeMaltDao(listOf(pilsnerEntity))
        val crossRefDao = FakeMaltStyleCrossRefDao()

        val repository = MaltRepository(maltDao, crossRefDao, emptyStyleLookup, testDispatcher)

        val malt = repository.getMaltById("pilsner_malt").first()
        assertThat(malt).isNotNull
        assertThat(malt!!.name).isEqualTo("Pilsner Malt")
        assertThat(malt.countryFlag).isEqualTo("🇩🇪")
        assertThat(malt.diastaticTier).isEqualTo("High")
        assertThat(malt.colorRangeText).contains("°L")
        assertThat(malt.functionalTags).contains("Base Grist", "Pale Color")
    }

    @Test
    fun getFilteredMalts_filtersByCategory() = runTest(testDispatcher) {
        val maltDao = FakeMaltDao(listOf(pilsnerEntity, wheatEntity, chocolateEntity))
        val repository = MaltRepository(maltDao, FakeMaltStyleCrossRefDao(), emptyStyleLookup, testDispatcher)

        val results = repository.getFilteredMalts(MaltFilterCriteria(selectedCategory = "Roasted & Dark")).first()
        assertThat(results).hasSize(1)
        assertThat(results[0].id).isEqualTo("chocolate_malt")
    }

    @Test
    fun getFilteredMalts_filtersByGrainType() = runTest(testDispatcher) {
        val maltDao = FakeMaltDao(listOf(pilsnerEntity, wheatEntity, chocolateEntity))
        val repository = MaltRepository(maltDao, FakeMaltStyleCrossRefDao(), emptyStyleLookup, testDispatcher)

        val results = repository.getFilteredMalts(MaltFilterCriteria(selectedGrainType = "Wheat")).first()
        assertThat(results).hasSize(1)
        assertThat(results[0].id).isEqualTo("pale_wheat_malt")
    }

    @Test
    fun getFilteredMalts_filtersByDiastaticTier() = runTest(testDispatcher) {
        val maltDao = FakeMaltDao(listOf(pilsnerEntity, wheatEntity, chocolateEntity))
        val repository = MaltRepository(maltDao, FakeMaltStyleCrossRefDao(), emptyStyleLookup, testDispatcher)

        val results = repository.getFilteredMalts(MaltFilterCriteria(selectedDiastaticTier = "None")).first()
        assertThat(results).hasSize(1)
        assertThat(results[0].id).isEqualTo("chocolate_malt")
    }

    @Test
    fun getFilteredMalts_filtersByFunctionalTags() = runTest(testDispatcher) {
        val maltDao = FakeMaltDao(listOf(pilsnerEntity, wheatEntity, chocolateEntity))
        val repository = MaltRepository(maltDao, FakeMaltStyleCrossRefDao(), emptyStyleLookup, testDispatcher)

        val results = repository.getFilteredMalts(
            MaltFilterCriteria(selectedFunctionalTags = setOf("Head Retention"))
        ).first()
        assertThat(results).hasSize(1)
        assertThat(results[0].id).isEqualTo("pale_wheat_malt")
    }

    @Test
    fun getSubstituteMalts_resolvesCorrectEntities() = runTest(testDispatcher) {
        val maltDao = FakeMaltDao(listOf(pilsnerEntity, wheatEntity, chocolateEntity))
        val repository = MaltRepository(maltDao, FakeMaltStyleCrossRefDao(), emptyStyleLookup, testDispatcher)

        val substitutes = repository.getSubstituteMalts(listOf("chocolate_malt")).first()
        assertThat(substitutes).hasSize(1)
        assertThat(substitutes[0].name).isEqualTo("Chocolate Malt")
    }

    @Test
    fun getCommonStylesForMalt_resolvesCrossReferencedStyles() = runTest(testDispatcher) {
        val maltDao = FakeMaltDao(listOf(pilsnerEntity))
        val crossRefDao = FakeMaltStyleCrossRefDao(
            styleIdsForMalts = mapOf("pilsner_malt" to listOf("5D"))
        )
        val styleEntity = BjcpStyleEntity(
            id = "5D",
            name = "German Pils",
            category = "German Lager",
            categoryNumber = "5",
            number = "5D",
            overallImpression = "A crisp, clean German lager.",
            aroma = "", appearance = "", flavor = "", mouthfeel = "", comments = "", history = "", characteristicIngredients = "", styleComparison = "",
            ibuMin = 22.0, ibuMax = 40.0, ogMin = 1.044, ogMax = 1.050, fgMin = 1.008, fgMax = 1.013, abvMin = 4.4, abvMax = 5.2, srmMin = 2.0, srmMax = 4.0,
            commercialExamples = "Bitburger", tags = "lager,pils"
        )
        val styleLookup = StyleLookup { idsByGuideline ->
            val ids = idsByGuideline[GuidelineType.BJCP_2021].orEmpty()
            flowOf(listOf(styleEntity).filter { ids.contains(it.id) }.map { it.toDomainModel() })
        }

        val repository = MaltRepository(maltDao, crossRefDao, styleLookup, testDispatcher)

        val styles = repository.getCommonStylesForMalt("pilsner_malt", GuidelineType.BJCP_2021).first()
        assertThat(styles).hasSize(1)
        assertThat(styles[0].name).isEqualTo("German Pils")
    }
}
