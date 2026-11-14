package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
interface IngredientFilterCriteria<Self : IngredientFilterCriteria<Self>> {
    val query: String
    val isFilterActive: Boolean
    fun withQuery(newQuery: String): Self
    fun resetKeepingQuery(): Self
}
