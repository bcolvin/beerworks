package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.OffFlavorDao
import com.bc.beerworks.data.local.dao.OffFlavorStyleCrossRefDao
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.OffFlavorEntity
import com.bc.beerworks.data.local.entity.OffFlavorStyleCrossRef
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.OffFlavorFilterCriteria
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FakeOffFlavorDao(
    private var offFlavors: List<OffFlavorEntity> = emptyList()
) : OffFlavorDao {
    override suspend fun insertAll(offFlavors: List<OffFlavorEntity>) {
        this.offFlavors = offFlavors
    }

    override fun getAllOffFlavors(): Flow<List<OffFlavorEntity>> = flowOf(offFlavors)

    override fun getOffFlavorById(id: String): Flow<OffFlavorEntity?> =
        flowOf(offFlavors.find { it.id == id })

    override suspend fun getOffFlavorByIdDirect(id: String): OffFlavorEntity? =
        offFlavors.find { it.id == id }

    override fun getAllCategories(): Flow<List<String>> =
        flowOf(offFlavors.map { it.category }.distinct().sorted())

    override fun searchAndFilterOffFlavors(
        ftsQuery: String,
        category: String?,
        modality: String?,
        discipline: String?,
        permissibility: String?
    ): Flow<List<OffFlavorEntity>> {
        var filtered = offFlavors
        if (ftsQuery.isNotBlank()) {
            val cleanTerm = ftsQuery.replace("*", "").trim()
            filtered = filtered.filter { entity ->
                entity.toDomainModel().matchesQuery(cleanTerm)
            }
        }
        if (category != null) {
            filtered = filtered.filter { it.category == category }
        }
        if (modality != null) {
            filtered = filtered.filter { it.detectedIn.contains(modality, ignoreCase = true) }
        }
        if (discipline != null) {
            filtered = filtered.filter { it.disciplines.contains(discipline, ignoreCase = true) }
        }
        if (permissibility != null) {
            filtered = filtered.filter { it.stylePermissibility == permissibility }
        }
        return flowOf(filtered)
    }

    override suspend fun getOffFlavorCount(): Int = offFlavors.size
}

class FakeOffFlavorStyleCrossRefDao(
    private val styleIdsForOffFlavors: Map<String, List<String>> = emptyMap()
) : OffFlavorStyleCrossRefDao {
    override suspend fun insertAll(crossRefs: List<OffFlavorStyleCrossRef>) {}
    override fun getStyleIdsForOffFlavor(offFlavorId: String, guidelineType: String): Flow<List<String>> =
        flowOf(styleIdsForOffFlavors[offFlavorId] ?: emptyList())
}

@OptIn(ExperimentalCoroutinesApi::class)
class OffFlavorRepositoryTest {

    private val diacetylEntity = OffFlavorEntity(
        id = "diacetyl",
        name = "Diacetyl",
        emoji = "🧈",
        category = "Fermentation & Yeast",
        chemicalCompound = "2,3-Butanedione",
        perceptionThreshold = "0.10 – 0.15 ppm",
        typicalConcentration = "0.02 – 0.08 ppm (normal); > 0.15 ppm (fault)",
        meilgaardCode = "0620",
        detectedIn = "Aroma,Flavor,Mouthfeel",
        disciplines = "Beer,Mead,Cider",
        stylePermissibility = "Style-Permissible",
        description = "Rich butter, butterscotch, or movie popcorn.",
        aromaPerception = "Melted butter or butterscotch candy.",
        flavorPerception = "Sweetish artificial butter.",
        mouthfeelPerception = "Slick, oily coating.",
        descriptors = "Butter,Butterscotch,Movie Popcorn,Slick Palate",
        diagnosticContrastsJson = """["vs. Caramel Malt: Diacetyl coats the tongue slickly."]""",
        typicalOriginsJson = """["Natural fermentation intermediate not reabsorbed."]""",
        preventionTipsJson = """["Perform a 2-3 day diacetyl rest."]""",
        whenAppropriate = "Low levels permissible in Czech Pale Lagers and British Bitters.",
        confusedWithIds = "butyric,oxidized"
    )

    private val lightstruckEntity = OffFlavorEntity(
        id = "lightstruck",
        name = "Lightstruck (Skunky)",
        emoji = "🦨",
        category = "Water & Packaging",
        chemicalCompound = "3-Methyl-2-butene-1-thiol (MBT)",
        perceptionThreshold = "0.000004 ppm (4 ppt)",
        typicalConcentration = "Undetectable in protected beer",
        meilgaardCode = "0725",
        detectedIn = "Aroma,Flavor",
        disciplines = "Beer",
        stylePermissibility = "Always a Fault",
        description = "Unmistakable skunk-spray aroma caused by UV light.",
        aromaPerception = "Fresh skunk spray.",
        flavorPerception = "Sharp, skunky bitterness.",
        mouthfeelPerception = "No direct mouthfeel impact.",
        descriptors = "Skunk,UV Light,Green Bottle,MBT",
        diagnosticContrastsJson = """["vs. Catty: Lightstruck smells strictly like skunk spray."]""",
        typicalOriginsJson = """["UV light photolyzing hop iso-alpha acids."]""",
        preventionTipsJson = """["Package in brown glass, cans, or kegs."]""",
        whenAppropriate = "Always a fault.",
        confusedWithIds = "catty"
    )

    private fun createRepository(
        offFlavors: List<OffFlavorEntity> = listOf(diacetylEntity, lightstruckEntity),
        styleIds: Map<String, List<String>> = mapOf("diacetyl" to listOf("3B")),
        styleLookup: StyleLookup = StyleLookup { flowOf(emptyList()) }
    ): OffFlavorRepository {
        return OffFlavorRepository(
            offFlavorDao = FakeOffFlavorDao(offFlavors),
            offFlavorStyleCrossRefDao = FakeOffFlavorStyleCrossRefDao(styleIds),
            styleLookup = styleLookup,
            defaultDispatcher = UnconfinedTestDispatcher()
        )
    }

    @Test
    fun getFilteredItems_filtersByCategoryModalityDisciplineAndPermissibility() = runTest {
        val repo = createRepository()

        val mouthfeelResults = repo.getFilteredItems(
            OffFlavorFilterCriteria(selectedModality = "Mouthfeel")
        ).first()
        assertThat(mouthfeelResults).hasSize(1)
        assertThat(mouthfeelResults.first().id).isEqualTo("diacetyl")

        val packagingResults = repo.getFilteredItems(
            OffFlavorFilterCriteria(selectedCategory = "Water & Packaging")
        ).first()
        assertThat(packagingResults).hasSize(1)
        assertThat(packagingResults.first().id).isEqualTo("lightstruck")

        val faultResults = repo.getFilteredItems(
            OffFlavorFilterCriteria(selectedPermissibility = "Always a Fault")
        ).first()
        assertThat(faultResults).hasSize(1)
        assertThat(faultResults.first().id).isEqualTo("lightstruck")

        val meadResults = repo.getFilteredItems(
            OffFlavorFilterCriteria(selectedDiscipline = "Mead")
        ).first()
        assertThat(meadResults).hasSize(1)
        assertThat(meadResults.first().id).isEqualTo("diacetyl")
    }

    @Test
    fun getFilteredItems_matchesChemicalCompoundAndMeilgaardCode() = runTest {
        val repo = createRepository()

        val compoundMatch = repo.getFilteredItems(
            OffFlavorFilterCriteria(query = "Butanedione")
        ).first()
        assertThat(compoundMatch.map { it.id }).containsExactly("diacetyl")

        val meilgaardMatch = repo.getFilteredItems(
            OffFlavorFilterCriteria(query = "0725")
        ).first()
        assertThat(meilgaardMatch.map { it.id }).containsExactly("lightstruck")
    }

    @Test
    fun getCommonStylesForItem_returnsPermissibleStyles_whileGetItemsForStyle_returnsEmpty() = runTest {
        val czechPils = BjcpStyleEntity(
            id = "3B", name = "Czech Premium Pale Lager", category = "Czech Lager",
            categoryNumber = "3", number = "3B", overallImpression = "Rich, refreshing",
            aroma = "", appearance = "", flavor = "", mouthfeel = "", comments = "",
            history = "", characteristicIngredients = "", styleComparison = "",
            ibuMin = 30.0, ibuMax = 45.0, ogMin = 1.044, ogMax = 1.060,
            fgMin = 1.013, fgMax = 1.017, abvMin = 4.2, abvMax = 5.8,
            srmMin = 3.5, srmMax = 6.0, commercialExamples = "Pilsner Urquell", tags = ""
        )
        val styleLookup = StyleLookup { idsByGuideline ->
            val ids = idsByGuideline[GuidelineType.BJCP_2021].orEmpty()
            flowOf(listOf(czechPils).filter { ids.contains(it.id) }.map { it.toDomainModel() })
        }

        val repo = createRepository(styleLookup = styleLookup)

        val permissibleStyles = repo.getCommonStylesForItem("diacetyl", GuidelineType.BJCP_2021).first()
        assertThat(permissibleStyles).hasSize(1)
        assertThat(permissibleStyles.first().id).isEqualTo("3B")

        val reverseItems = repo.getItemsForStyle("3B", GuidelineType.BJCP_2021).first()
        assertThat(reverseItems).isEmpty()
    }
}
