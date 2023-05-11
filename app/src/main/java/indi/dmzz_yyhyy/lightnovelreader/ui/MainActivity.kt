package indi.dmzz_yyhyy.lightnovelreader.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import indi.dmzz_yyhyy.lightnovelreader.data.BookData
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBooksData
import indi.dmzz_yyhyy.lightnovelreader.ui.home.HomeContent
import indi.dmzz_yyhyy.lightnovelreader.ui.reader.ReaderActivity
import indi.dmzz_yyhyy.lightnovelreader.ui.theme.LightNovelReaderTheme

val LocalMainNavController = compositionLocalOf<NavController> {
    error("No NavController provided")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LightNovelReaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainContent()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContent() {
    for (book in LocalBooksData.booksDataList) {
        BookData.addBook(book)
    }
    BookData.setReadingBookList(LocalBooksData.booksDataList)

    LightNovelReaderTheme {
        val navController = rememberNavController()
        CompositionLocalProvider(LocalMainNavController provides navController) {
            println(navController)
            NavHost(
                navController = navController,
                startDestination = "home",
            ) {
                composable(route = "home") {
                    HomeContent()
                }
                composable(
                    route = "reader/{bookUID}",
                    arguments = listOf(navArgument("bookUID") { type = NavType.StringType })
                ) { backStackEntry ->
                    val bookUID = backStackEntry.arguments?.getString("bookUID")
                    ReaderActivity(bookUID)
                }

                composable(route = "hoome") {
                    HomeContent()
                }

            }
        }
    }
}