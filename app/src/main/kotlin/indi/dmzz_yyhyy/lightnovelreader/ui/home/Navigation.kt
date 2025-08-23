package indi.dmzz_yyhyy.lightnovelreader.ui.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.bookshelfNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.explorationNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.readingNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.settingsNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.extensions.extensionsNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.fadeEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.fadeExit
import indi.dmzz_yyhyy.lightnovelreader.utils.fadePopEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.fadePopExit
import indi.dmzz_yyhyy.lightnovelreader.utils.isInMainNavigation

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.homeNavigation(sharedTransitionScope: SharedTransitionScope) {
    navigation<Route.Main>(
        startDestination = Route.Main.Reading,
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
        readingNavigation(sharedTransitionScope)
        explorationNavigation(sharedTransitionScope)
        bookshelfNavigation(sharedTransitionScope)
        settingsNavigation(sharedTransitionScope)
        extensionsNavigation(sharedTransitionScope)
    }
}

@Suppress("unused")
fun NavController.navigateToHomeNavigation() {
    navigate(Route.Main)
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun HomeNavigateBar(
    modifier: Modifier = Modifier,
    selectedRoute: Any,
    controller: NavController,
) {
    fun <T : Any> coverNavigate(route: T) {
        controller.popBackStack()
        controller.navigate(route)
    }

    NavigationBar(modifier) {
        NavigationBarItem(
            selected = selectedRoute is Route.Main.Reading,
            onClick = {
                if (selectedRoute !is Route.Main.Reading) coverNavigate(Route.Main.Reading)
            },
            icon = {
                Icon(
                    painter = rememberAnimatedVectorPainter(
                        AnimatedImageVector.animatedVectorResource(R.drawable.animated_book),
                        selectedRoute is Route.Main.Reading
                    ),
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_reading),
                    maxLines = 1
                )
            }
        )
        NavigationBarItem(
            selected = selectedRoute is Route.Main.Bookshelf,
            onClick = {
                if (selectedRoute !is Route.Main.Bookshelf) coverNavigate(Route.Main.Bookshelf)
            },
            icon = {
                Icon(
                    painter = rememberAnimatedVectorPainter(
                        AnimatedImageVector.animatedVectorResource(R.drawable.animated_bookshelf),
                        selectedRoute is Route.Main.Bookshelf
                    ),
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_bookshelf),
                    maxLines = 1
                )
            }
        )
        NavigationBarItem(
            selected = selectedRoute is Route.Main.Exploration,
            onClick = {
                if (selectedRoute !is Route.Main.Exploration) coverNavigate(Route.Main.Exploration)
            },
            icon = {
                Icon(
                    painter = rememberAnimatedVectorPainter(
                        AnimatedImageVector.animatedVectorResource(R.drawable.animated_exploration),
                        selectedRoute is Route.Main.Exploration
                    ),
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_explore),
                    maxLines = 1
                )
            }
        )
        NavigationBarItem(
            selected = selectedRoute is Route.Main.Extensions,
            onClick = {
                if (selectedRoute !is Route.Main.Extensions) coverNavigate(Route.Main.Extensions)
            },
            icon = {
                Icon(
                    painter = rememberAnimatedVectorPainter(
                        AnimatedImageVector.animatedVectorResource(R.drawable.animated_extensions),
                        selectedRoute is Route.Main.Extensions
                    ),
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_extensions),
                    maxLines = 1
                )
            }
        )
        NavigationBarItem(
            selected = selectedRoute is Route.Main.Settings,
            onClick = {
                if (selectedRoute !is Route.Main.Settings) coverNavigate(Route.Main.Settings)
            },
            icon = {
                Icon(
                    painter = rememberAnimatedVectorPainter(
                        AnimatedImageVector.animatedVectorResource(R.drawable.animated_settings),
                        selectedRoute is Route.Main.Settings
                    ),
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_settings),
                    maxLines = 1
                )
            }
        )
    }
}
