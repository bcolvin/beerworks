package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.BeerworksDatabase
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.HopDao
import com.bc.beerworks.data.local.dao.HopStyleCrossRefDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.HopVarietyEntity
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HopFilterCriteria
import com.bc.beerworks.data.model.HopVariety
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class HopRepository(
    private val hopDao: HopDao,
    private val hopStyleCrossRefDao: HopStyleCrossRefDao,
    bjcpStyleDao: BjcpStyleDao,
    baStyleDao: BaStyleDao,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    meadStyleDao: MeadStyleDao? = null,
    ciderStyleDao: CiderStyleDao? = null
) : BaseIngredientRepository<HopVarietyEntity, HopVariety, HopFilterCriteria>(
    bjcpStyleDao,
    baStyleDao,
    meadStyleDao,
    ciderStyleDao,
    defaultDispatcher
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
        database.meadStyleDao(),
        database.ciderStyleDao()
    )

    override fun performSearch(criteria: HopFilterCriteria): Flow<List<HopVarietyEntity>> {
        val ftsQuery = buildFtsQuery(criteria.query)
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
        )
    }

    override fun fetchById(id: String): Flow<HopVarietyEntity?> = hopDao.getHopById(id)
    override suspend fun fetchByIdDirect(id: String): HopVarietyEntity? = hopDao.getHopByIdDirect(id)
    override fun fetchAll(): Flow<List<HopVarietyEntity>> = hopDao.getAllHops()
    override fun fetchStyleIds(id: String, guidelineType: String): Flow<List<String>> =
        hopStyleCrossRefDao.getStyleIdsForHop(id, guidelineType)
    override suspend fun fetchCount(): Int = hopDao.getHopCount()
    override fun getEntityId(entity: HopVarietyEntity): String = entity.id
    override fun HopVarietyEntity.toDomain(): HopVariety = this.toDomainModel()

    // Domain alias methods for complete source-compatibility
    fun getFilteredHops(criteria: HopFilterCriteria): Flow<List<HopVariety>> = getFilteredItems(criteria)
    fun getHopById(id: String): Flow<HopVariety?> = getItemById(id)
    suspend fun getHopByIdDirect(id: String): HopVariety? = getItemByIdDirect(id)
    fun getSubstituteHops(substituteIds: List<String>): Flow<List<HopVariety>> = getSubstitutes(substituteIds)
    fun getCommonStylesForHop(hopId: String, guidelineType: GuidelineType = GuidelineType.BJCP_2021): Flow<List<BeerStyle>> =
        getCommonStylesForItem(hopId, guidelineType)
    suspend fun getHopCount(): Int = getItemCount()

    // Hop-specific methods
    fun getChildHops(parentHopId: String): Flow<List<HopVariety>> {
        return hopDao.getChildHops(parentHopId).map { entities ->
            entities.map { it.toDomainModel() }
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

    fun getAllOrigins(): Flow<List<String>> = hopDao.getAllOrigins().flowOn(defaultDispatcher)
}
