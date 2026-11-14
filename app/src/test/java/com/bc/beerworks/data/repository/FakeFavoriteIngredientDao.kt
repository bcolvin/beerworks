package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.dao.FavoriteIngredientDao
import com.bc.beerworks.data.local.entity.FavoriteIngredientEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFavoriteIngredientDao(initialFavorites: Set<String> = emptySet()) : FavoriteIngredientDao {
    private val favoritesFlow = MutableStateFlow(initialFavorites)
    private val typesById = mutableMapOf<String, String>()

    override suspend fun addFavorite(favorite: FavoriteIngredientEntity) {
        typesById[favorite.id] = favorite.type
        favoritesFlow.value = favoritesFlow.value + favorite.id
    }

    override suspend fun removeFavorite(id: String) {
        typesById.remove(id)
        favoritesFlow.value = favoritesFlow.value - id
    }

    override fun getAllFavoriteIds(): Flow<List<String>> =
        favoritesFlow.map { it.toList() }

    override fun getFavoriteIdsByType(type: String): Flow<List<String>> =
        favoritesFlow.map { ids ->
            ids.filter { id ->
                val recordedType = typesById[id]
                recordedType == null || recordedType == type
            }
        }

    override fun isFavorite(id: String): Flow<Boolean> =
        favoritesFlow.map { it.contains(id) }
}
