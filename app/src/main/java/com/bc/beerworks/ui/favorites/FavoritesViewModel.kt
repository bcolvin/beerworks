package com.bc.beerworks.ui.favorites

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.HoneyVariety
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.model.MaltVariety
import com.bc.beerworks.data.model.PomeVariety
import com.bc.beerworks.data.model.YeastStrain
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.HoneyRepository
import com.bc.beerworks.data.repository.HopRepository
import com.bc.beerworks.data.repository.IngredientCatalogRepository
import com.bc.beerworks.data.repository.MaltRepository
import com.bc.beerworks.data.repository.PomeRepository
import com.bc.beerworks.data.repository.YeastRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FavoriteDiscipline(val label: String) {
    STYLES("Styles"),
    HOPS("Hops"),
    MALTS("Malts"),
    HONEY("Honey"),
    POMES("Pomes"),
    YEAST("Yeast")
}

@Immutable
sealed class FavoriteItemTarget {
    abstract val key: String
    abstract val name: String

    @Immutable
    data class Style(val style: BeerStyle) : FavoriteItemTarget() {
        override val key: String = "style_${style.guidelineType.name}_${style.id}"
        override val name: String = style.name
    }

    @Immutable
    data class IngredientItem(
        override val key: String,
        override val name: String,
        val id: String,
        val type: String
    ) : FavoriteItemTarget()
}

@Immutable
data class FavoritesUiState(
    val query: String = "",
    val selectedDisciplines: Set<FavoriteDiscipline> = emptySet(),
    val disciplineCounts: Map<FavoriteDiscipline, Int> = emptyMap(),
    val styles: List<BeerStyle> = emptyList(),
    val hops: List<HopVariety> = emptyList(),
    val malts: List<MaltVariety> = emptyList(),
    val honeys: List<HoneyVariety> = emptyList(),
    val pomes: List<PomeVariety> = emptyList(),
    val yeasts: List<YeastStrain> = emptyList()
) {
    val totalFavoritesCount: Int
        get() = disciplineCounts.values.sum()

    val availableDisciplines: List<FavoriteDiscipline>
        get() = FavoriteDiscipline.entries.filter { (disciplineCounts[it] ?: 0) > 0 }

    val hasMatchingItems: Boolean
        get() = styles.isNotEmpty() || hops.isNotEmpty() || malts.isNotEmpty() ||
                honeys.isNotEmpty() || pomes.isNotEmpty() || yeasts.isNotEmpty()

    val isFilterActive: Boolean
        get() = query.isNotBlank() || selectedDisciplines.isNotEmpty()
}

class FavoritesViewModel(
    private val beerStyleRepository: BeerStyleRepository,
    private val ingredientCatalogRepository: IngredientCatalogRepository,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    constructor(
        beerStyleRepository: BeerStyleRepository,
        hopRepository: HopRepository,
        maltRepository: MaltRepository,
        honeyRepository: HoneyRepository,
        pomeRepository: PomeRepository,
        yeastRepository: YeastRepository,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : this(
        beerStyleRepository = beerStyleRepository,
        ingredientCatalogRepository = IngredientCatalogRepository(
            hopRepository = hopRepository,
            maltRepository = maltRepository,
            honeyRepository = honeyRepository,
            pomeRepository = pomeRepository,
            yeastRepository = yeastRepository,
            defaultDispatcher = defaultDispatcher
        ),
        defaultDispatcher = defaultDispatcher
    )

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedDisciplines = MutableStateFlow<Set<FavoriteDiscipline>>(emptySet())
    val selectedDisciplines: StateFlow<Set<FavoriteDiscipline>> = _selectedDisciplines.asStateFlow()

    private val _pendingRemoval = MutableStateFlow<FavoriteItemTarget?>(null)
    val pendingRemoval: StateFlow<FavoriteItemTarget?> = _pendingRemoval.asStateFlow()

    val confirmFavoriteRemoval: StateFlow<Boolean> = beerStyleRepository.confirmFavoriteRemoval.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    val uiState: StateFlow<FavoritesUiState> = combine(
        beerStyleRepository.getAllFavoriteStyles(),
        ingredientCatalogRepository.getFavoriteIngredients(),
        _query,
        _selectedDisciplines
    ) { styles, ingredients, rawQuery, selectedDisciplines ->
        val counts = mapOf(
            FavoriteDiscipline.STYLES to styles.size,
            FavoriteDiscipline.HOPS to ingredients.hops.size,
            FavoriteDiscipline.MALTS to ingredients.malts.size,
            FavoriteDiscipline.HONEY to ingredients.honeys.size,
            FavoriteDiscipline.POMES to ingredients.pomes.size,
            FavoriteDiscipline.YEAST to ingredients.yeasts.size
        )

        val activeSelected = selectedDisciplines.filter { (counts[it] ?: 0) > 0 }.toSet()
        val q = rawQuery.trim()

        fun isDisciplineVisible(discipline: FavoriteDiscipline): Boolean =
            activeSelected.isEmpty() || activeSelected.contains(discipline)

        val filteredStyles = if (isDisciplineVisible(FavoriteDiscipline.STYLES)) {
            if (q.isEmpty()) styles else styles.filter { it.matchesQuery(q) }
        } else emptyList()

        val filteredHops = if (isDisciplineVisible(FavoriteDiscipline.HOPS)) {
            if (q.isEmpty()) ingredients.hops else ingredients.hops.filter { it.matchesQuery(q) }
        } else emptyList()

        val filteredMalts = if (isDisciplineVisible(FavoriteDiscipline.MALTS)) {
            if (q.isEmpty()) ingredients.malts else ingredients.malts.filter { it.matchesQuery(q) }
        } else emptyList()

        val filteredHoneys = if (isDisciplineVisible(FavoriteDiscipline.HONEY)) {
            if (q.isEmpty()) ingredients.honeys else ingredients.honeys.filter { it.matchesQuery(q) }
        } else emptyList()

        val filteredPomes = if (isDisciplineVisible(FavoriteDiscipline.POMES)) {
            if (q.isEmpty()) ingredients.pomes else ingredients.pomes.filter { it.matchesQuery(q) }
        } else emptyList()

        val filteredYeasts = if (isDisciplineVisible(FavoriteDiscipline.YEAST)) {
            if (q.isEmpty()) ingredients.yeasts else ingredients.yeasts.filter { it.matchesQuery(q) }
        } else emptyList()

        FavoritesUiState(
            query = rawQuery,
            selectedDisciplines = activeSelected,
            disciplineCounts = counts,
            styles = filteredStyles,
            hops = filteredHops,
            malts = filteredMalts,
            honeys = filteredHoneys,
            pomes = filteredPomes,
            yeasts = filteredYeasts
        )
    }.flowOn(defaultDispatcher).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FavoritesUiState()
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onToggleDiscipline(discipline: FavoriteDiscipline) {
        val current = _selectedDisciplines.value
        _selectedDisciplines.value = if (current.contains(discipline)) {
            current - discipline
        } else {
            current + discipline
        }
    }

    fun resetFilters() {
        _query.value = ""
        _selectedDisciplines.value = emptySet()
    }

    fun onRequestRemove(target: FavoriteItemTarget) {
        viewModelScope.launch {
            val shouldConfirm = beerStyleRepository.confirmFavoriteRemoval.first()
            if (shouldConfirm) {
                _pendingRemoval.value = target
            } else {
                removeTarget(target)
            }
        }
    }

    fun onConfirmRemove(alwaysRemoveWithoutAsking: Boolean) {
        val target = _pendingRemoval.value ?: return
        _pendingRemoval.value = null
        viewModelScope.launch {
            if (alwaysRemoveWithoutAsking) {
                beerStyleRepository.setConfirmFavoriteRemoval(false)
            }
            removeTarget(target)
        }
    }

    fun onCancelRemove() {
        _pendingRemoval.value = null
    }

    private suspend fun removeTarget(target: FavoriteItemTarget) {
        when (target) {
            is FavoriteItemTarget.Style -> {
                beerStyleRepository.toggleFavorite(
                    styleId = target.style.id,
                    guidelineType = target.style.guidelineType,
                    currentFavorite = true
                )
            }
            is FavoriteItemTarget.IngredientItem -> {
                ingredientCatalogRepository.removeFavoriteIngredient(target.type, target.id)
            }
        }
    }

    private fun BeerStyle.matchesQuery(q: String): Boolean =
        name.contains(q, ignoreCase = true) ||
                number.contains(q, ignoreCase = true) ||
                category.contains(q, ignoreCase = true) ||
                overallImpression.contains(q, ignoreCase = true) ||
                tags.any { it.contains(q, ignoreCase = true) }
}
