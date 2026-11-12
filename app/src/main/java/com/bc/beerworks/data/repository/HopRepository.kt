package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.BeerworksDatabase
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.HopDao
import com.bc.beerworks.data.local.dao.HopStyleCrossRefDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HopFilterCriteria
import com.bc.beerworks.data.model.HopVariety
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class HopRepository(
    private val hopDao: HopDao,
    private val hopStyleCrossRefDao: HopStyleCrossRefDao,
    private val bjcpStyleDao: BjcpStyleDao,
    private val baStyleDao: BaStyleDao,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val meadStyleDao: MeadStyleDao? = null
) {
    constructor(
        database: BeerworksDatabase,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : this(
        database.hopDao(),
        database.hopStyleCrossRefDao(),
        database.bjcpStyleDao(),
        database.baStyleDao(),
        defaultDispatcher,
        database.meadStyleDao()
    )

    fun getFilteredHops(criteria: HopFilterCriteria): Flow<List<HopVariety>> {
        val cleanQuery = criteria.query.trim()
        val ftsQuery = if (cleanQuery.isNotEmpty()) {
            val terms = cleanQuery.split("\\s+".toRegex()).filter { it.isNotBlank() }
            terms.joinToString(" ") { "$it*" }
        } else {
            ""
        }

        val isAlphaFiltered = if (criteria.isAlphaFiltered) 1 else 0
        val originsList = criteria.selectedOrigins.toList()

        return hopDao.searchAndFilterHops(
            ftsQuery = ftsQuery,
            purpose = criteria.selectedPurpose,
            sensoryCategory = criteria.selectedSensoryCategory,
            isAlphaFiltered = isAlphaFiltered,
            minAlpha = criteria.minAlphaAcid,
            maxAlpha = criteria.maxAlphaAcid,
            originCount = originsList.size,
            origins = originsList,
            baseCategory = criteria.selectedBaseCategory
        ).map { entities ->
            entities.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    fun getHopById(id: String): Flow<HopVariety?> {
        return hopDao.getHopById(id).map { it?.toDomainModel() }.flowOn(defaultDispatcher)
    }

    suspend fun getHopByIdDirect(id: String): HopVariety? {
        return hopDao.getHopByIdDirect(id)?.toDomainModel()
    }

    fun getChildHops(parentHopId: String): Flow<List<HopVariety>> {
        return hopDao.getChildHops(parentHopId).map { entities ->
            entities.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    fun getSubstituteHops(substituteIds: List<String>): Flow<List<HopVariety>> {
        if (substituteIds.isEmpty()) return flowOf(emptyList())
        return hopDao.getAllHops().map { allHops ->
            val subSet = substituteIds.toSet()
            allHops.filter { subSet.contains(it.id) }.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCommonStylesForHop(
        hopId: String,
        guidelineType: GuidelineType = GuidelineType.BJCP_2021
    ): Flow<List<BeerStyle>> {
        return hopStyleCrossRefDao.getStyleIdsForHop(hopId, guidelineType.name).flatMapLatest { styleIds ->
            if (styleIds.isEmpty()) {
                flowOf(emptyList())
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

    fun getCommonHopsForStyle(
        styleId: String,
        guidelineType: GuidelineType = GuidelineType.BJCP_2021
    ): Flow<List<HopVariety>> {
        return hopDao.getHopsForStyle(styleId, guidelineType.name).map { entities ->
            entities.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    fun getAllOrigins(): Flow<List<String>> {
        return hopDao.getAllOrigins().flowOn(defaultDispatcher)
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
