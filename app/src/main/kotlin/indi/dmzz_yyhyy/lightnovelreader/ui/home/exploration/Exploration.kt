package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.ui.Screen
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.NavItem
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.expanded.ExpandedPageScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.expanded.ExpandedPageViewModel
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
    dialog: (@Composable () -> Unit) -> Unit,
    onClickBook: (Int) -> Unit,
    explorationViewModel: ExplorationViewModel = hiltViewModel(),
    explorationHomeViewModel: ExplorationHomeViewModel = hiltViewModel(),
    explorationSearchViewModel: ExplorationSearchViewModel = hiltViewModel(),
    expandedPageViewModel: ExpandedPageViewModel = hiltViewModel()
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
                    onClickExpand = { navController.navigate(Screen.Home.Exploration.Expanded.createRoute(it)) },
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
                    init = explorationSearchViewModel::init,
                    onChangeSearchType = { explorationSearchViewModel.changeSearchType(it) },
                    onSearch = { explorationSearchViewModel.search(it) },
                    onClickDeleteHistory = { explorationSearchViewModel.deleteHistory(it) },
                    onClickClearAllHistory = explorationSearchViewModel::clearAllHistory,
                    onClickBook = onClickBook,
                    uiState = explorationSearchViewModel.uiState
                )
            }
            composable(
                route = Screen.Home.Exploration.Expanded.route,
                arguments = Screen.Home.Exploration.Expanded.navArguments
            ) { navBackStackEntry ->
                navBackStackEntry.arguments?.getString("expandedPageDataSourceId")?.let { expandedPageDataSourceId ->
                    ExpandedPageScreen(
                        topBar = topBar,
                        dialog = dialog,
                        expandedPageDataSourceId = expandedPageDataSourceId,
                        uiState = expandedPageViewModel.uiState,
                        init = { expandedPageViewModel.init(it) },
                        loadMore = expandedPageViewModel::loadMore,
                        onClickBack = navController::popBackStack,
                        onClickBook = onClickBook
                    )
                }
            }
        }
    }
}

@Composable
fun ExplorationBookCard(
    modifier: Modifier = Modifier,
    bookInformation: BookInformation,
    onClickBook: (Int) -> Unit
) {
    Row(
        modifier = modifier
            .height(125.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClickBook(bookInformation.id)
            }
    ) {
        Cover(
            width = 82.dp,
            height = 125.dp,
            url = bookInformation.coverUrl,
            rounded = 8.dp
        )
        Column (
            modifier = Modifier.fillMaxWidth().padding(8.dp, 5.dp, 14.dp, 5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = bookInformation.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.W700,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    maxLines = 2
                )
                Box(modifier = Modifier.weight(2f))
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_bookmark_24px),
                        contentDescription = "mark"
                    )
                }
            }
            Text(
                text =
                "作者: ${bookInformation.author} / 文库: ${bookInformation.publishingHouse}\n" +
                        "更新: ${bookInformation.lastUpdated.year}-${bookInformation.lastUpdated.month}-${bookInformation.lastUpdated.dayOfMonth}" +
                        " / 数字: ${bookInformation.wordCount}" +
                        " / ${if (bookInformation.isComplete) "已完结" else "连载中"}\n" +
                        "简介: ${bookInformation.description}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.W700,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}