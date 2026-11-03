package com.bc.beerworks.ui.hops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.HopFilterCriteria
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.HopRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class HopsViewModel(
    private val hopRepository: HopRepository,
    private val beerStyleRepository: BeerStyleRepository
) : ViewModel() {

    private val _filterCriteria = MutableStateFlow(HopFilterCriteria())
    val filterCriteria: StateFlow<HopFilterCriteria> = _filterCriteria.asStateFlow()

    val hops: StateFlow<List<HopVariety>> = _filterCriteria.flatMapLatest { criteria ->
        hopRepository.getFilteredHops(criteria)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val origins: StateFlow<List<String>> = hopRepository.getAllOrigins().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categories: StateFlow<List<String>> = beerStyleRepository.getCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onQueryChange(query: String) {
        _filterCriteria.value = _filterCriteria.value.copy(query = query)
    }

    fun onPurposeSelected(purpose: String?) {
        val newPurpose = if (_filterCriteria.value.selectedPurpose == purpose) null else purpose
        _filterCriteria.value = _filterCriteria.value.copy(selectedPurpose = newPurpose)
    }

    fun onSensoryCategorySelected(category: String?) {
        val newCategory = if (_filterCriteria.value.selectedSensoryCategory == category) null else category
        _filterCriteria.value = _filterCriteria.value.copy(selectedSensoryCategory = newCategory)
    }

    fun onFilterCriteriaChange(criteria: HopFilterCriteria) {
        _filterCriteria.value = criteria
    }

    fun resetFilters() {
        _filterCriteria.value = HopFilterCriteria(query = _filterCriteria.value.query)
    }
}
