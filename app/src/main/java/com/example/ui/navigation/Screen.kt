package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object MyList : Screen("my_list")
    data object History : Screen("history")
    data object Profile : Screen("profile")
    data object Detail : Screen("detail/{seriesId}") {
        fun createRoute(seriesId: String) = "detail/$seriesId"
    }
    data object Player : Screen("player/{seriesId}/{episodeNumber}") {
        fun createRoute(seriesId: String, episodeNumber: Int) = "player/$seriesId/$episodeNumber"
    }
}
