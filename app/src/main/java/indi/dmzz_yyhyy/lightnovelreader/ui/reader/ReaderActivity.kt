package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import indi.dmzz_yyhyy.lightnovelreader.ui.reader.fragment.ChapterFragment
import indi.dmzz_yyhyy.lightnovelreader.ui.reader.fragment.ReaderFragment

@Composable
fun ReaderActivity(bookUID: String?) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "chapter",
    ) {
        composable(route = "chapter") {
            ChapterFragment(navController, bookUID)
        }
        composable(route = "reader/{chapterIndex}",
            arguments = listOf(navArgument("chapterIndex") { type = NavType.IntType })) {  backStackEntry ->
            val chapterIndex = backStackEntry.arguments?.getInt("chapterIndex")
            ReaderFragment(navController, bookUID, chapterIndex)
        }
    }
}

