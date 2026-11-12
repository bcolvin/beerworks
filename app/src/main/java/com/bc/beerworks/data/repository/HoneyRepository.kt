package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.BeerworksDatabase
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.HoneyDao
import com.bc.beerworks.data.local.dao.HoneyStyleCrossRefDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HoneyFilterCriteria
import com.bc.beerworks.data.model.HoneyVariety
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class HoneyRepository(
    private val honeyDao: HoneyDao,
    private val honeyStyleCrossRefDao: HoneyStyleCrossRefDao,
    private val bjcpStyleDao: BjcpStyleDao,
    private val baStyleDao: BaStyleDao,
    private val meadStyleDao: MeadStyleDao? = null,
    private val ciderStyleDao: CiderStyleDao? = null,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    constructor(
        database: BeerworksDatabase,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : this(
        database.honeyDao(),
        database.honeyStyleCrossRefDao(),
        database.bjcpStyleDao(),
        database.baStyleDao(),
        database.meadStyleDao(),
        database.ciderStyleDao(),
        defaultDispatcher
    )

    fun getFilteredHoneys(criteria: HoneyFilterCriteria): Flow<List<HoneyVariety>> {
        val cleanQuery = criteria.query.trim()
        val ftsQuery = if (cleanQuery.isNotEmpty()) {
            val terms = cleanQuery.split("\\s+".toRegex()).filter { it.isNotBlank() }
            terms.joinToString(" ") { "$it*" }
        } else {
            ""
        }

        return honeyDao.searchAndFilterHoneys(
            ftsQuery = ftsQuery,
            classification = criteria.selectedClassification,
            flavorIntensity = criteria.selectedFlavorIntensity,
            crystallizationTendency = criteria.selectedCrystallizationTendency,
            styleId = criteria.selectedStyleId
        ).map { entities ->
            var domainList = entities.map { it.toDomainModel() }

            if (criteria.selectedUsdaColors.isNotEmpty()) {
                domainList = domainList.filter { honey ->
                    criteria.selectedUsdaColors.contains(honey.usdaColor)
                }
            }

            domainList
        }.flowOn(defaultDispatcher)
    }

    fun getHoneyById(id: String): Flow<HoneyVariety?> {
        return honeyDao.getHoneyById(id).map { it?.toDomainModel() }.flowOn(defaultDispatcher)
    }

    suspend fun getHoneyByIdDirect(id: String): HoneyVariety? {
        return honeyDao.getHoneyByIdDirect(id)?.toDomainModel()
    }

    fun getSubstituteHoneys(substituteIds: List<String>): Flow<List<HoneyVariety>> {
        if (substituteIds.isEmpty()) return flowOf(emptyList())
        return honeyDao.getAllHoneys().map { allHoneys ->
            val subSet = substituteIds.toSet()
            allHoneys.filter { subSet.contains(it.id) }.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCommonStylesForHoney(
        honeyId: String,
        guidelineType: GuidelineType = GuidelineType.BJCP_MEAD
    ): Flow<List<BeerStyle>> {
        return honeyStyleCrossRefDao.getStyleIdsForHoney(honeyId, guidelineType.name).flatMapLatest { styleIds ->
            if (styleIds.isEmpty()) {
                flowOf<List<BeerStyle>>(emptyList())
            } else {
                when (guidelineType) {
                    GuidelineType.BJCP_MEAD -> meadStyleDao?.getAllStyles()?.map { allStyles ->
                        val idSet = styleIds.toSet()
                        allStyles.filter { idSet.contains(it.id) }.map { it.toDomainModel(false) }
                    } ?: flowOf(emptyList())
                    GuidelineType.BJCP_2021 -> bjcpStyleDao.getAllStyles().map { allStyles ->
                        val idSet = styleIds.toSet()
                        allStyles.filter { idSet.contains(it.id) }.map { it.toDomainModel(false) }
                    }
                    GuidelineType.BA_2026 -> baStyleDao.getAllStyles().map { allStyles ->
                        val idSet = styleIds.toSet()
                        allStyles.filter { idSet.contains(it.id) }.map { it.toDomainModel(false) }
                    }
                    GuidelineType.BJCP_CIDER -> ciderStyleDao?.getAllStyles()?.map { allStyles ->
                        val idSet = styleIds.toSet()
                        allStyles.filter { idSet.contains(it.id) }.map { it.toDomainModel(false) }
                    } ?: flowOf(emptyList())
                }
            }
        }.flowOn(defaultDispatcher)
    }

    fun getCommonHoneysForStyle(
        styleId: String,
        guidelineType: GuidelineType = GuidelineType.BJCP_MEAD
    ): Flow<List<HoneyVariety>> {
        return honeyDao.getHoneysForStyle(styleId, guidelineType.name).map { entities ->
            entities.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    fun getAllClassifications(): Flow<List<String>> {
        return honeyDao.getAllClassifications().flowOn(defaultDispatcher)
    }

    fun getAllUsdaColors(): Flow<List<String>> {
        return honeyDao.getAllUsdaColors().flowOn(defaultDispatcher)
    }

    private fun com.bc.beerworks.data.local.entity.BjcpStyleEntity.toDomainModel(isFavorite: Boolean) = BeerStyle(
        id = id,
        name = name,
        category = category,
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
        srmMin = srmMin,
        srmMax = srmMax,
        ogMin = ogMin,
        ogMax = ogMax,
        fgMin = fgMin,
        fgMax = fgMax,
        abvMin = abvMin,
        abvMax = abvMax,
        commercialExamples = commercialExamples,
        tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        guidelineType = GuidelineType.BJCP_2021,
        isFavorite = isFavorite
    )

    private fun com.bc.beerworks.data.local.entity.BaStyleEntity.toDomainModel(isFavorite: Boolean) = BeerStyle(
        id = id,
        name = name,
        category = category,
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
        srmMin = srmMin,
        srmMax = srmMax,
        ogMin = ogMin,
        ogMax = ogMax,
        fgMin = fgMin,
        fgMax = fgMax,
        abvMin = abvMin,
        abvMax = abvMax,
        commercialExamples = commercialExamples,
        tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        guidelineType = GuidelineType.BA_2026,
        isFavorite = isFavorite
    )

    private fun com.bc.beerworks.data.local.entity.MeadStyleEntity.toDomainModel(isFavorite: Boolean) = BeerStyle(
        id = id,
        name = name,
        category = category,
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
        ibuMin = null,
        ibuMax = null,
        srmMin = null,
        srmMax = null,
        ogMin = ogMin,
        ogMax = ogMax,
        fgMin = fgMin,
        fgMax = fgMax,
        abvMin = abvMin,
        abvMax = abvMax,
        sweetness = sweetness,
        carbonation = carbonation,
        strength = strength,
        commercialExamples = commercialExamples,
        tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        guidelineType = GuidelineType.BJCP_MEAD,
        isFavorite = isFavorite
    )

    private fun com.bc.beerworks.data.local.entity.CiderStyleEntity.toDomainModel(isFavorite: Boolean) = BeerStyle(
        id = id,
        name = name,
        category = category,
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
        ibuMin = null,
        ibuMax = null,
        srmMin = null,
        srmMax = null,
        ogMin = ogMin,
        ogMax = ogMax,
        fgMin = fgMin,
        fgMax = fgMax,
        abvMin = abvMin,
        abvMax = abvMax,
        sweetness = sweetness,
        carbonation = carbonation,
        strength = strength,
        commercialExamples = commercialExamples,
        tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        guidelineType = GuidelineType.BJCP_CIDER,
        isFavorite = isFavorite
    )
}
