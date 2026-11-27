package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.CustomExploreContent
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.PagedBookList
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.explore.CustomExploreExpandedPage
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpandedPageScreen(
    exploreUiState: ExploreUiState,
    viewModel: ExpandedPageViewModel,
    checkConnection: () -> Unit,
    onClickBack: () -> Unit,
    onClickBook: (Identifier.Book) -> Unit,
    onClickExpand: (String) -> Unit,
    onClickSearch: () -> Unit,
    addToBookshelf: (Identifier.Book) -> Unit
) {
    val page = viewModel.page
    val state by viewModel.pageState.collectAsStateWithLifecycle()
    val topBar = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        page?.let { stringResource(R.string.nav_explore_child, viewModel.processTitle(it.title)) }
                            ?: stringResource(R.string.nav_explore),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(painterResource(R.drawable.arrow_back_24px), contentDescription = "back")
                    }
                },
                scrollBehavior = topBar
            )
        },
        snackbarHost = { SnackbarHost(LocalSnackbarHost.current) }
    ) { padding ->
        ExploreScreen(Modifier.padding(padding), checkConnection, exploreUiState) {
            when (page) {
                is ExploreExpandedPageDataSource -> PullToRefreshBox(
                    isRefreshing = state.loading && !state.canAppend,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize().nestedScroll(topBar.nestedScrollConnection)
                ) {
                    PagedBookList(
                        state = state,
                        filters = viewModel.filterDefinitions,
                        filterValues = { viewModel.filters },
                        updateFilters = viewModel::updateFilters,
                        append = viewModel::append,
                        retry = viewModel::retry,
                        bookshelfIds = viewModel.bookshelfIds,
                        processBook = viewModel::processBook,
                        onOpenBook = onClickBook,
                        addToBookshelf = addToBookshelf
                    )
                }

                is CustomExploreExpandedPage -> CustomExploreContent(
                    page,
                    ExplorePageContext(topBar.nestedScrollConnection, onClickBook, onClickExpand, onClickSearch)
                )

                null -> Text(stringResource(R.string.explore_no_results), Modifier.padding(16.dp))
            }
        }
    }
}
