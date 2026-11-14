package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.BeerworksDatabase
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.FavoriteIngredientDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.dao.YeastDao
import com.bc.beerworks.data.local.dao.YeastStyleCrossRefDao
import com.bc.beerworks.data.local.entity.YeastStrainEntity
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.YeastFilterCriteria
import com.bc.beerworks.data.model.YeastStrain
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class YeastRepository(
    private val yeastDao: YeastDao,
    private val yeastStyleCrossRefDao: YeastStyleCrossRefDao,
    bjcpStyleDao: BjcpStyleDao,
    baStyleDao: BaStyleDao,
    meadStyleDao: MeadStyleDao? = null,
    ciderStyleDao: CiderStyleDao? = null,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    favoriteIngredientDao: FavoriteIngredientDao? = null
) : BaseIngredientRepository<YeastStrainEntity, YeastStrain, YeastFilterCriteria>(
    bjcpStyleDao,
    baStyleDao,
    meadStyleDao,
    ciderStyleDao,
    defaultDispatcher,
    favoriteIngredientDao
) {
    constructor(
        database: BeerworksDatabase,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : this(
        database.yeastDao(),
        database.yeastStyleCrossRefDao(),
        database.bjcpStyleDao(),
        database.baStyleDao(),
        database.meadStyleDao(),
        database.ciderStyleDao(),
        defaultDispatcher,
        database.favoriteIngredientDao()
    )

    override fun performSearch(criteria: YeastFilterCriteria): Flow<List<YeastStrainEntity>> {
        val ftsQuery = buildFtsQuery(criteria.query)

        return yeastDao.searchAndFilterYeasts(
            ftsQuery = ftsQuery,
            classification = criteria.selectedClassification,
            flocculation = criteria.selectedFlocculation,
            minAttenuation = criteria.minAttenuation,
            minAlcoholTolerance = criteria.minAlcoholTolerance,
            styleId = criteria.selectedStyleId
        )
    }

    override fun fetchById(id: String): Flow<YeastStrainEntity?> = yeastDao.getYeastById(id)
    override suspend fun fetchByIdDirect(id: String): YeastStrainEntity? = yeastDao.getYeastByIdDirect(id)
    override fun fetchAll(): Flow<List<YeastStrainEntity>> = yeastDao.getAllYeasts()
    override fun fetchStyleIds(id: String, guidelineType: String): Flow<List<String>> =
        yeastStyleCrossRefDao.getStyleIdsForYeast(id, guidelineType)
    override suspend fun fetchCount(): Int = yeastDao.getYeastCount()
    override fun getEntityId(entity: YeastStrainEntity): String = entity.id
    override fun YeastStrainEntity.toDomain(): YeastStrain = this.toDomainModel()

    // Domain alias methods for complete source-compatibility
    fun getFilteredYeasts(criteria: YeastFilterCriteria): Flow<List<YeastStrain>> = getFilteredItems(criteria)
    fun getYeastById(id: String): Flow<YeastStrain?> = getItemById(id)
    suspend fun getYeastByIdDirect(id: String): YeastStrain? = getItemByIdDirect(id)
    fun getSubstituteYeasts(substituteIds: List<String>): Flow<List<YeastStrain>> = getSubstitutes(substituteIds)
    fun getCommonStylesForYeast(yeastId: String, guidelineType: GuidelineType = GuidelineType.BJCP_2021): Flow<List<BeerStyle>> =
        getCommonStylesForItem(yeastId, guidelineType)
    suspend fun getYeastCount(): Int = getItemCount()

    // Yeast-specific methods
    fun getYeastsForStyle(styleId: String, guidelineType: GuidelineType = GuidelineType.BJCP_2021): Flow<List<YeastStrain>> {
        return yeastDao.getYeastsForStyle(styleId, guidelineType.name).map { entities ->
            entities.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    fun getYeastsForStyles(styleIds: List<String>, guidelineType: GuidelineType = GuidelineType.BJCP_2021): Flow<List<YeastStrain>> {
        if (styleIds.isEmpty()) return flowOf(emptyList())
        return yeastDao.getYeastsForStyles(styleIds, guidelineType.name).map { entities ->
            entities.map { it.toDomainModel() }
        }.flowOn(defaultDispatcher)
    }

    fun getAllClassifications(): Flow<List<String>> = yeastDao.getAllClassifications().flowOn(defaultDispatcher)
    fun getAllFlocculations(): Flow<List<String>> = yeastDao.getAllFlocculations().flowOn(defaultDispatcher)

    fun getFavoriteYeasts(): Flow<List<YeastStrain>> = getFavoriteItems("YEAST")
}
