package com.bc.beerworks.ui.maltdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.MaltVariety
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.MaltRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MaltDetailViewModel(
    private val maltRepository: MaltRepository,
    private val beerStyleRepository: BeerStyleRepository,
    val maltId: String
) : ViewModel() {

    val malt: StateFlow<MaltVariety?> = maltRepository.getMaltById(maltId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val substituteMalts: StateFlow<List<MaltVariety>> = malt.flatMapLatest { maltVariety ->
        if (maltVariety != null && maltVariety.substituteIds.isNotEmpty()) {
            maltRepository.getSubstituteMalts(maltVariety.substituteIds)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val commonStyles: StateFlow<List<BeerStyle>> = maltRepository.getCommonStylesForMalt(
        maltId = maltId,
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
