package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "hop_style_cross_ref",
    primaryKeys = ["hopId", "styleId", "guidelineType"],
    indices = [
        Index(value = ["hopId"]),
        Index(value = ["styleId", "guidelineType"])
    ]
)
data class HopStyleCrossRef(
    val hopId: String,
    val styleId: String,
    val guidelineType: String
)
