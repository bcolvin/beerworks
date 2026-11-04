package com.bc.beerworks.ui.stylelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BaseStyleGroup
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.FilterCriteria
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.repository.BeerStyleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StyleListViewModel(
    private val repository: BeerStyleRepository
) : ViewModel() {

    val selectedGuideline: StateFlow<GuidelineType> = repository.selectedGuideline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GuidelineType.BJCP_2021
    )

    private val _filterCriteria = MutableStateFlow(FilterCriteria())
    val filterCriteria: StateFlow<FilterCriteria> = _filterCriteria.asStateFlow()

    private val _expandedCategoryNumber = MutableStateFlow<String?>(null)
    val expandedCategoryNumber: StateFlow<String?> = _expandedCategoryNumber.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val groupedStyles: StateFlow<List<BaseStyleGroup>> = _filterCriteria.flatMapLatest { criteria ->
        repository.getGroupedFilteredStyles(criteria)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val styles: StateFlow<List<BeerStyle>> = _filterCriteria.flatMapLatest { criteria ->
        repository.getFilteredStyles(criteria)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categories: StateFlow<List<String>> = repository.getCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onQueryChange(query: String) {
        _filterCriteria.value = _filterCriteria.value.copy(query = query)
    }

    fun toggleCategoryExpansion(categoryNumber: String) {
        if (_expandedCategoryNumber.value == categoryNumber) {
            _expandedCategoryNumber.value = null
        } else {
            _expandedCategoryNumber.value = categoryNumber
        }
    }

    fun setExpandedCategory(categoryNumber: String?) {
        _expandedCategoryNumber.value = categoryNumber
    }

    fun onFilterCriteriaChange(criteria: FilterCriteria) {
        _filterCriteria.value = criteria
    }

    fun resetFilters() {
        _filterCriteria.value = FilterCriteria(query = _filterCriteria.value.query)
    }

    fun onGuidelineChange(guidelineType: GuidelineType) {
        viewModelScope.launch {
            repository.setGuideline(guidelineType)
        }
    }

    fun toggleFavorite(style: BeerStyle) {
        viewModelScope.launch {
            repository.toggleFavorite(style.id, style.guidelineType, style.isFavorite)
        }
    }
}
