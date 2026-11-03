package com.bc.beerworks.ui.styledetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.HopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StyleDetailViewModel(
    private val repository: BeerStyleRepository,
    private val hopRepository: HopRepository? = null,
    private val styleId: String,
    private val guidelineType: GuidelineType
) : ViewModel() {

    private val _style = MutableStateFlow<BeerStyle?>(null)
    val style: StateFlow<BeerStyle?> = _style.asStateFlow()

    val commonHops: StateFlow<List<HopVariety>> = if (hopRepository != null) {
        hopRepository.getCommonHopsForStyle(styleId, guidelineType).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    } else {
        MutableStateFlow(emptyList())
    }

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
