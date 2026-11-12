package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "honey_style_cross_ref",
    primaryKeys = ["honeyId", "styleId", "guidelineType"],
    indices = [
        Index(value = ["honeyId"]),
        Index(value = ["styleId", "guidelineType"])
    ]
)
data class HoneyStyleCrossRef(
    val honeyId: String,
    val styleId: String,
    val guidelineType: String
)
