package com.bc.beerworks.ui.pomedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.PomeVariety
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.PomeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PomeDetailViewModel(
    private val pomeRepository: PomeRepository,
    private val beerStyleRepository: BeerStyleRepository,
    val pomeId: String
) : ViewModel() {

    val pome: StateFlow<PomeVariety?> = pomeRepository.getPomeById(pomeId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val substitutePomes: StateFlow<List<PomeVariety>> = pome.flatMapLatest { pomeVariety ->
        if (pomeVariety != null && pomeVariety.substituteIds.isNotEmpty()) {
            pomeRepository.getSubstitutePomes(pomeVariety.substituteIds)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val commonStyles: StateFlow<List<BeerStyle>> = pomeRepository.getCommonStylesForPome(
        pomeId = pomeId,
        guidelineType = GuidelineType.BJCP_CIDER
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
