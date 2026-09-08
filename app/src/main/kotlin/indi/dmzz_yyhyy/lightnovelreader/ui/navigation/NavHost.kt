package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import android.annotation.SuppressLint
import android.os.Build
import android.view.RoundedCorner
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalImageHeaderGetter
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.book.bookNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.bookmanager.bookManagerDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.components.MainNavigationBar
import indi.dmzz_yyhyy.lightnovelreader.ui.components.LnrSnackbar
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.addBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.markAllChaptersAsReadDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.pluginInstallerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.pluginStoreInstallBottomSheet
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.updatesAvailableDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.homeNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.overlay.OverlaySceneStrategy
import indi.dmzz_yyhyy.lightnovelreader.ui.storagemanager.storageManagerDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalClaimSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalPopBackStack
import io.nightfish.lightnovelreader.api.ui.LocalReaderStyle
import io.nightfish.lightnovelreader.api.ui.ReaderStyle
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalSharedTransitionApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LightNovelReaderNavHost(
    navigator: Navigator,
    onBuildNavHost: NavEntryScope.() -> Unit,
    readerStyle: ReaderStyle,
    imageHeaderGetter: () -> Map<String, String>,
    webBookDataSourceFoundedFlow: Flow<Boolean>
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val view = LocalView.current
    val density = LocalDensity.current
    var deviceCornerRadiusPx by remember { mutableIntStateOf(0) }

    var claimCount by remember { mutableIntStateOf(0) }
    val claim: (Boolean) -> Unit = remember { { take -> claimCount += if (take) 1 else -1 } }

    CompositionLocalProvider(
        LocalReaderStyle provides readerStyle,
        LocalSnackbarHost provides snackbarHostState,
        LocalClaimSnackbarHost provides claim,
        LocalImageHeaderGetter provides imageHeaderGetter,
        LocalNavigator provides navigator,
        LocalPopBackStack provides { navigator.popBackStack() }
    ) {
        val coroutineScope = rememberCoroutineScope()
        val currentWebDataSourceNotFounded = stringResource(R.string.current_web_data_source_not_founded)
        val webBookDataSourceFounded by webBookDataSourceFoundedFlow.collectAsStateWithLifecycle(true)

        LaunchedEffect(webBookDataSourceFounded) {
            if (!webBookDataSourceFounded) {
                showSnackbar(
                    coroutineScope = coroutineScope,
                    hostState = snackbarHostState,
                    message = currentWebDataSourceNotFounded
                )
            }
        }

        val bottomPadding by animateDpAsState(
            if (navigator.isAtMainDestination) 80.dp else 0.dp,
            animationSpec = tween(300)
        )

        Scaffold(
            snackbarHost = {
                if (claimCount == 0) {
                    Box(Modifier
                        .navigationBarsPadding()
                        .padding(bottom = bottomPadding)) {
                        SnackbarHost(snackbarHostState) { data -> LnrSnackbar(data) }
                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                SharedTransitionLayout {
                    val sharedTransitionScope = this

                    val destinationProvider = remember(
                        sharedTransitionScope,
                        onBuildNavHost
                    ) {
                        entryProvider {
                            homeNavigation(sharedTransitionScope)
                            bookNavigation()
                            updatesAvailableDialog()
                            addBookToBookshelfDialog()
                            bookManagerDestination()
                            storageManagerDestination()
                            pluginInstallerDialog()
                            markAllChaptersAsReadDialog()
                            pluginStoreInstallBottomSheet()
                            onBuildNavHost()
                        }
                    }

                    val overlaySceneStrategy = remember { OverlaySceneStrategy() }

                    val currentDeviceCornerRadius =
                        with(density) { deviceCornerRadiusPx.toDp() }

                    val decoratedEntries = navigator.backStacks.mapValues { (_, backStack) ->
                        rememberDecoratedNavEntries(
                            backStack = backStack,
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator(),
                            ),
                            entryProvider = destinationProvider
                        )
                    }

                    val rootEntries = MainDestination.entries.associateWith { destination ->
                        decoratedEntries.getValue(destination).first()
                    }
                    val currentRootEntries = rememberUpdatedState(rootEntries)
                    val mainShellEntry = remember {
                        NavEntry<NavKey>(Route.Main) {
                            MainNavigationBar(
                                navigator = navigator,
                                rootEntries = currentRootEntries.value
                            )
                        }
                    }
                    val entries = listOf(mainShellEntry) + decoratedEntries.getValue(navigator.mainDestination).drop(1)

                    LaunchedEffect(Unit) {
                        deviceCornerRadiusPx = if (Build.VERSION.SDK_INT >= 31) {
                            val topLeftRadius = view.rootWindowInsets
                                ?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
                                ?.radius ?: 0

                            val bottomLeftRadius = view.rootWindowInsets
                                ?.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT)
                                ?.radius ?: 0

                            maxOf(topLeftRadius, bottomLeftRadius)
                        } else 0
                    }

                    InterruptibleNavDisplay(
                        entries = entries,
                        onBack = navigator::popBackStack,
                        sceneStrategies = listOf(overlaySceneStrategy),
                        sharedTransitionScope = sharedTransitionScope,
                        pageCornerRadius = currentDeviceCornerRadius
                    )
                }
            }
        }
    }
}
