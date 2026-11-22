package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.BeerworksDatabase
import com.bc.beerworks.data.local.PreferencesManager
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.FavoriteStyleEntity
import com.bc.beerworks.data.local.entity.StyleEntity
import com.bc.beerworks.data.local.entity.normalizeIpa
import com.bc.beerworks.data.model.BaseStyleGroup
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.FilterCriteria
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.util.normalizeStyleTagKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BeerStyleRepository(
    bjcpStyleDao: BjcpStyleDao,
    baStyleDao: BaStyleDao,
    private val favoriteStyleDao: FavoriteStyleDao,
    private val preferencesManager: PreferencesManager,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    meadStyleDao: MeadStyleDao? = null,
    ciderStyleDao: CiderStyleDao? = null
) : StyleLookup {
    constructor(
        database: BeerworksDatabase,
        preferencesManager: PreferencesManager,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : this(
        database.bjcpStyleDao(),
        database.baStyleDao(),
        database.favoriteStyleDao(),
        preferencesManager,
        defaultDispatcher,
        database.meadStyleDao(),
        database.ciderStyleDao()
    )

    private interface GuidelineStyleSource {
        val isMeadOrCider: Boolean
        fun getAllStyles(): Flow<List<StyleEntity>>
        suspend fun getStyleById(id: String): StyleEntity?
        fun getCategories(): Flow<List<String>>
        fun filterStyles(criteria: FilterCriteria, isSrmActive: Int, isAbvActive: Int, isIbuActive: Int): Flow<List<StyleEntity>>
        fun searchAndFilterStyles(
            ftsQuery: String,
            likeQuery: String,
            criteria: FilterCriteria,
            isSrmActive: Int,
            isAbvActive: Int,
            isIbuActive: Int
        ): Flow<List<StyleEntity>>
    }

    private val sources: Map<GuidelineType, GuidelineStyleSource> = mapOf(
        GuidelineType.BJCP_2021 to object : GuidelineStyleSource {
            override val isMeadOrCider: Boolean = false
            override fun getAllStyles(): Flow<List<StyleEntity>> = bjcpStyleDao.getAllStyles()
            override suspend fun getStyleById(id: String): StyleEntity? = bjcpStyleDao.getStyleById(id)
            override fun getCategories(): Flow<List<String>> = bjcpStyleDao.getCategories()
            override fun filterStyles(
                criteria: FilterCriteria,
                isSrmActive: Int,
                isAbvActive: Int,
                isIbuActive: Int
            ): Flow<List<StyleEntity>> = bjcpStyleDao.filterStyles(
                isSrmActive = isSrmActive,
                minSrm = criteria.minSrm,
                maxSrm = criteria.maxSrm,
                isAbvActive = isAbvActive,
                minAbv = criteria.minAbv,
                maxAbv = criteria.maxAbv,
                isIbuActive = isIbuActive,
                minIbu = criteria.minIbu,
                maxIbu = criteria.maxIbu,
                category = criteria.selectedCategory
            )
            override fun searchAndFilterStyles(
                ftsQuery: String,
                likeQuery: String,
                criteria: FilterCriteria,
                isSrmActive: Int,
                isAbvActive: Int,
                isIbuActive: Int
            ): Flow<List<StyleEntity>> = bjcpStyleDao.searchAndFilterStyles(
                ftsQuery = ftsQuery,
                likeQuery = likeQuery,
                isSrmActive = isSrmActive,
                minSrm = criteria.minSrm,
                maxSrm = criteria.maxSrm,
                isAbvActive = isAbvActive,
                minAbv = criteria.minAbv,
                maxAbv = criteria.maxAbv,
                isIbuActive = isIbuActive,
                minIbu = criteria.minIbu,
                maxIbu = criteria.maxIbu,
                category = criteria.selectedCategory
            )
        },
        GuidelineType.BA_2026 to object : GuidelineStyleSource {
            override val isMeadOrCider: Boolean = false
            override fun getAllStyles(): Flow<List<StyleEntity>> = baStyleDao.getAllStyles()
            override suspend fun getStyleById(id: String): StyleEntity? = baStyleDao.getStyleById(id)
            override fun getCategories(): Flow<List<String>> = baStyleDao.getCategories()
            override fun filterStyles(
                criteria: FilterCriteria,
                isSrmActive: Int,
                isAbvActive: Int,
                isIbuActive: Int
            ): Flow<List<StyleEntity>> = baStyleDao.filterStyles(
                isSrmActive = isSrmActive,
                minSrm = criteria.minSrm,
                maxSrm = criteria.maxSrm,
                isAbvActive = isAbvActive,
                minAbv = criteria.minAbv,
                maxAbv = criteria.maxAbv,
                isIbuActive = isIbuActive,
                minIbu = criteria.minIbu,
                maxIbu = criteria.maxIbu,
                category = criteria.selectedCategory
            )
            override fun searchAndFilterStyles(
                ftsQuery: String,
                likeQuery: String,
                criteria: FilterCriteria,
                isSrmActive: Int,
                isAbvActive: Int,
                isIbuActive: Int
            ): Flow<List<StyleEntity>> = baStyleDao.searchAndFilterStyles(
                ftsQuery = ftsQuery,
                likeQuery = likeQuery,
                isSrmActive = isSrmActive,
                minSrm = criteria.minSrm,
                maxSrm = criteria.maxSrm,
                isAbvActive = isAbvActive,
                minAbv = criteria.minAbv,
                maxAbv = criteria.maxAbv,
                isIbuActive = isIbuActive,
                minIbu = criteria.minIbu,
                maxIbu = criteria.maxIbu,
                category = criteria.selectedCategory
            )
        },
        GuidelineType.BJCP_MEAD to object : GuidelineStyleSource {
            override val isMeadOrCider: Boolean = true
            override fun getAllStyles(): Flow<List<StyleEntity>> = meadStyleDao?.getAllStyles() ?: flowOf(emptyList())
            override suspend fun getStyleById(id: String): StyleEntity? = meadStyleDao?.getStyleById(id)
            override fun getCategories(): Flow<List<String>> = meadStyleDao?.getCategories() ?: flowOf(emptyList())
            override fun filterStyles(
                criteria: FilterCriteria,
                isSrmActive: Int,
                isAbvActive: Int,
                isIbuActive: Int
            ): Flow<List<StyleEntity>> = meadStyleDao?.filterStyles(
                isAbvActive = isAbvActive,
                minAbv = criteria.minAbv,
                maxAbv = criteria.maxAbv,
                category = criteria.selectedCategory
            ) ?: flowOf(emptyList())
            override fun searchAndFilterStyles(
                ftsQuery: String,
                likeQuery: String,
                criteria: FilterCriteria,
                isSrmActive: Int,
                isAbvActive: Int,
                isIbuActive: Int
            ): Flow<List<StyleEntity>> = meadStyleDao?.searchAndFilterStyles(
                ftsQuery = ftsQuery,
                likeQuery = likeQuery,
                isAbvActive = isAbvActive,
                minAbv = criteria.minAbv,
                maxAbv = criteria.maxAbv,
                category = criteria.selectedCategory
            ) ?: flowOf(emptyList())
        },
        GuidelineType.BJCP_CIDER to object : GuidelineStyleSource {
            override val isMeadOrCider: Boolean = true
            override fun getAllStyles(): Flow<List<StyleEntity>> = ciderStyleDao?.getAllStyles() ?: flowOf(emptyList())
            override suspend fun getStyleById(id: String): StyleEntity? = ciderStyleDao?.getStyleById(id)
            override fun getCategories(): Flow<List<String>> = ciderStyleDao?.getCategories() ?: flowOf(emptyList())
            override fun filterStyles(
                criteria: FilterCriteria,
                isSrmActive: Int,
                isAbvActive: Int,
                isIbuActive: Int
            ): Flow<List<StyleEntity>> = ciderStyleDao?.filterStyles(
                isAbvActive = isAbvActive,
                minAbv = criteria.minAbv,
                maxAbv = criteria.maxAbv,
                category = criteria.selectedCategory
            ) ?: flowOf(emptyList())
            override fun searchAndFilterStyles(
                ftsQuery: String,
                likeQuery: String,
                criteria: FilterCriteria,
                isSrmActive: Int,
                isAbvActive: Int,
                isIbuActive: Int
            ): Flow<List<StyleEntity>> = ciderStyleDao?.searchAndFilterStyles(
                ftsQuery = ftsQuery,
                likeQuery = likeQuery,
                isAbvActive = isAbvActive,
                minAbv = criteria.minAbv,
                maxAbv = criteria.maxAbv,
                category = criteria.selectedCategory
            ) ?: flowOf(emptyList())
        }
    )

    private fun sourceFor(guidelineType: GuidelineType): GuidelineStyleSource =
        sources.getValue(guidelineType)

    val selectedGuideline: Flow<GuidelineType> = preferencesManager.selectedGuideline

    suspend fun setGuideline(guidelineType: GuidelineType) {
        preferencesManager.setGuideline(guidelineType)
    }

    fun getFavoriteIds(): Flow<List<String>> = favoriteStyleDao.getAllFavoriteIds()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllFavoriteStyles(): Flow<List<BeerStyle>> {
        return getFavoriteIds().flatMapLatest { favoriteIds ->
            val favSet = favoriteIds.toSet()
            if (favSet.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(sources.values.map { it.getAllStyles() }) { lists ->
                    lists.flatMap { entities ->
                        entities.filter { favSet.contains(it.id) }.map { it.toDomainModel(true) }
                    }.sortedWith(CANONICAL_STYLE_COMPARATOR)
                }
            }
        }.flowOn(defaultDispatcher)
    }

    override fun getStylesByIds(styleIdsByGuideline: Map<GuidelineType, Set<String>>): Flow<List<BeerStyle>> {
        val nonEmpty = styleIdsByGuideline.filterValues { it.isNotEmpty() }
        if (nonEmpty.isEmpty()) return flowOf(emptyList())

        val perGuidelineFlows = nonEmpty.map { (guidelineType, idSet) ->
            sourceFor(guidelineType).getAllStyles().map { entities ->
                entities.filter { idSet.contains(it.id) }
            }
        }

        return combine(
            combine(perGuidelineFlows) { lists -> lists.flatMap { it } },
            getFavoriteIds()
        ) { matchingEntities, favoriteIds ->
            val favSet = favoriteIds.toSet()
            matchingEntities
                .map { it.toDomainModel(favSet.contains(it.id)) }
                .sortedWith(CANONICAL_STYLE_COMPARATOR)
        }.flowOn(defaultDispatcher)
    }

    suspend fun toggleFavorite(styleId: String, guidelineType: GuidelineType, currentFavorite: Boolean) {
        if (currentFavorite) {
            favoriteStyleDao.removeFavorite(styleId)
        } else {
            favoriteStyleDao.addFavorite(FavoriteStyleEntity(styleId, guidelineType.name))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFilteredStyles(criteria: FilterCriteria): Flow<List<BeerStyle>> {
        return combine(selectedGuideline, getFavoriteIds()) { guideline, favoriteIds ->
            Pair(guideline, favoriteIds.toSet())
        }.flatMapLatest { (guideline, favSet) ->
            val rawQuery = criteria.query.trim()
            val isSearchActive = rawQuery.isNotBlank()
            val likeQuery = "%$rawQuery%"
            val ftsQuery = buildFtsQuery(rawQuery)

            val isSrmActive = if (criteria.minSrm > 0.0 || criteria.maxSrm < 40.0) 1 else 0
            val isAbvActive = if (criteria.minAbv > 0.0 || criteria.maxAbv < 20.0) 1 else 0
            val isIbuActive = if (criteria.minIbu > 0.0 || criteria.maxIbu < 120.0) 1 else 0

            val source = sourceFor(guideline)
            val entitiesFlow = if (!isSearchActive) {
                source.filterStyles(criteria, isSrmActive, isAbvActive, isIbuActive)
            } else {
                source.searchAndFilterStyles(ftsQuery, likeQuery, criteria, isSrmActive, isAbvActive, isIbuActive)
            }

            entitiesFlow.map { entities ->
                val domainStyles = entities.map { it.toDomainModel(favSet.contains(it.id)) }
                    .filter { !criteria.favoritesOnly || favSet.contains(it.id) }
                    .let { list ->
                        if (!source.isMeadOrCider) {
                            list
                        } else {
                            list.filter { style ->
                                val matchesSweetness = criteria.selectedSweetness.isEmpty() ||
                                    criteria.selectedSweetness.any { style.sweetness?.contains(it, ignoreCase = true) == true }
                                val matchesCarbonation = criteria.selectedCarbonation.isEmpty() ||
                                    criteria.selectedCarbonation.any { style.carbonation?.contains(it, ignoreCase = true) == true }
                                val matchesStrength = criteria.selectedStrength.isEmpty() ||
                                    criteria.selectedStrength.any { style.strength?.contains(it, ignoreCase = true) == true }
                                matchesSweetness && matchesCarbonation && matchesStrength
                            }
                        }
                    }
                    .let { list ->
                        if (criteria.selectedTags.isEmpty()) {
                            list
                        } else {
                            val requiredTags = criteria.selectedTags.map(::normalizeStyleTagKey).toSet()
                            list.filter { style ->
                                val styleTagKeys = style.tags.map(::normalizeStyleTagKey).toSet()
                                requiredTags.all { it in styleTagKeys }
                            }
                        }
                    }

                if (isSearchActive) {
                    if (source.isMeadOrCider) {
                        domainStyles.sortedWith(
                            compareByDescending<BeerStyle> { calculateStyleSearchScore(it, rawQuery) }
                                .thenBy { it.categoryNumber }
                                .thenBy { it.number }
                        )
                    } else {
                        domainStyles.sortedWith(
                            compareByDescending<BeerStyle> { calculateStyleSearchScore(it, rawQuery) }
                                .thenBy { it.categoryNumber.toIntOrNull() ?: Int.MAX_VALUE }
                                .thenBy { it.number }
                        )
                    }
                } else {
                    domainStyles
                }
            }
        }.flowOn(defaultDispatcher)
    }

    fun getGroupedFilteredStyles(criteria: FilterCriteria): Flow<List<BaseStyleGroup>> {
        val rawQuery = criteria.query.trim()
        val isSearchActive = rawQuery.isNotBlank()

        return getFilteredStyles(criteria).map { styles ->
            val grouped = styles.groupBy { Pair(it.categoryNumber, it.category) }
                .map { (key, groupStyles) ->
                    val sortedStyles = if (isSearchActive) {
                        groupStyles.sortedWith(
                            compareByDescending<BeerStyle> { calculateStyleSearchScore(it, rawQuery) }
                                .thenBy { it.number }
                        )
                    } else {
                        groupStyles
                    }
                    val groupMaxScore = if (isSearchActive) {
                        sortedStyles.maxOfOrNull { calculateStyleSearchScore(it, rawQuery) } ?: 0
                    } else 0

                    Pair(
                        groupMaxScore,
                        BaseStyleGroup(
                            categoryNumber = key.first,
                            categoryName = key.second,
                            styles = sortedStyles,
                            guidelineType = sortedStyles.firstOrNull()?.guidelineType ?: GuidelineType.BJCP_2021
                        )
                    )
                }

            if (isSearchActive) {
                grouped.sortedWith(
                    compareByDescending<Pair<Int, BaseStyleGroup>> { it.first }
                        .thenBy { it.second.categoryNumber.toIntOrNull() ?: Int.MAX_VALUE }
                ).map { it.second }
            } else {
                grouped.map { it.second }
            }
        }.flowOn(defaultDispatcher)
    }

    fun getCategoryGroup(categoryNumber: String, guidelineType: GuidelineType): Flow<BaseStyleGroup?> {
        return combine(
            sourceFor(guidelineType).getAllStyles(),
            getFavoriteIds()
        ) { entities, favoriteIds ->
            val favSet = favoriteIds.toSet()
            val groupStyles = entities
                .map { it.toDomainModel(favSet.contains(it.id)) }
                .filter { it.categoryNumber == categoryNumber }
            if (groupStyles.isNotEmpty()) {
                BaseStyleGroup(
                    categoryNumber = categoryNumber,
                    categoryName = groupStyles.first().category,
                    styles = groupStyles,
                    guidelineType = guidelineType
                )
            } else {
                null
            }
        }.flowOn(defaultDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCategories(): Flow<List<String>> {
        return selectedGuideline.flatMapLatest { guideline ->
            sourceFor(guideline).getCategories().map { list -> list.map { it.normalizeIpa() } }
        }.flowOn(defaultDispatcher)
    }

    fun getStyles(guidelineType: GuidelineType): Flow<List<BeerStyle>> =
        sourceFor(guidelineType).getAllStyles()
            .map { entities -> entities.map { it.toDomainModel(isFavorite = false) } }
            .flowOn(defaultDispatcher)

    suspend fun getStyleById(id: String, guidelineType: GuidelineType): BeerStyle? = withContext(defaultDispatcher) {
        val isFav = favoriteStyleDao.isFavorite(id).first()
        sourceFor(guidelineType).getStyleById(id)?.toDomainModel(isFav)
    }

    suspend fun getNextStyle(currentStyleId: String, guidelineType: GuidelineType): BeerStyle? = withContext(defaultDispatcher) {
        val entities = sourceFor(guidelineType).getAllStyles().first()
        val currentIndex = entities.indexOfFirst { it.id == currentStyleId }
        if (currentIndex == -1) return@withContext null
        val nextEntity = entities.getOrNull(currentIndex + 1) ?: return@withContext null
        val isNextFav = favoriteStyleDao.isFavorite(nextEntity.id).first()
        nextEntity.toDomainModel(isNextFav)
    }

    private companion object {
        val CANONICAL_STYLE_COMPARATOR: Comparator<BeerStyle> =
            compareBy<BeerStyle> { it.guidelineType.ordinal }
                .thenBy { it.categoryNumber.toIntOrNull() ?: Int.MAX_VALUE }
                .thenBy { it.categoryNumber }
                .thenBy { it.number }
    }
}
