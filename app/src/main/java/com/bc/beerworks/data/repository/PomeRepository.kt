package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.BeerworksDatabase
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.dao.PomeDao
import com.bc.beerworks.data.local.dao.PomeStyleCrossRefDao
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.PomeFilterCriteria
import com.bc.beerworks.data.model.PomeVariety
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class PomeRepository(
    private val pomeDao: PomeDao,
    private val pomeStyleCrossRefDao: PomeStyleCrossRefDao,
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
        database.pomeDao(),
        database.pomeStyleCrossRefDao(),
        database.bjcpStyleDao(),
        database.baStyleDao(),
        database.meadStyleDao(),
        database.ciderStyleDao(),
        defaultDispatcher
    )

    fun getFilteredPomes(criteria: PomeFilterCriteria): Flow<List<PomeVariety>> {
        val cleanQuery = criteria.query.trim()
        val ftsQuery = if (cleanQuery.isNotEmpty()) {
            val terms = cleanQuery.split("\\s+".toRegex()).filter { it.isNotBlank() }
            terms.joinToString(" ") { "$it*" }
        } else {
            ""
        }

        return pomeDao.searchAndFilterPomes(
            ftsQuery = ftsQuery,
            fruitType = criteria.selectedFruitType,
            classification = criteria.selectedClassification,
            harvestSeason = criteria.selectedHarvestSeason,
            tanninLevel = criteria.selectedTanninLevel,
            styleId = criteria.selectedStyleId
        ).map { entities ->
            entities.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    fun getPomeById(id: String): Flow<PomeVariety?> {
        return pomeDao.getPomeById(id).map { it?.toDomainModel() }.flowOn(defaultDispatcher)
    }

    suspend fun getPomeByIdDirect(id: String): PomeVariety? {
        return pomeDao.getPomeByIdDirect(id)?.toDomainModel()
    }

    fun getSubstitutePomes(substituteIds: List<String>): Flow<List<PomeVariety>> {
        if (substituteIds.isEmpty()) return flowOf(emptyList())
        return pomeDao.getAllPomes().map { allPomes ->
            val subSet = substituteIds.toSet()
            allPomes.filter { subSet.contains(it.id) }.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCommonStylesForPome(
        pomeId: String,
        guidelineType: GuidelineType = GuidelineType.BJCP_CIDER
    ): Flow<List<BeerStyle>> {
        return pomeStyleCrossRefDao.getStyleIdsForPome(pomeId, guidelineType.name).flatMapLatest { styleIds ->
            if (styleIds.isEmpty()) {
                flowOf<List<BeerStyle>>(emptyList())
            } else {
                when (guidelineType) {
                    GuidelineType.BJCP_CIDER -> ciderStyleDao?.getAllStyles()?.map { allStyles ->
                        val idSet = styleIds.toSet()
                        allStyles.filter { idSet.contains(it.id) }.map { it.toDomainModel(false) }
                    } ?: flowOf(emptyList())

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
                }
            }
        }.flowOn(defaultDispatcher)
    }

    fun getPomesForStyle(styleId: String, guidelineType: GuidelineType = GuidelineType.BJCP_CIDER): Flow<List<PomeVariety>> {
        return pomeDao.getPomesForStyle(styleId, guidelineType.name).map { entities ->
            entities.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    fun getPomesForStyles(styleIds: List<String>, guidelineType: GuidelineType = GuidelineType.BJCP_CIDER): Flow<List<PomeVariety>> {
        if (styleIds.isEmpty()) return flowOf(emptyList())
        return pomeDao.getPomesForStyles(styleIds, guidelineType.name).map { entities ->
            entities.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    fun getAllFruitTypes(): Flow<List<String>> {
        return pomeDao.getAllFruitTypes().flowOn(defaultDispatcher)
    }

    fun getAllClassifications(): Flow<List<String>> {
        return pomeDao.getAllClassifications().flowOn(defaultDispatcher)
    }

    suspend fun getPomeCount(): Int {
        return pomeDao.getPomeCount()
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
