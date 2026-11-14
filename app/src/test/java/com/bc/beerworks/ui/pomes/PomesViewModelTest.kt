package com.bc.beerworks.ui.pomes

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.PomeVarietyEntity
import com.bc.beerworks.data.model.PomeFilterCriteria
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeBaDao
import com.bc.beerworks.data.repository.FakeCiderDao
import com.bc.beerworks.data.repository.FakeFavDao
import com.bc.beerworks.data.repository.FakeMeadDao
import com.bc.beerworks.data.repository.FakePomeDao
import com.bc.beerworks.data.repository.FakePomeStyleCrossRefDao
import com.bc.beerworks.data.repository.FakePrefsManager
import com.bc.beerworks.data.repository.PomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class PomesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dabinett = PomeVarietyEntity(
        id = "dabinett",
        name = "Dabinett",
        fruitType = "Apple",
        classification = "Bittersweet",
        fruitUse = "Heritage Cider/Perry",
        botanicalName = "Malus domestica",
        origin = "Somerset, England",
        countryCode = "GB",
        acidityMalicPercent = 0.18,
        tanninPolyphenolPercent = 0.35,
        tanninLevel = "High",
        sgMin = 1.050,
        sgMax = 1.058,
        harvestSeason = "Late",
        sorbitolGramsPerLiter = null,
        description = "English bittersweet apple.",
        blendingGuidance = "Single varietal cider.",
        pressingCharacteristics = "Presses cleanly.",
        descriptors = "Soft Tannin, Baked Apple",
        substituteIds = "yarlington-mill",
        radarAcidity = 1.5f,
        radarTannin = 3.8f,
        radarFreshFruit = 3.0f,
        radarBakedFruit = 4.0f,
        radarCitrus = 1.2f,
        radarFloral = 2.2f,
        radarEarthy = 2.8f,
        radarSweet = 3.5f
    )

    private val thorn = PomeVarietyEntity(
        id = "thorn",
        name = "Thorn",
        fruitType = "Pear",
        classification = "Bittersharp",
        fruitUse = "Heritage Cider/Perry",
        botanicalName = "Pyrus communis",
        origin = "Gloucestershire, England",
        countryCode = "GB",
        acidityMalicPercent = 0.50,
        tanninPolyphenolPercent = 0.22,
        tanninLevel = "Medium",
        sgMin = 1.055,
        sgMax = 1.065,
        harvestSeason = "Mid",
        sorbitolGramsPerLiter = 32.0,
        description = "Perry pear.",
        blendingGuidance = "Single varietal perry.",
        pressingCharacteristics = "Narrow harvest window.",
        descriptors = "Elderflower, Citrus",
        substituteIds = "yellow-huffcap",
        radarAcidity = 3.5f,
        radarTannin = 3.0f,
        radarFreshFruit = 4.4f,
        radarBakedFruit = 3.2f,
        radarCitrus = 3.4f,
        radarFloral = 4.5f,
        radarEarthy = 2.0f,
        radarSweet = 3.5f
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        pomes: List<PomeVarietyEntity> = listOf(dabinett, thorn)
    ): PomesViewModel {
        val fakePomeDao = FakePomeDao(pomes)
        val fakeCrossRefDao = FakePomeStyleCrossRefDao()
        val mockBjcp = mock(BjcpStyleDao::class.java)
        val mockBa = mock(BaStyleDao::class.java)
        val mockMead = mock(MeadStyleDao::class.java)
        val mockCider = mock(CiderStyleDao::class.java)
        val mockFav = mock(FavoriteStyleDao::class.java)
        val prefs = FakePrefsManager()

        val pomeRepo = PomeRepository(
            fakePomeDao,
            fakeCrossRefDao,
            mockBjcp,
            mockBa,
            mockMead,
            mockCider,
            testDispatcher
        )

        return PomesViewModel(pomeRepo)
    }

    @Test
    fun initialState_hasDefaultFilterCriteriaAndPomes() = runTest {
        val viewModel = createViewModel()
        val job = launch { viewModel.pomes.collect {} }
        runCurrent()

        assertThat(viewModel.filterCriteria.value).isEqualTo(PomeFilterCriteria())
        assertThat(viewModel.pomes.value).hasSize(2)
        assertThat(viewModel.pomes.value.map { it.id }).containsExactly("dabinett", "thorn")
        job.cancel()
    }

    @Test
    fun onQueryChange_updatesCriteriaAndFilters() = runTest {
        val viewModel = createViewModel()
        val job = launch { viewModel.pomes.collect {} }
        runCurrent()

        viewModel.onQueryChange("Dabinett")
        runCurrent()

        assertThat(viewModel.filterCriteria.value.query).isEqualTo("Dabinett")
        assertThat(viewModel.filterCriteria.value.isFilterActive).isTrue()
        job.cancel()
    }

    @Test
    fun onFruitTypeSelected_togglesFruitTypeFilter() = runTest {
        val viewModel = createViewModel()
        val job = launch { viewModel.pomes.collect {} }
        runCurrent()

        viewModel.onFruitTypeSelected("Pear")
        runCurrent()
        assertThat(viewModel.filterCriteria.value.selectedFruitType).isEqualTo("Pear")
        assertThat(viewModel.pomes.value).hasSize(1)
        assertThat(viewModel.pomes.value.first().id).isEqualTo("thorn")

        // Toggle off
        viewModel.onFruitTypeSelected("Pear")
        runCurrent()
        assertThat(viewModel.filterCriteria.value.selectedFruitType).isNull()
        assertThat(viewModel.pomes.value).hasSize(2)
        job.cancel()
    }

    @Test
    fun onClassificationSelected_togglesClassificationFilter() = runTest {
        val viewModel = createViewModel()
        val job = launch { viewModel.pomes.collect {} }
        runCurrent()

        viewModel.onClassificationSelected("Bittersweet")
        runCurrent()
        assertThat(viewModel.filterCriteria.value.selectedClassification).isEqualTo("Bittersweet")
        assertThat(viewModel.pomes.value).hasSize(1)
        assertThat(viewModel.pomes.value.first().id).isEqualTo("dabinett")

        // Toggle off
        viewModel.onClassificationSelected("Bittersweet")
        runCurrent()
        assertThat(viewModel.filterCriteria.value.selectedClassification).isNull()
        assertThat(viewModel.pomes.value).hasSize(2)
        job.cancel()
    }

    @Test
    fun resetFilters_preservesQueryAndClearsOtherFilters() = runTest {
        val viewModel = createViewModel()
        val job = launch { viewModel.pomes.collect {} }
        runCurrent()

        viewModel.onQueryChange("apple")
        viewModel.onFruitTypeSelected("Apple")
        viewModel.onClassificationSelected("Bittersweet")
        runCurrent()

        assertThat(viewModel.filterCriteria.value.isFilterActive).isTrue()

        viewModel.resetFilters()
        runCurrent()

        assertThat(viewModel.filterCriteria.value).isEqualTo(PomeFilterCriteria(query = "apple"))
        assertThat(viewModel.filterCriteria.value.selectedFruitType).isNull()
        assertThat(viewModel.filterCriteria.value.selectedClassification).isNull()
        job.cancel()
    }
}
