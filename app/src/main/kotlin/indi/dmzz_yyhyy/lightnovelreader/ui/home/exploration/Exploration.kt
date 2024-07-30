package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.Screen
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.NavItem
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home.ExplorationHomeScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home.ExplorationHomeViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.search.ExplorationSearchScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.search.ExplorationSearchViewModel


val ExplorationScreenInfo = NavItem (
    route = Screen.Home.Exploration.route,
    drawable = R.drawable.animated_exploration,
    label = R.string.nav_exploration
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Exploration(
    topBar: (@Composable (TopAppBarScrollBehavior, TopAppBarScrollBehavior) -> Unit) -> Unit,
    onClickBook: (Int) -> Unit,
    explorationViewModel: ExplorationViewModel = hiltViewModel(),
    explorationHomeViewModel: ExplorationHomeViewModel = hiltViewModel(),
    explorationSearchViewModel: ExplorationSearchViewModel = hiltViewModel(),
    ) {
    val navController = rememberNavController()
    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        explorationViewModel.init()
    }
    AnimatedVisibility(
        visible = explorationViewModel.uiState.isOffLine,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        EmptyPage(
            painter = painterResource(R.drawable.wifi_off_90dp),
            title = "网络未连接",
            description = "请检查你的网络连接与代理"
        )
    }
    AnimatedVisibility(
        visible = !explorationViewModel.uiState.isOffLine,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        NavHost(navController, startDestination = Screen.Home.Exploration.Home.route) {
            composable(route = Screen.Home.Exploration.Home.route) {
                ExplorationHomeScreen(
                    topBar = topBar,
                    onClickBook = onClickBook,
                    uiState = explorationHomeViewModel.uiState,
                    init = { explorationHomeViewModel.init() },
                    changePage = { explorationHomeViewModel.changePage(it) },
                    onClickSearch = { navController.navigate(Screen.Home.Exploration.Search.route) }
                )
            }
            composable(route = Screen.Home.Exploration.Search.route) {
                ExplorationSearchScreen(
                    topBar = topBar,
                    onCLickBack = { navController.popBackStack() },
                    init = { explorationSearchViewModel.init() },
                    onChangeSearchType = { explorationSearchViewModel.changeSearchType(it) },
                    onSearch = { explorationSearchViewModel.search(it) },
                    onClickDeleteHistory = { explorationSearchViewModel.deleteHistory(it) },
                    onClickClearAllHistory = { explorationSearchViewModel.clearAllHistory() },
                    onClickBook = onClickBook,
                    uiState = explorationSearchViewModel.uiState
                )
            }
        }
    }
}