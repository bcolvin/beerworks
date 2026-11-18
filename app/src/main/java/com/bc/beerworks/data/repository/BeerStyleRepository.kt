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
import com.bc.beerworks.data.model.TemperatureUnit
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
    private val bjcpStyleDao: BjcpStyleDao,
    private val baStyleDao: BaStyleDao,
    private val favoriteStyleDao: FavoriteStyleDao,
    private val preferencesManager: PreferencesManager,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val meadStyleDao: MeadStyleDao? = null,
    private val ciderStyleDao: CiderStyleDao? = null
) {
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

    val selectedGuideline: Flow<GuidelineType> = preferencesManager.selectedGuideline

    suspend fun setGuideline(guidelineType: GuidelineType) {
        preferencesManager.setGuideline(guidelineType)
    }

    val keepScreenOn: Flow<Boolean> = preferencesManager.keepScreenOn

    suspend fun setKeepScreenOn(enabled: Boolean) {
        preferencesManager.setKeepScreenOn(enabled)
    }

    val temperatureUnit: Flow<TemperatureUnit> = preferencesManager.temperatureUnit

    suspend fun setTemperatureUnit(temperatureUnit: TemperatureUnit) {
        preferencesManager.setTemperatureUnit(temperatureUnit)
    }

    val confirmFavoriteRemoval: Flow<Boolean> = preferencesManager.confirmFavoriteRemoval

    suspend fun setConfirmFavoriteRemoval(enabled: Boolean) {
        preferencesManager.setConfirmFavoriteRemoval(enabled)
    }

    fun getFavoriteIds(): Flow<List<String>> = favoriteStyleDao.getAllFavoriteIds()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllFavoriteStyles(): Flow<List<BeerStyle>> {
        return getFavoriteIds().flatMapLatest { favoriteIds ->
            val favSet = favoriteIds.toSet()
            if (favSet.isEmpty()) {
                flowOf(emptyList())
            } else {
                val bjcpFlow = bjcpStyleDao.getAllStyles()
                val baFlow = baStyleDao.getAllStyles()
                val meadFlow = meadStyleDao?.getAllStyles() ?: flowOf(emptyList())
                val ciderFlow = ciderStyleDao?.getAllStyles() ?: flowOf(emptyList())

                combine(bjcpFlow, baFlow, meadFlow, ciderFlow) { bjcp, ba, mead, cider ->
                    val styles = buildList {
                        addAll(bjcp.filter { favSet.contains(it.id) }.map { it.toDomainModel(true) })
                        addAll(ba.filter { favSet.contains(it.id) }.map { it.toDomainModel(true) })
                        addAll(mead.filter { favSet.contains(it.id) }.map { it.toDomainModel(true) })
                        addAll(cider.filter { favSet.contains(it.id) }.map { it.toDomainModel(true) })
                    }
                    styles.sortedWith(
                        compareBy<BeerStyle> { it.guidelineType.ordinal }
                            .thenBy { it.categoryNumber.toIntOrNull() ?: Int.MAX_VALUE }
                            .thenBy { it.categoryNumber }
                            .thenBy { it.number }
                    )
                }
            }
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
            val cleanedFtsTokens = rawQuery.split("\\s+".toRegex())
                .map { it.replace(Regex("[^a-zA-Z0-9]"), "") }
                .filter { it.isNotBlank() }
            val ftsQuery = if (cleanedFtsTokens.isNotEmpty()) {
                cleanedFtsTokens.joinToString(" ") { "$it*" }
            } else ""

            val isSrmActive = if (criteria.minSrm > 0.0 || criteria.maxSrm < 40.0) 1 else 0
            val isAbvActive = if (criteria.minAbv > 0.0 || criteria.maxAbv < 20.0) 1 else 0
            val isIbuActive = if (criteria.minIbu > 0.0 || criteria.maxIbu < 120.0) 1 else 0

            val entitiesFlow: Flow<List<StyleEntity>> = when (guideline) {
                GuidelineType.BJCP_2021 -> if (!isSearchActive) {
                    bjcpStyleDao.filterStyles(
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
                } else {
                    bjcpStyleDao.searchAndFilterStyles(
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
                }
                GuidelineType.BA_2026 -> if (!isSearchActive) {
                    baStyleDao.filterStyles(
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
                } else {
                    baStyleDao.searchAndFilterStyles(
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
                }
                GuidelineType.BJCP_MEAD -> {
                    val meadDao = meadStyleDao
                    if (meadDao == null) {
                        flowOf(emptyList())
                    } else if (!isSearchActive) {
                        meadDao.filterStyles(
                            isAbvActive = isAbvActive,
                            minAbv = criteria.minAbv,
                            maxAbv = criteria.maxAbv,
                            category = criteria.selectedCategory
                        )
                    } else {
                        meadDao.searchAndFilterStyles(
                            ftsQuery = ftsQuery,
                            likeQuery = likeQuery,
                            isAbvActive = isAbvActive,
                            minAbv = criteria.minAbv,
                            maxAbv = criteria.maxAbv,
                            category = criteria.selectedCategory
                        )
                    }
                }
                GuidelineType.BJCP_CIDER -> {
                    val ciderDao = ciderStyleDao
                    if (ciderDao == null) {
                        flowOf(emptyList())
                    } else if (!isSearchActive) {
                        ciderDao.filterStyles(
                            isAbvActive = isAbvActive,
                            minAbv = criteria.minAbv,
                            maxAbv = criteria.maxAbv,
                            category = criteria.selectedCategory
                        )
                    } else {
                        ciderDao.searchAndFilterStyles(
                            ftsQuery = ftsQuery,
                            likeQuery = likeQuery,
                            isAbvActive = isAbvActive,
                            minAbv = criteria.minAbv,
                            maxAbv = criteria.maxAbv,
                            category = criteria.selectedCategory
                        )
                    }
                }
            }

            val isMeadOrCider = guideline == GuidelineType.BJCP_MEAD || guideline == GuidelineType.BJCP_CIDER
            entitiesFlow.map { entities ->
                val domainStyles = entities.map { it.toDomainModel(favSet.contains(it.id)) }
                    .filter { !criteria.favoritesOnly || favSet.contains(it.id) }
                    .let { list ->
                        if (!isMeadOrCider) {
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

                if (isSearchActive) {
                    if (isMeadOrCider) {
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
        return getFilteredStyles(FilterCriteria()).map { styles ->
            val groupStyles = styles.filter {
                it.categoryNumber == categoryNumber && it.guidelineType == guidelineType
            }
            if (groupStyles.isNotEmpty()) {
                BaseStyleGroup(
                    categoryNumber = categoryNumber,
                    categoryName = groupStyles.first().category,
                    styles = groupStyles,
                    guidelineType = guidelineType
                )
            } else null
        }.flowOn(defaultDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCategories(): Flow<List<String>> {
        return selectedGuideline.flatMapLatest { guideline ->
            val flow = when (guideline) {
                GuidelineType.BJCP_2021 -> bjcpStyleDao.getCategories()
                GuidelineType.BA_2026 -> baStyleDao.getCategories()
                GuidelineType.BJCP_MEAD -> meadStyleDao?.getCategories() ?: flowOf(emptyList())
                GuidelineType.BJCP_CIDER -> ciderStyleDao?.getCategories() ?: flowOf(emptyList())
            }
            flow.map { list -> list.map { it.normalizeIpa() } }
        }.flowOn(defaultDispatcher)
    }

    fun getBjcpBeerStyles(): Flow<List<BeerStyle>> =
        bjcpStyleDao.getAllStyles()
            .map { entities -> entities.map { it.toDomainModel(isFavorite = false) } }
            .flowOn(defaultDispatcher)

    suspend fun getStyleById(id: String, guidelineType: GuidelineType): BeerStyle? {
        val isFav = favoriteStyleDao.isFavorite(id).first()
        return when (guidelineType) {
            GuidelineType.BJCP_2021 -> bjcpStyleDao.getStyleById(id)?.toDomainModel(isFav)
            GuidelineType.BA_2026 -> baStyleDao.getStyleById(id)?.toDomainModel(isFav)
            GuidelineType.BJCP_MEAD -> meadStyleDao?.getStyleById(id)?.toDomainModel(isFav)
            GuidelineType.BJCP_CIDER -> ciderStyleDao?.getStyleById(id)?.toDomainModel(isFav)
        }
    }

    suspend fun getNextStyle(currentStyleId: String, guidelineType: GuidelineType): BeerStyle? = withContext(defaultDispatcher) {
        val isFav = favoriteStyleDao.isFavorite(currentStyleId).first()
        val styles = when (guidelineType) {
            GuidelineType.BJCP_2021 -> bjcpStyleDao.getAllStyles().first().map { it.toDomainModel(isFav) }
            GuidelineType.BA_2026 -> baStyleDao.getAllStyles().first().map { it.toDomainModel(isFav) }
            GuidelineType.BJCP_MEAD -> meadStyleDao?.getAllStyles()?.first()?.map { it.toDomainModel(isFav) } ?: emptyList()
            GuidelineType.BJCP_CIDER -> ciderStyleDao?.getAllStyles()?.first()?.map { it.toDomainModel(isFav) } ?: emptyList()
        }
        val currentIndex = styles.indexOfFirst { it.id == currentStyleId }
        if (currentIndex != -1 && currentIndex + 1 < styles.size) {
            styles[currentIndex + 1]
        } else {
            null
        }
    }
}
