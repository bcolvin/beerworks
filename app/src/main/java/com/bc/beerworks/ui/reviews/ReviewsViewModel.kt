package com.bc.beerworks.ui.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.OffFlavorFilterCriteria
import com.bc.beerworks.data.model.Scoresheet
import com.bc.beerworks.data.model.ScoresheetDiscipline
import com.bc.beerworks.data.repository.OffFlavorRepository
import com.bc.beerworks.data.repository.ScoresheetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReviewsViewModel(
    private val scoresheetRepository: ScoresheetRepository,
    offFlavorRepository: OffFlavorRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDisciplines = MutableStateFlow<Set<ScoresheetDiscipline>>(emptySet())
    val selectedDisciplines: StateFlow<Set<ScoresheetDiscipline>> = _selectedDisciplines.asStateFlow()

    private val allScoresheets = scoresheetRepository.getAllScoresheets()

    val offFlavorNames: StateFlow<Map<String, String>> = offFlavorRepository.getFilteredItems(OffFlavorFilterCriteria())
        .map { list -> list.associate { it.id to it.name } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val disciplineCounts: StateFlow<Map<ScoresheetDiscipline, Int>> = allScoresheets
        .map { list ->
            ScoresheetDiscipline.entries.associateWith { discipline ->
                list.count { it.discipline == discipline }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val totalCount: StateFlow<Int> = allScoresheets
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val scoresheets: StateFlow<List<Scoresheet>> = combine(
        allScoresheets,
        _searchQuery,
        _selectedDisciplines
    ) { all, query, selectedDisciplines ->
        val trimmed = query.trim()
        all.filter { sheet ->
            val matchesDiscipline = selectedDisciplines.isEmpty() || sheet.discipline in selectedDisciplines
            val matchesQuery = trimmed.isEmpty() ||
                sheet.entryTitle.contains(trimmed, ignoreCase = true) ||
                sheet.styleName.contains(trimmed, ignoreCase = true) ||
                sheet.styleNumber.contains(trimmed, ignoreCase = true) ||
                sheet.brewer.contains(trimmed, ignoreCase = true)
            matchesDiscipline && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleDiscipline(discipline: ScoresheetDiscipline) {
        _selectedDisciplines.update { current ->
            if (discipline in current) current - discipline else current + discipline
        }
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedDisciplines.value = emptySet()
    }

    fun deleteScoresheet(id: String) {
        viewModelScope.launch {
            scoresheetRepository.deleteScoresheet(id)
        }
    }
}
