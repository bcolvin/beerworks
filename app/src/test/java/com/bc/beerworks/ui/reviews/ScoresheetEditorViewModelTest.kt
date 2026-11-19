package com.bc.beerworks.ui.reviews

import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.dao.ScoresheetDao
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import com.bc.beerworks.data.local.entity.ScoresheetEntity
import com.bc.beerworks.data.model.BeerScoresheet
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.IntensityLevel
import com.bc.beerworks.data.model.MeadScoresheet
import com.bc.beerworks.data.model.OffFlavor
import com.bc.beerworks.data.model.OffFlavorFilterCriteria
import com.bc.beerworks.data.model.ScoresheetDescriptor
import com.bc.beerworks.data.model.ScoresheetDiscipline
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

private class FakeMeadDao(
    styles: List<MeadStyleEntity> = emptyList()
) : MeadStyleDao {
    private val items = MutableStateFlow(styles)
    override suspend fun insertAll(styles: List<MeadStyleEntity>) { items.value = styles }
    override fun getAllStyles(): Flow<List<MeadStyleEntity>> = items
    override suspend fun getStyleById(id: String): MeadStyleEntity? = items.value.firstOrNull { it.id == id }
    override fun getCategories(): Flow<List<String>> = flowOf(items.value.map { it.category }.distinct())
    override fun filterStyles(isAbvActive: Int, minAbv: Double, maxAbv: Double, category: String?): Flow<List<MeadStyleEntity>> = items
    override fun searchAndFilterStyles(
        ftsQuery: String,
        likeQuery: String,
        isAbvActive: Int,
        minAbv: Double,
        maxAbv: Double,
        category: String?
    ): Flow<List<MeadStyleEntity>> = items
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
        scoresheetDao: FakeScoresheetDao = FakeScoresheetDao(),
        meadDao: MeadStyleDao? = null
    ): Pair<ScoresheetEditorViewModel, ScoresheetRepository> {
        val scoresheetRepo = ScoresheetRepository(scoresheetDao, prefsManager)
        val bjcpDao = FakeBjcpDao(emptyList())
        val baDao = FakeBaDao()
        val styleRepo = BeerStyleRepository(
            bjcpDao,
            baDao,
            FakeFavDao(),
            prefsManager,
            defaultDispatcher = testDispatcher,
            meadStyleDao = meadDao
        )
        val offFlavorRepo = mock(OffFlavorRepository::class.java)
        doReturn(flowOf(emptyList<OffFlavor>())).`when`(offFlavorRepo).getFilteredItems(OffFlavorFilterCriteria())
        val vm = ScoresheetEditorViewModel(scoresheetId, scoresheetRepo, styleRepo, offFlavorRepo)
        return vm to scoresheetRepo
    }

    @Test
    fun newScoresheet_startsAllSlidersAtZero() = runTest {
        val (vm, _) = createViewModel()
        advanceUntilIdle()

        val sheet = vm.uiState.value.scoresheet as BeerScoresheet
        assertThat(sheet.discipline).isEqualTo(ScoresheetDiscipline.BJCP_BEER)
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

    @Test
    fun updateDiscipline_switchesSilentlyWhenCleanAndPreservesMetadataOnDirtyConfirm() = runTest {
        val (vm, _) = createViewModel()
        advanceUntilIdle()

        vm.updateEntryTitle("House Entry")
        vm.updateBrewer("Jordan")
        vm.updateBottleInspectionOk(false)
        vm.updateBottleInspectionNotes("Low fill")

        // Clean switch (no judging scores/style/descriptors/notes) switches silently & keeps header metadata
        vm.updateDiscipline(ScoresheetDiscipline.BJCP_MEAD)
        assertThat(vm.uiState.value.pendingDisciplineSwitch).isNull()
        assertThat(vm.uiState.value.scoresheet).isInstanceOf(MeadScoresheet::class.java)
        assertThat(vm.uiState.value.scoresheet.entryTitle).isEqualTo("House Entry")
        assertThat(vm.uiState.value.scoresheet.brewer).isEqualTo("Jordan")
        assertThat(vm.uiState.value.scoresheet.bottleInspectionOk).isFalse()
        assertThat(vm.uiState.value.scoresheet.bottleInspectionNotes).isEqualTo("Low fill")

        // Previewing unsupported Cider keeps Mead draft intact
        vm.updateDiscipline(ScoresheetDiscipline.BJCP_CIDER)
        assertThat(vm.uiState.value.selectedDiscipline).isEqualTo(ScoresheetDiscipline.BJCP_CIDER)
        assertThat(vm.uiState.value.canSave).isFalse()

        vm.updateDiscipline(ScoresheetDiscipline.BJCP_MEAD)
        assertThat(vm.uiState.value.selectedDiscipline).isEqualTo(ScoresheetDiscipline.BJCP_MEAD)
        assertThat(vm.uiState.value.canSave).isTrue()

        // Add judging data -> switching to Beer triggers confirmation
        vm.updateFlavorScore(22)
        vm.updateDiscipline(ScoresheetDiscipline.BJCP_BEER)
        assertThat(vm.uiState.value.pendingDisciplineSwitch).isEqualTo(ScoresheetDiscipline.BJCP_BEER)
        assertThat(vm.uiState.value.scoresheet).isInstanceOf(MeadScoresheet::class.java)

        // Confirm resets judging data while keeping entry header metadata
        vm.confirmDisciplineSwitch()
        assertThat(vm.uiState.value.pendingDisciplineSwitch).isNull()
        val beer = vm.uiState.value.scoresheet as BeerScoresheet
        assertThat(beer.flavorScore).isEqualTo(0)
        assertThat(beer.entryTitle).isEqualTo("House Entry")
        assertThat(beer.brewer).isEqualTo("Jordan")
        assertThat(beer.bottleInspectionOk).isFalse()
    }

    @Test
    fun meadScoresheet_clampsSectionMaxesLeavesAdditionalChipsUnsetAndPersists() = runTest {
        val meadDao = FakeMeadDao(
            listOf(
                MeadStyleEntity(
                    id = "M1A",
                    name = "Dry Mead",
                    category = "Traditional Mead",
                    categoryNumber = "M1",
                    number = "M1A",
                    overallImpression = "Dry traditional",
                    aroma = "",
                    appearance = "",
                    flavor = "",
                    mouthfeel = "",
                    comments = "",
                    history = "",
                    characteristicIngredients = "",
                    styleComparison = "",
                    ogMin = 1.035,
                    ogMax = 1.170,
                    fgMin = 0.990,
                    fgMax = 1.010,
                    abvMin = 3.5,
                    abvMax = 18.0,
                    sweetness = "Dry",
                    carbonation = "Still, Pétillant, Sparkling",
                    strength = "Hydromel, Standard, Sack",
                    commercialExamples = "",
                    tags = ""
                )
            )
        )
        val (vm, repo) = createViewModel(meadDao = meadDao)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.bjcpMeadStyles.collect()
        }
        advanceUntilIdle()

        vm.updateDiscipline(ScoresheetDiscipline.BJCP_MEAD)
        vm.updateEntryTitle("Clover Dry")
        vm.selectBjcpStyle(vm.bjcpMeadStyles.value.first())

        // Selecting M1A must not auto-populate collapsed Additional Information chips
        val afterStyle = vm.uiState.value.scoresheet as MeadScoresheet
        assertThat(afterStyle.declaredSweetness).isNull()
        assertThat(afterStyle.declaredCarbonation).isNull()
        assertThat(afterStyle.declaredStrength).isNull()

        // Clamp bounds for BJCP Mead: Aroma 10, Appearance 6, Flavor 24, Overall 10
        vm.updateAromaScore(15)
        vm.updateAppearanceScore(8)
        vm.updateFlavorScore(30)
        vm.updateOverallScore(12)
        vm.cycleMeadCarbonation()
        vm.cycleMeadSweetness()
        vm.cycleMeadStrength()
        vm.updateSpecialIngredients("Local wildflower honey")

        val updated = vm.uiState.value.scoresheet as MeadScoresheet
        assertThat(updated.aromaScore).isEqualTo(10)
        assertThat(updated.appearanceScore).isEqualTo(6)
        assertThat(updated.flavorScore).isEqualTo(24)
        assertThat(updated.overallScore).isEqualTo(10)
        assertThat(updated.totalScore).isEqualTo(50)
        assertThat(updated.declaredCarbonation).isEqualTo("Still")
        assertThat(updated.declaredSweetness).isEqualTo("Dry")
        assertThat(updated.declaredStrength).isEqualTo("Hydromel")

        var saved = false
        vm.save { saved = true }
        advanceUntilIdle()
        assertThat(saved).isTrue()

        val persisted = repo.getAllScoresheets().first().single() as MeadScoresheet
        assertThat(persisted.specialIngredients).isEqualTo("Local wildflower honey")
        assertThat(persisted.declaredCarbonation).isEqualTo("Still")
        assertThat(persisted.totalScore).isEqualTo(50)
    }

    @Test
    fun updateDiscipline_isIgnoredWhenEditingExistingScoresheet() = runTest {
        val dao = FakeScoresheetDao()
        val repo = ScoresheetRepository(dao)
        val existing = BeerScoresheet(
            id = "saved-1",
            entryTitle = "Saved IPA"
        )
        repo.saveScoresheet(existing)

        val (vm, _) = createViewModel(scoresheetId = "saved-1", scoresheetDao = dao)
        advanceUntilIdle()

        vm.updateDiscipline(ScoresheetDiscipline.BJCP_MEAD)
        assertThat(vm.uiState.value.scoresheet.discipline).isEqualTo(ScoresheetDiscipline.BJCP_BEER)
    }

    @Test
    fun newScoresheet_startsWithLastCreatedDisciplineAndIgnoresEdits() = runTest {
        val prefs = FakePrefsManager()
        val dao = FakeScoresheetDao()

        // Seed an existing saved Beer scoresheet
        val existingBeer = BeerScoresheet(id = "beer-1", entryTitle = "Old Pale Ale")
        ScoresheetRepository(dao, prefs).saveScoresheet(existingBeer)

        // Create and save a new Mead scoresheet
        val (firstNewVm, _) = createViewModel(prefsManager = prefs, scoresheetDao = dao)
        advanceUntilIdle()
        firstNewVm.updateDiscipline(ScoresheetDiscipline.BJCP_MEAD)
        firstNewVm.updateEntryTitle("Flight Mead #1")
        firstNewVm.save {}
        advanceUntilIdle()

        // Next new scoresheet starts with BJCP_MEAD selected
        val (secondNewVm, _) = createViewModel(prefsManager = prefs, scoresheetDao = dao)
        advanceUntilIdle()
        assertThat(secondNewVm.uiState.value.selectedDiscipline).isEqualTo(ScoresheetDiscipline.BJCP_MEAD)
        assertThat(secondNewVm.uiState.value.scoresheet).isInstanceOf(MeadScoresheet::class.java)

        // Editing an existing Beer scoresheet does not overwrite the remembered creation discipline
        val (editBeerVm, _) = createViewModel(scoresheetId = "beer-1", prefsManager = prefs, scoresheetDao = dao)
        advanceUntilIdle()
        editBeerVm.updateEntryTitle("Old Pale Ale Updated")
        editBeerVm.save {}
        advanceUntilIdle()

        val (thirdNewVm, _) = createViewModel(prefsManager = prefs, scoresheetDao = dao)
        advanceUntilIdle()
        assertThat(thirdNewVm.uiState.value.selectedDiscipline).isEqualTo(ScoresheetDiscipline.BJCP_MEAD)
    }
}
