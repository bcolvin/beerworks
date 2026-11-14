package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_ingredients")
data class FavoriteIngredientEntity(
    @PrimaryKey val id: String,
    val type: String
)
