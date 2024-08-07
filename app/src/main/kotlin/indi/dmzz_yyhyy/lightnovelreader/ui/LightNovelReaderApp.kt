package indi.dmzz_yyhyy.lightnovelreader.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.book.BookScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.HomeScreen

@Composable
fun LightNovelReaderApp() {
    val navController = rememberNavController()
    LightNovelReaderNavHost(navController)
}

@Composable
fun LightNovelReaderNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onClickBook = {
                    navController.navigate(Screen.Book.createRoute(it))
                },
                onClickContinueReading = { bookId, chapterId ->
                    navController.navigate(Screen.Book.createRoute(bookId, chapterId))
                }
            )
        }
        composable(
            route = Screen.Book.route,
            arguments = Screen.Book.navArguments
        ) {
            it.arguments?.let { it1 ->
                BookScreen(
                    onClickBackButton = { navController.popBackStack() },
                    bookId = it1.getInt("bookId"),
                    chapterId = it1.getInt("chapterId"),
            ) }
        }
    }
}