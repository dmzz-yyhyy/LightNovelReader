package indi.dmzz_yyhyy.lightnovelreader.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import indi.dmzz_yyhyy.lightnovelreader.MainNavController
import indi.dmzz_yyhyy.lightnovelreader.data.BookContent
import indi.dmzz_yyhyy.lightnovelreader.ui.home.HomeContent
import indi.dmzz_yyhyy.lightnovelreader.ui.home.RouteConfig
import indi.dmzz_yyhyy.lightnovelreader.ui.home.fragment.BookcaseFragment
import indi.dmzz_yyhyy.lightnovelreader.ui.home.fragment.ReadingFragment
import indi.dmzz_yyhyy.lightnovelreader.ui.reader.ReaderActivity
import indi.dmzz_yyhyy.lightnovelreader.ui.theme.LightNovelReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LightNovelReaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HomeContent()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LightNovelReaderTheme {
        NavHost(
            navController = MainNavController.mainNavController,
            startDestination = "home",
        ) {
            composable(route = "home") {
                HomeContent()
            }
            composable(route = "reader/$(bookContent)", arguments = listOf(navArgument("bookContent") {})) {
                ReaderActivity(it.arguments?.get("bookContent") as BookContent)
            }
        }
    }
}