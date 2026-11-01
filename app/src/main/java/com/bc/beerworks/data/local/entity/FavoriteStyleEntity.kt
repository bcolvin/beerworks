package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_styles")
data class FavoriteStyleEntity(
    @PrimaryKey val styleId: String,
    val guidelineType: String
)
