package com.bc.beerworks.ui.offflavors

import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.entity.OffFlavorEntity
import com.bc.beerworks.data.model.OffFlavorFilterCriteria
import com.bc.beerworks.data.repository.FakeOffFlavorDao
import com.bc.beerworks.data.repository.FakeOffFlavorStyleCrossRefDao
import com.bc.beerworks.data.repository.OffFlavorRepository
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
class OffFlavorsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val diacetyl = OffFlavorEntity(
        id = "diacetyl",
        name = "Diacetyl",
        emoji = "🧈",
        category = "Fermentation & Yeast",
        chemicalCompound = "2,3-Butanedione",
        perceptionThreshold = "0.10 – 0.15 ppm",
        typicalConcentration = "0.02 – 0.08 ppm",
        meilgaardCode = "0620",
        detectedIn = "Aroma,Flavor,Mouthfeel",
        disciplines = "Beer,Mead,Cider",
        stylePermissibility = "Style-Permissible",
        description = "Butter and butterscotch.",
        aromaPerception = "Melted butter.",
        flavorPerception = "Butterscotch.",
        mouthfeelPerception = "Slick palate.",
        descriptors = "Butter,Butterscotch",
        diagnosticContrastsJson = "[]",
        typicalOriginsJson = "[]",
        preventionTipsJson = "[]",
        whenAppropriate = "",
        confusedWithIds = ""
    )

    private val dms = OffFlavorEntity(
        id = "dms",
        name = "DMS (Dimethyl Sulfide)",
        emoji = "🌽",
        category = "Mashing & Boil",
        chemicalCompound = "Dimethyl Sulfide",
        perceptionThreshold = "0.030 – 0.050 ppm",
        typicalConcentration = "0.010 – 0.035 ppm",
        meilgaardCode = "0732",
        detectedIn = "Aroma,Flavor",
        disciplines = "Beer",
        stylePermissibility = "Style-Permissible",
        description = "Cooked corn and tomato.",
        aromaPerception = "Cooked sweet corn.",
        flavorPerception = "Creamed corn.",
        mouthfeelPerception = "None.",
        descriptors = "Cooked Corn,Tomato Sauce",
        diagnosticContrastsJson = "[]",
        typicalOriginsJson = "[]",
        preventionTipsJson = "[]",
        whenAppropriate = "",
        confusedWithIds = ""
    )

    private lateinit var viewModel: OffFlavorsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val repository = OffFlavorRepository(
            offFlavorDao = FakeOffFlavorDao(listOf(diacetyl, dms)),
            offFlavorStyleCrossRefDao = FakeOffFlavorStyleCrossRefDao(),
            bjcpStyleDao = mock(BjcpStyleDao::class.java),
            baStyleDao = mock(BaStyleDao::class.java),
            defaultDispatcher = testDispatcher
        )
        viewModel = OffFlavorsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onCategorySelected_togglesCategoryFilter_andResetFiltersPreservesQuery() = runTest {
        val collectJob = backgroundScope.launch {
            viewModel.offFlavors.collect {}
        }
        runCurrent()

        assertThat(viewModel.offFlavors.value).hasSize(2)

        viewModel.onCategorySelected("Mashing & Boil")
        runCurrent()
        assertThat(viewModel.offFlavors.value.map { it.id }).containsExactly("dms")

        // Clicking again toggles off
        viewModel.onCategorySelected("Mashing & Boil")
        runCurrent()
        assertThat(viewModel.offFlavors.value).hasSize(2)

        // Apply query + filter, then resetFilters preserves query
        viewModel.onQueryChange("butter")
        viewModel.onFilterCriteriaChange(
            OffFlavorFilterCriteria(query = "butter", selectedModality = "Mouthfeel")
        )
        runCurrent()
        assertThat(viewModel.offFlavors.value.map { it.id }).containsExactly("diacetyl")

        viewModel.resetFilters()
        runCurrent()
        assertThat(viewModel.filterCriteria.value.query).isEqualTo("butter")
        assertThat(viewModel.filterCriteria.value.selectedModality).isNull()

        collectJob.cancel()
    }
}
