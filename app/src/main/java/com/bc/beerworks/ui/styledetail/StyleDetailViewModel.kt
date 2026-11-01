package com.bc.beerworks.ui.styledetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.repository.BeerStyleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StyleDetailViewModel(
    private val repository: BeerStyleRepository,
    private val styleId: String,
    private val guidelineType: GuidelineType
) : ViewModel() {

    private val _style = MutableStateFlow<BeerStyle?>(null)
    val style: StateFlow<BeerStyle?> = _style.asStateFlow()

    init {
        loadStyle()
    }

    fun loadStyle() {
        viewModelScope.launch {
            _style.value = repository.getStyleById(styleId, guidelineType)
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
