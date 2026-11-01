package com.bc.beerworks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.ui.categorydetail.CategoryDetailContent
import com.bc.beerworks.ui.categorydetail.CategoryDetailViewModel
import com.bc.beerworks.ui.components.AppDrawer
import com.bc.beerworks.ui.components.DrawerDestination
import com.bc.beerworks.ui.hops.HopsContent
import com.bc.beerworks.ui.malt.MaltContent
import com.bc.beerworks.ui.reviewhistory.ReviewHistoryContent
import com.bc.beerworks.ui.settings.SettingsContent
import com.bc.beerworks.ui.settings.SettingsViewModel
import com.bc.beerworks.ui.styledetail.StyleDetailContent
import com.bc.beerworks.ui.styledetail.StyleDetailViewModel
import com.bc.beerworks.ui.stylelist.StyleListContent
import com.bc.beerworks.ui.stylelist.StyleListViewModel
import com.bc.beerworks.ui.theme.BeerworksTheme
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
    val database = remember { BeerworksDatabase.getInstance(context) }
    val prefsManager = remember { DefaultPreferencesManager(context) }
    val repository = remember { BeerStyleRepository(database, prefsManager) }

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

                StyleListContent(
                    groupedStyles = groupedStyles,
                    expandedCategoryNumber = expandedCategoryNumber,
                    criteria = criteria,
                    categories = categories,
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
                HopsContent(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            composable("malt") {
                MaltContent(
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
                val viewModel: SettingsViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return SettingsViewModel(repository) as T
                        }
                    }
                )

                val selectedGuideline by viewModel.selectedGuideline.collectAsState()

                SettingsContent(
                    selectedGuideline = selectedGuideline,
                    onGuidelineChange = viewModel::onGuidelineChange,
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
                            return StyleDetailViewModel(repository, styleId, guidelineType) as T
                        }
                    }
                )

                val style by viewModel.style.collectAsState()

                style?.let { beerStyle ->
                    StyleDetailContent(
                        style = beerStyle,
                        onBackClick = { navController.popBackStack() },
                        onToggleFavorite = viewModel::toggleFavorite
                    )
                }
            }
        }
    }
}
