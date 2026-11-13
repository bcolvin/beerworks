package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "yeast_style_cross_ref",
    primaryKeys = ["yeastId", "styleId", "guidelineType"],
    indices = [
        Index(value = ["yeastId"]),
        Index(value = ["styleId", "guidelineType"])
    ]
)
data class YeastStyleCrossRef(
    val yeastId: String,
    val styleId: String,
    val guidelineType: String
)
