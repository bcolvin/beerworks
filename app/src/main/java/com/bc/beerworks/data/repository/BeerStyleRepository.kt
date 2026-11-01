package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.BeerworksDatabase
import com.bc.beerworks.data.local.PreferencesManager
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.entity.BaStyleEntity
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.FavoriteStyleEntity
import com.bc.beerworks.data.model.BaseStyleGroup
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.FilterCriteria
import com.bc.beerworks.data.model.GuidelineType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class BeerStyleRepository(
    private val bjcpStyleDao: BjcpStyleDao,
    private val baStyleDao: BaStyleDao,
    private val favoriteStyleDao: FavoriteStyleDao,
    private val preferencesManager: PreferencesManager
) {
    constructor(database: BeerworksDatabase, preferencesManager: PreferencesManager) : this(
        database.bjcpStyleDao(),
        database.baStyleDao(),
        database.favoriteStyleDao(),
        preferencesManager
    )

    val selectedGuideline: Flow<GuidelineType> = preferencesManager.selectedGuideline

    suspend fun setGuideline(guidelineType: GuidelineType) {
        preferencesManager.setGuideline(guidelineType)
    }

    fun getFavoriteIds(): Flow<List<String>> = favoriteStyleDao.getAllFavoriteIds()

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

            when (guideline) {
                GuidelineType.BJCP_2021 -> {
                    val flow = if (!isSearchActive) {
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
                    flow.combine(getFavoriteIds()) { entities, _ ->
                        entities.map { it.toDomainModel(favSet.contains(it.id)) }
                            .filter { !criteria.favoritesOnly || favSet.contains(it.id) }
                    }
                }
                GuidelineType.BA_2026 -> {
                    val flow = if (!isSearchActive) {
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
                    flow.combine(getFavoriteIds()) { entities, _ ->
                        entities.map { it.toDomainModel(favSet.contains(it.id)) }
                            .filter { !criteria.favoritesOnly || favSet.contains(it.id) }
                    }
                }
            }
        }.flowOn(Dispatchers.Default)
    }

    fun getGroupedFilteredStyles(criteria: FilterCriteria): Flow<List<BaseStyleGroup>> {
        return getFilteredStyles(criteria).map { styles ->
            styles.groupBy { Pair(it.categoryNumber, it.category) }
                .map { (key, groupStyles) ->
                    BaseStyleGroup(
                        categoryNumber = key.first,
                        categoryName = key.second,
                        styles = groupStyles,
                        guidelineType = groupStyles.firstOrNull()?.guidelineType ?: GuidelineType.BJCP_2021
                    )
                }
        }.flowOn(Dispatchers.Default)
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
        }.flowOn(Dispatchers.Default)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCategories(): Flow<List<String>> {
        return selectedGuideline.flatMapLatest { guideline ->
            when (guideline) {
                GuidelineType.BJCP_2021 -> bjcpStyleDao.getCategories()
                GuidelineType.BA_2026 -> baStyleDao.getCategories()
            }
        }.flowOn(Dispatchers.Default)
    }

    suspend fun getStyleById(id: String, guidelineType: GuidelineType): BeerStyle? {
        return when (guidelineType) {
            GuidelineType.BJCP_2021 -> bjcpStyleDao.getStyleById(id)?.toDomainModel(false)
            GuidelineType.BA_2026 -> baStyleDao.getStyleById(id)?.toDomainModel(false)
        }
    }

    private fun BjcpStyleEntity.toDomainModel(isFav: Boolean) = BeerStyle(
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
        ibuMin = ibuMin,
        ibuMax = ibuMax,
        ogMin = ogMin,
        ogMax = ogMax,
        fgMin = fgMin,
        fgMax = fgMax,
        abvMin = abvMin,
        abvMax = abvMax,
        srmMin = srmMin,
        srmMax = srmMax,
        commercialExamples = commercialExamples,
        tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
        guidelineType = GuidelineType.BJCP_2021,
        isFavorite = isFav
    )

    private fun BaStyleEntity.toDomainModel(isFav: Boolean) = BeerStyle(
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
        ibuMin = ibuMin,
        ibuMax = ibuMax,
        ogMin = ogMin,
        ogMax = ogMax,
        fgMin = fgMin,
        fgMax = fgMax,
        abvMin = abvMin,
        abvMax = abvMax,
        srmMin = srmMin,
        srmMax = srmMax,
        commercialExamples = commercialExamples,
        tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
        guidelineType = GuidelineType.BA_2026,
        isFavorite = isFav
    )
}
