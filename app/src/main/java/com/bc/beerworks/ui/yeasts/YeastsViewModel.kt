package com.bc.beerworks.ui.yeasts

import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.local.PreferencesManager
import com.bc.beerworks.data.model.TemperatureUnit
import com.bc.beerworks.data.model.YeastFilterCriteria
import com.bc.beerworks.data.model.YeastStrain
import com.bc.beerworks.data.repository.YeastRepository
import com.bc.beerworks.ui.ingredientlist.IngredientListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class YeastsViewModel(
    yeastRepository: YeastRepository,
    preferencesManager: PreferencesManager
) : IngredientListViewModel<YeastStrain, YeastFilterCriteria>(
    repository = yeastRepository,
    initialCriteria = YeastFilterCriteria()
) {

    val yeasts: StateFlow<List<YeastStrain>> = items

    val temperatureUnit: StateFlow<TemperatureUnit> = preferencesManager.temperatureUnit.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TemperatureUnit.FAHRENHEIT
    )

    val classifications: StateFlow<List<String>> = yeastRepository.getAllClassifications().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = YeastFilterCriteria.DEFAULT_CLASSIFICATIONS
    )

    val flocculations: StateFlow<List<String>> =
        MutableStateFlow(YeastFilterCriteria.FLOCCULATIONS).asStateFlow()

    fun onClassificationSelected(classification: String?) {
        updateCriteria { current ->
            current.copy(selectedClassification = if (current.selectedClassification == classification) null else classification)
        }
    }

    fun onFlocculationSelected(flocculation: String?) {
        updateCriteria { current ->
            current.copy(selectedFlocculation = if (current.selectedFlocculation == flocculation) null else flocculation)
        }
    }

    fun onMinAttenuationChange(minAtt: Double?) {
        updateCriteria { current ->
            current.copy(minAttenuation = minAtt)
        }
    }

    fun onMinAlcoholToleranceChange(minAbv: Double?) {
        updateCriteria { current ->
            current.copy(minAlcoholTolerance = minAbv)
        }
    }
}
