package indi.dmzz_yyhyy.lightnovelreader.ui

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList()
) {
    data object Home : Screen("home") {
        data object Reading : Screen("home_reading")
        data object Exploration : Screen("home_exploration")
        data object Bookshelf : Screen("home_bookshelf")
        data object Settings : Screen("home_settings")
    }
    data object Book : Screen(
        route = "book/{bookId}",
        navArguments = listOf(navArgument("bookId") {
            type = NavType.IntType
        })
    ) {
        fun createRoute(bookId: Int) = "book/${bookId}"

        data object Detail: Screen("detail")
        data object Content : Screen(
            route = "book_content/{chapterId}",
            navArguments = listOf(navArgument("chapterId") {
                type = NavType.IntType
            })
        ) {
            fun createRoute(chapterId: Int) = "book_content/${chapterId}"
        }
    }
}
