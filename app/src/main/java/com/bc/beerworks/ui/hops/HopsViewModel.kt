package com.bc.beerworks.ui.hops

import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.HopFilterCriteria
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.HopRepository
import com.bc.beerworks.ui.ingredientlist.IngredientListViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HopsViewModel(
    hopRepository: HopRepository,
    beerStyleRepository: BeerStyleRepository
) : IngredientListViewModel<HopVariety, HopFilterCriteria>(
    repository = hopRepository,
    initialCriteria = HopFilterCriteria()
) {

    val hops: StateFlow<List<HopVariety>> = items

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

    fun onPurposeSelected(purpose: String?) {
        updateCriteria { current ->
            current.copy(selectedPurpose = if (current.selectedPurpose == purpose) null else purpose)
        }
    }

    fun onSensoryCategorySelected(category: String?) {
        updateCriteria { current ->
            current.copy(selectedSensoryCategory = if (current.selectedSensoryCategory == category) null else category)
        }
    }
}
