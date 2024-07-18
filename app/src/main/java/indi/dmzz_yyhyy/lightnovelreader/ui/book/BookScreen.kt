package indi.dmzz_yyhyy.lightnovelreader.ui.book

import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.Screen
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.DetailScreen

@Composable
fun BookScreen(
    onClickBackButton: () -> Unit,
    id: Int) {
    val navController = rememberNavController()
    var topBar : @Composable () -> Unit by remember { mutableStateOf(@Composable {}) }
    var dialog : @Composable () -> Unit by remember { mutableStateOf(@Composable {}) }
    Scaffold(
        topBar = topBar
    ) { it ->
        NavHost(
            modifier = Modifier.padding(it),
            navController = navController,
            startDestination = Screen.Book.Detail.route
        ) {
            composable(
                route = Screen.Book.Detail.route,
                arguments = Screen.Book.Detail.navArguments
            ) {
                DetailScreen(
                    onClickBackButton = onClickBackButton,
                    topBar = {newTopBar ->
                        topBar = newTopBar
                    },
                    dialog = {newdialog ->
                        dialog = newdialog
                    }
                )
            }
        }
        dialog()
    }
}