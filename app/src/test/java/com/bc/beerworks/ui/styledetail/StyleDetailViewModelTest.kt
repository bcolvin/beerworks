package com.bc.beerworks.ui.styledetail

import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.FakeBaDao
import com.bc.beerworks.data.repository.FakeBjcpDao
import com.bc.beerworks.data.repository.FakeFavDao
import com.bc.beerworks.data.repository.FakePrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class StyleDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testEntity1 = BjcpStyleEntity(
        id = "1A", name = "American Light Lager", category = "Standard American Beer",
        categoryNumber = "1", number = "1A", overallImpression = "Clean and crisp", aroma = "Low malt",
        appearance = "Pale yellow", flavor = "Neutral", mouthfeel = "Light", comments = "", history = "",
        characteristicIngredients = "Corn", styleComparison = "", ibuMin = 8.0, ibuMax = 12.0,
        ogMin = 1.028, ogMax = 1.040, fgMin = 0.998, fgMax = 1.008, abvMin = 2.8, abvMax = 4.2,
        srmMin = 2.0, srmMax = 3.0, commercialExamples = "Bud Light", tags = "session, pale"
    )

    private val testEntity2 = BjcpStyleEntity(
        id = "1B", name = "American Lager", category = "Standard American Beer",
        categoryNumber = "1", number = "1B", overallImpression = "Refreshing", aroma = "Grainy",
        appearance = "Gold", flavor = "Crisp", mouthfeel = "Medium-light", comments = "", history = "",
        characteristicIngredients = "Rice", styleComparison = "", ibuMin = 8.0, ibuMax = 18.0,
        ogMin = 1.040, ogMax = 1.050, fgMin = 1.004, fgMax = 1.010, abvMin = 4.2, abvMax = 5.3,
        srmMin = 2.0, srmMax = 3.5, commercialExamples = "Budweiser", tags = "pale, lager"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadStyle_loadsCurrentAndNextStyleInSequentialOrder() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(listOf(testEntity1, testEntity2)),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            testDispatcher
        )

        val viewModel = StyleDetailViewModel(
            repository = repository,
            styleId = "1A",
            guidelineType = GuidelineType.BJCP_2021
        )

        runCurrent()

        val style = viewModel.style.value
        assertThat(style).isNotNull
        assertThat(style?.id).isEqualTo("1A")
        assertThat(style?.name).isEqualTo("American Light Lager")

        val next = viewModel.nextStyle.value
        assertThat(next).isNotNull
        assertThat(next?.id).isEqualTo("1B")
        assertThat(next?.name).isEqualTo("American Lager")
    }

    @Test
    fun loadStyle_nextStyleIsNullWhenAtLastStyle() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(listOf(testEntity1, testEntity2)),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            testDispatcher
        )

        val viewModel = StyleDetailViewModel(
            repository = repository,
            styleId = "1B",
            guidelineType = GuidelineType.BJCP_2021
        )

        runCurrent()

        val style = viewModel.style.value
        assertThat(style).isNotNull
        assertThat(style?.id).isEqualTo("1B")

        val next = viewModel.nextStyle.value
        assertThat(next).isNull()
    }

    @Test
    fun toggleFavorite_togglesFavoriteState() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(listOf(testEntity1)),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager(),
            testDispatcher
        )

        val viewModel = StyleDetailViewModel(
            repository = repository,
            styleId = "1A",
            guidelineType = GuidelineType.BJCP_2021
        )

        runCurrent()

        assertThat(viewModel.style.value?.isFavorite).isFalse()

        viewModel.toggleFavorite()
        runCurrent()

        assertThat(viewModel.style.value?.isFavorite).isTrue()

        viewModel.toggleFavorite()
        runCurrent()

        assertThat(viewModel.style.value?.isFavorite).isFalse()
    }
}
