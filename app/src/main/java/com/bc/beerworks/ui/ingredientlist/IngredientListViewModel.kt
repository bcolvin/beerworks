package com.bc.beerworks.ui.ingredientlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.Ingredient
import com.bc.beerworks.data.model.IngredientFilterCriteria
import com.bc.beerworks.data.repository.IngredientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
open class IngredientListViewModel<T : Ingredient, C : IngredientFilterCriteria<C>>(
    repository: IngredientRepository<T, C>,
    initialCriteria: C
) : ViewModel() {

    private val _filterCriteria = MutableStateFlow(initialCriteria)
    val filterCriteria: StateFlow<C> = _filterCriteria.asStateFlow()

    val items: StateFlow<List<T>> = _filterCriteria
        .flatMapLatest { criteria -> repository.getFilteredItems(criteria) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    protected fun updateCriteria(transform: (C) -> C) {
        _filterCriteria.update(transform)
    }

    fun onQueryChange(query: String) {
        _filterCriteria.update { it.withQuery(query) }
    }

    fun onFilterCriteriaChange(criteria: C) {
        _filterCriteria.value = criteria
    }

    fun resetFilters() {
        _filterCriteria.update { it.resetKeepingQuery() }
    }
}
