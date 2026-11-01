package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.FavoriteStyleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStyleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteStyleEntity)

    @Query("DELETE FROM favorite_styles WHERE styleId = :styleId")
    suspend fun removeFavorite(styleId: String)

    @Query("SELECT styleId FROM favorite_styles")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_styles WHERE styleId = :styleId)")
    fun isFavorite(styleId: String): Flow<Boolean>
}
