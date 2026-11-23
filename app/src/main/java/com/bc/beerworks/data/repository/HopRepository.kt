package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.FavoriteIngredientDao
import com.bc.beerworks.data.local.dao.HopDao
import com.bc.beerworks.data.local.dao.HopStyleCrossRefDao
import com.bc.beerworks.data.local.entity.HopVarietyEntity
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HopFilterCriteria
import com.bc.beerworks.data.model.HopSensoryProfileFilter
import com.bc.beerworks.data.model.HopVariety
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class HopRepository(
    private val hopDao: HopDao,
    private val hopStyleCrossRefDao: HopStyleCrossRefDao,
    styleLookup: StyleLookup,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    favoriteIngredientDao: FavoriteIngredientDao? = null
) : BaseIngredientRepository<HopVarietyEntity, HopVariety, HopFilterCriteria>(
    styleLookup = styleLookup,
    defaultDispatcher = defaultDispatcher,
    favoriteIngredientDao = favoriteIngredientDao
) {
    override val ingredientType: String = "HOP"

    override fun performSearch(criteria: HopFilterCriteria): Flow<List<HopVarietyEntity>> {
        val ftsQuery = buildFtsQuery(criteria.query)
        val isAlphaFiltered = if (criteria.isAlphaFiltered) 1 else 0
        val originsList = criteria.selectedOrigins.toList()

        val selectedSensory = criteria.selectedSensoryCategory

        return hopDao.searchAndFilterHops(
            ftsQuery = ftsQuery,
            purpose = criteria.selectedPurpose,
            sensoryCategory = null,
            isAlphaFiltered = isAlphaFiltered,
            minAlpha = criteria.minAlphaAcid,
            maxAlpha = criteria.maxAlphaAcid,
            originCount = originsList.size,
            origins = originsList,
            baseCategory = criteria.selectedBaseCategory
        ).map { entities ->
            if (selectedSensory == null) {
                entities
            } else {
                entities.filter { HopSensoryProfileFilter.matches(it, selectedSensory) }
            }
        }
    }

    override fun fetchById(id: String): Flow<HopVarietyEntity?> = hopDao.getHopById(id)
    override suspend fun fetchByIdDirect(id: String): HopVarietyEntity? = hopDao.getHopByIdDirect(id)
    override fun fetchAll(): Flow<List<HopVarietyEntity>> = hopDao.getAllHops()
    override fun fetchStyleIds(id: String, guidelineType: String): Flow<List<String>> =
        hopStyleCrossRefDao.getStyleIdsForHop(id, guidelineType)
    override fun fetchForStyle(styleId: String, guidelineType: String): Flow<List<HopVarietyEntity>> =
        hopDao.getHopsForStyle(styleId, guidelineType)
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
    ): Flow<List<HopVariety>> = getItemsForStyle(styleId, guidelineType)

    fun getAllOrigins(): Flow<List<String>> = hopDao.getAllOrigins().flowOn(defaultDispatcher)

    fun getFavoriteHops(): Flow<List<HopVariety>> = getFavoriteItems("HOP")
}
