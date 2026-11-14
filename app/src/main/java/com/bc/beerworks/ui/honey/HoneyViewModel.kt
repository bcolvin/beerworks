package com.bc.beerworks.ui.honey

import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.HoneyFilterCriteria
import com.bc.beerworks.data.model.HoneyVariety
import com.bc.beerworks.data.repository.HoneyRepository
import com.bc.beerworks.ui.ingredientlist.IngredientListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class HoneyViewModel(
    honeyRepository: HoneyRepository
) : IngredientListViewModel<HoneyVariety, HoneyFilterCriteria>(
    repository = honeyRepository,
    initialCriteria = HoneyFilterCriteria()
) {

    val honeys: StateFlow<List<HoneyVariety>> = items

    val classifications: StateFlow<List<String>> = honeyRepository.getAllClassifications().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HoneyFilterCriteria.DEFAULT_CLASSIFICATIONS
    )

    val usdaColors: StateFlow<List<String>> =
        MutableStateFlow(HoneyFilterCriteria.USDA_COLORS).asStateFlow()

    val flavorIntensities: StateFlow<List<String>> =
        MutableStateFlow(HoneyFilterCriteria.FLAVOR_INTENSITIES).asStateFlow()

    val crystallizationTendencies: StateFlow<List<String>> =
        MutableStateFlow(HoneyFilterCriteria.CRYSTALLIZATION_TENDENCIES).asStateFlow()

    fun onClassificationSelected(classification: String?) {
        updateCriteria { current ->
            current.copy(selectedClassification = if (current.selectedClassification == classification) null else classification)
        }
    }

    fun onFlavorIntensitySelected(intensity: String?) {
        updateCriteria { current ->
            current.copy(selectedFlavorIntensity = if (current.selectedFlavorIntensity == intensity) null else intensity)
        }
    }

    fun onCrystallizationTendencySelected(tendency: String?) {
        updateCriteria { current ->
            current.copy(selectedCrystallizationTendency = if (current.selectedCrystallizationTendency == tendency) null else tendency)
        }
    }
}
