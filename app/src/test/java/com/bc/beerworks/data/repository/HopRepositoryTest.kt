package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.HopDao
import com.bc.beerworks.data.local.dao.HopStyleCrossRefDao
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.HopVarietyEntity
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HopFilterCriteria
import com.bc.beerworks.data.model.HopSensoryProfileFilter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

class FakeHopDao(
    private var hops: List<HopVarietyEntity> = emptyList()
) : HopDao {
    override suspend fun insertAll(hops: List<HopVarietyEntity>) { this.hops = hops }
    override fun getAllHops(): Flow<List<HopVarietyEntity>> = flowOf(hops)
    override fun getHopById(id: String): Flow<HopVarietyEntity?> = flowOf(hops.find { it.id == id })
    override suspend fun getHopByIdDirect(id: String): HopVarietyEntity? = hops.find { it.id == id }
    override fun getChildHops(parentHopId: String): Flow<List<HopVarietyEntity>> =
        flowOf(hops.filter { it.parentHopIds.contains(parentHopId) })
    override fun getAllOrigins(): Flow<List<String>> =
        flowOf(hops.map { it.origin }.distinct().sorted())

    override fun searchAndFilterHops(
        ftsQuery: String,
        purpose: String?,
        sensoryCategory: String?,
        isAlphaFiltered: Int,
        minAlpha: Double,
        maxAlpha: Double,
        originCount: Int,
        origins: List<String>,
        baseCategory: String?
    ): Flow<List<HopVarietyEntity>> {
        var filtered = hops
        if (purpose != null) {
            filtered = filtered.filter { it.purpose == purpose || it.purpose == "Dual-Purpose" }
                .sortedWith(
                    compareBy<HopVarietyEntity> { if (it.purpose == purpose) 0 else 1 }
                        .thenBy { it.name }
                )
        }
        if (sensoryCategory != null) {
            filtered = filtered.filter { it.sensoryCategories.contains(sensoryCategory) }
        }
        if (isAlphaFiltered == 1) {
            filtered = filtered.filter { it.alphaAcidMax >= minAlpha && it.alphaAcidMin <= maxAlpha }
        }
        if (originCount > 0) {
            filtered = filtered.filter { origins.contains(it.origin) }
        }
        return flowOf(filtered)
    }

    override fun getHopsForStyle(styleId: String, guidelineType: String): Flow<List<HopVarietyEntity>> =
        flowOf(hops)

    override suspend fun getHopCount(): Int = hops.size
}

class FakeHopStyleCrossRefDao(
    private val styleIdsForHops: Map<String, List<String>> = emptyMap(),
    private val hopIdsForStyles: Map<String, List<String>> = emptyMap()
) : HopStyleCrossRefDao {
    override suspend fun insertAll(crossRefs: List<com.bc.beerworks.data.local.entity.HopStyleCrossRef>) {}
    override fun getStyleIdsForHop(hopId: String, guidelineType: String): Flow<List<String>> =
        flowOf(styleIdsForHops[hopId] ?: emptyList())
    override fun getHopIdsForStyle(styleId: String, guidelineType: String): Flow<List<String>> =
        flowOf(hopIdsForStyles[styleId] ?: emptyList())
}

@OptIn(ExperimentalCoroutinesApi::class)
class HopRepositoryTest {

    private val testCitraEntity = HopVarietyEntity(
        id = "citra",
        name = "Citra",
        origin = "United States",
        countryCode = "US",
        purpose = "Dual-Purpose",
        alphaAcidMin = 11.0,
        alphaAcidMax = 14.0,
        betaAcidMin = 3.0,
        betaAcidMax = 4.5,
        sensoryCategories = "Citrus,Tropical Fruit",
        descriptors = "Grapefruit,Lime,Mango",
        description = "Intense citrus and tropical notes.",
        parentHopIds = "",
        substituteIds = "mosaic,simcoe",
        radarCitrus = 4.9f,
        radarTropical = 4.7f,
        radarFloral = 2.2f,
        radarHerbal = 1.0f,
        radarSpicy = 0.8f,
        radarPiney = 2.1f,
        radarEarthy = 0.7f,
        radarGrassy = 1.0f
    )

    private val testCascadeEntity = HopVarietyEntity(
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
        descriptors = "Grapefruit,Floral,Pine",
        description = "Classic American aroma hop.",
        parentHopIds = "fuggle",
        substituteIds = "centennial,amarillo",
        radarCitrus = 4.5f,
        radarTropical = 1.8f,
        radarFloral = 4.2f,
        radarHerbal = 2.0f,
        radarSpicy = 2.6f,
        radarPiney = 2.8f,
        radarEarthy = 1.5f,
        radarGrassy = 1.8f
    )

    private val emptyStyleLookup = StyleLookup { flowOf(emptyList()) }

    @Test
    fun getFilteredHops_mapsEntityToDomainModelCorrectly() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val fakeHopDao = FakeHopDao(listOf(testCitraEntity))
        val fakeCrossRefDao = FakeHopStyleCrossRefDao()

        val repository = HopRepository(fakeHopDao, fakeCrossRefDao, emptyStyleLookup, testDispatcher)

        val hops = repository.getFilteredHops(HopFilterCriteria()).first()

        assertThat(hops).hasSize(1)
        val hop = hops[0]
        assertThat(hop.id).isEqualTo("citra")
        assertThat(hop.name).isEqualTo("Citra")
        assertThat(hop.countryFlag).isEqualTo("🇺🇸")
        assertThat(hop.purpose).isEqualTo("Dual-Purpose")
        assertThat(hop.alphaAcidRangeText).isEqualTo("11% – 14%")
        assertThat(hop.sensoryCategories).containsExactly("Citrus", "Tropical Fruit")
        assertThat(hop.descriptors).containsExactly("Grapefruit", "Lime", "Mango")
        assertThat(hop.radarProfile).isNotNull
        assertThat(hop.radarProfile?.citrus).isEqualTo(4.9f)
    }

    @Test
    fun getFilteredHops_appliesPurposeAndSensoryFilters() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val fakeHopDao = FakeHopDao(listOf(testCitraEntity, testCascadeEntity))
        val fakeCrossRefDao = FakeHopStyleCrossRefDao()

        val repository = HopRepository(fakeHopDao, fakeCrossRefDao, emptyStyleLookup, testDispatcher)

        val aromaHops = repository.getFilteredHops(HopFilterCriteria(selectedPurpose = "Aroma")).first()
        assertThat(aromaHops).hasSize(2)
        assertThat(aromaHops.map { it.id }).containsExactlyInAnyOrder("cascade", "citra")

        val tropicalHops =
            repository.getFilteredHops(HopFilterCriteria(selectedSensoryCategory = "Tropical"))
                .first()
        assertThat(tropicalHops).hasSize(1)
        assertThat(tropicalHops[0].id).isEqualTo("citra")

        val floralHops =
            repository.getFilteredHops(HopFilterCriteria(selectedSensoryCategory = "Floral"))
                .first()
        assertThat(floralHops).hasSize(1)
        assertThat(floralHops[0].id).isEqualTo("cascade")
    }

    @Test
    fun getFilteredHops_earthyThresholdExcludesLowEarthyAndSortsStrongestFirst() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val chinookLike = testCascadeEntity.copy(
            id = "chinook",
            name = "Chinook",
            radarPiney = 4.9f,
            radarEarthy = 2.5f
        )
        val challengerLike = testCascadeEntity.copy(
            id = "challenger",
            name = "Challenger",
            radarEarthy = 3.6f
        )
        val fuggleLike = testCascadeEntity.copy(
            id = "fuggle",
            name = "Fuggle",
            radarEarthy = 4.8f
        )
        val northernBrewerLike = testCascadeEntity.copy(
            id = "northern_brewer",
            name = "Northern Brewer",
            radarEarthy = 4.2f
        )
        val fakeHopDao =
            FakeHopDao(listOf(challengerLike, chinookLike, fuggleLike, northernBrewerLike))
        val repository =
            HopRepository(fakeHopDao, FakeHopStyleCrossRefDao(), emptyStyleLookup, testDispatcher)

        val earthyHops =
            repository.getFilteredHops(HopFilterCriteria(selectedSensoryCategory = "Earthy"))
                .first()
        assertThat(earthyHops.map { it.id }).containsExactly(
            "fuggle",
            "northern_brewer",
            "challenger"
        )
    }

    @Test
    fun getFilteredHops_sensoryProfileSupportsGrassyAndWordBoundaryOther() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val fuggleEntity = testCascadeEntity.copy(
            id = "fuggle",
            name = "Fuggle",
            descriptors = "Earthy,Damp Wood,Green Tea",
            radarCitrus = 1.2f,
            radarTropical = 0.5f,
            radarFloral = 2.0f,
            radarHerbal = 3.5f,
            radarSpicy = 2.2f,
            radarPiney = 1.0f,
            radarEarthy = 4.6f,
            radarGrassy = 3.0f
        )
        // Citra has "Grapefruit" and Galaxy has "Pineapple" -> neither should match "grape" or "apple" in Other
        val galaxyEntity = testCitraEntity.copy(
            id = "galaxy",
            name = "Galaxy",
            descriptors = "Passionfruit,Pineapple,Citrus",
            radarGrassy = 1.0f
        )
        val fakeHopDao = FakeHopDao(listOf(testCitraEntity, galaxyEntity, fuggleEntity))
        val repository =
            HopRepository(fakeHopDao, FakeHopStyleCrossRefDao(), emptyStyleLookup, testDispatcher)

        val grassyHops =
            repository.getFilteredHops(HopFilterCriteria(selectedSensoryCategory = "Grassy"))
                .first()
        assertThat(grassyHops.map { it.id }).containsExactly("fuggle")

        val otherHops =
            repository.getFilteredHops(HopFilterCriteria(selectedSensoryCategory = "Other")).first()
        assertThat(otherHops.map { it.id }).containsExactly("fuggle")
    }

    private data class JsonHopAsset(
        val id: String,
        val descriptors: List<String> = emptyList(),
        val radarProfile: Map<String, Float> = emptyMap()
    )

    @Test
    fun allNineSensoryProfileFilterOptionsMatchHopsInCatalog() {
        val assetsDir = listOf(File("src/main/assets"), File("app/src/main/assets"))
            .first { it.isDirectory }
        val hopType = object : TypeToken<List<JsonHopAsset>>() {}.type
        val rawHops: List<JsonHopAsset> =
            Gson().fromJson(File(assetsDir, "hops.json").readText(), hopType)

        val entities = rawHops.map { raw ->
            HopVarietyEntity(
                id = raw.id,
                name = raw.id,
                origin = "",
                countryCode = "",
                purpose = "Dual-Purpose",
                alphaAcidMin = 0.0,
                alphaAcidMax = 0.0,
                betaAcidMin = 0.0,
                betaAcidMax = 0.0,
                sensoryCategories = "",
                descriptors = raw.descriptors.joinToString(","),
                description = "",
                parentHopIds = "",
                substituteIds = "",
                radarCitrus = raw.radarProfile["citrus"],
                radarTropical = raw.radarProfile["tropical"],
                radarFloral = raw.radarProfile["floral"],
                radarHerbal = raw.radarProfile["herbal"],
                radarSpicy = raw.radarProfile["spicy"],
                radarPiney = raw.radarProfile["piney"],
                radarEarthy = raw.radarProfile["earthy"],
                radarGrassy = raw.radarProfile["grassy"]
            )
        }

        HopSensoryProfileFilter.OPTIONS.forEach { option ->
            val matching = entities.filter { HopSensoryProfileFilter.matches(it, option) }
            assertThat(matching)
                .describedAs(
                    "Expected non-empty hop matches for sensory filter option '%s'",
                    option
                )
                .isNotEmpty()
        }
    }

    @Test
    fun getHopById_returnsTargetHop() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val fakeHopDao = FakeHopDao(listOf(testCitraEntity, testCascadeEntity))
        val fakeCrossRefDao = FakeHopStyleCrossRefDao()

        val repository = HopRepository(fakeHopDao, fakeCrossRefDao, emptyStyleLookup, testDispatcher)

        val hop = repository.getHopById("citra").first()
        assertThat(hop).isNotNull
        assertThat(hop?.name).isEqualTo("Citra")
    }

    @Test
    fun getChildHops_dynamicallyResolvesDescendants() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val fakeHopDao = FakeHopDao(listOf(testCitraEntity, testCascadeEntity))
        val fakeCrossRefDao = FakeHopStyleCrossRefDao()

        val repository = HopRepository(fakeHopDao, fakeCrossRefDao, emptyStyleLookup, testDispatcher)

        val childrenOfFuggle = repository.getChildHops("fuggle").first()
        assertThat(childrenOfFuggle).hasSize(1)
        assertThat(childrenOfFuggle[0].id).isEqualTo("cascade")
    }

    @Test
    fun getCommonStylesForHop_resolvesAssociatedBeerStyles() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val fakeHopDao = FakeHopDao(listOf(testCitraEntity))
        val fakeCrossRefDao = FakeHopStyleCrossRefDao(
            styleIdsForHops = mapOf("citra" to listOf("21A"))
        )

        val styleEntity = BjcpStyleEntity(
            id = "21A", name = "American IPA", category = "IPA",
            categoryNumber = "21", number = "21A", overallImpression = "", aroma = "", appearance = "",
            flavor = "", mouthfeel = "", comments = "", history = "", characteristicIngredients = "",
            styleComparison = "", ibuMin = 40.0, ibuMax = 70.0, ogMin = 1.056, ogMax = 1.070,
            fgMin = 1.008, fgMax = 1.014, abvMin = 5.5, abvMax = 7.5, srmMin = 6.0, srmMax = 14.0,
            commercialExamples = "Bell's Two Hearted", tags = "ipa, bitter"
        )
        val styleLookup = StyleLookup { idsByGuideline ->
            val ids = idsByGuideline[GuidelineType.BJCP_2021].orEmpty()
            flowOf(listOf(styleEntity).filter { ids.contains(it.id) }.map { it.toDomainModel() })
        }

        val repository = HopRepository(fakeHopDao, fakeCrossRefDao, styleLookup, testDispatcher)

        val styles = repository.getCommonStylesForHop("citra", GuidelineType.BJCP_2021).first()
        assertThat(styles).hasSize(1)
        assertThat(styles[0].id).isEqualTo("21A")
        assertThat(styles[0].name).isEqualTo("American IPA")
    }
}
