package com.bc.beerworks.ui.hopdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.HopRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HopDetailViewModel(
    private val hopRepository: HopRepository,
    private val beerStyleRepository: BeerStyleRepository,
    val hopId: String
) : ViewModel() {

    val hop: StateFlow<HopVariety?> = hopRepository.getHopById(hopId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val childHops: StateFlow<List<HopVariety>> = hopRepository.getChildHops(hopId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val substituteHops: StateFlow<List<HopVariety>> = hop.flatMapLatest { hopVariety ->
        if (hopVariety != null && hopVariety.substituteIds.isNotEmpty()) {
            hopRepository.getSubstituteHops(hopVariety.substituteIds)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val commonStyles: StateFlow<List<BeerStyle>> = hopRepository.getCommonStylesForHop(
        hopId = hopId,
        guidelineType = GuidelineType.BJCP_2021
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleStyleFavorite(style: BeerStyle) {
        viewModelScope.launch {
            beerStyleRepository.toggleFavorite(style.id, style.guidelineType, style.isFavorite)
        }
    }
}
