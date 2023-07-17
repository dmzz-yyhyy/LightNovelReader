package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import indi.dmzz_yyhyy.lightnovelreader.ui.reader.fragment.ChapterFragment
import indi.dmzz_yyhyy.lightnovelreader.ui.reader.fragment.ReaderFragment

@Composable
fun ReaderActivity(bookID: Int?) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "chapter",
    ) {
        composable(route = "chapter") {
            bookID?.let { it1 -> ChapterFragment(navController, it1) }
        }
        composable(route = "reader/{chapterIndex}",
            arguments = listOf(navArgument("chapterIndex") { type = NavType.IntType })) {  backStackEntry ->
            val chapterIndex = backStackEntry.arguments?.getInt("chapterIndex")
            ReaderFragment(navController, bookID, chapterIndex)
        }
    }
}


