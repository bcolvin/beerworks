package com.bc.beerworks.data.repository

import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository<T : Ingredient, C : Any> {
    fun getFilteredItems(criteria: C): Flow<List<T>>
    fun getItemById(id: String): Flow<T?>
    suspend fun getItemByIdDirect(id: String): T?
    fun getSubstitutes(substituteIds: List<String>): Flow<List<T>>
    fun getCommonStylesForItem(id: String): Flow<List<BeerStyle>>
    fun getCommonStylesForItem(id: String, guidelineType: GuidelineType): Flow<List<BeerStyle>>
    fun isFavorite(id: String): Flow<Boolean>
    fun getFavoriteItems(): Flow<List<T>>
    suspend fun toggleFavorite(id: String, currentFavorite: Boolean)
    suspend fun getItemCount(): Int
}
