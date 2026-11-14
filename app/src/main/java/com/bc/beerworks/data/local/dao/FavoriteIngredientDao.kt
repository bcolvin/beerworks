package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.FavoriteIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteIngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteIngredientEntity)

    @Query("DELETE FROM favorite_ingredients WHERE id = :id")
    suspend fun removeFavorite(id: String)

    @Query("SELECT id FROM favorite_ingredients")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Query("SELECT id FROM favorite_ingredients WHERE type = :type")
    fun getFavoriteIdsByType(type: String): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_ingredients WHERE id = :id)")
    fun isFavorite(id: String): Flow<Boolean>
}
