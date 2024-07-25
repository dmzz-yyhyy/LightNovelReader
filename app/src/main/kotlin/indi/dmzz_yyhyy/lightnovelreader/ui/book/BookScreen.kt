package indi.dmzz_yyhyy.lightnovelreader.ui.book

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.Screen
import indi.dmzz_yyhyy.lightnovelreader.ui.book.content.ContentScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.DetailScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    onClickBackButton: () -> Unit,
    id: Int) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val navController = rememberNavController()
    var topBar : @Composable () -> Unit by remember { mutableStateOf(@Composable {}) }
    var bottomBar : @Composable () -> Unit by remember { mutableStateOf(@Composable {}) }
    var dialog : @Composable () -> Unit by remember { mutableStateOf(@Composable {}) }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = topBar,
        bottomBar = bottomBar
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = Screen.Book.Detail.route
        ) {
            composable(
                route = Screen.Book.Detail.route,
                arguments = Screen.Book.Detail.navArguments
            ) {
                DetailScreen(
                    onClickChapter = {
                        navController.navigate(Screen.Book.Content.createRoute(it))
                    },
                    onClickBackButton = onClickBackButton,
                    topBar = { newTopBar -> topBar = newTopBar },
                    dialog = { newDialog -> dialog = newDialog },
                    scrollBehavior = scrollBehavior,
                    id = id,
                )
                bottomBar = {}
            }
            composable(
                route = Screen.Book.Content.route,
                arguments = Screen.Book.Content.navArguments
            ) { navBackStackEntry ->
                navBackStackEntry.arguments?.let {
                    ContentScreen(
                        onClickBackButton = {
                            navController.popBackStack()
                        },
                        topBar = {newTopBar ->
                            topBar = newTopBar
                        },
                        bottomBar = {newbottomBar ->
                            bottomBar = newbottomBar
                        },
                        id,
                        it.getInt("chapterId")
                    )
                }
            }
        }
        dialog()
    }
}