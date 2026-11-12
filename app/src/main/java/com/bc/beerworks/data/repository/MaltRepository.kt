package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.BeerworksDatabase
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.MaltDao
import com.bc.beerworks.data.local.dao.MaltStyleCrossRefDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.MaltFilterCriteria
import com.bc.beerworks.data.model.MaltVariety
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class MaltRepository(
    private val maltDao: MaltDao,
    private val maltStyleCrossRefDao: MaltStyleCrossRefDao,
    private val bjcpStyleDao: BjcpStyleDao,
    private val baStyleDao: BaStyleDao,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val meadStyleDao: MeadStyleDao? = null
) {
    constructor(
        database: BeerworksDatabase,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : this(
        database.maltDao(),
        database.maltStyleCrossRefDao(),
        database.bjcpStyleDao(),
        database.baStyleDao(),
        defaultDispatcher,
        database.meadStyleDao()
    )

    fun getFilteredMalts(criteria: MaltFilterCriteria): Flow<List<MaltVariety>> {
        val cleanQuery = criteria.query.trim()
        val ftsQuery = if (cleanQuery.isNotEmpty()) {
            val terms = cleanQuery.split("\\s+".toRegex()).filter { it.isNotBlank() }
            terms.joinToString(" ") { "$it*" }
        } else {
            ""
        }

        // Color tier conversion for query
        val colorTier = when (criteria.selectedColorTier) {
            "Pale (<10°L)" -> "Pale"
            "Amber (10–50°L)" -> "Amber"
            "Brown (50–150°L)" -> "Brown"
            "Dark (>150°L)" -> "Dark"
            else -> criteria.selectedColorTier
        }

        return maltDao.searchAndFilterMalts(
            ftsQuery = ftsQuery,
            category = criteria.selectedCategory,
            grainType = criteria.selectedGrainType,
            diastaticTier = criteria.selectedDiastaticTier,
            colorTier = colorTier,
            baseCategory = criteria.selectedBaseCategory
        ).map { entities ->
            var domainList = entities.map { it.toDomainModel() }

            // Filter by functional tags if any are selected
            if (criteria.selectedFunctionalTags.isNotEmpty()) {
                domainList = domainList.filter { malt ->
                    criteria.selectedFunctionalTags.all { selectedTag ->
                        malt.functionalTags.any { it.equals(selectedTag, ignoreCase = true) }
                    }
                }
            }

            domainList
        }.flowOn(defaultDispatcher)
    }

    fun getMaltById(id: String): Flow<MaltVariety?> {
        return maltDao.getMaltById(id).map { it?.toDomainModel() }.flowOn(defaultDispatcher)
    }

    suspend fun getMaltByIdDirect(id: String): MaltVariety? {
        return maltDao.getMaltByIdDirect(id)?.toDomainModel()
    }

    fun getSubstituteMalts(substituteIds: List<String>): Flow<List<MaltVariety>> {
        if (substituteIds.isEmpty()) return flowOf(emptyList())
        return maltDao.getAllMalts().map { allMalts ->
            val subSet = substituteIds.toSet()
            allMalts.filter { subSet.contains(it.id) }.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCommonStylesForMalt(
        maltId: String,
        guidelineType: GuidelineType = GuidelineType.BJCP_2021
    ): Flow<List<BeerStyle>> {
        return maltStyleCrossRefDao.getStyleIdsForMalt(maltId, guidelineType.name).flatMapLatest { styleIds ->
            if (styleIds.isEmpty()) {
                flowOf<List<BeerStyle>>(emptyList())
            } else {
                when (guidelineType) {
                    GuidelineType.BJCP_2021 -> bjcpStyleDao.getAllStyles().map { allStyles ->
                        val idSet = styleIds.toSet()
                        allStyles.filter { idSet.contains(it.id) }.map { it.toDomainModel(false) }
                    }
                    GuidelineType.BA_2026 -> baStyleDao.getAllStyles().map { allStyles ->
                        val idSet = styleIds.toSet()
                        allStyles.filter { idSet.contains(it.id) }.map { it.toDomainModel(false) }
                    }
                    GuidelineType.BJCP_MEAD -> meadStyleDao?.getAllStyles()?.map { allStyles ->
                        val idSet = styleIds.toSet()
                        allStyles.filter { idSet.contains(it.id) }.map { it.toDomainModel(false) }
                    } ?: flowOf(emptyList())
                    GuidelineType.BJCP_CIDER -> flowOf(emptyList())
                }
            }
        }.flowOn(defaultDispatcher)
    }

    fun getCommonMaltsForStyle(
        styleId: String,
        guidelineType: GuidelineType = GuidelineType.BJCP_2021
    ): Flow<List<MaltVariety>> {
        return maltDao.getMaltsForStyle(styleId, guidelineType.name).map { entities ->
            entities.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    fun getAllCategories(): Flow<List<String>> {
        return maltDao.getAllCategories().flowOn(defaultDispatcher)
    }

    fun getAllGrainTypes(): Flow<List<String>> {
        return maltDao.getAllGrainTypes().flowOn(defaultDispatcher)
    }

    private fun String.normalizeIpa(): String =
        this.replace(Regex("\\bipa\\b", RegexOption.IGNORE_CASE), "IPA")

    private fun com.bc.beerworks.data.local.entity.BjcpStyleEntity.toDomainModel(isFavorite: Boolean) = BeerStyle(
        id = id,
        name = name.normalizeIpa(),
        category = category.normalizeIpa(),
        categoryNumber = categoryNumber,
        number = number,
        overallImpression = overallImpression,
        aroma = aroma,
        appearance = appearance,
        flavor = flavor,
        mouthfeel = mouthfeel,
        comments = comments,
        history = history,
        characteristicIngredients = characteristicIngredients,
        styleComparison = styleComparison,
        ibuMin = ibuMin,
        ibuMax = ibuMax,
        ogMin = ogMin,
        ogMax = ogMax,
        fgMin = fgMin,
        fgMax = fgMax,
        abvMin = abvMin,
        abvMax = abvMax,
        srmMin = srmMin,
        srmMax = srmMax,
        commercialExamples = commercialExamples,
        tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
        isFavorite = isFavorite,
        guidelineType = GuidelineType.BJCP_2021
    )

    private fun com.bc.beerworks.data.local.entity.BaStyleEntity.toDomainModel(isFavorite: Boolean) = BeerStyle(
        id = id,
        name = name.normalizeIpa(),
        category = category.normalizeIpa(),
        categoryNumber = categoryNumber,
        number = number,
        overallImpression = overallImpression,
        aroma = aroma,
        appearance = appearance,
        flavor = flavor,
        mouthfeel = mouthfeel,
        comments = comments,
        history = history,
        characteristicIngredients = characteristicIngredients,
        styleComparison = styleComparison,
        ibuMin = ibuMin,
        ibuMax = ibuMax,
        ogMin = ogMin,
        ogMax = ogMax,
        fgMin = fgMin,
        fgMax = fgMax,
        abvMin = abvMin,
        abvMax = abvMax,
        srmMin = srmMin,
        srmMax = srmMax,
        commercialExamples = commercialExamples,
        tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
        isFavorite = isFavorite,
        guidelineType = GuidelineType.BA_2026
    )
}
