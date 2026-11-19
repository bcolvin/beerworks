package com.bc.beerworks.ui.reviews

import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.dao.ScoresheetDao
import com.bc.beerworks.data.local.entity.CiderStyleEntity
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import com.bc.beerworks.data.local.entity.ScoresheetEntity
import com.bc.beerworks.data.model.BeerScoresheet
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.FourSectionScoresheet
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.IntensityLevel
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

private class FakeCiderDao(
    styles: List<CiderStyleEntity> = emptyList()
) : CiderStyleDao {
    private val items = MutableStateFlow(styles)
    override suspend fun insertAll(styles: List<CiderStyleEntity>) { items.value = styles }
    override fun getAllStyles(): Flow<List<CiderStyleEntity>> = items
    override suspend fun getStyleById(id: String): CiderStyleEntity? = items.value.firstOrNull { it.id == id }
    override fun getCategories(): Flow<List<String>> = flowOf(items.value.map { it.category }.distinct())
    override fun filterStyles(isAbvActive: Int, minAbv: Double, maxAbv: Double, category: String?): Flow<List<CiderStyleEntity>> = items
    override fun searchAndFilterStyles(
        ftsQuery: String,
        likeQuery: String,
        isAbvActive: Int,
        minAbv: Double,
        maxAbv: Double,
        category: String?
    ): Flow<List<CiderStyleEntity>> = items
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
        meadDao: MeadStyleDao? = null,
        ciderDao: CiderStyleDao? = null
    ): Pair<ScoresheetEditorViewModel, ScoresheetRepository> {
        val scoresheetRepo = ScoresheetRepository(scoresheetDao, defaultDispatcher = testDispatcher)
        val bjcpDao = FakeBjcpDao(emptyList())
        val baDao = FakeBaDao()
        val styleRepo = BeerStyleRepository(
            bjcpDao,
            baDao,
            FakeFavDao(),
            prefsManager,
            defaultDispatcher = testDispatcher,
            meadStyleDao = meadDao,
            ciderStyleDao = ciderDao
        )
        val offFlavorRepo = mock(OffFlavorRepository::class.java)
        doReturn(flowOf(emptyList<OffFlavor>())).`when`(offFlavorRepo).getFilteredItems(OffFlavorFilterCriteria())
        val vm = ScoresheetEditorViewModel(scoresheetId, scoresheetRepo, styleRepo, offFlavorRepo, prefsManager)
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

        // Clean switch across Beer -> Mead -> Cider switches silently & keeps header metadata
        vm.updateDiscipline(ScoresheetDiscipline.BJCP_MEAD)
        assertThat(vm.uiState.value.pendingDisciplineSwitch).isNull()
        assertThat(vm.uiState.value.scoresheet).isInstanceOf(FourSectionScoresheet::class.java)
        assertThat(vm.uiState.value.scoresheet.discipline).isEqualTo(ScoresheetDiscipline.BJCP_MEAD)
        assertThat(vm.uiState.value.scoresheet.entryTitle).isEqualTo("House Entry")
        assertThat(vm.uiState.value.scoresheet.brewer).isEqualTo("Jordan")
        assertThat(vm.uiState.value.scoresheet.bottleInspectionOk).isFalse()
        assertThat(vm.uiState.value.scoresheet.bottleInspectionNotes).isEqualTo("Low fill")

        vm.updateDiscipline(ScoresheetDiscipline.BJCP_CIDER)
        assertThat(vm.uiState.value.pendingDisciplineSwitch).isNull()
        assertThat(vm.uiState.value.scoresheet).isInstanceOf(FourSectionScoresheet::class.java)
        assertThat(vm.uiState.value.scoresheet.discipline).isEqualTo(ScoresheetDiscipline.BJCP_CIDER)
        assertThat(vm.uiState.value.scoresheet.entryTitle).isEqualTo("House Entry")
        assertThat(vm.uiState.value.canSave).isTrue()

        // Add judging data -> switching to Beer triggers confirmation
        vm.updateFlavorScore(22)
        vm.updateDiscipline(ScoresheetDiscipline.BJCP_BEER)
        assertThat(vm.uiState.value.pendingDisciplineSwitch).isEqualTo(ScoresheetDiscipline.BJCP_BEER)
        assertThat(vm.uiState.value.scoresheet).isInstanceOf(FourSectionScoresheet::class.java)
        assertThat(vm.uiState.value.scoresheet.discipline).isEqualTo(ScoresheetDiscipline.BJCP_CIDER)

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
        advanceUntilIdle()

        vm.updateDiscipline(ScoresheetDiscipline.BJCP_MEAD)
        advanceUntilIdle()
        vm.updateEntryTitle("Clover Dry")
        vm.selectBjcpStyle(vm.uiState.value.filteredStyles.first())
        advanceUntilIdle()

        // Selecting M1A must not auto-populate collapsed Additional Information chips
        val afterStyle = vm.uiState.value.scoresheet as FourSectionScoresheet
        assertThat(afterStyle.declaredSweetness).isNull()
        assertThat(afterStyle.declaredCarbonation).isNull()
        assertThat(afterStyle.declaredStrength).isNull()

        // Clamp bounds for BJCP Mead: Aroma 10, Appearance 6, Flavor 24, Overall 10
        vm.updateAromaScore(15)
        vm.updateAppearanceScore(8)
        vm.updateFlavorScore(30)
        vm.updateOverallScore(12)
        vm.cycleDeclaredCarbonation()
        vm.cycleDeclaredSweetness()
        vm.cycleDeclaredStrength()
        vm.updateSpecialIngredients("Local wildflower honey")

        val updated = vm.uiState.value.scoresheet as FourSectionScoresheet
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

        val persisted = repo.getAllScoresheets().first().single() as FourSectionScoresheet
        assertThat(persisted.specialIngredients).isEqualTo("Local wildflower honey")
        assertThat(persisted.declaredCarbonation).isEqualTo("Still")
        assertThat(persisted.totalScore).isEqualTo(50)
    }

    @Test
    fun updateDiscipline_isIgnoredWhenEditingExistingScoresheet() = runTest {
        val dao = FakeScoresheetDao()
        val repo = ScoresheetRepository(dao, defaultDispatcher = testDispatcher)
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
        ScoresheetRepository(dao, defaultDispatcher = testDispatcher).saveScoresheet(existingBeer)

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
        assertThat(secondNewVm.uiState.value.scoresheet).isInstanceOf(FourSectionScoresheet::class.java)
        assertThat(secondNewVm.uiState.value.scoresheet.discipline).isEqualTo(ScoresheetDiscipline.BJCP_MEAD)

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

    @Test
    fun ciderScoresheet_clampsSectionMaxesCycles5TierSweetnessAndRemembersDefault() = runTest {
        val ciderDao = FakeCiderDao(
            listOf(
                CiderStyleEntity(
                    id = "C2D",
                    name = "Ice Cider",
                    category = "Specialty Cider & Perry",
                    categoryNumber = "C2",
                    number = "C2D",
                    overallImpression = "Intense concentrated apple",
                    aroma = "",
                    appearance = "",
                    flavor = "",
                    mouthfeel = "",
                    comments = "",
                    history = "",
                    characteristicIngredients = "",
                    styleComparison = "",
                    ogMin = 1.130,
                    ogMax = 1.180,
                    fgMin = 1.060,
                    fgMax = 1.085,
                    abvMin = 7.0,
                    abvMax = 13.0,
                    sweetness = "Sweet",
                    carbonation = "Still, Pétillant",
                    strength = "High Gravity",
                    commercialExamples = "",
                    tags = ""
                )
            )
        )
        val prefs = FakePrefsManager()
        val dao = FakeScoresheetDao()
        val (vm, repo) = createViewModel(prefsManager = prefs, scoresheetDao = dao, ciderDao = ciderDao)
        advanceUntilIdle()

        vm.updateDiscipline(ScoresheetDiscipline.BJCP_CIDER)
        advanceUntilIdle()
        vm.updateEntryTitle("Winter Frost Ice Cider")
        vm.selectBjcpStyle(vm.uiState.value.filteredStyles.first())
        advanceUntilIdle()

        // Selecting C2D must not auto-populate collapsed Additional Information chips
        val afterStyle = vm.uiState.value.scoresheet as FourSectionScoresheet
        assertThat(afterStyle.declaredSweetness).isNull()
        assertThat(afterStyle.declaredCarbonation).isNull()
        assertThat(afterStyle.declaredStrength).isNull()

        // Clamp bounds: 10 / 6 / 24 / 10, cycle 5-tier sweetness twice (Dry -> Medium-Dry) & strength (Session -> Standard -> High Gravity)
        vm.updateAromaScore(14)
        vm.updateAppearanceScore(9)
        vm.updateFlavorScore(28)
        vm.updateOverallScore(11)
        vm.cycleDeclaredCarbonation()
        vm.cycleDeclaredSweetness()
        vm.cycleDeclaredSweetness()
        vm.cycleDeclaredStrength()
        vm.cycleDeclaredStrength()
        vm.cycleDeclaredStrength()
        vm.updateSpecialIngredients("Northern Spy, Kingston Black")

        val updated = vm.uiState.value.scoresheet as FourSectionScoresheet
        assertThat(updated.aromaScore).isEqualTo(10)
        assertThat(updated.appearanceScore).isEqualTo(6)
        assertThat(updated.flavorScore).isEqualTo(24)
        assertThat(updated.overallScore).isEqualTo(10)
        assertThat(updated.totalScore).isEqualTo(50)
        assertThat(updated.declaredCarbonation).isEqualTo("Still")
        assertThat(updated.declaredSweetness).isEqualTo("Medium-Dry")
        assertThat(updated.declaredStrength).isEqualTo("High Gravity")

        var saved = false
        vm.save { saved = true }
        advanceUntilIdle()
        assertThat(saved).isTrue()

        val persisted = repo.getAllScoresheets().first().single() as FourSectionScoresheet
        assertThat(persisted.specialIngredients).isEqualTo("Northern Spy, Kingston Black")
        assertThat(persisted.declaredSweetness).isEqualTo("Medium-Dry")
        assertThat(persisted.totalScore).isEqualTo(50)

        // Next new scoresheet starts with BJCP_CIDER selected
        val (nextVm, _) = createViewModel(prefsManager = prefs, scoresheetDao = dao, ciderDao = ciderDao)
        advanceUntilIdle()
        assertThat(nextVm.uiState.value.selectedDiscipline).isEqualTo(ScoresheetDiscipline.BJCP_CIDER)
        assertThat(nextVm.uiState.value.scoresheet).isInstanceOf(FourSectionScoresheet::class.java)
        assertThat(nextVm.uiState.value.scoresheet.discipline).isEqualTo(ScoresheetDiscipline.BJCP_CIDER)
    }

    @Test
    fun hasUnsavedChanges_tracksEditsRevertsAndCleanDisciplineSwitches() = runTest {
        val prefs = FakePrefsManager()
        val dao = FakeScoresheetDao()

        // New sheet starts clean, even after switching discipline while untouched
        val (newVm, repo) = createViewModel(prefsManager = prefs, scoresheetDao = dao)
        advanceUntilIdle()
        assertThat(newVm.uiState.value.hasUnsavedChanges).isFalse()

        newVm.updateDiscipline(ScoresheetDiscipline.BJCP_MEAD)
        assertThat(newVm.uiState.value.hasUnsavedChanges).isFalse()

        newVm.updateEntryTitle("Unsaved Mead")
        assertThat(newVm.uiState.value.hasUnsavedChanges).isTrue()

        // Editing an existing saved sheet starts clean, becomes dirty on change, and clears if reverted
        val saved = BeerScoresheet(id = "existing-1", entryTitle = "House Bitter", aromaScore = 9)
        repo.saveScoresheet(saved)

        val (editVm, _) = createViewModel(scoresheetId = "existing-1", prefsManager = prefs, scoresheetDao = dao)
        advanceUntilIdle()
        assertThat(editVm.uiState.value.hasUnsavedChanges).isFalse()

        editVm.updateAromaScore(11)
        assertThat(editVm.uiState.value.hasUnsavedChanges).isTrue()

        editVm.updateAromaScore(9)
        assertThat(editVm.uiState.value.hasUnsavedChanges).isFalse()
    }

    @Test
    fun uiState_projectsFilteredStylesLinkedStyleAndDisciplineOffFlavors() = runTest {
        val scoresheetDao = FakeScoresheetDao()
        val prefsManager = FakePrefsManager()
        val scoresheetRepo = ScoresheetRepository(scoresheetDao, defaultDispatcher = testDispatcher)
        val bjcpDao = FakeBjcpDao(emptyList())
        val baDao = FakeBaDao()
        val meadDao = FakeMeadDao(
            listOf(
                MeadStyleEntity(
                    id = "M1A", name = "Dry Mead", category = "Traditional Mead", categoryNumber = "M1", number = "M1A",
                    overallImpression = "Dry", aroma = "", appearance = "", flavor = "", mouthfeel = "", comments = "",
                    history = "", characteristicIngredients = "", styleComparison = "", ogMin = 1.035, ogMax = 1.170,
                    fgMin = 0.990, fgMax = 1.010, abvMin = 3.5, abvMax = 18.0, sweetness = "Dry",
                    carbonation = "Still", strength = "Standard", commercialExamples = "", tags = ""
                ),
                MeadStyleEntity(
                    id = "M2A", name = "Cyser", category = "Melomel", categoryNumber = "M2", number = "M2A",
                    overallImpression = "Apple mead", aroma = "", appearance = "", flavor = "", mouthfeel = "", comments = "",
                    history = "", characteristicIngredients = "", styleComparison = "", ogMin = 1.035, ogMax = 1.170,
                    fgMin = 0.990, fgMax = 1.025, abvMin = 3.5, abvMax = 18.0, sweetness = "Dry",
                    carbonation = "Still", strength = "Standard", commercialExamples = "", tags = ""
                )
            )
        )
        val styleRepo = BeerStyleRepository(
            bjcpDao, baDao, FakeFavDao(), prefsManager, defaultDispatcher = testDispatcher, meadStyleDao = meadDao
        )
        val beerFault = OffFlavor(
            id = "dms", name = "DMS", emoji = "🌽", category = "Mashing & Boil", chemicalCompound = "DMS",
            perceptionThreshold = "30 ppb", typicalConcentration = "10 ppb", meilgaardCode = "0732",
            detectedIn = listOf("Aroma"), disciplines = listOf("Beer"), stylePermissibility = "Style-Permissible",
            description = "", aromaPerception = "", flavorPerception = "", mouthfeelPerception = "",
            descriptors = emptyList(), diagnosticContrasts = emptyList(), typicalOrigins = emptyList(),
            preventionTips = emptyList(), whenAppropriate = "", confusedWithIds = emptyList()
        )
        val meadFault = beerFault.copy(id = "thp", name = "THP", disciplines = listOf("Beer", "Mead"))
        val offFlavorRepo = mock(OffFlavorRepository::class.java)
        doReturn(flowOf(listOf(beerFault, meadFault))).`when`(offFlavorRepo).getFilteredItems(OffFlavorFilterCriteria())

        val vm = ScoresheetEditorViewModel(null, scoresheetRepo, styleRepo, offFlavorRepo, prefsManager)
        advanceUntilIdle()

        assertThat(vm.uiState.value.availableOffFlavors.map { it.id }).containsExactly("dms", "thp")

        vm.updateDiscipline(ScoresheetDiscipline.BJCP_MEAD)
        advanceUntilIdle()

        assertThat(vm.uiState.value.availableOffFlavors.map { it.id }).containsExactly("thp")
        assertThat(vm.uiState.value.filteredStyles.map { it.id }).containsExactly("M1A", "M2A")

        vm.onStyleQueryChange("Cyser")
        advanceUntilIdle()
        assertThat(vm.uiState.value.filteredStyles.map { it.id }).containsExactly("M2A")
        assertThat(vm.uiState.value.linkedStyle).isNull()

        vm.selectBjcpStyle(vm.uiState.value.filteredStyles.single())
        advanceUntilIdle()
        assertThat(vm.uiState.value.linkedStyle?.id).isEqualTo("M2A")
    }
}
