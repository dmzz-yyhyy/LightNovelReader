package indi.dmzz_yyhyy.lightnovelreader.ui.home

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.Screen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.ReadingScreen

@Composable
fun HomeScreen(
    onOpenBook: (Int) -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.Reading.route) {
        composable(route = Screen.Home.route) {
            ReadingScreen(
                onOpenBook = onOpenBook
            )
        }
    }
}