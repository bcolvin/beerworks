package com.bc.beerworks.ui.settings

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
class SettingsViewModelTest {

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
    fun selectedGuideline_defaultsToBJCP2021() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(emptyList()),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager()
        )

        val viewModel = SettingsViewModel(repository)

        assertThat(viewModel.selectedGuideline.value).isEqualTo(GuidelineType.BJCP_2021)
    }

    @Test
    fun onGuidelineChange_updatesSelectedGuideline() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(emptyList()),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager()
        )

        val viewModel = SettingsViewModel(repository)
        val collectJob = backgroundScope.launch {
            viewModel.selectedGuideline.collect {}
        }

        viewModel.onGuidelineChange(GuidelineType.BA_2026)
        runCurrent()

        assertThat(viewModel.selectedGuideline.value).isEqualTo(GuidelineType.BA_2026)

        viewModel.onGuidelineChange(GuidelineType.BJCP_MEAD)
        runCurrent()

        assertThat(viewModel.selectedGuideline.value).isEqualTo(GuidelineType.BJCP_MEAD)
        collectJob.cancel()
    }

    @Test
    fun keepScreenOn_defaultsToFalse() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(emptyList()),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager()
        )

        val viewModel = SettingsViewModel(repository)

        assertThat(viewModel.keepScreenOn.value).isFalse()
    }

    @Test
    fun onKeepScreenOnChange_updatesKeepScreenOn() = runTest {
        val repository = BeerStyleRepository(
            FakeBjcpDao(emptyList()),
            FakeBaDao(),
            FakeFavDao(),
            FakePrefsManager()
        )

        val viewModel = SettingsViewModel(repository)
        val collectJob = backgroundScope.launch {
            viewModel.keepScreenOn.collect {}
        }

        viewModel.onKeepScreenOnChange(true)
        runCurrent()

        assertThat(viewModel.keepScreenOn.value).isTrue()

        viewModel.onKeepScreenOnChange(false)
        runCurrent()

        assertThat(viewModel.keepScreenOn.value).isFalse()
        collectJob.cancel()
    }
}
