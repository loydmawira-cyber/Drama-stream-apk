package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.di.ServiceLocator
import com.example.ui.detail.DetailViewModel
import com.example.ui.detail.SeriesDetailScreen
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.history.HistoryScreen
import com.example.ui.history.HistoryViewModel
import com.example.ui.player.PlayerViewModel
import com.example.ui.player.VerticalPlayerScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.profile.ProfileViewModel
import com.example.ui.search.SearchScreen
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.DramaSurface
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun DramaNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val factory = ServiceLocator.provideViewModelFactory(context)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom navigation bar on player screen
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.MyList.route,
        Screen.History.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NavigationBar(
                    containerColor = DramaSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val navItems = listOf(
                        Triple(Screen.Home.route, "Home", Icons.Default.Home),
                        Triple(Screen.Search.route, "Search", Icons.Default.Search),
                        Triple(Screen.MyList.route, "My List", Icons.Default.Bookmark),
                        Triple(Screen.History.route, "History", Icons.Default.History),
                        Triple(Screen.Profile.route, "Profile", Icons.Default.Person)
                    )

                    navItems.forEach { (route, label, icon) ->
                        val isSelected = currentRoute == route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PinkAccent,
                                selectedTextColor = PinkAccent,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = Color(0x33EC4899)
                            ),
                            modifier = Modifier.testTag("nav_item_$label")
                        )
                    }
                }
            }
        },
        containerColor = DramaBlack,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(factory = factory)
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToDetail = { seriesId ->
                        navController.navigate(Screen.Detail.createRoute(seriesId))
                    },
                    onNavigateToPlayer = { seriesId, episodeNumber ->
                        navController.navigate(Screen.Player.createRoute(seriesId, episodeNumber))
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }

            composable(Screen.Search.route) {
                val homeViewModel: HomeViewModel = viewModel(factory = factory)
                SearchScreen(
                    viewModel = homeViewModel,
                    onNavigateToDetail = { seriesId ->
                        navController.navigate(Screen.Detail.createRoute(seriesId))
                    }
                )
            }

            composable(Screen.MyList.route) {
                val profileViewModel: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToPlayer = { seriesId, epNum ->
                        navController.navigate(Screen.Player.createRoute(seriesId, epNum))
                    }
                )
            }

            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = viewModel(factory = factory)
                HistoryScreen(
                    viewModel = historyViewModel,
                    onNavigateToPlayer = { seriesId, epNum ->
                        navController.navigate(Screen.Player.createRoute(seriesId, epNum))
                    }
                )
            }

            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToPlayer = { seriesId, epNum ->
                        navController.navigate(Screen.Player.createRoute(seriesId, epNum))
                    }
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("seriesId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val seriesId = backStackEntry.arguments?.getString("seriesId") ?: ""
                val detailViewModel: DetailViewModel = viewModel(factory = factory)
                SeriesDetailScreen(
                    seriesId = seriesId,
                    viewModel = detailViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = { sId, epNum ->
                        navController.navigate(Screen.Player.createRoute(sId, epNum))
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }

            composable(
                route = Screen.Player.route,
                arguments = listOf(
                    navArgument("seriesId") { type = NavType.StringType },
                    navArgument("episodeNumber") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val seriesId = backStackEntry.arguments?.getString("seriesId") ?: ""
                val episodeNumber = backStackEntry.arguments?.getInt("episodeNumber") ?: 1
                val playerViewModel: PlayerViewModel = viewModel(factory = factory)
                VerticalPlayerScreen(
                    seriesId = seriesId,
                    initialEpisodeNumber = episodeNumber,
                    viewModel = playerViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }
        }
    }
}
