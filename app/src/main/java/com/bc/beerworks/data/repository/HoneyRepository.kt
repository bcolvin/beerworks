package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.FavoriteIngredientDao
import com.bc.beerworks.data.local.dao.HoneyDao
import com.bc.beerworks.data.local.dao.HoneyStyleCrossRefDao
import com.bc.beerworks.data.local.entity.HoneyVarietyEntity
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HoneyFilterCriteria
import com.bc.beerworks.data.model.HoneyVariety
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class HoneyRepository(
    private val honeyDao: HoneyDao,
    private val honeyStyleCrossRefDao: HoneyStyleCrossRefDao,
    styleLookup: StyleLookup,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    favoriteIngredientDao: FavoriteIngredientDao? = null
) : BaseIngredientRepository<HoneyVarietyEntity, HoneyVariety, HoneyFilterCriteria>(
    styleLookup = styleLookup,
    defaultDispatcher = defaultDispatcher,
    favoriteIngredientDao = favoriteIngredientDao
) {
    override val ingredientType: String = "HONEY"

    override fun performSearch(criteria: HoneyFilterCriteria): Flow<List<HoneyVarietyEntity>> {
        val ftsQuery = buildFtsQuery(criteria.query)

        return honeyDao.searchAndFilterHoneys(
            ftsQuery = ftsQuery,
            classification = criteria.selectedClassification,
            flavorIntensity = criteria.selectedFlavorIntensity,
            crystallizationTendency = criteria.selectedCrystallizationTendency,
            styleId = criteria.selectedStyleId
        ).map { entities ->
            if (criteria.selectedUsdaColors.isNotEmpty()) {
                entities.filter { entity ->
                    criteria.selectedUsdaColors.contains(entity.usdaColor)
                }
            } else {
                entities
            }
        }
    }

    override fun fetchById(id: String): Flow<HoneyVarietyEntity?> = honeyDao.getHoneyById(id)
    override suspend fun fetchByIdDirect(id: String): HoneyVarietyEntity? = honeyDao.getHoneyByIdDirect(id)
    override fun fetchAll(): Flow<List<HoneyVarietyEntity>> = honeyDao.getAllHoneys()
    override fun fetchStyleIds(id: String, guidelineType: String): Flow<List<String>> =
        honeyStyleCrossRefDao.getStyleIdsForHoney(id, guidelineType)
    override fun fetchForStyle(styleId: String, guidelineType: String): Flow<List<HoneyVarietyEntity>> =
        honeyDao.getHoneysForStyle(styleId, guidelineType)
    override suspend fun fetchCount(): Int = honeyDao.getHoneyCount()
    override fun getEntityId(entity: HoneyVarietyEntity): String = entity.id
    override fun HoneyVarietyEntity.toDomain(): HoneyVariety = this.toDomainModel()

    // Domain alias methods for complete source-compatibility
    fun getFilteredHoneys(criteria: HoneyFilterCriteria): Flow<List<HoneyVariety>> = getFilteredItems(criteria)
    fun getHoneyById(id: String): Flow<HoneyVariety?> = getItemById(id)
    suspend fun getHoneyByIdDirect(id: String): HoneyVariety? = getItemByIdDirect(id)
    fun getSubstituteHoneys(substituteIds: List<String>): Flow<List<HoneyVariety>> = getSubstitutes(substituteIds)
    fun getCommonStylesForHoney(honeyId: String, guidelineType: GuidelineType = GuidelineType.BJCP_MEAD): Flow<List<BeerStyle>> =
        getCommonStylesForItem(honeyId, guidelineType)
    suspend fun getHoneyCount(): Int = getItemCount()

    // Honey-specific methods
    fun getCommonHoneysForStyle(
        styleId: String,
        guidelineType: GuidelineType = GuidelineType.BJCP_MEAD
    ): Flow<List<HoneyVariety>> = getItemsForStyle(styleId, guidelineType)

    fun getAllClassifications(): Flow<List<String>> = honeyDao.getAllClassifications().flowOn(defaultDispatcher)

    fun getFavoriteHoneys(): Flow<List<HoneyVariety>> = getFavoriteItems("HONEY")
}
