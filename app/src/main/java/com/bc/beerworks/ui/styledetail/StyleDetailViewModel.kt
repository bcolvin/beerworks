package com.bc.beerworks.ui.styledetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.StyleIngredients
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.IngredientCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StyleDetailViewModel(
    private val repository: BeerStyleRepository,
    private val ingredientCatalogRepository: IngredientCatalogRepository? = null,
    private val styleId: String,
    private val guidelineType: GuidelineType
) : ViewModel() {

    private val _style = MutableStateFlow<BeerStyle?>(null)
    val style: StateFlow<BeerStyle?> = _style.asStateFlow()

    private val _nextStyle = MutableStateFlow<BeerStyle?>(null)
    val nextStyle: StateFlow<BeerStyle?> = _nextStyle.asStateFlow()

    val ingredients: StateFlow<StyleIngredients> = if (ingredientCatalogRepository != null) {
        ingredientCatalogRepository.getIngredientsForStyle(styleId, guidelineType).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StyleIngredients()
        )
    } else {
        MutableStateFlow(StyleIngredients())
    }

    init {
        loadStyle()
    }

    fun loadStyle() {
        viewModelScope.launch {
            _style.value = repository.getStyleById(styleId, guidelineType)
            _nextStyle.value = repository.getNextStyle(styleId, guidelineType)
        }
    }

    fun toggleFavorite() {
        val current = _style.value ?: return
        viewModelScope.launch {
            repository.toggleFavorite(current.id, current.guidelineType, current.isFavorite)
            _style.value = current.copy(isFavorite = !current.isFavorite)
        }
    }
}
