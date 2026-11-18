package com.bc.beerworks.ui.reviews

import com.bc.beerworks.data.local.dao.ScoresheetDao
import com.bc.beerworks.data.local.entity.ScoresheetEntity
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.IntensityLevel
import com.bc.beerworks.data.model.OffFlavor
import com.bc.beerworks.data.model.OffFlavorFilterCriteria
import com.bc.beerworks.data.model.ScoresheetDescriptor
import com.bc.beerworks.data.model.ScoresheetFormType
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeBaDao
import com.bc.beerworks.data.repository.FakeBjcpDao
import com.bc.beerworks.data.repository.FakeFavDao
import com.bc.beerworks.data.repository.FakePrefsManager
import com.bc.beerworks.data.repository.OffFlavorRepository
import com.bc.beerworks.data.repository.ScoresheetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

private class FakeScoresheetDao : ScoresheetDao {
    private val items = MutableStateFlow<List<ScoresheetEntity>>(emptyList())

    override fun getAllScoresheets(): Flow<List<ScoresheetEntity>> = items

    override suspend fun getScoresheetById(id: String): ScoresheetEntity? =
        items.value.firstOrNull { it.id == id }

    override suspend fun upsert(scoresheet: ScoresheetEntity) {
        items.value = items.value.filterNot { it.id == scoresheet.id } + scoresheet
    }

    override suspend fun deleteById(id: String) {
        items.value = items.value.filterNot { it.id == id }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ScoresheetEditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        scoresheetId: String? = null,
        prefsManager: FakePrefsManager = FakePrefsManager(),
        scoresheetDao: FakeScoresheetDao = FakeScoresheetDao()
    ): Pair<ScoresheetEditorViewModel, ScoresheetRepository> {
        val scoresheetRepo = ScoresheetRepository(scoresheetDao, prefsManager)
        val bjcpDao = FakeBjcpDao(emptyList())
        val baDao = FakeBaDao()
        val styleRepo = BeerStyleRepository(bjcpDao, baDao, FakeFavDao(), prefsManager)
        val offFlavorRepo = mock(OffFlavorRepository::class.java)
        doReturn(flowOf(emptyList<OffFlavor>())).`when`(offFlavorRepo).getFilteredItems(OffFlavorFilterCriteria())
        val vm = ScoresheetEditorViewModel(scoresheetId, scoresheetRepo, styleRepo, offFlavorRepo)
        return vm to scoresheetRepo
    }

    @Test
    fun newScoresheet_startsAllSlidersAtZero() = runTest {
        val (vm, _) = createViewModel()
        advanceUntilIdle()

        val sheet = vm.uiState.value.scoresheet
        assertThat(sheet.formType).isEqualTo(ScoresheetFormType.LONG_FORM)
        assertThat(sheet.totalScore).isEqualTo(0)
        assertThat(sheet.aromaScore).isEqualTo(0)
        assertThat(sheet.appearanceScore).isEqualTo(0)
        assertThat(sheet.flavorScore).isEqualTo(0)
        assertThat(sheet.mouthfeelScore).isEqualTo(0)
        assertThat(sheet.overallScore).isEqualTo(0)
        assertThat(sheet.stylisticAccuracy).isEqualTo(0)
        assertThat(sheet.technicalMerit).isEqualTo(0)
        assertThat(sheet.intangibles).isEqualTo(0)
    }

    @Test
    fun updateScoresAndDescriptors_updatesState() = runTest {
        val (vm, _) = createViewModel()
        advanceUntilIdle()

        vm.updateEntryTitle("Entry #104")
        vm.updateAromaScore(11)
        vm.updateStylisticAccuracy(8)
        vm.cycleDescriptor(ScoresheetDescriptor.AROMA_HOPS)
        vm.cycleOffFlavor("diacetyl")
        advanceUntilIdle()

        val sheet = vm.uiState.value.scoresheet
        assertThat(sheet.entryTitle).isEqualTo("Entry #104")
        assertThat(sheet.aromaScore).isEqualTo(11)
        assertThat(sheet.stylisticAccuracy).isEqualTo(8)
        assertThat(sheet.descriptorValues[ScoresheetDescriptor.AROMA_HOPS.key]).isEqualTo("Low")
        assertThat(sheet.offFlavorLevels["diacetyl"]).isEqualTo(IntensityLevel.LOW)
    }

    @Test
    fun cycleOffFlavor_cyclesFourStatesAndRemovesOnFourthTap() = runTest {
        val (vm, _) = createViewModel()
        advanceUntilIdle()

        vm.cycleOffFlavor("dms")
        assertThat(vm.uiState.value.scoresheet.offFlavorLevels["dms"]).isEqualTo(IntensityLevel.LOW)

        vm.cycleOffFlavor("dms")
        assertThat(vm.uiState.value.scoresheet.offFlavorLevels["dms"]).isEqualTo(IntensityLevel.MEDIUM)

        vm.cycleOffFlavor("dms")
        assertThat(vm.uiState.value.scoresheet.offFlavorLevels["dms"]).isEqualTo(IntensityLevel.HIGH)

        vm.cycleOffFlavor("dms")
        assertThat(vm.uiState.value.scoresheet.offFlavorLevels).doesNotContainKey("dms")
    }

    @Test
    fun saveAndDelete_persistsAndRemovesScoresheet() = runTest {
        val (vm, repo) = createViewModel()
        advanceUntilIdle()

        assertThat(vm.uiState.value.canSave).isFalse()

        vm.updateEntryTitle("Pliny Clone")
        vm.selectBjcpStyle(
            BeerStyle(
                id = "22A",
                name = "Double IPA",
                category = "Strong American Ale",
                categoryNumber = "22",
                number = "22A",
                overallImpression = "",
                aroma = "",
                appearance = "",
                flavor = "",
                mouthfeel = "",
                comments = "",
                history = "",
                characteristicIngredients = "",
                styleComparison = "",
                ibuMin = 60.0,
                ibuMax = 100.0,
                ogMin = 1.065,
                ogMax = 1.085,
                fgMin = 1.008,
                fgMax = 1.018,
                abvMin = 7.5,
                abvMax = 10.0,
                srmMin = 6.0,
                srmMax = 14.0,
                commercialExamples = "",
                tags = emptyList(),
                guidelineType = GuidelineType.BJCP_2021
            )
        )
        assertThat(vm.uiState.value.canSave).isTrue()

        var saved = false
        vm.save { saved = true }
        advanceUntilIdle()

        assertThat(saved).isTrue()
        val all = repo.getAllScoresheets().first()
        assertThat(all).hasSize(1)
        assertThat(all.first().entryTitle).isEqualTo("Pliny Clone")
        assertThat(all.first().styleId).isEqualTo("22A")

        repo.deleteScoresheet(all.first().id)
        advanceUntilIdle()

        assertThat(repo.getAllScoresheets().first()).isEmpty()
    }
}
