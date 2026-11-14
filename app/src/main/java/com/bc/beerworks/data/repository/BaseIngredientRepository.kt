package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.FavoriteIngredientDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.FavoriteIngredientEntity
import com.bc.beerworks.data.local.entity.StyleEntity
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
    protected val bjcpStyleDao: BjcpStyleDao,
    protected val baStyleDao: BaStyleDao,
    protected val meadStyleDao: MeadStyleDao? = null,
    protected val ciderStyleDao: CiderStyleDao? = null,
    protected val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    protected val favoriteIngredientDao: FavoriteIngredientDao? = null,
    protected val favoriteStyleDao: FavoriteStyleDao? = null
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

    @Suppress("UNNECESSARY_SAFE_CALL", "USELESS_ELVIS")
    private fun getStyleEntitiesForGuideline(guidelineType: GuidelineType): Flow<List<StyleEntity>> =
        when (guidelineType) {
            GuidelineType.BJCP_2021 -> bjcpStyleDao.getAllStyles() ?: flowOf(emptyList())
            GuidelineType.BA_2026 -> baStyleDao.getAllStyles() ?: flowOf(emptyList())
            GuidelineType.BJCP_MEAD -> meadStyleDao?.getAllStyles() ?: flowOf(emptyList())
            GuidelineType.BJCP_CIDER -> ciderStyleDao?.getAllStyles() ?: flowOf(emptyList())
        }

    override fun getCommonStylesForItem(id: String, guidelineType: GuidelineType): Flow<List<BeerStyle>> {
        val favIdsFlow = favoriteStyleDao?.getAllFavoriteIds() ?: flowOf(emptyList())
        return fetchStyleIds(id, guidelineType.name).flatMapLatest { styleIds ->
            if (styleIds.isEmpty()) {
                flowOf(emptyList())
            } else {
                val idSet = styleIds.toSet()
                getStyleEntitiesForGuideline(guidelineType).flatMapLatest { all ->
                    favIdsFlow.map { favIds ->
                        val favSet = favIds.toSet()
                        all.filter { idSet.contains(it.id) }
                            .map { it.toDomainModel(favSet.contains(it.id)) }
                    }
                }
            }
        }.flowOn(defaultDispatcher)
    }

    override fun getCommonStylesForItem(id: String): Flow<List<BeerStyle>> {
        return getCommonStylesForItem(id, GuidelineType.BJCP_2021).flatMapLatest { bjcp ->
            getCommonStylesForItem(id, GuidelineType.BA_2026).flatMapLatest { ba ->
                getCommonStylesForItem(id, GuidelineType.BJCP_MEAD).flatMapLatest { mead ->
                    getCommonStylesForItem(id, GuidelineType.BJCP_CIDER).map { cider ->
                        (bjcp + ba + mead + cider).sortedWith(
                            compareBy<BeerStyle> { it.guidelineType.ordinal }
                                .thenBy { it.categoryNumber.toIntOrNull() ?: Int.MAX_VALUE }
                                .thenBy { it.categoryNumber }
                                .thenBy { it.number }
                        )
                    }
                }
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

    protected fun buildFtsQuery(query: String): String {
        val clean = query.trim()
        if (clean.isEmpty()) return ""
        val terms = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
        return terms.joinToString(" ") { "$it*" }
    }
}
