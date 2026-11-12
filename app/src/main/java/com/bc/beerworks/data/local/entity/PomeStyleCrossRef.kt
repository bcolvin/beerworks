package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "pome_style_cross_ref",
    primaryKeys = ["pomeId", "styleId", "guidelineType"],
    indices = [
        Index(value = ["pomeId"]),
        Index(value = ["styleId", "guidelineType"])
    ]
)
data class PomeStyleCrossRef(
    val pomeId: String,
    val styleId: String,
    val guidelineType: String
)
