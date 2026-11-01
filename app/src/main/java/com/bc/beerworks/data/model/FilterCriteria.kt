package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class FilterCriteria(
    val query: String = "",
    val minSrm: Double = 0.0,
    val maxSrm: Double = 40.0,
    val minAbv: Double = 0.0,
    val maxAbv: Double = 20.0,
    val minIbu: Double = 0.0,
    val maxIbu: Double = 120.0,
    val selectedCategory: String? = null,
    val favoritesOnly: Boolean = false
)
