package com.bc.beerworks.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.repository.BeerStyleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: BeerStyleRepository
) : ViewModel() {

    val selectedGuideline: StateFlow<GuidelineType> = repository.selectedGuideline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GuidelineType.BJCP_2021
    )

    val keepScreenOn: StateFlow<Boolean> = repository.keepScreenOn.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun onGuidelineChange(guidelineType: GuidelineType) {
        viewModelScope.launch {
            repository.setGuideline(guidelineType)
        }
    }

    fun onKeepScreenOnChange(enabled: Boolean) {
        viewModelScope.launch {
            repository.setKeepScreenOn(enabled)
        }
    }
}
