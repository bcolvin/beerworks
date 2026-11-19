package com.bc.beerworks.ui.reviews

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BeerScoresheet
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.IntensityLevel
import com.bc.beerworks.data.model.MeadScoresheet
import com.bc.beerworks.data.model.OffFlavor
import com.bc.beerworks.data.model.OffFlavorFilterCriteria
import com.bc.beerworks.data.model.Scoresheet
import com.bc.beerworks.data.model.ScoresheetDescriptor
import com.bc.beerworks.data.model.ScoresheetDiscipline
import com.bc.beerworks.data.model.cycleOption
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
    val scoresheet: Scoresheet = BeerScoresheet(),
    val selectedDiscipline: ScoresheetDiscipline = ScoresheetDiscipline.BJCP_BEER,
    val pendingDisciplineSwitch: ScoresheetDiscipline? = null,
    val isNew: Boolean = true,
    val isLoading: Boolean = true,
    val styleQuery: String = ""
) {
    val canSave: Boolean
        get() = selectedDiscipline.isSupported && scoresheet.entryTitle.isNotBlank()
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

    val bjcpMeadStyles: StateFlow<List<BeerStyle>> = beerStyleRepository.getMeadStyles()
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
                        selectedDiscipline = existing.discipline,
                        isNew = false,
                        isLoading = false,
                        styleQuery = displayStyle
                    )
                    return@launch
                }
            }
            val defaultDiscipline = scoresheetRepository.lastScoresheetDiscipline.first()
                .let { if (it.isSupported) it else ScoresheetDiscipline.BJCP_BEER }
            _uiState.value = ScoresheetEditorUiState(
                scoresheet = Scoresheet.createBlank(defaultDiscipline),
                selectedDiscipline = defaultDiscipline,
                isNew = true,
                isLoading = false,
                styleQuery = ""
            )
        }
    }

    fun updateDiscipline(discipline: ScoresheetDiscipline) {
        val current = _uiState.value
        if (!current.isNew || current.selectedDiscipline == discipline) return

        // Unsupported disciplines (e.g., Cider) display Coming Soon without discarding the active draft.
        if (!discipline.isSupported) {
            _uiState.update { it.copy(selectedDiscipline = discipline, pendingDisciplineSwitch = null) }
            return
        }

        // Switching back from an unsupported preview to the already-active draft preserves everything.
        if (current.scoresheet.discipline == discipline) {
            _uiState.update { it.copy(selectedDiscipline = discipline, pendingDisciplineSwitch = null) }
            return
        }

        if (hasJudgingData(current.scoresheet)) {
            _uiState.update { it.copy(pendingDisciplineSwitch = discipline) }
        } else {
            applyDisciplineSwitch(discipline)
        }
    }

    fun confirmDisciplineSwitch() {
        val target = _uiState.value.pendingDisciplineSwitch ?: return
        applyDisciplineSwitch(target)
    }

    fun dismissDisciplineSwitch() {
        _uiState.update { it.copy(pendingDisciplineSwitch = null) }
    }

    private fun applyDisciplineSwitch(target: ScoresheetDiscipline) {
        _uiState.update { state ->
            val current = state.scoresheet
            val converted = Scoresheet.createBlank(
                discipline = target,
                id = current.id,
                entryTitle = current.entryTitle,
                brewer = current.brewer,
                evaluatedAtMillis = current.evaluatedAtMillis,
                bottleInspectionOk = current.bottleInspectionOk,
                bottleInspectionNotes = current.bottleInspectionNotes
            )
            state.copy(
                scoresheet = converted,
                selectedDiscipline = target,
                pendingDisciplineSwitch = null,
                styleQuery = ""
            )
        }
    }

    private fun hasJudgingData(sheet: Scoresheet): Boolean {
        val beerDirty = sheet is BeerScoresheet && (sheet.mouthfeelScore > 0 || sheet.mouthfeelNotes.isNotBlank())
        val meadDirty = sheet is MeadScoresheet && sheet.hasAdditionalInfo
        return sheet.styleId != null ||
            sheet.styleName.isNotBlank() ||
            sheet.styleNumber.isNotBlank() ||
            sheet.aromaScore > 0 ||
            sheet.appearanceScore > 0 ||
            sheet.flavorScore > 0 ||
            sheet.overallScore > 0 ||
            sheet.aromaNotes.isNotBlank() ||
            sheet.appearanceNotes.isNotBlank() ||
            sheet.flavorNotes.isNotBlank() ||
            sheet.overallNotes.isNotBlank() ||
            sheet.stylisticAccuracy > 0 ||
            sheet.technicalMerit > 0 ||
            sheet.intangibles > 0 ||
            sheet.descriptorValues.isNotEmpty() ||
            sheet.offFlavorLevels.isNotEmpty() ||
            beerDirty ||
            meadDirty
    }

    fun updateEntryTitle(title: String) = updateCommon(entryTitle = title)
    fun updateBrewer(brewer: String) = updateCommon(brewer = brewer)
    fun updateEvaluatedAt(millis: Long) = updateCommon(evaluatedAtMillis = millis)

    fun onStyleQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                styleQuery = query,
                scoresheet = state.scoresheet.copyCommon(
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
                scoresheet = state.scoresheet.copyCommon(
                    styleId = style.id,
                    styleNumber = style.number,
                    styleName = style.name
                )
            )
        }
    }

    fun updateAromaScore(score: Int) {
        val max = when (_uiState.value.scoresheet) {
            is BeerScoresheet -> 12
            is MeadScoresheet -> 10
        }
        updateCommon(aromaScore = score.coerceIn(0, max))
    }

    fun updateAppearanceScore(score: Int) {
        val max = when (_uiState.value.scoresheet) {
            is BeerScoresheet -> 3
            is MeadScoresheet -> 6
        }
        updateCommon(appearanceScore = score.coerceIn(0, max))
    }

    fun updateFlavorScore(score: Int) {
        val max = when (_uiState.value.scoresheet) {
            is BeerScoresheet -> 20
            is MeadScoresheet -> 24
        }
        updateCommon(flavorScore = score.coerceIn(0, max))
    }

    fun updateMouthfeelScore(score: Int) {
        updateBeer { it.copy(mouthfeelScore = score.coerceIn(0, 5)) }
    }

    fun updateOverallScore(score: Int) = updateCommon(overallScore = score.coerceIn(0, 10))

    fun updateAromaNotes(notes: String) = updateCommon(aromaNotes = notes)
    fun updateAppearanceNotes(notes: String) = updateCommon(appearanceNotes = notes)
    fun updateFlavorNotes(notes: String) = updateCommon(flavorNotes = notes)
    fun updateMouthfeelNotes(notes: String) = updateBeer { it.copy(mouthfeelNotes = notes) }
    fun updateOverallNotes(notes: String) = updateCommon(overallNotes = notes)

    fun updateBottleInspectionOk(ok: Boolean) = updateCommon(bottleInspectionOk = ok)
    fun updateBottleInspectionNotes(notes: String) = updateCommon(bottleInspectionNotes = notes)

    fun updateStylisticAccuracy(value: Int) = updateCommon(stylisticAccuracy = value.coerceIn(0, 10))
    fun updateTechnicalMerit(value: Int) = updateCommon(technicalMerit = value.coerceIn(0, 10))
    fun updateIntangibles(value: Int) = updateCommon(intangibles = value.coerceIn(0, 10))

    fun cycleMeadCarbonation() {
        updateMead { it.copy(declaredCarbonation = cycleOption(it.declaredCarbonation, MeadScoresheet.CARBONATION_OPTIONS)) }
    }

    fun cycleMeadSweetness() {
        updateMead { it.copy(declaredSweetness = cycleOption(it.declaredSweetness, MeadScoresheet.SWEETNESS_OPTIONS)) }
    }

    fun cycleMeadStrength() {
        updateMead { it.copy(declaredStrength = cycleOption(it.declaredStrength, MeadScoresheet.STRENGTH_OPTIONS)) }
    }

    fun updateSpecialIngredients(ingredients: String) {
        updateMead { it.copy(specialIngredients = ingredients) }
    }

    fun cycleDescriptor(descriptor: ScoresheetDescriptor) {
        updateScoresheet { sheet ->
            val updated = sheet.descriptorValues.toMutableMap()
            val next = descriptor.cycle(updated[descriptor.key])
            if (next == null) updated.remove(descriptor.key) else updated[descriptor.key] = next
            sheet.copyCommon(descriptorValues = updated)
        }
    }

    fun cycleOffFlavor(offFlavorId: String) {
        updateScoresheet { sheet ->
            val updated = sheet.offFlavorLevels.toMutableMap()
            val next = IntensityLevel.cycle(updated[offFlavorId])
            if (next == null) updated.remove(offFlavorId) else updated[offFlavorId] = next
            sheet.copyCommon(offFlavorLevels = updated)
        }
    }

    fun save(onSaved: () -> Unit) {
        val current = _uiState.value
        if (!current.canSave) return
        viewModelScope.launch {
            val trimmedTitle = current.scoresheet.entryTitle.trim()
            val sanitized = when (val sheet = current.scoresheet) {
                is BeerScoresheet -> sheet.copy(entryTitle = trimmedTitle)
                is MeadScoresheet -> sheet.copy(
                    entryTitle = trimmedTitle,
                    specialIngredients = sheet.specialIngredients.trim()
                )
            }
            scoresheetRepository.saveScoresheet(sanitized)
            if (current.isNew) {
                scoresheetRepository.setLastScoresheetDiscipline(sanitized.discipline)
            }
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

    private inline fun updateBeer(transform: (BeerScoresheet) -> BeerScoresheet) {
        updateScoresheet { sheet -> if (sheet is BeerScoresheet) transform(sheet) else sheet }
    }

    private inline fun updateMead(transform: (MeadScoresheet) -> MeadScoresheet) {
        updateScoresheet { sheet -> if (sheet is MeadScoresheet) transform(sheet) else sheet }
    }

    private fun updateCommon(
        entryTitle: String? = null,
        brewer: String? = null,
        evaluatedAtMillis: Long? = null,
        aromaScore: Int? = null,
        appearanceScore: Int? = null,
        flavorScore: Int? = null,
        overallScore: Int? = null,
        aromaNotes: String? = null,
        appearanceNotes: String? = null,
        flavorNotes: String? = null,
        overallNotes: String? = null,
        bottleInspectionOk: Boolean? = null,
        bottleInspectionNotes: String? = null,
        stylisticAccuracy: Int? = null,
        technicalMerit: Int? = null,
        intangibles: Int? = null
    ) {
        updateScoresheet { sheet ->
            sheet.copyCommon(
                entryTitle = entryTitle ?: sheet.entryTitle,
                brewer = brewer ?: sheet.brewer,
                evaluatedAtMillis = evaluatedAtMillis ?: sheet.evaluatedAtMillis,
                aromaScore = aromaScore ?: sheet.aromaScore,
                appearanceScore = appearanceScore ?: sheet.appearanceScore,
                flavorScore = flavorScore ?: sheet.flavorScore,
                overallScore = overallScore ?: sheet.overallScore,
                aromaNotes = aromaNotes ?: sheet.aromaNotes,
                appearanceNotes = appearanceNotes ?: sheet.appearanceNotes,
                flavorNotes = flavorNotes ?: sheet.flavorNotes,
                overallNotes = overallNotes ?: sheet.overallNotes,
                bottleInspectionOk = bottleInspectionOk ?: sheet.bottleInspectionOk,
                bottleInspectionNotes = bottleInspectionNotes ?: sheet.bottleInspectionNotes,
                stylisticAccuracy = stylisticAccuracy ?: sheet.stylisticAccuracy,
                technicalMerit = technicalMerit ?: sheet.technicalMerit,
                intangibles = intangibles ?: sheet.intangibles
            )
        }
    }

    private fun Scoresheet.copyCommon(
        entryTitle: String = this.entryTitle,
        brewer: String = this.brewer,
        styleId: String? = this.styleId,
        styleNumber: String = this.styleNumber,
        styleName: String = this.styleName,
        evaluatedAtMillis: Long = this.evaluatedAtMillis,
        aromaScore: Int = this.aromaScore,
        appearanceScore: Int = this.appearanceScore,
        flavorScore: Int = this.flavorScore,
        overallScore: Int = this.overallScore,
        aromaNotes: String = this.aromaNotes,
        appearanceNotes: String = this.appearanceNotes,
        flavorNotes: String = this.flavorNotes,
        overallNotes: String = this.overallNotes,
        bottleInspectionOk: Boolean = this.bottleInspectionOk,
        bottleInspectionNotes: String = this.bottleInspectionNotes,
        stylisticAccuracy: Int = this.stylisticAccuracy,
        technicalMerit: Int = this.technicalMerit,
        intangibles: Int = this.intangibles,
        descriptorValues: Map<String, String> = this.descriptorValues,
        offFlavorLevels: Map<String, IntensityLevel> = this.offFlavorLevels
    ): Scoresheet = when (this) {
        is BeerScoresheet -> copy(
            entryTitle = entryTitle,
            brewer = brewer,
            styleId = styleId,
            styleNumber = styleNumber,
            styleName = styleName,
            evaluatedAtMillis = evaluatedAtMillis,
            aromaScore = aromaScore,
            appearanceScore = appearanceScore,
            flavorScore = flavorScore,
            overallScore = overallScore,
            aromaNotes = aromaNotes,
            appearanceNotes = appearanceNotes,
            flavorNotes = flavorNotes,
            overallNotes = overallNotes,
            bottleInspectionOk = bottleInspectionOk,
            bottleInspectionNotes = bottleInspectionNotes,
            stylisticAccuracy = stylisticAccuracy,
            technicalMerit = technicalMerit,
            intangibles = intangibles,
            descriptorValues = descriptorValues,
            offFlavorLevels = offFlavorLevels
        )
        is MeadScoresheet -> copy(
            entryTitle = entryTitle,
            brewer = brewer,
            styleId = styleId,
            styleNumber = styleNumber,
            styleName = styleName,
            evaluatedAtMillis = evaluatedAtMillis,
            aromaScore = aromaScore,
            appearanceScore = appearanceScore,
            flavorScore = flavorScore,
            overallScore = overallScore,
            aromaNotes = aromaNotes,
            appearanceNotes = appearanceNotes,
            flavorNotes = flavorNotes,
            overallNotes = overallNotes,
            bottleInspectionOk = bottleInspectionOk,
            bottleInspectionNotes = bottleInspectionNotes,
            stylisticAccuracy = stylisticAccuracy,
            technicalMerit = technicalMerit,
            intangibles = intangibles,
            descriptorValues = descriptorValues,
            offFlavorLevels = offFlavorLevels
        )
    }

    private fun formatStyleDisplay(number: String, name: String): String =
        listOf(number.takeIf { it.isNotBlank() }, name.takeIf { it.isNotBlank() })
            .filterNotNull()
            .joinToString(" • ")
}
