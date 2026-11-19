package com.bc.beerworks.ui.reviews

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.IntensityLevel
import com.bc.beerworks.data.model.OffFlavor
import com.bc.beerworks.data.model.OffFlavorFilterCriteria
import com.bc.beerworks.data.model.Scoresheet
import com.bc.beerworks.data.model.ScoresheetDescriptor
import com.bc.beerworks.data.model.ScoresheetDiscipline
import com.bc.beerworks.data.model.formatForShare
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.OffFlavorRepository
import com.bc.beerworks.data.repository.ScoresheetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ScoresheetEditorUiState(
    val scoresheet: Scoresheet = Scoresheet(),
    val isNew: Boolean = true,
    val isLoading: Boolean = true,
    val styleQuery: String = ""
) {
    val canSave: Boolean
        get() = scoresheet.discipline.isSupported && scoresheet.entryTitle.isNotBlank()
}

class ScoresheetEditorViewModel(
    private val scoresheetId: String?,
    private val scoresheetRepository: ScoresheetRepository,
    beerStyleRepository: BeerStyleRepository,
    offFlavorRepository: OffFlavorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ScoresheetEditorUiState(isNew = scoresheetId == null)
    )
    val uiState: StateFlow<ScoresheetEditorUiState> = _uiState.asStateFlow()

    val bjcpBeerStyles: StateFlow<List<BeerStyle>> = beerStyleRepository.getBjcpBeerStyles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val offFlavors: StateFlow<List<OffFlavor>> = offFlavorRepository.getFilteredItems(OffFlavorFilterCriteria())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            if (scoresheetId != null) {
                val existing = scoresheetRepository.getScoresheetById(scoresheetId)
                if (existing != null) {
                    val displayStyle = formatStyleDisplay(existing.styleNumber, existing.styleName)
                    _uiState.value = ScoresheetEditorUiState(
                        scoresheet = existing,
                        isNew = false,
                        isLoading = false,
                        styleQuery = displayStyle
                    )
                    return@launch
                }
            }
            _uiState.value = ScoresheetEditorUiState(
                scoresheet = Scoresheet(discipline = ScoresheetDiscipline.BJCP_BEER),
                isNew = true,
                isLoading = false,
                styleQuery = ""
            )
        }
    }

    fun updateDiscipline(discipline: ScoresheetDiscipline) {
        if (!_uiState.value.isNew) return
        updateScoresheet { it.copy(discipline = discipline) }
    }

    fun updateEntryTitle(title: String) = updateScoresheet { it.copy(entryTitle = title) }
    fun updateBrewer(brewer: String) = updateScoresheet { it.copy(brewer = brewer) }
    fun updateEvaluatedAt(millis: Long) = updateScoresheet { it.copy(evaluatedAtMillis = millis) }

    fun onStyleQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                styleQuery = query,
                scoresheet = state.scoresheet.copy(
                    styleId = null,
                    styleNumber = "",
                    styleName = query.trim()
                )
            )
        }
    }

    fun selectBjcpStyle(style: BeerStyle) {
        val display = formatStyleDisplay(style.number, style.name)
        _uiState.update { state ->
            state.copy(
                styleQuery = display,
                scoresheet = state.scoresheet.copy(
                    styleId = style.id,
                    styleNumber = style.number,
                    styleName = style.name
                )
            )
        }
    }

    fun updateAromaScore(score: Int) = updateScoresheet { it.copy(aromaScore = score.coerceIn(0, 12)) }
    fun updateAppearanceScore(score: Int) = updateScoresheet { it.copy(appearanceScore = score.coerceIn(0, 3)) }
    fun updateFlavorScore(score: Int) = updateScoresheet { it.copy(flavorScore = score.coerceIn(0, 20)) }
    fun updateMouthfeelScore(score: Int) = updateScoresheet { it.copy(mouthfeelScore = score.coerceIn(0, 5)) }
    fun updateOverallScore(score: Int) = updateScoresheet { it.copy(overallScore = score.coerceIn(0, 10)) }

    fun updateAromaNotes(notes: String) = updateScoresheet { it.copy(aromaNotes = notes) }
    fun updateAppearanceNotes(notes: String) = updateScoresheet { it.copy(appearanceNotes = notes) }
    fun updateFlavorNotes(notes: String) = updateScoresheet { it.copy(flavorNotes = notes) }
    fun updateMouthfeelNotes(notes: String) = updateScoresheet { it.copy(mouthfeelNotes = notes) }
    fun updateOverallNotes(notes: String) = updateScoresheet { it.copy(overallNotes = notes) }

    fun updateBottleInspectionOk(ok: Boolean) = updateScoresheet { it.copy(bottleInspectionOk = ok) }
    fun updateBottleInspectionNotes(notes: String) = updateScoresheet { it.copy(bottleInspectionNotes = notes) }

    fun updateStylisticAccuracy(value: Int) = updateScoresheet { it.copy(stylisticAccuracy = value.coerceIn(0, 10)) }
    fun updateTechnicalMerit(value: Int) = updateScoresheet { it.copy(technicalMerit = value.coerceIn(0, 10)) }
    fun updateIntangibles(value: Int) = updateScoresheet { it.copy(intangibles = value.coerceIn(0, 10)) }

    fun cycleDescriptor(descriptor: ScoresheetDescriptor) {
        updateScoresheet { sheet ->
            val updated = sheet.descriptorValues.toMutableMap()
            val next = descriptor.cycle(updated[descriptor.key])
            if (next == null) updated.remove(descriptor.key) else updated[descriptor.key] = next
            sheet.copy(descriptorValues = updated)
        }
    }

    fun cycleOffFlavor(offFlavorId: String) {
        updateScoresheet { sheet ->
            val updated = sheet.offFlavorLevels.toMutableMap()
            val next = IntensityLevel.cycle(updated[offFlavorId])
            if (next == null) updated.remove(offFlavorId) else updated[offFlavorId] = next
            sheet.copy(offFlavorLevels = updated)
        }
    }

    fun save(onSaved: () -> Unit) {
        val current = _uiState.value
        if (!current.canSave) return
        viewModelScope.launch {
            scoresheetRepository.saveScoresheet(
                current.scoresheet.copy(entryTitle = current.scoresheet.entryTitle.trim())
            )
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val current = _uiState.value
        if (current.isNew) {
            onDeleted()
            return
        }
        viewModelScope.launch {
            scoresheetRepository.deleteScoresheet(current.scoresheet.id)
            onDeleted()
        }
    }

    fun buildShareText(): String {
        val names = offFlavors.value.associate { it.id to it.name }
        return _uiState.value.scoresheet.formatForShare(names)
    }

    private inline fun updateScoresheet(transform: (Scoresheet) -> Scoresheet) {
        _uiState.update { state -> state.copy(scoresheet = transform(state.scoresheet)) }
    }

    private fun formatStyleDisplay(number: String, name: String): String =
        listOf(number.takeIf { it.isNotBlank() }, name.takeIf { it.isNotBlank() })
            .filterNotNull()
            .joinToString(" • ")
}
