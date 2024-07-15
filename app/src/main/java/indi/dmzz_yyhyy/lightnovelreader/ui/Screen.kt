package indi.dmzz_yyhyy.lightnovelreader.ui

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList()
) {
    data object Home : Screen("home") {
        data object Reading : Screen("home/reading")
        data object Exploration : Screen("home/exploration")
        data object Bookshelf : Screen("home/bookshelf")
        data object Settings : Screen("home/settings")
    }
    data object Book {
        data object Detail : Screen(
            route = "detail/{bookId}",
            navArguments = listOf(navArgument("bookId") {
                type = NavType.IntType
            })
        ) {
            fun createRoute(bookId: Int) = "detail/${bookId}"
        }
        data object Content : Screen(
            route = "content/content/{chapterId}",
            navArguments = listOf(navArgument("chapterId") {
                type = NavType.IntType
            })
        ) {
            fun createRoute(chapterId: Int) = "content/${chapterId}"
        }
    }
}
