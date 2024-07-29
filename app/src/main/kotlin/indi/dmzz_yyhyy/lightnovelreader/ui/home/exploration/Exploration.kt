package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.Screen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home.ExplorationHomeScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home.ExplorationHomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Exploration(
    topBar: (@Composable (TopAppBarScrollBehavior, TopAppBarScrollBehavior) -> Unit) -> Unit,
    onClickBook: (Int) -> Unit,
    explorationHomeViewModel: ExplorationHomeViewModel = hiltViewModel(),
    ) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screen.Home.Exploration.Home.route) {
        composable(route = Screen.Home.Exploration.Home.route) {
            ExplorationHomeScreen(
                topBar = topBar,
                onClickBook = onClickBook,
                explorationHomeUiState = explorationHomeViewModel.uiState,
                init = { explorationHomeViewModel.init() },
                changePage = { explorationHomeViewModel.changePage(it) }
            )
        }
        composable(route = Screen.Home.Exploration.Search.route) {

        }
    }
}