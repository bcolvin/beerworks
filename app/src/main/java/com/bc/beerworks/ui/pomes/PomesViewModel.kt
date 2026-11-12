package com.bc.beerworks.ui.pomes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.PomeFilterCriteria
import com.bc.beerworks.data.model.PomeVariety
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.PomeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class PomesViewModel(
    private val pomeRepository: PomeRepository,
    private val beerStyleRepository: BeerStyleRepository
) : ViewModel() {

    private val _filterCriteria = MutableStateFlow(PomeFilterCriteria())
    val filterCriteria: StateFlow<PomeFilterCriteria> = _filterCriteria.asStateFlow()

    val pomes: StateFlow<List<PomeVariety>> = _filterCriteria.flatMapLatest { criteria ->
        pomeRepository.getFilteredPomes(criteria)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val fruitTypes: StateFlow<List<String>> = pomeRepository.getAllFruitTypes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Apple", "Pear")
    )

    val classifications: StateFlow<List<String>> = pomeRepository.getAllClassifications().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Bittersweet", "Bittersharp", "Sharp", "Sweet")
    )

    val harvestSeasons: StateFlow<List<String>> = flowOf(
        listOf("Early", "Mid", "Late")
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Early", "Mid", "Late")
    )

    val tanninLevels: StateFlow<List<String>> = flowOf(
        listOf("Low", "Medium", "High")
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Low", "Medium", "High")
    )

    fun onQueryChange(query: String) {
        _filterCriteria.value = _filterCriteria.value.copy(query = query)
    }

    fun onFruitTypeSelected(fruitType: String?) {
        val newType = if (_filterCriteria.value.selectedFruitType == fruitType) null else fruitType
        _filterCriteria.value = _filterCriteria.value.copy(selectedFruitType = newType)
    }

    fun onClassificationSelected(classification: String?) {
        val newClassification = if (_filterCriteria.value.selectedClassification == classification) null else classification
        _filterCriteria.value = _filterCriteria.value.copy(selectedClassification = newClassification)
    }

    fun onFilterCriteriaChange(criteria: PomeFilterCriteria) {
        _filterCriteria.value = criteria
    }

    fun resetFilters() {
        _filterCriteria.value = PomeFilterCriteria()
    }
}
