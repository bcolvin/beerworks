package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.FavoriteIngredientDao
import com.bc.beerworks.data.local.dao.MaltDao
import com.bc.beerworks.data.local.dao.MaltStyleCrossRefDao
import com.bc.beerworks.data.local.entity.MaltVarietyEntity
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.MaltFilterCriteria
import com.bc.beerworks.data.model.MaltVariety
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class MaltRepository(
    private val maltDao: MaltDao,
    private val maltStyleCrossRefDao: MaltStyleCrossRefDao,
    styleLookup: StyleLookup,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    favoriteIngredientDao: FavoriteIngredientDao? = null
) : BaseIngredientRepository<MaltVarietyEntity, MaltVariety, MaltFilterCriteria>(
    styleLookup = styleLookup,
    defaultDispatcher = defaultDispatcher,
    favoriteIngredientDao = favoriteIngredientDao
) {
    override val ingredientType: String = "MALT"

    override fun performSearch(criteria: MaltFilterCriteria): Flow<List<MaltVarietyEntity>> {
        val ftsQuery = buildFtsQuery(criteria.query)
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
            if (criteria.selectedFunctionalTags.isNotEmpty()) {
                entities.filter { entity ->
                    val domain = entity.toDomainModel()
                    criteria.selectedFunctionalTags.all { selectedTag ->
                        domain.functionalTags.any { it.equals(selectedTag, ignoreCase = true) }
                    }
                }
            } else {
                entities
            }
        }
    }

    override fun fetchById(id: String): Flow<MaltVarietyEntity?> = maltDao.getMaltById(id)
    override suspend fun fetchByIdDirect(id: String): MaltVarietyEntity? = maltDao.getMaltByIdDirect(id)
    override fun fetchAll(): Flow<List<MaltVarietyEntity>> = maltDao.getAllMalts()
    override fun fetchStyleIds(id: String, guidelineType: String): Flow<List<String>> =
        maltStyleCrossRefDao.getStyleIdsForMalt(id, guidelineType)
    override fun fetchForStyle(styleId: String, guidelineType: String): Flow<List<MaltVarietyEntity>> =
        maltDao.getMaltsForStyle(styleId, guidelineType)
    override suspend fun fetchCount(): Int = maltDao.getMaltCount()
    override fun getEntityId(entity: MaltVarietyEntity): String = entity.id
    override fun MaltVarietyEntity.toDomain(): MaltVariety = this.toDomainModel()

    // Domain alias methods for complete source-compatibility
    fun getFilteredMalts(criteria: MaltFilterCriteria): Flow<List<MaltVariety>> = getFilteredItems(criteria)
    fun getMaltById(id: String): Flow<MaltVariety?> = getItemById(id)
    suspend fun getMaltByIdDirect(id: String): MaltVariety? = getItemByIdDirect(id)
    fun getSubstituteMalts(substituteIds: List<String>): Flow<List<MaltVariety>> = getSubstitutes(substituteIds)
    fun getCommonStylesForMalt(maltId: String, guidelineType: GuidelineType = GuidelineType.BJCP_2021): Flow<List<BeerStyle>> =
        getCommonStylesForItem(maltId, guidelineType)
    suspend fun getMaltCount(): Int = getItemCount()

    // Malt-specific methods
    fun getCommonMaltsForStyle(
        styleId: String,
        guidelineType: GuidelineType = GuidelineType.BJCP_2021
    ): Flow<List<MaltVariety>> = getItemsForStyle(styleId, guidelineType)

    fun getAllCategories(): Flow<List<String>> = maltDao.getAllCategories().flowOn(defaultDispatcher)
    fun getAllGrainTypes(): Flow<List<String>> = maltDao.getAllGrainTypes().flowOn(defaultDispatcher)

    fun getFavoriteMalts(): Flow<List<MaltVariety>> = getFavoriteItems("MALT")
}
