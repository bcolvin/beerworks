package com.bc.beerworks.ui.offflavors

import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.OffFlavor
import com.bc.beerworks.data.model.OffFlavorFilterCriteria
import com.bc.beerworks.data.repository.OffFlavorRepository
import com.bc.beerworks.ui.ingredientlist.IngredientListViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class OffFlavorsViewModel(
    offFlavorRepository: OffFlavorRepository
) : IngredientListViewModel<OffFlavor, OffFlavorFilterCriteria>(
    repository = offFlavorRepository,
    initialCriteria = OffFlavorFilterCriteria()
) {
    val offFlavors: StateFlow<List<OffFlavor>> = items

    val categories: StateFlow<List<String>> = offFlavorRepository.getAllCategories()
        .map { dbCategories ->
            val defaultOrder = OffFlavorFilterCriteria.CATEGORIES_ORDER
            if (dbCategories.isEmpty()) {
                OffFlavorFilterCriteria.DEFAULT_CATEGORIES
            } else {
                dbCategories.sortedBy { cat ->
                    val idx = defaultOrder.indexOf(cat)
                    if (idx >= 0) idx else Int.MAX_VALUE
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OffFlavorFilterCriteria.DEFAULT_CATEGORIES
        )

    fun onCategorySelected(category: String?) {
        updateCriteria { current ->
            current.copy(selectedCategory = if (current.selectedCategory == category) null else category)
        }
    }
}

private val OffFlavorFilterCriteria.Companion.CATEGORIES_ORDER: List<String>
    get() = DEFAULT_CATEGORIES
