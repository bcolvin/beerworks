package com.bc.beerworks.ui.categorydetail

import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeBaDao
import com.bc.beerworks.data.repository.FakeBjcpDao
import com.bc.beerworks.data.repository.FakeFavDao
import com.bc.beerworks.data.repository.FakePrefsManager
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

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun categoryGroup_fetchesCategoryByNumberAndGuideline() = runTest {
        val testEntity = BjcpStyleEntity(
            id = "1A", name = "American Light Lager", category = "Standard American Beer",
            categoryNumber = "1", number = "1A", overallImpression = "", aroma = "", appearance = "",
            flavor = "", mouthfeel = "", comments = "", history = "", characteristicIngredients = "",
            styleComparison = "", ibuMin = 8.0, ibuMax = 12.0, ogMin = 1.028, ogMax = 1.040,
            fgMin = 0.998, fgMax = 1.008, abvMin = 2.8, abvMax = 4.2, srmMin = 2.0, srmMax = 3.0,
            commercialExamples = "Bud Light", tags = "session, pale"
        )

        val repository = BeerStyleRepository(
            FakeBjcpDao(listOf(testEntity)),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager()
        )

        val viewModel = CategoryDetailViewModel(repository, "1", GuidelineType.BJCP_2021)
        val collectJob = backgroundScope.launch {
            viewModel.categoryGroup.collect {}
        }
        runCurrent()

        val group = viewModel.categoryGroup.value
        assertThat(group).isNotNull
        assertThat(group?.categoryNumber).isEqualTo("1")
        assertThat(group?.categoryName).isEqualTo("Standard American Beer")
        assertThat(group?.styles).hasSize(1)

        collectJob.cancel()
    }
}
