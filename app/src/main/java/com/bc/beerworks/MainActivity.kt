package com.bc.beerworks

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bc.beerworks.data.local.BeerworksDatabase
import com.bc.beerworks.data.local.DatabaseInitializer
import com.bc.beerworks.data.local.DefaultPreferencesManager
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HoneyVariety
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.model.MaltVariety
import com.bc.beerworks.data.model.PomeVariety
import com.bc.beerworks.data.model.YeastStrain
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.HoneyRepository
import com.bc.beerworks.data.repository.HopRepository
import com.bc.beerworks.data.repository.MaltRepository
import com.bc.beerworks.data.repository.PomeRepository
import com.bc.beerworks.data.repository.YeastRepository
import com.bc.beerworks.ui.categorydetail.CategoryDetailContent
import com.bc.beerworks.ui.categorydetail.CategoryDetailViewModel
import com.bc.beerworks.ui.components.AppDrawer
import com.bc.beerworks.ui.components.DrawerDestination
import com.bc.beerworks.ui.favorites.FavoritesScreen
import com.bc.beerworks.ui.favorites.FavoritesViewModel
import com.bc.beerworks.ui.honey.HoneyContent
import com.bc.beerworks.ui.honey.HoneyViewModel
import com.bc.beerworks.ui.honeydetail.HoneyDetailScreen
import com.bc.beerworks.ui.hopdetail.HopDetailScreen
import com.bc.beerworks.ui.hops.HopsContent
import com.bc.beerworks.ui.hops.HopsViewModel
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailViewModel
import com.bc.beerworks.ui.malt.MaltContent
import com.bc.beerworks.ui.malt.MaltViewModel
import com.bc.beerworks.ui.maltdetail.MaltDetailScreen
import com.bc.beerworks.ui.pomedetail.PomeDetailScreen
import com.bc.beerworks.ui.pomes.PomesContent
import com.bc.beerworks.ui.pomes.PomesViewModel
import com.bc.beerworks.ui.reviewhistory.ReviewHistoryContent
import com.bc.beerworks.ui.settings.SettingsContent
import com.bc.beerworks.ui.settings.SettingsViewModel
import com.bc.beerworks.ui.styledetail.StyleDetailContent
import com.bc.beerworks.ui.styledetail.StyleDetailViewModel
import com.bc.beerworks.ui.stylelist.StyleListContent
import com.bc.beerworks.ui.stylelist.StyleListViewModel
import com.bc.beerworks.ui.theme.BeerworksTheme
import com.bc.beerworks.ui.yeastdetail.YeastDetailScreen
import com.bc.beerworks.ui.yeasts.YeastsContent
import com.bc.beerworks.ui.yeasts.YeastsViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BeerworksTheme {
                Surface {
                    BeerworksApp()
                }
            }
        }
    }
}

@Composable
fun BeerworksApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val database = remember { BeerworksDatabase.getInstance(context) }
    val prefsManager = remember { DefaultPreferencesManager(context) }
    val repository = remember { BeerStyleRepository(database, prefsManager) }
    val hopRepository = remember { HopRepository(database) }
    val maltRepository = remember { MaltRepository(database) }
    val honeyRepository = remember { HoneyRepository(database) }
    val pomeRepository = remember { PomeRepository(database) }
    val yeastRepository = remember { YeastRepository(database) }

    val keepScreenOn by prefsManager.keepScreenOn.collectAsState(initial = false)
    val selectedGuideline by repository.selectedGuideline.collectAsState(initial = GuidelineType.BJCP_2021)

    DisposableEffect(keepScreenOn, activity) {
        if (keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(Unit) {
        DatabaseInitializer.initializeIfNeeded(context, database)
    }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: DrawerDestination.StyleGuide.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                selectedGuideline = selectedGuideline,
                keepScreenOn = keepScreenOn,
                onKeepScreenOnChange = { enabled ->
                    scope.launch { prefsManager.setKeepScreenOn(enabled) }
                },
                onNavigate = { destination ->
                    scope.launch { drawerState.close() }
                    if (currentRoute != destination.route) {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) {
        NavHost(navController = navController, startDestination = "style_list") {
            composable("style_list") {
                val viewModel: StyleListViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return StyleListViewModel(repository) as T
                        }
                    }
                )

                val groupedStyles by viewModel.groupedStyles.collectAsState()
                val expandedCategoryNumber by viewModel.expandedCategoryNumber.collectAsState()
                val criteria by viewModel.filterCriteria.collectAsState()
                val categories by viewModel.categories.collectAsState()
                val selectedGuideline by viewModel.selectedGuideline.collectAsState()

                StyleListContent(
                    groupedStyles = groupedStyles,
                    expandedCategoryNumber = expandedCategoryNumber,
                    criteria = criteria,
                    categories = categories,
                    selectedGuideline = selectedGuideline,
                    onGuidelineChange = viewModel::onGuidelineChange,
                    onQueryChange = viewModel::onQueryChange,
                    onFilterCriteriaChange = viewModel::onFilterCriteriaChange,
                    onResetFilters = viewModel::resetFilters,
                    onCategoryClick = { group ->
                        viewModel.setExpandedCategory(group.categoryNumber)
                        navController.navigate("category_detail/${group.categoryNumber}/${group.guidelineType.name}")
                    },
                    onToggleExpand = viewModel::toggleCategoryExpansion,
                    onStyleClick = { style ->
                        viewModel.setExpandedCategory(style.categoryNumber)
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    },
                    onToggleFavorite = viewModel::toggleFavorite,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            composable(
                route = "category_detail/{categoryNumber}/{guidelineType}",
                arguments = listOf(
                    navArgument("categoryNumber") { type = NavType.StringType },
                    navArgument("guidelineType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val categoryNumber = backStackEntry.arguments?.getString("categoryNumber") ?: ""
                val guidelineName = backStackEntry.arguments?.getString("guidelineType") ?: GuidelineType.BJCP_2021.name
                val guidelineType = runCatching { GuidelineType.valueOf(guidelineName) }.getOrDefault(GuidelineType.BJCP_2021)

                val viewModel: CategoryDetailViewModel = viewModel(
                    key = "cat_${categoryNumber}_$guidelineName",
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return CategoryDetailViewModel(repository, categoryNumber, guidelineType) as T
                        }
                    }
                )

                val categoryGroup by viewModel.categoryGroup.collectAsState()

                categoryGroup?.let { group ->
                    CategoryDetailContent(
                        group = group,
                        onBackClick = { navController.popBackStack() },
                        onStyleClick = { style ->
                            navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                        },
                        onToggleFavorite = viewModel::toggleFavorite
                    )
                }
            }

            composable("hops") {
                val viewModel: HopsViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return HopsViewModel(hopRepository, repository) as T
                        }
                    }
                )

                val hops by viewModel.hops.collectAsState()
                val criteria by viewModel.filterCriteria.collectAsState()
                val origins by viewModel.origins.collectAsState()
                val categories by viewModel.categories.collectAsState()

                HopsContent(
                    hops = hops,
                    criteria = criteria,
                    availableOrigins = origins,
                    availableCategories = categories,
                    onQueryChange = viewModel::onQueryChange,
                    onFilterCriteriaChange = viewModel::onFilterCriteriaChange,
                    onResetFilters = viewModel::resetFilters,
                    onHopClick = { hop ->
                        navController.navigate("hop_detail/${hop.id}")
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            composable("malt") {
                val viewModel: MaltViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return MaltViewModel(maltRepository, repository) as T
                        }
                    }
                )

                val malts by viewModel.malts.collectAsState()
                val criteria by viewModel.filterCriteria.collectAsState()
                val categories by viewModel.categories.collectAsState()
                val grainTypes by viewModel.grainTypes.collectAsState()
                val functionalTags by viewModel.functionalTags.collectAsState()
                val baseCategories by viewModel.baseCategories.collectAsState()

                MaltContent(
                    malts = malts,
                    criteria = criteria,
                    categories = categories,
                    grainTypes = grainTypes,
                    functionalTags = functionalTags,
                    baseCategories = baseCategories,
                    onQueryChange = viewModel::onQueryChange,
                    onFilterCriteriaChange = viewModel::onFilterCriteriaChange,
                    onResetFilters = viewModel::resetFilters,
                    onMaltClick = { maltId ->
                        navController.navigate("malt_detail/$maltId")
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            composable("honey") {
                val viewModel: HoneyViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return HoneyViewModel(honeyRepository, repository) as T
                        }
                    }
                )

                val honeys by viewModel.honeys.collectAsState()
                val criteria by viewModel.filterCriteria.collectAsState()
                val classifications by viewModel.classifications.collectAsState()
                val usdaColors by viewModel.usdaColors.collectAsState()
                val flavorIntensities by viewModel.flavorIntensities.collectAsState()
                val crystallizationTendencies by viewModel.crystallizationTendencies.collectAsState()

                HoneyContent(
                    honeys = honeys,
                    criteria = criteria,
                    classifications = classifications,
                    usdaColors = usdaColors,
                    flavorIntensities = flavorIntensities,
                    crystallizationTendencies = crystallizationTendencies,
                    onQueryChange = viewModel::onQueryChange,
                    onFilterCriteriaChange = viewModel::onFilterCriteriaChange,
                    onResetFilters = viewModel::resetFilters,
                    onHoneyClick = { honeyId ->
                        navController.navigate("honey_detail/$honeyId")
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            composable("pomes") {
                val viewModel: PomesViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return PomesViewModel(pomeRepository, repository) as T
                        }
                    }
                )

                val pomes by viewModel.pomes.collectAsState()
                val criteria by viewModel.filterCriteria.collectAsState()
                val fruitTypes by viewModel.fruitTypes.collectAsState()
                val classifications by viewModel.classifications.collectAsState()
                val harvestSeasons by viewModel.harvestSeasons.collectAsState()
                val tanninLevels by viewModel.tanninLevels.collectAsState()

                PomesContent(
                    pomes = pomes,
                    criteria = criteria,
                    fruitTypes = fruitTypes,
                    classifications = classifications,
                    harvestSeasons = harvestSeasons,
                    tanninLevels = tanninLevels,
                    onQueryChange = viewModel::onQueryChange,
                    onFilterCriteriaChange = viewModel::onFilterCriteriaChange,
                    onResetFilters = viewModel::resetFilters,
                    onPomeClick = { pomeId ->
                        navController.navigate("pome_detail/$pomeId")
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            composable("yeasts") {
                val viewModel: YeastsViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return YeastsViewModel(yeastRepository, prefsManager) as T
                        }
                    }
                )

                val criteria by viewModel.filterCriteria.collectAsState()
                val yeasts by viewModel.yeasts.collectAsState()
                val classifications by viewModel.classifications.collectAsState()
                val flocculations by viewModel.flocculations.collectAsState()
                val temperatureUnit by viewModel.temperatureUnit.collectAsState()

                YeastsContent(
                    yeasts = yeasts,
                    criteria = criteria,
                    classifications = classifications,
                    flocculations = flocculations,
                    temperatureUnit = temperatureUnit,
                    onQueryChange = viewModel::onQueryChange,
                    onClassificationSelected = viewModel::onClassificationSelected,
                    onFilterCriteriaChange = viewModel::onFilterCriteriaChange,
                    onResetFilters = viewModel::resetFilters,
                    onYeastClick = { yeastId ->
                        navController.navigate("yeast_detail/$yeastId")
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            composable("favorites") {
                val viewModel: FavoritesViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return FavoritesViewModel(
                                beerStyleRepository = repository,
                                hopRepository = hopRepository,
                                maltRepository = maltRepository,
                                honeyRepository = honeyRepository,
                                pomeRepository = pomeRepository,
                                yeastRepository = yeastRepository
                            ) as T
                        }
                    }
                )

                FavoritesScreen(
                    viewModel = viewModel,
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    },
                    onHopClick = { hopId ->
                        navController.navigate("hop_detail/$hopId")
                    },
                    onMaltClick = { maltId ->
                        navController.navigate("malt_detail/$maltId")
                    },
                    onHoneyClick = { honeyId ->
                        navController.navigate("honey_detail/$honeyId")
                    },
                    onPomeClick = { pomeId ->
                        navController.navigate("pome_detail/$pomeId")
                    },
                    onYeastClick = { yeastId ->
                        navController.navigate("yeast_detail/$yeastId")
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            composable("review_history") {
                ReviewHistoryContent(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            composable("settings") {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return SettingsViewModel(repository) as T
                        }
                    }
                )
                val isScreenOn by settingsViewModel.keepScreenOn.collectAsState()
                val temperatureUnit by settingsViewModel.temperatureUnit.collectAsState()
                val confirmFavoriteRemoval by settingsViewModel.confirmFavoriteRemoval.collectAsState()

                SettingsContent(
                    keepScreenOn = isScreenOn,
                    onKeepScreenOnChange = settingsViewModel::onKeepScreenOnChange,
                    temperatureUnit = temperatureUnit,
                    onTemperatureUnitChange = settingsViewModel::onTemperatureUnitChange,
                    confirmFavoriteRemoval = confirmFavoriteRemoval,
                    onConfirmFavoriteRemovalChange = settingsViewModel::onConfirmFavoriteRemovalChange,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            composable(
                route = "style_detail/{styleId}/{guidelineType}",
                arguments = listOf(
                    navArgument("styleId") { type = NavType.StringType },
                    navArgument("guidelineType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val styleId = backStackEntry.arguments?.getString("styleId") ?: ""
                val guidelineName = backStackEntry.arguments?.getString("guidelineType") ?: GuidelineType.BJCP_2021.name
                val guidelineType = runCatching { GuidelineType.valueOf(guidelineName) }.getOrDefault(GuidelineType.BJCP_2021)

                val viewModel: StyleDetailViewModel = viewModel(
                    key = "${styleId}_$guidelineName",
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return StyleDetailViewModel(repository, hopRepository, maltRepository, honeyRepository, pomeRepository, yeastRepository, styleId, guidelineType) as T
                        }
                    }
                )

                val style by viewModel.style.collectAsState()
                val nextStyle by viewModel.nextStyle.collectAsState()
                val commonHops by viewModel.commonHops.collectAsState()
                val commonMalts by viewModel.commonMalts.collectAsState()
                val commonHoneys by viewModel.commonHoneys.collectAsState()
                val commonPomes by viewModel.commonPomes.collectAsState()
                val commonYeasts by viewModel.commonYeasts.collectAsState()

                style?.let { beerStyle ->
                    StyleDetailContent(
                        style = beerStyle,
                        onBackClick = { navController.popBackStack() },
                        onToggleFavorite = viewModel::toggleFavorite,
                        commonHops = commonHops,
                        onHopClick = { hopId ->
                            navController.navigate("hop_detail/$hopId")
                        },
                        commonMalts = commonMalts,
                        onMaltClick = { maltId ->
                            navController.navigate("malt_detail/$maltId")
                        },
                        commonHoneys = commonHoneys,
                        onHoneyClick = { honeyId ->
                            navController.navigate("honey_detail/$honeyId")
                        },
                        commonPomes = commonPomes,
                        onPomeClick = { pomeId ->
                            navController.navigate("pome_detail/$pomeId")
                        },
                        commonYeasts = commonYeasts,
                        onYeastClick = { yeastId ->
                            navController.navigate("yeast_detail/$yeastId")
                        },
                        nextStyle = nextStyle,
                        onSwipeForward = {
                            nextStyle?.let { next ->
                                navController.navigate("style_detail/${next.id}/${next.guidelineType.name}")
                            }
                        },
                        onSwipeBack = { navController.popBackStack() }
                    )
                }
            }

            composable(
                route = "hop_detail/{hopId}",
                arguments = listOf(
                    navArgument("hopId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val hopId = backStackEntry.arguments?.getString("hopId") ?: ""

                val viewModel: IngredientDetailViewModel<HopVariety> = viewModel(
                    key = "hop_$hopId",
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return IngredientDetailViewModel(
                                repository = hopRepository,
                                beerStyleRepository = repository,
                                itemId = hopId,
                                relatedItemsProvider = hopRepository::getChildHops
                            ) as T
                        }
                    }
                )

                HopDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onHopClick = { targetHopId ->
                        navController.navigate("hop_detail/$targetHopId")
                    },
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    }
                )
            }

            composable(
                route = "malt_detail/{maltId}",
                arguments = listOf(
                    navArgument("maltId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val maltId = backStackEntry.arguments?.getString("maltId") ?: ""

                val viewModel: IngredientDetailViewModel<MaltVariety> = viewModel(
                    key = "malt_$maltId",
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return IngredientDetailViewModel(maltRepository, repository, maltId) as T
                        }
                    }
                )

                MaltDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onMaltClick = { targetMaltId ->
                        navController.navigate("malt_detail/$targetMaltId")
                    },
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    }
                )
            }

            composable(
                route = "honey_detail/{honeyId}",
                arguments = listOf(
                    navArgument("honeyId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val honeyId = backStackEntry.arguments?.getString("honeyId") ?: ""

                val viewModel: IngredientDetailViewModel<HoneyVariety> = viewModel(
                    key = "honey_$honeyId",
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return IngredientDetailViewModel(honeyRepository, repository, honeyId) as T
                        }
                    }
                )

                HoneyDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onHoneyClick = { targetHoneyId ->
                        navController.navigate("honey_detail/$targetHoneyId")
                    },
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    }
                )
            }

            composable(
                route = "pome_detail/{pomeId}",
                arguments = listOf(
                    navArgument("pomeId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val pomeId = backStackEntry.arguments?.getString("pomeId") ?: ""

                val viewModel: IngredientDetailViewModel<PomeVariety> = viewModel(
                    key = "pome_$pomeId",
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return IngredientDetailViewModel(pomeRepository, repository, pomeId) as T
                        }
                    }
                )

                PomeDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onPomeClick = { targetPomeId ->
                        navController.navigate("pome_detail/$targetPomeId")
                    },
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    }
                )
            }

            composable(
                route = "yeast_detail/{yeastId}",
                arguments = listOf(
                    navArgument("yeastId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val yeastId = backStackEntry.arguments?.getString("yeastId") ?: ""

                val viewModel: IngredientDetailViewModel<YeastStrain> = viewModel(
                    key = "yeast_$yeastId",
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return IngredientDetailViewModel(yeastRepository, repository, yeastId) as T
                        }
                    }
                )

                YeastDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onYeastClick = { targetYeastId ->
                        navController.navigate("yeast_detail/$targetYeastId")
                    },
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    }
                )
            }
        }
    }
}
