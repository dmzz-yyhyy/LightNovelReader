package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import indi.dmzz_yyhyy.lightnovelreader.ui.reader.fragment.ChapterFragment
import indi.dmzz_yyhyy.lightnovelreader.ui.reader.fragment.ReaderFragment
import indi.dmzz_yyhyy.lightnovelreader.ui.theme.LightNovelReaderTheme


class ReaderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val bookId = intent.getIntExtra("bookId", 0)
        val navController = rememberNavController()
        setContent {
            LightNovelReaderTheme {
        NavHost(
            navController = navController,
            startDestination = "chapter",
        ) {
            composable(route = "chapter") {
                bookId?.let { it1 -> ChapterFragment(navController, it1) }
            }
            composable(route = "reader/{chapterIndex}",
                arguments = listOf(navArgument("chapterIndex") { type = NavType.IntType })) {  backStackEntry ->
                val chapterIndex = backStackEntry.arguments?.getInt("chapterIndex")
                ReaderFragment(navController, bookId, chapterIndex)
            }
        }
    }}}
}


