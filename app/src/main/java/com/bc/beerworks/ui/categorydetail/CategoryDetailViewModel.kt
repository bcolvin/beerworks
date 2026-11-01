package com.bc.beerworks.ui.categorydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BaseStyleGroup
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.repository.BeerStyleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryDetailViewModel(
    private val repository: BeerStyleRepository,
    val categoryNumber: String,
    val guidelineType: GuidelineType
) : ViewModel() {

    val categoryGroup: StateFlow<BaseStyleGroup?> = repository.getCategoryGroup(categoryNumber, guidelineType)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun toggleFavorite(style: BeerStyle) {
        viewModelScope.launch {
            repository.toggleFavorite(style.id, style.guidelineType, style.isFavorite)
        }
    }
}
