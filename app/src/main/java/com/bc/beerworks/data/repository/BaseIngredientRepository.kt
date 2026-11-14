package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.FavoriteIngredientDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.BaStyleEntity
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.CiderStyleEntity
import com.bc.beerworks.data.local.entity.FavoriteIngredientEntity
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.Ingredient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
    protected val favoriteIngredientDao: FavoriteIngredientDao? = null
) : IngredientRepository<Domain, Criteria> {

    protected abstract fun performSearch(criteria: Criteria): Flow<List<Entity>>
    protected abstract fun fetchById(id: String): Flow<Entity?>
    protected abstract suspend fun fetchByIdDirect(id: String): Entity?
    protected abstract fun fetchAll(): Flow<List<Entity>>
    protected abstract fun fetchStyleIds(id: String, guidelineType: String): Flow<List<String>>
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

    override fun getCommonStylesForItem(id: String, guidelineType: GuidelineType): Flow<List<BeerStyle>> {
        return fetchStyleIds(id, guidelineType.name).flatMapLatest { styleIds ->
            if (styleIds.isEmpty()) {
                flowOf(emptyList())
            } else {
                when (guidelineType) {
                    GuidelineType.BJCP_2021 -> bjcpStyleDao.getAllStyles().map { all ->
                        val set = styleIds.toSet()
                        all.filter { set.contains(it.id) }.map { it.toDomainModel(false) }
                    }
                    GuidelineType.BA_2026 -> baStyleDao.getAllStyles().map { all ->
                        val set = styleIds.toSet()
                        all.filter { set.contains(it.id) }.map { it.toDomainModel(false) }
                    }
                    GuidelineType.BJCP_MEAD -> meadStyleDao?.getAllStyles()?.map { all ->
                        val set = styleIds.toSet()
                        all.filter { set.contains(it.id) }.map { it.toDomainModel(false) }
                    } ?: flowOf(emptyList())
                    GuidelineType.BJCP_CIDER -> ciderStyleDao?.getAllStyles()?.map { all ->
                        val set = styleIds.toSet()
                        all.filter { set.contains(it.id) }.map { it.toDomainModel(false) }
                    } ?: flowOf(emptyList())
                }
            }
        }.flowOn(defaultDispatcher)
    }

    override suspend fun getItemCount(): Int = fetchCount()

    fun isFavorite(id: String): Flow<Boolean> =
        favoriteIngredientDao?.isFavorite(id) ?: flowOf(false)

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

    private fun String.normalizeIpa(): String =
        this.replace(Regex("\\bipa\\b", RegexOption.IGNORE_CASE), "IPA")

    private fun BjcpStyleEntity.toDomainModel(isFavorite: Boolean) = BeerStyle(
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

    private fun BaStyleEntity.toDomainModel(isFavorite: Boolean) = BeerStyle(
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

    private fun MeadStyleEntity.toDomainModel(isFavorite: Boolean) = BeerStyle(
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

    private fun CiderStyleEntity.toDomainModel(isFavorite: Boolean) = BeerStyle(
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
