package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.FavoriteIngredientDao
import com.bc.beerworks.data.local.entity.FavoriteIngredientEntity
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.Ingredient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseIngredientRepository<Entity : Any, Domain : Ingredient, Criteria : Any>(
    private val styleLookup: StyleLookup,
    protected val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    protected val favoriteIngredientDao: FavoriteIngredientDao? = null
) : IngredientRepository<Domain, Criteria> {

    protected open val ingredientType: String = ""

    protected abstract fun performSearch(criteria: Criteria): Flow<List<Entity>>
    protected abstract fun fetchById(id: String): Flow<Entity?>
    protected abstract suspend fun fetchByIdDirect(id: String): Entity?
    protected abstract fun fetchAll(): Flow<List<Entity>>
    protected abstract fun fetchStyleIds(id: String, guidelineType: String): Flow<List<String>>
    protected open fun fetchForStyle(styleId: String, guidelineType: String): Flow<List<Entity>> = flowOf(emptyList())
    protected abstract suspend fun fetchCount(): Int
    protected abstract fun getEntityId(entity: Entity): String
    protected abstract fun Entity.toDomain(): Domain

    override fun getFilteredItems(criteria: Criteria): Flow<List<Domain>> {
        return performSearch(criteria).map { list ->
            list.map { it.toDomain() }
        }.flowOn(defaultDispatcher)
    }

    override fun getItemById(id: String): Flow<Domain?> {
        return fetchById(id).map { it?.toDomain() }.flowOn(defaultDispatcher)
    }

    override suspend fun getItemByIdDirect(id: String): Domain? {
        return fetchByIdDirect(id)?.toDomain()
    }

    override fun getSubstitutes(substituteIds: List<String>): Flow<List<Domain>> {
        if (substituteIds.isEmpty()) return flowOf(emptyList())
        return fetchAll().map { all ->
            val set = substituteIds.toSet()
            all.filter { set.contains(getEntityId(it)) }.map { it.toDomain() }
        }.flowOn(defaultDispatcher)
    }

    override fun getItemsForStyle(styleId: String, guidelineType: GuidelineType): Flow<List<Domain>> {
        return fetchForStyle(styleId, guidelineType.name).map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(defaultDispatcher)
    }

    override fun getCommonStylesForItem(id: String, guidelineType: GuidelineType): Flow<List<BeerStyle>> {
        return fetchStyleIds(id, guidelineType.name).flatMapLatest { styleIds ->
            if (styleIds.isEmpty()) {
                flowOf(emptyList())
            } else {
                styleLookup.getStylesByIds(mapOf(guidelineType to styleIds.toSet()))
            }
        }.flowOn(defaultDispatcher)
    }

    override fun getCommonStylesForItem(id: String): Flow<List<BeerStyle>> {
        val perGuidelineIdFlows = GuidelineType.entries.map { guidelineType ->
            fetchStyleIds(id, guidelineType.name).map { ids -> guidelineType to ids.toSet() }
        }
        return combine(perGuidelineIdFlows) { pairs ->
            pairs.toMap()
        }.flatMapLatest { idsByGuideline ->
            if (idsByGuideline.values.all { it.isEmpty() }) {
                flowOf(emptyList())
            } else {
                styleLookup.getStylesByIds(idsByGuideline)
            }
        }.flowOn(defaultDispatcher)
    }

    override suspend fun getItemCount(): Int = fetchCount()

    override fun isFavorite(id: String): Flow<Boolean> =
        favoriteIngredientDao?.isFavorite(id) ?: flowOf(false)

    override fun getFavoriteItems(): Flow<List<Domain>> = getFavoriteItems(ingredientType)

    fun getFavoriteItems(type: String): Flow<List<Domain>> {
        val dao = favoriteIngredientDao ?: return flowOf(emptyList())
        return dao.getFavoriteIdsByType(type).flatMapLatest { favIds ->
            if (favIds.isEmpty()) {
                flowOf(emptyList())
            } else {
                val favSet = favIds.toSet()
                fetchAll().map { all ->
                    all.filter { favSet.contains(getEntityId(it)) }
                        .map { it.toDomain() }
                        .sortedBy { it.name.lowercase() }
                }
            }
        }.flowOn(defaultDispatcher)
    }

    override suspend fun toggleFavorite(id: String, currentFavorite: Boolean) {
        toggleFavorite(id, ingredientType, currentFavorite)
    }

    suspend fun toggleFavorite(id: String, type: String, currentFavorite: Boolean) {
        val dao = favoriteIngredientDao ?: return
        if (currentFavorite) {
            dao.removeFavorite(id)
        } else {
            dao.addFavorite(FavoriteIngredientEntity(id, type))
        }
    }

    protected fun buildFtsQuery(query: String): String =
        com.bc.beerworks.data.repository.buildFtsQuery(query)
}
