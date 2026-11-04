package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "malt_style_cross_ref",
    primaryKeys = ["maltId", "styleId", "guidelineType"],
    indices = [
        Index(value = ["maltId"]),
        Index(value = ["styleId", "guidelineType"])
    ]
)
data class MaltStyleCrossRef(
    val maltId: String,
    val styleId: String,
    val guidelineType: String
)
