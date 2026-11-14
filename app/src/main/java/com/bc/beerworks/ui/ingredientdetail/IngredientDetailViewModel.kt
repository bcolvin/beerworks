package com.bc.beerworks.ui.ingredientdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.Ingredient
import com.bc.beerworks.data.model.TemperatureUnit
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.IngredientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
open class IngredientDetailViewModel<T : Ingredient>(
    private val repository: IngredientRepository<T, *>,
    private val beerStyleRepository: BeerStyleRepository,
    val itemId: String,
    relatedItemsProvider: (String) -> Flow<List<T>> = { flowOf(emptyList()) }
) : ViewModel() {

    val item: StateFlow<T?> = repository.getItemById(itemId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val relatedItems: StateFlow<List<T>> = relatedItemsProvider(itemId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val substitutes: StateFlow<List<T>> = item.flatMapLatest { current ->
        if (current != null && current.substituteIds.isNotEmpty()) {
            repository.getSubstitutes(current.substituteIds)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val commonStyles: StateFlow<List<BeerStyle>> = repository.getCommonStylesForItem(itemId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isFavorite: StateFlow<Boolean> = repository.isFavorite(itemId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    @Suppress("UNNECESSARY_SAFE_CALL", "USELESS_ELVIS")
    val temperatureUnit: StateFlow<TemperatureUnit> =
        (beerStyleRepository.temperatureUnit ?: flowOf(TemperatureUnit.FAHRENHEIT)).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TemperatureUnit.FAHRENHEIT
        )

    fun toggleFavorite() {
        val current = isFavorite.value
        viewModelScope.launch {
            repository.toggleFavorite(itemId, current)
        }
    }

    fun toggleStyleFavorite(style: BeerStyle) {
        viewModelScope.launch {
            beerStyleRepository.toggleFavorite(style.id, style.guidelineType, style.isFavorite)
        }
    }
}
