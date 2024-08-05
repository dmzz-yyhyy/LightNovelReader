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
        data object Bookshelf : Screen("home_bookshelf")
        data object Exploration : Screen("home_exploration") {
            data object Home : Screen("home_exploration_home")
            data object Search : Screen( "home_exploration_search")
            data object Expanded : Screen(
                route = "home_exploration_expanded/{expandedPageDataSourceId}",
                navArguments = listOf(
                    navArgument("expandedPageDataSourceId") { type = NavType.StringType },
                )
            ) {
                fun createRoute(expandedPageDataSourceId: String) = "home_exploration_expanded/${expandedPageDataSourceId}"
            }
        }
        data object Settings : Screen("home_settings")
    }
    data object Book : Screen(
        route = "book/{bookId}/{chapterId}",
        navArguments = listOf(
            navArgument("bookId") { type = NavType.IntType },
            navArgument("chapterId") { type = NavType.IntType }
        )
    ) {
        fun createRoute(bookId: Int) = "book/${bookId}/${-1}"
        fun createRoute(bookId: Int, chapterId: Int) = "book/${bookId}/${chapterId}"

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
