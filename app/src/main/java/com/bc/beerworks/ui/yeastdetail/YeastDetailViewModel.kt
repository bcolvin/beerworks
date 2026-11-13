package com.bc.beerworks.ui.yeastdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.local.PreferencesManager
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.TemperatureUnit
import com.bc.beerworks.data.model.YeastStrain
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.YeastRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class YeastDetailViewModel(
    private val yeastRepository: YeastRepository,
    private val beerStyleRepository: BeerStyleRepository,
    private val preferencesManager: PreferencesManager,
    val yeastId: String
) : ViewModel() {

    val yeast: StateFlow<YeastStrain?> = yeastRepository.getYeastById(yeastId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val temperatureUnit: StateFlow<TemperatureUnit> = preferencesManager.temperatureUnit.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TemperatureUnit.FAHRENHEIT
    )

    val substituteYeasts: StateFlow<List<YeastStrain>> = yeast.flatMapLatest { strain ->
        if (strain != null && strain.substituteIds.isNotEmpty()) {
            yeastRepository.getSubstituteYeasts(strain.substituteIds)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val commonStyles: StateFlow<List<BeerStyle>> = yeastRepository.getCommonStylesForYeast(
        yeastId = yeastId,
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
