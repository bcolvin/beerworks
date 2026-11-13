package com.bc.beerworks.ui.yeasts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.local.PreferencesManager
import com.bc.beerworks.data.model.TemperatureUnit
import com.bc.beerworks.data.model.YeastFilterCriteria
import com.bc.beerworks.data.model.YeastStrain
import com.bc.beerworks.data.repository.YeastRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class YeastsViewModel(
    private val yeastRepository: YeastRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _filterCriteria = MutableStateFlow(YeastFilterCriteria())
    val filterCriteria: StateFlow<YeastFilterCriteria> = _filterCriteria.asStateFlow()

    val temperatureUnit: StateFlow<TemperatureUnit> = preferencesManager.temperatureUnit.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TemperatureUnit.FAHRENHEIT
    )

    val yeasts: StateFlow<List<YeastStrain>> = _filterCriteria.flatMapLatest { criteria ->
        yeastRepository.getFilteredYeasts(criteria)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val classifications: StateFlow<List<String>> = yeastRepository.getAllClassifications().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Ale", "Lager", "Wheat", "Kveik", "Wild / Brett", "Bacteria", "Wine / Mead / Cider")
    )

    val flocculations: StateFlow<List<String>> = flowOf(
        listOf("Low", "Medium", "Medium-High", "High", "Very High")
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Low", "Medium", "Medium-High", "High", "Very High")
    )

    fun onQueryChange(query: String) {
        _filterCriteria.value = _filterCriteria.value.copy(query = query)
    }

    fun onClassificationSelected(classification: String?) {
        val newClassification = if (_filterCriteria.value.selectedClassification == classification) null else classification
        _filterCriteria.value = _filterCriteria.value.copy(selectedClassification = newClassification)
    }

    fun onFlocculationSelected(flocculation: String?) {
        val newFlocculation = if (_filterCriteria.value.selectedFlocculation == flocculation) null else flocculation
        _filterCriteria.value = _filterCriteria.value.copy(selectedFlocculation = newFlocculation)
    }

    fun onMinAttenuationChange(minAtt: Double?) {
        _filterCriteria.value = _filterCriteria.value.copy(minAttenuation = minAtt)
    }

    fun onMinAlcoholToleranceChange(minAbv: Double?) {
        _filterCriteria.value = _filterCriteria.value.copy(minAlcoholTolerance = minAbv)
    }

    fun onFilterCriteriaChange(criteria: YeastFilterCriteria) {
        _filterCriteria.value = criteria
    }

    fun resetFilters() {
        _filterCriteria.value = YeastFilterCriteria()
    }
}
