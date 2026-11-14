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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bc.beerworks.data.local.DatabaseInitializer
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HoneyVariety
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.model.MaltVariety
import com.bc.beerworks.data.model.PomeVariety
import com.bc.beerworks.data.model.YeastStrain
import com.bc.beerworks.ui.categorydetail.CategoryDetailScreen
import com.bc.beerworks.ui.categorydetail.CategoryDetailViewModel
import com.bc.beerworks.ui.components.AppDrawer
import com.bc.beerworks.ui.components.DrawerDestination
import com.bc.beerworks.ui.favorites.FavoritesScreen
import com.bc.beerworks.ui.favorites.FavoritesViewModel
import com.bc.beerworks.ui.honey.HoneyScreen
import com.bc.beerworks.ui.honey.HoneyViewModel
import com.bc.beerworks.ui.honeydetail.HoneyDetailScreen
import com.bc.beerworks.ui.hopdetail.HopDetailScreen
import com.bc.beerworks.ui.hops.HopsScreen
import com.bc.beerworks.ui.hops.HopsViewModel
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailViewModel
import com.bc.beerworks.ui.malt.MaltScreen
import com.bc.beerworks.ui.malt.MaltViewModel
import com.bc.beerworks.ui.maltdetail.MaltDetailScreen
import com.bc.beerworks.ui.pomedetail.PomeDetailScreen
import com.bc.beerworks.ui.pomes.PomesScreen
import com.bc.beerworks.ui.pomes.PomesViewModel
import com.bc.beerworks.ui.reviewhistory.ReviewHistoryContent
import com.bc.beerworks.ui.settings.SettingsScreen
import com.bc.beerworks.ui.settings.SettingsViewModel
import com.bc.beerworks.ui.styledetail.StyleDetailScreen
import com.bc.beerworks.ui.styledetail.StyleDetailViewModel
import com.bc.beerworks.ui.stylelist.StyleListScreen
import com.bc.beerworks.ui.stylelist.StyleListViewModel
import com.bc.beerworks.ui.theme.BeerworksTheme
import com.bc.beerworks.ui.yeastdetail.YeastDetailScreen
import com.bc.beerworks.ui.yeasts.YeastsScreen
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
    val container = remember { AppContainer(context) }

    val keepScreenOn by container.prefsManager.keepScreenOn.collectAsState(initial = false)
    val selectedGuideline by container.beerStyleRepository.selectedGuideline.collectAsState(
        initial = GuidelineType.BJCP_2021
    )

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
        DatabaseInitializer.initializeIfNeeded(context, container.database)
    }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: DrawerDestination.StyleGuide.route
    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                selectedGuideline = selectedGuideline,
                keepScreenOn = keepScreenOn,
                onKeepScreenOnChange = { enabled ->
                    scope.launch { container.prefsManager.setKeepScreenOn(enabled) }
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
                    factory = viewModelFactory { StyleListViewModel(container.beerStyleRepository) }
                )
                StyleListScreen(
                    viewModel = viewModel,
                    onCategoryClick = { group ->
                        navController.navigate("category_detail/${group.categoryNumber}/${group.guidelineType.name}")
                    },
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    },
                    onMenuClick = openDrawer
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
                    factory = viewModelFactory {
                        CategoryDetailViewModel(container.beerStyleRepository, categoryNumber, guidelineType)
                    }
                )
                CategoryDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    }
                )
            }

            composable("hops") {
                val viewModel: HopsViewModel = viewModel(
                    factory = viewModelFactory { HopsViewModel(container.hopRepository, container.beerStyleRepository) }
                )
                HopsScreen(
                    viewModel = viewModel,
                    onHopClick = { hop -> navController.navigate("hop_detail/${hop.id}") },
                    onMenuClick = openDrawer
                )
            }

            composable("malt") {
                val viewModel: MaltViewModel = viewModel(
                    factory = viewModelFactory { MaltViewModel(container.maltRepository, container.beerStyleRepository) }
                )
                MaltScreen(
                    viewModel = viewModel,
                    onMaltClick = { maltId -> navController.navigate("malt_detail/$maltId") },
                    onMenuClick = openDrawer
                )
            }

            composable("honey") {
                val viewModel: HoneyViewModel = viewModel(
                    factory = viewModelFactory { HoneyViewModel(container.honeyRepository) }
                )
                HoneyScreen(
                    viewModel = viewModel,
                    onHoneyClick = { honeyId -> navController.navigate("honey_detail/$honeyId") },
                    onMenuClick = openDrawer
                )
            }

            composable("pomes") {
                val viewModel: PomesViewModel = viewModel(
                    factory = viewModelFactory { PomesViewModel(container.pomeRepository) }
                )
                PomesScreen(
                    viewModel = viewModel,
                    onPomeClick = { pomeId -> navController.navigate("pome_detail/$pomeId") },
                    onMenuClick = openDrawer
                )
            }

            composable("yeasts") {
                val viewModel: YeastsViewModel = viewModel(
                    factory = viewModelFactory { YeastsViewModel(container.yeastRepository, container.prefsManager) }
                )
                YeastsScreen(
                    viewModel = viewModel,
                    onYeastClick = { yeastId -> navController.navigate("yeast_detail/$yeastId") },
                    onMenuClick = openDrawer
                )
            }

            composable("favorites") {
                val viewModel: FavoritesViewModel = viewModel(
                    factory = viewModelFactory {
                        FavoritesViewModel(
                            beerStyleRepository = container.beerStyleRepository,
                            ingredientCatalogRepository = container.ingredientCatalogRepository
                        )
                    }
                )
                FavoritesScreen(
                    viewModel = viewModel,
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    },
                    onHopClick = { hopId -> navController.navigate("hop_detail/$hopId") },
                    onMaltClick = { maltId -> navController.navigate("malt_detail/$maltId") },
                    onHoneyClick = { honeyId -> navController.navigate("honey_detail/$honeyId") },
                    onPomeClick = { pomeId -> navController.navigate("pome_detail/$pomeId") },
                    onYeastClick = { yeastId -> navController.navigate("yeast_detail/$yeastId") },
                    onMenuClick = openDrawer
                )
            }

            composable("review_history") {
                ReviewHistoryContent(onMenuClick = openDrawer)
            }

            composable("settings") {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = viewModelFactory { SettingsViewModel(container.beerStyleRepository) }
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onMenuClick = openDrawer
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
                    factory = viewModelFactory {
                        StyleDetailViewModel(
                            repository = container.beerStyleRepository,
                            ingredientCatalogRepository = container.ingredientCatalogRepository,
                            styleId = styleId,
                            guidelineType = guidelineType
                        )
                    }
                )
                StyleDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onHopClick = { hopId -> navController.navigate("hop_detail/$hopId") },
                    onMaltClick = { maltId -> navController.navigate("malt_detail/$maltId") },
                    onHoneyClick = { honeyId -> navController.navigate("honey_detail/$honeyId") },
                    onPomeClick = { pomeId -> navController.navigate("pome_detail/$pomeId") },
                    onYeastClick = { yeastId -> navController.navigate("yeast_detail/$yeastId") },
                    onNavigateToStyle = { next ->
                        navController.navigate("style_detail/${next.id}/${next.guidelineType.name}")
                    }
                )
            }

            composable(
                route = "hop_detail/{hopId}",
                arguments = listOf(navArgument("hopId") { type = NavType.StringType })
            ) { backStackEntry ->
                val hopId = backStackEntry.arguments?.getString("hopId") ?: ""
                val viewModel: IngredientDetailViewModel<HopVariety> = viewModel(
                    key = "hop_$hopId",
                    factory = viewModelFactory {
                        IngredientDetailViewModel(
                            repository = container.hopRepository,
                            beerStyleRepository = container.beerStyleRepository,
                            itemId = hopId,
                            relatedItemsProvider = container.hopRepository::getChildHops
                        )
                    }
                )
                HopDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onHopClick = { targetHopId -> navController.navigate("hop_detail/$targetHopId") },
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    }
                )
            }

            composable(
                route = "malt_detail/{maltId}",
                arguments = listOf(navArgument("maltId") { type = NavType.StringType })
            ) { backStackEntry ->
                val maltId = backStackEntry.arguments?.getString("maltId") ?: ""
                val viewModel: IngredientDetailViewModel<MaltVariety> = viewModel(
                    key = "malt_$maltId",
                    factory = viewModelFactory {
                        IngredientDetailViewModel(container.maltRepository, container.beerStyleRepository, maltId)
                    }
                )
                MaltDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onMaltClick = { targetMaltId -> navController.navigate("malt_detail/$targetMaltId") },
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    }
                )
            }

            composable(
                route = "honey_detail/{honeyId}",
                arguments = listOf(navArgument("honeyId") { type = NavType.StringType })
            ) { backStackEntry ->
                val honeyId = backStackEntry.arguments?.getString("honeyId") ?: ""
                val viewModel: IngredientDetailViewModel<HoneyVariety> = viewModel(
                    key = "honey_$honeyId",
                    factory = viewModelFactory {
                        IngredientDetailViewModel(container.honeyRepository, container.beerStyleRepository, honeyId)
                    }
                )
                HoneyDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onHoneyClick = { targetHoneyId -> navController.navigate("honey_detail/$targetHoneyId") },
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    }
                )
            }

            composable(
                route = "pome_detail/{pomeId}",
                arguments = listOf(navArgument("pomeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val pomeId = backStackEntry.arguments?.getString("pomeId") ?: ""
                val viewModel: IngredientDetailViewModel<PomeVariety> = viewModel(
                    key = "pome_$pomeId",
                    factory = viewModelFactory {
                        IngredientDetailViewModel(container.pomeRepository, container.beerStyleRepository, pomeId)
                    }
                )
                PomeDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onPomeClick = { targetPomeId -> navController.navigate("pome_detail/$targetPomeId") },
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    }
                )
            }

            composable(
                route = "yeast_detail/{yeastId}",
                arguments = listOf(navArgument("yeastId") { type = NavType.StringType })
            ) { backStackEntry ->
                val yeastId = backStackEntry.arguments?.getString("yeastId") ?: ""
                val viewModel: IngredientDetailViewModel<YeastStrain> = viewModel(
                    key = "yeast_$yeastId",
                    factory = viewModelFactory {
                        IngredientDetailViewModel(container.yeastRepository, container.beerStyleRepository, yeastId)
                    }
                )
                YeastDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onYeastClick = { targetYeastId -> navController.navigate("yeast_detail/$targetYeastId") },
                    onStyleClick = { style ->
                        navController.navigate("style_detail/${style.id}/${style.guidelineType.name}")
                    }
                )
            }
        }
    }
}
