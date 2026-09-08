package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.MainDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.utils.theme.Anim

@Composable
fun MainNavigationBar(
    navigator: Navigator,
    rootEntries: Map<MainDestination, NavEntry<NavKey>>
) {
    BackHandler(
        enabled = navigator.isAtMainDestination &&
                navigator.mainDestination != MainDestination.Reading,
        onBack = navigator::popBackStack
    )
    val transitionSpec = Anim.destinationChange(MainDestination::index)

    AnimatedContent(
        targetState = navigator.mainDestination,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = transitionSpec,
        contentKey = MainDestination::index,
        label = "main destination",
    ) { destination ->
        CompositionLocalProvider(LocalNavAnimatedContentScope provides this) {
            rootEntries.getValue(destination).Content()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(Modifier.align(Alignment.BottomCenter)) {
            NavigateBar(
                navigator = navigator
            )
        }
    }
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
private fun NavigateBar(
    navigator: Navigator
) {
    val selected = navigator.mainDestination
    val isReading = selected == MainDestination.Reading
    val isBookshelf = selected == MainDestination.Bookshelf
    val isExploration = selected == MainDestination.Explore
    val isSettings = selected == MainDestination.Settings

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
            onClick = { navigator.navigate(MainDestination.Reading) },
            icon = { Icon(painter = rememberAnimatedVectorPainter(avdReading, isReading), null) },
            label = { Text(stringResource(R.string.nav_reading), maxLines = 1) }
        )
        NavigationBarItem(
            selected = isBookshelf,
            onClick = { navigator.navigate(MainDestination.Bookshelf) },
            icon = { Icon(painter = rememberAnimatedVectorPainter(avdShelf, isBookshelf), null) },
            label = { Text(stringResource(R.string.nav_bookshelf), maxLines = 1) }
        )
        NavigationBarItem(
            selected = isExploration,
            onClick = { navigator.navigate(MainDestination.Explore) },
            icon = {
                Icon(
                    painter = rememberAnimatedVectorPainter(avdExplore, isExploration),
                    null
                )
            },
            label = { Text(stringResource(R.string.nav_explore), maxLines = 1) }
        )
        NavigationBarItem(
            selected = isSettings,
            onClick = { navigator.navigate(MainDestination.Settings) },
            icon = { Icon(painter = rememberAnimatedVectorPainter(avdSettings, isSettings), null) },
            label = { Text(stringResource(R.string.nav_settings), maxLines = 1) }
        )
    }
}
