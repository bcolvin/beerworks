package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.BeerworksDatabase
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.FavoriteIngredientDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.dao.OffFlavorDao
import com.bc.beerworks.data.local.dao.OffFlavorStyleCrossRefDao
import com.bc.beerworks.data.local.entity.OffFlavorEntity
import com.bc.beerworks.data.model.OffFlavor
import com.bc.beerworks.data.model.OffFlavorFilterCriteria
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

class OffFlavorRepository(
    private val offFlavorDao: OffFlavorDao,
    private val offFlavorStyleCrossRefDao: OffFlavorStyleCrossRefDao,
    bjcpStyleDao: BjcpStyleDao,
    baStyleDao: BaStyleDao,
    meadStyleDao: MeadStyleDao? = null,
    ciderStyleDao: CiderStyleDao? = null,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    favoriteIngredientDao: FavoriteIngredientDao? = null,
    favoriteStyleDao: FavoriteStyleDao? = null
) : BaseIngredientRepository<OffFlavorEntity, OffFlavor, OffFlavorFilterCriteria>(
    bjcpStyleDao,
    baStyleDao,
    meadStyleDao,
    ciderStyleDao,
    defaultDispatcher,
    favoriteIngredientDao,
    favoriteStyleDao
) {
    override val ingredientType: String = TYPE_OFF_FLAVOR

    constructor(
        database: BeerworksDatabase,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : this(
        database.offFlavorDao(),
        database.offFlavorStyleCrossRefDao(),
        database.bjcpStyleDao(),
        database.baStyleDao(),
        database.meadStyleDao(),
        database.ciderStyleDao(),
        defaultDispatcher,
        database.favoriteIngredientDao(),
        database.favoriteStyleDao()
    )

    override fun performSearch(criteria: OffFlavorFilterCriteria): Flow<List<OffFlavorEntity>> {
        val ftsQuery = buildFtsQuery(criteria.query)
        return offFlavorDao.searchAndFilterOffFlavors(
            ftsQuery = ftsQuery,
            category = criteria.selectedCategory,
            modality = criteria.selectedModality,
            discipline = criteria.selectedDiscipline,
            permissibility = criteria.selectedPermissibility
        )
    }

    override fun fetchById(id: String): Flow<OffFlavorEntity?> = offFlavorDao.getOffFlavorById(id)
    override suspend fun fetchByIdDirect(id: String): OffFlavorEntity? = offFlavorDao.getOffFlavorByIdDirect(id)
    override fun fetchAll(): Flow<List<OffFlavorEntity>> = offFlavorDao.getAllOffFlavors()
    override fun fetchStyleIds(id: String, guidelineType: String): Flow<List<String>> =
        offFlavorStyleCrossRefDao.getStyleIdsForOffFlavor(id, guidelineType)

    // Per ADR 0008, Off-Flavors use one-way style cross-referencing (excluded from StyleDetailScreen).
    override fun fetchForStyle(styleId: String, guidelineType: String): Flow<List<OffFlavorEntity>> =
        flowOf(emptyList())

    override suspend fun fetchCount(): Int = offFlavorDao.getOffFlavorCount()
    override fun getEntityId(entity: OffFlavorEntity): String = entity.id
    override fun OffFlavorEntity.toDomain(): OffFlavor = this.toDomainModel()

    fun getAllCategories(): Flow<List<String>> = offFlavorDao.getAllCategories().flowOn(defaultDispatcher)

    companion object {
        const val TYPE_OFF_FLAVOR = "OFF_FLAVOR"
    }
}
