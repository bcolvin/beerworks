package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.YeastDao
import com.bc.beerworks.data.local.dao.YeastStyleCrossRefDao
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.YeastStrainEntity
import com.bc.beerworks.data.local.entity.YeastStyleCrossRef
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.YeastFilterCriteria
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FakeYeastDao(
    private var yeasts: List<YeastStrainEntity> = emptyList()
) : YeastDao {
    override suspend fun insertAll(yeasts: List<YeastStrainEntity>) { this.yeasts = yeasts }
    override fun getAllYeasts(): Flow<List<YeastStrainEntity>> = flowOf(yeasts)
    override fun getYeastById(id: String): Flow<YeastStrainEntity?> = flowOf(yeasts.find { it.id == id })
    override suspend fun getYeastByIdDirect(id: String): YeastStrainEntity? = yeasts.find { it.id == id }
    override fun getAllClassifications(): Flow<List<String>> =
        flowOf(yeasts.map { it.classification }.distinct().sorted())
    override fun getAllFlocculations(): Flow<List<String>> =
        flowOf(yeasts.map { it.flocculation }.distinct().sorted())

    override fun searchAndFilterYeasts(
        ftsQuery: String,
        classification: String?,
        flocculation: String?,
        minAttenuation: Double?,
        minAlcoholTolerance: Double?,
        styleId: String?
    ): Flow<List<YeastStrainEntity>> {
        var filtered = yeasts
        if (classification != null) {
            filtered = filtered.filter { it.classification == classification }
        }
        if (flocculation != null) {
            filtered = filtered.filter { it.flocculation == flocculation }
        }
        if (minAttenuation != null) {
            filtered = filtered.filter { it.attenuationMax >= minAttenuation }
        }
        if (minAlcoholTolerance != null) {
            filtered = filtered.filter { it.alcoholTolerance >= minAlcoholTolerance }
        }
        return flowOf(filtered)
    }

    override fun getYeastsForStyle(styleId: String, guidelineType: String): Flow<List<YeastStrainEntity>> =
        flowOf(yeasts)

    override fun getYeastsForStyles(styleIds: List<String>, guidelineType: String): Flow<List<YeastStrainEntity>> =
        flowOf(yeasts)

    override suspend fun getYeastCount(): Int = yeasts.size
}

class FakeYeastStyleCrossRefDao(
    private val styleIdsForYeasts: Map<String, List<String>> = emptyMap(),
    private val yeastIdsForStyles: Map<String, List<String>> = emptyMap()
) : YeastStyleCrossRefDao {
    override suspend fun insertAll(crossRefs: List<YeastStyleCrossRef>) {}
    override fun getStyleIdsForYeast(yeastId: String, guidelineType: String): Flow<List<String>> =
        flowOf(styleIdsForYeasts[yeastId] ?: emptyList())
    override fun getYeastIdsForStyle(styleId: String, guidelineType: String): Flow<List<String>> =
        flowOf(yeastIdsForStyles[styleId] ?: emptyList())
}

@OptIn(ExperimentalCoroutinesApi::class)
class YeastRepositoryTest {

    private val chicoEntity = YeastStrainEntity(
        id = "chico-american-ale",
        name = "Chico / American Ale",
        scientificName = "Saccharomyces cerevisiae",
        origin = "Chico, California, USA",
        countryCode = "US",
        discipline = "Beer",
        classification = "Ale",
        form = "Both",
        attenuationMin = 73.0,
        attenuationMax = 80.0,
        flocculation = "Medium",
        tempMinC = 18.0,
        tempMaxC = 22.0,
        tempMinF = 64.0,
        tempMaxF = 72.0,
        alcoholTolerance = 11.0,
        isDiastaticus = false,
        isPhenolic = false,
        description = "Clean, crisp American ale yeast.",
        pitchingDynamics = "Rapid start with short lag phase.",
        descriptors = "clean, crisp, neutral, balanced",
        commercialEquivalentsJson = """[{"laboratory":"White Labs","productCode":"WLP001","productName":"California Ale","form":"Liquid"},{"laboratory":"Fermentis","productCode":"US-05","productName":"SafAle American","form":"Dry"}]""",
        substituteIds = "california-ale-v, lutra-kveik",
        radarClean = 4.8f,
        radarFruity = 1.5f,
        radarSpicy = 0.0f,
        radarTart = 0.5f,
        radarFunky = 0.0f,
        radarSulfury = 1.0f,
        radarAlcohol = 1.5f,
        radarMouthfeel = 2.0f
    )

    private val weizenEntity = YeastStrainEntity(
        id = "weihenstephan-weizen",
        name = "Weihenstephan Weizen",
        scientificName = "Saccharomyces cerevisiae",
        origin = "Freising, Bavaria, Germany",
        countryCode = "DE",
        discipline = "Beer",
        classification = "Wheat",
        form = "Both",
        attenuationMin = 73.0,
        attenuationMax = 78.0,
        flocculation = "Low",
        tempMinC = 17.0,
        tempMaxC = 22.0,
        tempMinF = 63.0,
        tempMaxF = 72.0,
        alcoholTolerance = 10.0,
        isDiastaticus = false,
        isPhenolic = true,
        description = "Classic Bavarian wheat strain.",
        pitchingDynamics = "Banana ester intensity increases at 70°F+.",
        descriptors = "banana, clove, bubblegum",
        commercialEquivalentsJson = """[{"laboratory":"Wyeast","productCode":"3068","productName":"Weihenstephan Weizen","form":"Liquid"}]""",
        substituteIds = "munich-weizen",
        radarClean = 0.6f,
        radarFruity = 4.8f,
        radarSpicy = 4.6f,
        radarTart = 1.8f,
        radarFunky = 0.2f,
        radarSulfury = 0.6f,
        radarAlcohol = 1.2f,
        radarMouthfeel = 3.0f
    )

    private val ec1118Entity = YeastStrainEntity(
        id = "lalvin-ec-1118",
        name = "Montpellier / Lalvin EC-1118",
        scientificName = "Saccharomyces cerevisiae",
        origin = "Montpellier, France",
        countryCode = "FR",
        discipline = "Multi-Discipline",
        classification = "Wine / Mead / Cider",
        form = "Dry",
        attenuationMin = 90.0,
        attenuationMax = 100.0,
        flocculation = "High",
        tempMinC = 10.0,
        tempMaxC = 30.0,
        tempMinF = 50.0,
        tempMaxF = 86.0,
        alcoholTolerance = 18.0,
        isDiastaticus = false,
        isPhenolic = false,
        description = "Renowned Champagne strain.",
        pitchingDynamics = "Vigorous killer factor K2 active.",
        descriptors = "neutral, clean, sparkling, dry",
        commercialEquivalentsJson = """[{"laboratory":"Lallemand","productCode":"EC-1118","productName":"Lalvin EC-1118","form":"Dry"}]""",
        substituteIds = "lalvin-k1-v1116",
        radarClean = 5.0f,
        radarFruity = 1.0f,
        radarSpicy = 0.0f,
        radarTart = 1.5f,
        radarFunky = 0.0f,
        radarSulfury = 2.2f,
        radarAlcohol = 3.8f,
        radarMouthfeel = 0.2f
    )

    private val testEntities = listOf(chicoEntity, weizenEntity, ec1118Entity)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val emptyStyleLookup = StyleLookup { flowOf(emptyList()) }

    @Test
    fun getFilteredYeasts_mapsEntityToDomainCorrectly() = runTest {
        val fakeDao = FakeYeastDao(testEntities)
        val fakeCrossRefDao = FakeYeastStyleCrossRefDao()
        val repo = YeastRepository(
            yeastDao = fakeDao,
            yeastStyleCrossRefDao = fakeCrossRefDao,
            styleLookup = emptyStyleLookup,
            defaultDispatcher = testDispatcher
        )

        val results = repo.getFilteredYeasts(YeastFilterCriteria()).first()
        assertThat(results).hasSize(3)

        val chico = results.find { it.id == "chico-american-ale" }
        assertThat(chico).isNotNull()
        assertThat(chico!!.name).isEqualTo("Chico / American Ale")
        assertThat(chico.scientificName).isEqualTo("Saccharomyces cerevisiae")
        assertThat(chico.classification).isEqualTo("Ale")
        assertThat(chico.flocculation).isEqualTo("Medium")
        assertThat(chico.attenuationMin).isEqualTo(73.0)
        assertThat(chico.attenuationMax).isEqualTo(80.0)
        assertThat(chico.alcoholTolerance).isEqualTo(11.0)
        assertThat(chico.isDiastaticus).isFalse()
        assertThat(chico.isPhenolic).isFalse()
        assertThat(chico.sta1Text).isEqualTo("STA1-")
        assertThat(chico.pofText).isEqualTo("POF-")
        assertThat(chico.commercialEquivalents).hasSize(2)
        assertThat(chico.commercialEquivalents[0].laboratory).isEqualTo("White Labs")
        assertThat(chico.commercialEquivalents[0].productCode).isEqualTo("WLP001")
        assertThat(chico.commercialEquivalents[1].laboratory).isEqualTo("Fermentis")
        assertThat(chico.commercialEquivalents[1].productCode).isEqualTo("US-05")
        assertThat(chico.descriptors).containsExactly("clean", "crisp", "neutral", "balanced")
        assertThat(chico.substituteIds).containsExactly("california-ale-v", "lutra-kveik")
        assertThat(chico.radarProfile?.cleanNeutral).isEqualTo(4.8f)
    }

    @Test
    fun getFilteredYeasts_filtersByClassificationAndFlocculation() = runTest {
        val fakeDao = FakeYeastDao(testEntities)
        val fakeCrossRefDao = FakeYeastStyleCrossRefDao()
        val repo = YeastRepository(
            yeastDao = fakeDao,
            yeastStyleCrossRefDao = fakeCrossRefDao,
            styleLookup = emptyStyleLookup,
            defaultDispatcher = testDispatcher
        )

        val wheatResults = repo.getFilteredYeasts(YeastFilterCriteria(selectedClassification = "Wheat")).first()
        assertThat(wheatResults).hasSize(1)
        assertThat(wheatResults[0].id).isEqualTo("weihenstephan-weizen")

        val highFlocResults = repo.getFilteredYeasts(YeastFilterCriteria(selectedFlocculation = "High")).first()
        assertThat(highFlocResults).hasSize(1)
        assertThat(highFlocResults[0].id).isEqualTo("lalvin-ec-1118")
    }

    @Test
    fun getFilteredYeasts_filtersByAttenuationAndAlcoholToleranceSliders() = runTest {
        val fakeDao = FakeYeastDao(testEntities)
        val fakeCrossRefDao = FakeYeastStyleCrossRefDao()
        val repo = YeastRepository(
            yeastDao = fakeDao,
            yeastStyleCrossRefDao = fakeCrossRefDao,
            styleLookup = emptyStyleLookup,
            defaultDispatcher = testDispatcher
        )

        val highAttenuation = repo.getFilteredYeasts(YeastFilterCriteria(minAttenuation = 85.0)).first()
        assertThat(highAttenuation).hasSize(1)
        assertThat(highAttenuation[0].id).isEqualTo("lalvin-ec-1118")

        val highAbv = repo.getFilteredYeasts(YeastFilterCriteria(minAlcoholTolerance = 15.0)).first()
        assertThat(highAbv).hasSize(1)
        assertThat(highAbv[0].id).isEqualTo("lalvin-ec-1118")
    }

    @Test
    fun getYeastById_returnsCorrectYeastOrNull() = runTest {
        val fakeDao = FakeYeastDao(testEntities)
        val fakeCrossRefDao = FakeYeastStyleCrossRefDao()
        val repo = YeastRepository(
            yeastDao = fakeDao,
            yeastStyleCrossRefDao = fakeCrossRefDao,
            styleLookup = emptyStyleLookup,
            defaultDispatcher = testDispatcher
        )

        val found = repo.getYeastById("weihenstephan-weizen").first()
        assertThat(found).isNotNull()
        assertThat(found!!.name).isEqualTo("Weihenstephan Weizen")

        val notFound = repo.getYeastById("nonexistent").first()
        assertThat(notFound).isNull()
    }

    @Test
    fun getSubstituteYeasts_resolvesRequestedSubstitutes() = runTest {
        val fakeDao = FakeYeastDao(testEntities)
        val fakeCrossRefDao = FakeYeastStyleCrossRefDao()
        val repo = YeastRepository(
            yeastDao = fakeDao,
            yeastStyleCrossRefDao = fakeCrossRefDao,
            styleLookup = emptyStyleLookup,
            defaultDispatcher = testDispatcher
        )

        val subs = repo.getSubstituteYeasts(listOf("weihenstephan-weizen", "lalvin-ec-1118")).first()
        assertThat(subs).hasSize(2)
        assertThat(subs.map { it.id }).containsExactlyInAnyOrder("weihenstephan-weizen", "lalvin-ec-1118")
    }

    @Test
    fun getCommonStylesForYeast_resolvesBjcpStyles() = runTest {
        val fakeDao = FakeYeastDao(testEntities)
        val fakeCrossRefDao = FakeYeastStyleCrossRefDao(
            styleIdsForYeasts = mapOf("chico-american-ale" to listOf("18B"))
        )
        val styleEntity = BjcpStyleEntity(
            id = "18B",
            name = "American Pale Ale",
            category = "Pale American Ale",
            categoryNumber = "18",
            number = "18B",
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
            ibuMax = 50.0,
            srmMin = 5.0,
            srmMax = 10.0,
            ogMin = 1.045,
            ogMax = 1.060,
            fgMin = 1.010,
            fgMax = 1.015,
            abvMin = 4.5,
            abvMax = 6.2,
            commercialExamples = "",
            tags = ""
        )
        val styleLookup = StyleLookup { idsByGuideline ->
            val ids = idsByGuideline[GuidelineType.BJCP_2021].orEmpty()
            flowOf(listOf(styleEntity).filter { ids.contains(it.id) }.map { it.toDomainModel() })
        }

        val repo = YeastRepository(
            yeastDao = fakeDao,
            yeastStyleCrossRefDao = fakeCrossRefDao,
            styleLookup = styleLookup,
            defaultDispatcher = testDispatcher
        )

        val styles = repo.getCommonStylesForYeast("chico-american-ale", GuidelineType.BJCP_2021).first()
        assertThat(styles).hasSize(1)
        assertThat(styles[0].id).isEqualTo("18B")
        assertThat(styles[0].name).isEqualTo("American Pale Ale")
    }

    @Test
    fun getYeastCount_returnsTotal() = runTest {
        val fakeDao = FakeYeastDao(testEntities)
        val fakeCrossRefDao = FakeYeastStyleCrossRefDao()
        val repo = YeastRepository(
            yeastDao = fakeDao,
            yeastStyleCrossRefDao = fakeCrossRefDao,
            styleLookup = emptyStyleLookup,
            defaultDispatcher = testDispatcher
        )

        assertThat(repo.getYeastCount()).isEqualTo(3)
    }
}
