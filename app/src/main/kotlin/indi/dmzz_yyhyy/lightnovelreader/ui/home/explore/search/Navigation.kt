package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToAddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.utils.activityHiltViewModel
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.exploreSearchDestination() {
    entry<Route.Main.Explore.Search> {
        val navigator = LocalNavigator.current
        val exploreViewModel = activityHiltViewModel<ExploreViewModel>()
        ExploreSearchScreen(
            exploreUiState = exploreViewModel.uiState,
            viewModel = hiltViewModel(),
            isActive = navigator.currentRoute == Route.Main.Explore.Search,
            checkConnection = exploreViewModel::refresh,
            onBack = { navigator.popBackStack() },
            onOpenBook = navigator::navigateToBookDetailDestination,
            addToBookshelf = { navigator.navigateToAddBookToBookshelfDialog(it) }
        )
    }
}

fun Navigator.navigateToSearchDestination() {
    navigate(Route.Main.Explore.Search)
}
