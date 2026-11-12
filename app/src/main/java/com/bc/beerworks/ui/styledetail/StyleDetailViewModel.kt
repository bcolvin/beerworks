package com.bc.beerworks.ui.styledetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HoneyVariety
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.model.MaltVariety
import com.bc.beerworks.data.model.PomeVariety
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.HoneyRepository
import com.bc.beerworks.data.repository.HopRepository
import com.bc.beerworks.data.repository.MaltRepository
import com.bc.beerworks.data.repository.PomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StyleDetailViewModel(
    private val repository: BeerStyleRepository,
    private val hopRepository: HopRepository? = null,
    private val maltRepository: MaltRepository? = null,
    private val honeyRepository: HoneyRepository? = null,
    private val pomeRepository: PomeRepository? = null,
    private val styleId: String,
    private val guidelineType: GuidelineType
) : ViewModel() {

    private val _style = MutableStateFlow<BeerStyle?>(null)
    val style: StateFlow<BeerStyle?> = _style.asStateFlow()

    private val _nextStyle = MutableStateFlow<BeerStyle?>(null)
    val nextStyle: StateFlow<BeerStyle?> = _nextStyle.asStateFlow()

    val commonHops: StateFlow<List<HopVariety>> = if (hopRepository != null) {
        hopRepository.getCommonHopsForStyle(styleId, guidelineType).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    } else {
        MutableStateFlow(emptyList())
    }

    val commonMalts: StateFlow<List<MaltVariety>> = if (maltRepository != null) {
        maltRepository.getCommonMaltsForStyle(styleId, guidelineType).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    } else {
        MutableStateFlow(emptyList())
    }

    val commonHoneys: StateFlow<List<HoneyVariety>> = if (honeyRepository != null) {
        honeyRepository.getCommonHoneysForStyle(styleId, guidelineType).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    } else {
        MutableStateFlow(emptyList())
    }

    val commonPomes: StateFlow<List<PomeVariety>> = if (pomeRepository != null) {
        pomeRepository.getPomesForStyle(styleId, guidelineType).stateIn(
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
