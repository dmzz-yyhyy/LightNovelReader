package indi.dmzz_yyhyy.lightnovelreader.ui.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.bookshelfNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.exploreNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.readingNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.settingsNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.homeNavigation(sharedTransitionScope: SharedTransitionScope) {
    navigation<Route.Main>(
        startDestination = Route.Main.Reading
    ) {
        readingNavigation(sharedTransitionScope)
        exploreNavigation()
        bookshelfNavigation(sharedTransitionScope)
        settingsNavigation()
    }
}

@Suppress("unused")
fun NavController.navigateToHomeNavigation() {
    navigate(Route.Main)
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun HomeNavigateBar(
    selectedRoute: Any?,
    controller: NavController,
) {
    fun <T: Any> NavController.navigateSingleTopTo(route: T) {
        navigate(route) {
            launchSingleTop = true
            restoreState = true
            val start = graph.findStartDestination().id
            popUpTo(start) { saveState = true }
        }
    }

    val isReading = selectedRoute is Route.Main.Reading
    val isBookshelf = selectedRoute is Route.Main.Bookshelf
    val isExploration = selectedRoute is Route.Main.Explore
    val isSettings = selectedRoute is Route.Main.Settings

    val avdReading = AnimatedImageVector.animatedVectorResource(R.drawable.animated_book)
    val avdShelf = AnimatedImageVector.animatedVectorResource(R.drawable.animated_bookshelf)
    val avdExplore = AnimatedImageVector.animatedVectorResource(R.drawable.animated_explore)
    val avdSettings = AnimatedImageVector.animatedVectorResource(R.drawable.animated_settings)


    NavigationBar(
        windowInsets = WindowInsets.systemBars.only(
            WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
        )
    ) {
        NavigationBarItem(
            selected = isReading,
            onClick = { controller.navigateSingleTopTo(Route.Main.Reading) },
            icon = { Icon(painter = rememberAnimatedVectorPainter(avdReading, isReading), null) },
            label = { Text(stringResource(R.string.nav_reading), maxLines = 1) }
        )
        NavigationBarItem(
            selected = isBookshelf,
            onClick = { controller.navigateSingleTopTo(Route.Main.Bookshelf) },
            icon = { Icon(painter = rememberAnimatedVectorPainter(avdShelf, isBookshelf), null) },
            label = { Text(stringResource(R.string.nav_bookshelf), maxLines = 1) }
        )
        NavigationBarItem(
            selected = isExploration,
            onClick = { controller.navigateSingleTopTo(Route.Main.Explore) },
            icon = { Icon(painter = rememberAnimatedVectorPainter(avdExplore, isExploration), null) },
            label = { Text(stringResource(R.string.nav_explore), maxLines = 1) }
        )
        NavigationBarItem(
            selected = isSettings,
            onClick = { controller.navigateSingleTopTo(Route.Main.Settings) },
            icon = { Icon(painter = rememberAnimatedVectorPainter(avdSettings, isSettings), null) },
            label = { Text(stringResource(R.string.nav_settings), maxLines = 1) }
        )
    }
}