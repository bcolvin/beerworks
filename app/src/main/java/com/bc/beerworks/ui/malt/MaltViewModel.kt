package com.bc.beerworks.ui.malt

import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.MaltFilterCriteria
import com.bc.beerworks.data.model.MaltVariety
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.MaltRepository
import com.bc.beerworks.ui.ingredientlist.IngredientListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class MaltViewModel(
    maltRepository: MaltRepository,
    beerStyleRepository: BeerStyleRepository
) : IngredientListViewModel<MaltVariety, MaltFilterCriteria>(
    repository = maltRepository,
    initialCriteria = MaltFilterCriteria()
) {

    val malts: StateFlow<List<MaltVariety>> = items

    val categories: StateFlow<List<String>> = maltRepository.getAllCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MaltFilterCriteria.DEFAULT_CATEGORIES
    )

    val grainTypes: StateFlow<List<String>> = maltRepository.getAllGrainTypes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MaltFilterCriteria.DEFAULT_GRAIN_TYPES
    )

    val functionalTags: StateFlow<List<String>> =
        MutableStateFlow(MaltFilterCriteria.FUNCTIONAL_TAGS).asStateFlow()

    val baseCategories: StateFlow<List<String>> = beerStyleRepository.getCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onCategorySelected(category: String?) {
        updateCriteria { current ->
            current.copy(selectedCategory = if (current.selectedCategory == category) null else category)
        }
    }

    fun onGrainTypeSelected(grainType: String?) {
        updateCriteria { current ->
            current.copy(selectedGrainType = if (current.selectedGrainType == grainType) null else grainType)
        }
    }
}
