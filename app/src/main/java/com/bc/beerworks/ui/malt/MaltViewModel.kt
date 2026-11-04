package com.bc.beerworks.ui.malt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.MaltFilterCriteria
import com.bc.beerworks.data.model.MaltVariety
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.MaltRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class MaltViewModel(
    private val maltRepository: MaltRepository,
    private val beerStyleRepository: BeerStyleRepository
) : ViewModel() {

    private val _filterCriteria = MutableStateFlow(MaltFilterCriteria())
    val filterCriteria: StateFlow<MaltFilterCriteria> = _filterCriteria.asStateFlow()

    val malts: StateFlow<List<MaltVariety>> = _filterCriteria.flatMapLatest { criteria ->
        maltRepository.getFilteredMalts(criteria)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categories: StateFlow<List<String>> = maltRepository.getAllCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(
            "Base Malt",
            "Kilned & Toasted",
            "Caramel & Crystal",
            "Roasted & Dark",
            "Adjunct & Raw Grain"
        )
    )

    val grainTypes: StateFlow<List<String>> = maltRepository.getAllGrainTypes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Barley", "Wheat", "Rye", "Oats", "Corn", "Rice")
    )

    val functionalTags: StateFlow<List<String>> = flowOf(
        listOf(
            "Base Grist",
            "Head Retention",
            "Body & Mouthfeel",
            "Color Adjustment",
            "Caramel Sweetness",
            "Toasty Aroma",
            "Roast & Coffee",
            "Dark Chocolate",
            "Haze & Cloudiness",
            "Lighten Body",
            "Smoke & Campfire"
        )
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val baseCategories: StateFlow<List<String>> = beerStyleRepository.getCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onQueryChange(query: String) {
        _filterCriteria.value = _filterCriteria.value.copy(query = query)
    }

    fun onCategorySelected(category: String?) {
        val newCategory = if (_filterCriteria.value.selectedCategory == category) null else category
        _filterCriteria.value = _filterCriteria.value.copy(selectedCategory = newCategory)
    }

    fun onGrainTypeSelected(grainType: String?) {
        val newGrain = if (_filterCriteria.value.selectedGrainType == grainType) null else grainType
        _filterCriteria.value = _filterCriteria.value.copy(selectedGrainType = newGrain)
    }

    fun onFilterCriteriaChange(criteria: MaltFilterCriteria) {
        _filterCriteria.value = criteria
    }

    fun resetFilters() {
        _filterCriteria.value = MaltFilterCriteria(query = _filterCriteria.value.query)
    }
}
