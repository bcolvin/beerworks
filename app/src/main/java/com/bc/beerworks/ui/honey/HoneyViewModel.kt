package com.bc.beerworks.ui.honey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.HoneyFilterCriteria
import com.bc.beerworks.data.model.HoneyVariety
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.HoneyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class HoneyViewModel(
    private val honeyRepository: HoneyRepository,
    private val beerStyleRepository: BeerStyleRepository
) : ViewModel() {

    private val _filterCriteria = MutableStateFlow(HoneyFilterCriteria())
    val filterCriteria: StateFlow<HoneyFilterCriteria> = _filterCriteria.asStateFlow()

    val honeys: StateFlow<List<HoneyVariety>> = _filterCriteria.flatMapLatest { criteria ->
        honeyRepository.getFilteredHoneys(criteria)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val classifications: StateFlow<List<String>> = honeyRepository.getAllClassifications().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Monofloral", "Polyfloral", "Honeydew")
    )

    val usdaColors: StateFlow<List<String>> = flowOf(
        listOf(
            "Water White",
            "Extra White",
            "White",
            "Extra Light Amber",
            "Light Amber",
            "Amber",
            "Dark Amber"
        )
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val flavorIntensities: StateFlow<List<String>> = flowOf(
        listOf("Delicate", "Moderate", "Robust")
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Delicate", "Moderate", "Robust")
    )

    val crystallizationTendencies: StateFlow<List<String>> = flowOf(
        listOf("Slow", "Moderate", "Rapid")
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Slow", "Moderate", "Rapid")
    )

    val meadStyles: StateFlow<List<String>> = beerStyleRepository.getCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onQueryChange(query: String) {
        _filterCriteria.value = _filterCriteria.value.copy(query = query)
    }

    fun onClassificationSelected(classification: String?) {
        val newClassification = if (_filterCriteria.value.selectedClassification == classification) null else classification
        _filterCriteria.value = _filterCriteria.value.copy(selectedClassification = newClassification)
    }

    fun onFlavorIntensitySelected(intensity: String?) {
        val newIntensity = if (_filterCriteria.value.selectedFlavorIntensity == intensity) null else intensity
        _filterCriteria.value = _filterCriteria.value.copy(selectedFlavorIntensity = newIntensity)
    }

    fun onCrystallizationTendencySelected(tendency: String?) {
        val newTendency = if (_filterCriteria.value.selectedCrystallizationTendency == tendency) null else tendency
        _filterCriteria.value = _filterCriteria.value.copy(selectedCrystallizationTendency = newTendency)
    }

    fun onFilterCriteriaChange(criteria: HoneyFilterCriteria) {
        _filterCriteria.value = criteria
    }

    fun resetFilters() {
        _filterCriteria.value = HoneyFilterCriteria(query = _filterCriteria.value.query)
    }
}
