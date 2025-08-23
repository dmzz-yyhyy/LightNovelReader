package indi.dmzz_yyhyy.lightnovelreader.ui.extensions

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.browse.browseNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.home.extensionsHomeNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.installed.installedNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.repositories.repositoriesNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.fadeEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.fadeExit
import indi.dmzz_yyhyy.lightnovelreader.utils.fadePopEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.fadePopExit
import indi.dmzz_yyhyy.lightnovelreader.utils.isInMainNavigation

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.extensionsNavigation(sharedTransitionScope: SharedTransitionScope) {
    navigation<Route.Main.Extensions>(
        startDestination = Route.Main.Extensions.Home,
        enterTransition = {
            if (isInMainNavigation(initialState.destination, targetState.destination)) fadeEnter()
            else null
        },
        exitTransition = {
            fadeExit()
        },
        popEnterTransition = {
            if (isInMainNavigation(initialState.destination, targetState.destination)) fadePopEnter()
            else null
        },
        popExitTransition = {
            if (isInMainNavigation(initialState.destination, targetState.destination)) fadePopExit()
            else null
        }
    ) {
        extensionsHomeNavigation(sharedTransitionScope)
        repositoriesNavigation(sharedTransitionScope)
        browseNavigation(sharedTransitionScope)
        installedNavigation(sharedTransitionScope)
    }
}
