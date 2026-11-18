package com.bc.beerworks.ui.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.OffFlavorFilterCriteria
import com.bc.beerworks.data.model.Scoresheet
import com.bc.beerworks.data.repository.OffFlavorRepository
import com.bc.beerworks.data.repository.ScoresheetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReviewsViewModel(
    private val scoresheetRepository: ScoresheetRepository,
    offFlavorRepository: OffFlavorRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val offFlavorNames: StateFlow<Map<String, String>> = offFlavorRepository.getFilteredItems(OffFlavorFilterCriteria())
        .map { list -> list.associate { it.id to it.name } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val scoresheets: StateFlow<List<Scoresheet>> = combine(
        scoresheetRepository.getAllScoresheets(),
        _searchQuery
    ) { all, query ->
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            all
        } else {
            all.filter { sheet ->
                sheet.entryTitle.contains(trimmed, ignoreCase = true) ||
                    sheet.styleName.contains(trimmed, ignoreCase = true) ||
                    sheet.styleNumber.contains(trimmed, ignoreCase = true) ||
                    sheet.brewer.contains(trimmed, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteScoresheet(id: String) {
        viewModelScope.launch {
            scoresheetRepository.deleteScoresheet(id)
        }
    }
}
