package com.bc.beerworks.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.local.PreferencesManager
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.TemperatureUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val selectedGuideline: StateFlow<GuidelineType> = preferencesManager.selectedGuideline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GuidelineType.BJCP_2021
    )

    val temperatureUnit: StateFlow<TemperatureUnit> = preferencesManager.temperatureUnit.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TemperatureUnit.FAHRENHEIT
    )

    val confirmFavoriteRemoval: StateFlow<Boolean> = preferencesManager.confirmFavoriteRemoval.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun onGuidelineChange(guidelineType: GuidelineType) {
        viewModelScope.launch {
            preferencesManager.setGuideline(guidelineType)
        }
    }

    fun onTemperatureUnitChange(unit: TemperatureUnit) {
        viewModelScope.launch {
            preferencesManager.setTemperatureUnit(unit)
        }
    }

    fun onConfirmFavoriteRemovalChange(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setConfirmFavoriteRemoval(enabled)
        }
    }
}
