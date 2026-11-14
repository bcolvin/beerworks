package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.BeerworksDatabase
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.FavoriteIngredientDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.dao.PomeDao
import com.bc.beerworks.data.local.dao.PomeStyleCrossRefDao
import com.bc.beerworks.data.local.entity.PomeVarietyEntity
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.PomeFilterCriteria
import com.bc.beerworks.data.model.PomeVariety
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class PomeRepository(
    private val pomeDao: PomeDao,
    private val pomeStyleCrossRefDao: PomeStyleCrossRefDao,
    bjcpStyleDao: BjcpStyleDao,
    baStyleDao: BaStyleDao,
    meadStyleDao: MeadStyleDao? = null,
    ciderStyleDao: CiderStyleDao? = null,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    favoriteIngredientDao: FavoriteIngredientDao? = null,
    favoriteStyleDao: FavoriteStyleDao? = null
) : BaseIngredientRepository<PomeVarietyEntity, PomeVariety, PomeFilterCriteria>(
    bjcpStyleDao,
    baStyleDao,
    meadStyleDao,
    ciderStyleDao,
    defaultDispatcher,
    favoriteIngredientDao,
    favoriteStyleDao
) {
    override val ingredientType: String = "POME"

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
        defaultDispatcher,
        database.favoriteIngredientDao(),
        database.favoriteStyleDao()
    )

    override fun performSearch(criteria: PomeFilterCriteria): Flow<List<PomeVarietyEntity>> {
        val ftsQuery = buildFtsQuery(criteria.query)

        return pomeDao.searchAndFilterPomes(
            ftsQuery = ftsQuery,
            fruitType = criteria.selectedFruitType,
            classification = criteria.selectedClassification,
            harvestSeason = criteria.selectedHarvestSeason,
            tanninLevel = criteria.selectedTanninLevel,
            styleId = criteria.selectedStyleId
        )
    }

    override fun fetchById(id: String): Flow<PomeVarietyEntity?> = pomeDao.getPomeById(id)
    override suspend fun fetchByIdDirect(id: String): PomeVarietyEntity? = pomeDao.getPomeByIdDirect(id)
    override fun fetchAll(): Flow<List<PomeVarietyEntity>> = pomeDao.getAllPomes()
    override fun fetchStyleIds(id: String, guidelineType: String): Flow<List<String>> =
        pomeStyleCrossRefDao.getStyleIdsForPome(id, guidelineType)
    override suspend fun fetchCount(): Int = pomeDao.getPomeCount()
    override fun getEntityId(entity: PomeVarietyEntity): String = entity.id
    override fun PomeVarietyEntity.toDomain(): PomeVariety = this.toDomainModel()

    // Domain alias methods for complete source-compatibility
    fun getFilteredPomes(criteria: PomeFilterCriteria): Flow<List<PomeVariety>> = getFilteredItems(criteria)
    fun getPomeById(id: String): Flow<PomeVariety?> = getItemById(id)
    suspend fun getPomeByIdDirect(id: String): PomeVariety? = getItemByIdDirect(id)
    fun getSubstitutePomes(substituteIds: List<String>): Flow<List<PomeVariety>> = getSubstitutes(substituteIds)
    fun getCommonStylesForPome(pomeId: String, guidelineType: GuidelineType = GuidelineType.BJCP_CIDER): Flow<List<BeerStyle>> =
        getCommonStylesForItem(pomeId, guidelineType)
    suspend fun getPomeCount(): Int = getItemCount()

    // Pome-specific methods
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

    fun getAllFruitTypes(): Flow<List<String>> = pomeDao.getAllFruitTypes().flowOn(defaultDispatcher)
    fun getAllClassifications(): Flow<List<String>> = pomeDao.getAllClassifications().flowOn(defaultDispatcher)

    fun getFavoritePomes(): Flow<List<PomeVariety>> = getFavoriteItems("POME")
}
