package com.bc.beerworks.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "off_flavor_style_cross_ref",
    primaryKeys = ["offFlavorId", "styleId", "guidelineType"],
    indices = [
        Index(value = ["offFlavorId"]),
        Index(value = ["styleId", "guidelineType"])
    ]
)
data class OffFlavorStyleCrossRef(
    val offFlavorId: String,
    val styleId: String,
    val guidelineType: String
)
