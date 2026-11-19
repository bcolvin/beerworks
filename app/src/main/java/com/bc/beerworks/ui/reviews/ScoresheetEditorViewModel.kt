package com.bc.beerworks.ui.reviews

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.local.PreferencesManager
import com.bc.beerworks.data.model.BeerScoresheet
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.FourSectionScoresheet
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.IntensityLevel
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAX_STYLE_DROPDOWN_RESULTS = 25

@Immutable
data class ScoresheetEditorUiState(
    val scoresheet: Scoresheet = BeerScoresheet(),
    val initialScoresheet: Scoresheet? = null,
    val selectedDiscipline: ScoresheetDiscipline = ScoresheetDiscipline.BJCP_BEER,
    val pendingDisciplineSwitch: ScoresheetDiscipline? = null,
    val isNew: Boolean = true,
    val isLoading: Boolean = true,
    val styleQuery: String = "",
    val filteredStyles: List<BeerStyle> = emptyList(),
    val linkedStyle: BeerStyle? = null,
    val availableOffFlavors: List<OffFlavor> = emptyList()
) {
    val canSave: Boolean
        get() = scoresheet.entryTitle.isNotBlank()

    val hasUnsavedChanges: Boolean
        get() = !isLoading && initialScoresheet != null && scoresheet != initialScoresheet
}

class ScoresheetEditorViewModel(
    private val scoresheetId: String?,
    private val scoresheetRepository: ScoresheetRepository,
    beerStyleRepository: BeerStyleRepository,
    offFlavorRepository: OffFlavorRepository,
    private val preferencesManager: PreferencesManager? = null
) : ViewModel() {

    private var stylesByDiscipline: Map<ScoresheetDiscipline, List<BeerStyle>> = emptyMap()
    private var allOffFlavors: List<OffFlavor> = emptyList()

    private val _uiState = MutableStateFlow(
        ScoresheetEditorUiState(isNew = scoresheetId == null)
    )
    val uiState: StateFlow<ScoresheetEditorUiState> = _uiState.asStateFlow()

    private val stylesByDisciplineFlow: Flow<Map<ScoresheetDiscipline, List<BeerStyle>>> = combine(
        beerStyleRepository.getStyles(GuidelineType.BJCP_2021),
        beerStyleRepository.getStyles(GuidelineType.BJCP_MEAD),
        beerStyleRepository.getStyles(GuidelineType.BJCP_CIDER)
    ) { beer, mead, cider ->
        mapOf(
            ScoresheetDiscipline.BJCP_BEER to beer,
            ScoresheetDiscipline.BJCP_MEAD to mead,
            ScoresheetDiscipline.BJCP_CIDER to cider
        )
    }

    private val offFlavorsFlow: Flow<List<OffFlavor>> =
        offFlavorRepository.getFilteredItems(OffFlavorFilterCriteria())

    init {
        viewModelScope.launch {
            stylesByDisciplineFlow.collect { map ->
                stylesByDiscipline = map
                updateState { this }
            }
        }
        viewModelScope.launch {
            offFlavorsFlow.collect { list ->
                allOffFlavors = list
                updateState { this }
            }
        }
        viewModelScope.launch {
            if (scoresheetId != null) {
                val existing = scoresheetRepository.getScoresheetById(scoresheetId)
                if (existing != null) {
                    val displayStyle = formatStyleDisplay(existing.styleNumber, existing.styleName)
                    updateState {
                        ScoresheetEditorUiState(
                            scoresheet = existing,
                            initialScoresheet = existing,
                            selectedDiscipline = existing.discipline,
                            isNew = false,
                            isLoading = false,
                            styleQuery = displayStyle
                        )
                    }
                    return@launch
                }
            }
            val defaultDiscipline = preferencesManager?.lastScoresheetDiscipline?.first()
                ?: ScoresheetDiscipline.BJCP_BEER
            val blank = Scoresheet.createBlank(defaultDiscipline)
            updateState {
                ScoresheetEditorUiState(
                    scoresheet = blank,
                    initialScoresheet = blank,
                    selectedDiscipline = defaultDiscipline,
                    isNew = true,
                    isLoading = false,
                    styleQuery = ""
                )
            }
        }
    }

    fun updateDiscipline(discipline: ScoresheetDiscipline) {
        val current = _uiState.value
        if (!current.isNew || current.selectedDiscipline == discipline) return

        if (current.scoresheet.hasJudgingData()) {
            updateState { copy(pendingDisciplineSwitch = discipline) }
        } else {
            applyDisciplineSwitch(discipline)
        }
    }

    fun confirmDisciplineSwitch() {
        val target = _uiState.value.pendingDisciplineSwitch ?: return
        applyDisciplineSwitch(target)
    }

    fun dismissDisciplineSwitch() {
        updateState { copy(pendingDisciplineSwitch = null) }
    }

    private fun applyDisciplineSwitch(target: ScoresheetDiscipline) {
        updateState {
            val current = scoresheet
            val hadNoEditsYet = initialScoresheet != null && current == initialScoresheet
            val converted = Scoresheet.createBlank(
                discipline = target,
                id = current.id,
                entryTitle = current.entryTitle,
                brewer = current.brewer,
                evaluatedAtMillis = current.evaluatedAtMillis,
                bottleInspectionOk = current.bottleInspectionOk,
                bottleInspectionNotes = current.bottleInspectionNotes
            )
            copy(
                scoresheet = converted,
                initialScoresheet = if (hadNoEditsYet) converted else initialScoresheet,
                selectedDiscipline = target,
                pendingDisciplineSwitch = null,
                styleQuery = ""
            )
        }
    }

    fun updateEntryTitle(title: String) = updateCommon(entryTitle = title)
    fun updateBrewer(brewer: String) = updateCommon(brewer = brewer)
    fun updateEvaluatedAt(millis: Long) = updateCommon(evaluatedAtMillis = millis)

    fun onStyleQueryChange(query: String) {
        updateState {
            copy(
                styleQuery = query,
                scoresheet = scoresheet.copyCommon(
                    styleId = null,
                    styleNumber = "",
                    styleName = query.trim()
                )
            )
        }
    }

    fun selectBjcpStyle(style: BeerStyle) {
        val display = formatStyleDisplay(style.number, style.name)
        updateState {
            copy(
                styleQuery = display,
                scoresheet = scoresheet.copyCommon(
                    styleId = style.id,
                    styleNumber = style.number,
                    styleName = style.name
                )
            )
        }
    }

    fun updateAromaScore(score: Int) = updateScoresheet {
        it.copyCommon(aromaScore = score.coerceIn(0, it.discipline.aromaMax))
    }

    fun updateAppearanceScore(score: Int) = updateScoresheet {
        it.copyCommon(appearanceScore = score.coerceIn(0, it.discipline.appearanceMax))
    }

    fun updateFlavorScore(score: Int) = updateScoresheet {
        it.copyCommon(flavorScore = score.coerceIn(0, it.discipline.flavorMax))
    }

    fun updateMouthfeelScore(score: Int) = updateBeer {
        it.copy(mouthfeelScore = score.coerceIn(0, it.discipline.mouthfeelMax))
    }

    fun updateOverallScore(score: Int) = updateScoresheet {
        it.copyCommon(overallScore = score.coerceIn(0, it.discipline.overallMax))
    }

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

    fun cycleDeclaredCarbonation() {
        updateFourSection { sheet ->
            sheet.copy(declaredCarbonation = cycleOption(sheet.declaredCarbonation, sheet.carbonationOptions))
        }
    }

    fun cycleDeclaredSweetness() {
        updateFourSection { sheet ->
            sheet.copy(declaredSweetness = cycleOption(sheet.declaredSweetness, sheet.sweetnessOptions))
        }
    }

    fun cycleDeclaredStrength() {
        updateFourSection { sheet ->
            sheet.copy(declaredStrength = cycleOption(sheet.declaredStrength, sheet.strengthOptions))
        }
    }

    fun updateSpecialIngredients(ingredients: String) {
        updateFourSection { sheet ->
            sheet.copy(specialIngredients = ingredients)
        }
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
            val sanitized = current.scoresheet.sanitizedForSave()
            scoresheetRepository.saveScoresheet(sanitized)
            if (current.isNew) {
                preferencesManager?.setLastScoresheetDiscipline(sanitized.discipline)
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
        val current = _uiState.value
        val names = current.availableOffFlavors.associate { it.id to it.name }
        return current.scoresheet.formatForShare(names)
    }

    private inline fun updateState(transform: ScoresheetEditorUiState.() -> ScoresheetEditorUiState) {
        _uiState.update { state -> transform(state).withCatalogProjections() }
    }

    private fun ScoresheetEditorUiState.withCatalogProjections(): ScoresheetEditorUiState {
        val activeStyles = stylesByDiscipline[scoresheet.discipline].orEmpty()
        val trimmedQuery = styleQuery.trim()
        val projectedFilteredStyles = if (trimmedQuery.isEmpty()) {
            activeStyles.take(MAX_STYLE_DROPDOWN_RESULTS)
        } else {
            activeStyles.filter { style ->
                style.name.contains(trimmedQuery, ignoreCase = true) ||
                    style.number.contains(trimmedQuery, ignoreCase = true) ||
                    style.category.contains(trimmedQuery, ignoreCase = true)
            }.take(MAX_STYLE_DROPDOWN_RESULTS)
        }
        val projectedLinkedStyle = scoresheet.styleId?.let { id ->
            activeStyles.firstOrNull { it.id == id }
        }
        val projectedOffFlavors = allOffFlavors.filter {
            it.disciplines.contains(scoresheet.discipline.shortLabel)
        }
        return copy(
            filteredStyles = projectedFilteredStyles,
            linkedStyle = projectedLinkedStyle,
            availableOffFlavors = projectedOffFlavors
        )
    }

    private inline fun updateScoresheet(transform: (Scoresheet) -> Scoresheet) {
        updateState { copy(scoresheet = transform(scoresheet)) }
    }

    private inline fun updateBeer(transform: (BeerScoresheet) -> BeerScoresheet) {
        updateScoresheet { sheet -> if (sheet is BeerScoresheet) transform(sheet) else sheet }
    }

    private inline fun updateFourSection(transform: (FourSectionScoresheet) -> FourSectionScoresheet) {
        updateScoresheet { sheet -> if (sheet is FourSectionScoresheet) transform(sheet) else sheet }
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

    private fun formatStyleDisplay(number: String, name: String): String =
        listOf(number.takeIf { it.isNotBlank() }, name.takeIf { it.isNotBlank() })
            .filterNotNull()
            .joinToString(" • ")
}
