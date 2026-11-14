package com.bc.beerworks.ui.pomes

import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.PomeFilterCriteria
import com.bc.beerworks.data.model.PomeVariety
import com.bc.beerworks.data.repository.PomeRepository
import com.bc.beerworks.ui.ingredientlist.IngredientListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class PomesViewModel(
    pomeRepository: PomeRepository
) : IngredientListViewModel<PomeVariety, PomeFilterCriteria>(
    repository = pomeRepository,
    initialCriteria = PomeFilterCriteria()
) {

    val pomes: StateFlow<List<PomeVariety>> = items

    val fruitTypes: StateFlow<List<String>> = pomeRepository.getAllFruitTypes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PomeFilterCriteria.DEFAULT_FRUIT_TYPES
    )

    val classifications: StateFlow<List<String>> = pomeRepository.getAllClassifications().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PomeFilterCriteria.DEFAULT_CLASSIFICATIONS
    )

    val harvestSeasons: StateFlow<List<String>> =
        MutableStateFlow(PomeFilterCriteria.HARVEST_SEASONS).asStateFlow()

    val tanninLevels: StateFlow<List<String>> =
        MutableStateFlow(PomeFilterCriteria.TANNIN_LEVELS).asStateFlow()

    fun onFruitTypeSelected(fruitType: String?) {
        updateCriteria { current ->
            current.copy(selectedFruitType = if (current.selectedFruitType == fruitType) null else fruitType)
        }
    }

    fun onClassificationSelected(classification: String?) {
        updateCriteria { current ->
            current.copy(selectedClassification = if (current.selectedClassification == classification) null else classification)
        }
    }
}
