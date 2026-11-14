package com.bc.beerworks.ui.honeydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HoneyVariety
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.HoneyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HoneyDetailViewModel(
    private val honeyRepository: HoneyRepository,
    private val beerStyleRepository: BeerStyleRepository,
    val honeyId: String
) : ViewModel() {

    val honey: StateFlow<HoneyVariety?> = honeyRepository.getHoneyById(honeyId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val substituteHoneys: StateFlow<List<HoneyVariety>> = honey.flatMapLatest { honeyVariety ->
        if (honeyVariety != null && honeyVariety.substituteIds.isNotEmpty()) {
            honeyRepository.getSubstituteHoneys(honeyVariety.substituteIds)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val commonStyles: StateFlow<List<BeerStyle>> = honeyRepository.getCommonStylesForHoney(
        honeyId = honeyId,
        guidelineType = GuidelineType.BJCP_MEAD
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isFavorite: StateFlow<Boolean> = honeyRepository.isFavorite(honeyId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun toggleFavorite() {
        val current = isFavorite.value
        viewModelScope.launch {
            honeyRepository.toggleFavorite(honeyId, "HONEY", current)
        }
    }

    fun toggleStyleFavorite(style: BeerStyle) {
        viewModelScope.launch {
            beerStyleRepository.toggleFavorite(style.id, style.guidelineType, style.isFavorite)
        }
    }
}
