package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalBottomBarController
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalImageHeaderGetter
import indi.dmzz_yyhyy.lightnovelreader.ui.book.bookNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.components.LnrNavigationBar
import indi.dmzz_yyhyy.lightnovelreader.ui.components.LnrSnackbar
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.addBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.markAllChaptersAsReadDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.pluginInstallerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.pluginStoreInstallBottomSheet
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.updatesAvailableDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.bookmanager.bookManager
import indi.dmzz_yyhyy.lightnovelreader.ui.home.homeNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.storagemanager.storageManager
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalClaimSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.currentMainRoute
import indi.dmzz_yyhyy.lightnovelreader.utils.expandEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandExit
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopExit
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import io.nightfish.lightnovelreader.api.ui.LocalReaderStyle
import io.nightfish.lightnovelreader.api.ui.ReaderStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LightNovelReaderNavHost(
    navController: NavHostController,
    onBuildNavHost: NavGraphBuilder.() -> Unit,
    readerStyle: ReaderStyle,
    imageHeaderGetter: () -> Map<String, String>
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var claimCount by remember { mutableIntStateOf(0) }
    val claim: (Boolean) -> Unit = remember { { take -> claimCount += if (take) 1 else -1 } }

    var bottomBarVisible by remember { mutableStateOf(true) }
    CompositionLocalProvider(
        LocalReaderStyle provides readerStyle,
        LocalNavController provides navController,
        LocalSnackbarHost provides snackbarHostState,
        LocalClaimSnackbarHost provides claim,
        LocalBottomBarController provides { visible -> bottomBarVisible = visible },
        LocalImageHeaderGetter provides imageHeaderGetter
    ) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDest = backStackEntry?.destination
        val selectedRoute = currentDest.currentMainRoute()
        val hasBottomBarByRoute = selectedRoute != null

        LaunchedEffect(selectedRoute) {
            bottomBarVisible = true
        }

        val bottomPadding by animateDpAsState(
            if (bottomBarVisible && hasBottomBarByRoute) 80.dp else 0.dp,
            animationSpec = tween(300)
        )

        Scaffold(
            snackbarHost = {
                if (claimCount == 0) {
                    Box(Modifier.navigationBarsPadding().padding(bottom = bottomPadding)) {
                        SnackbarHost(snackbarHostState) { data -> LnrSnackbar(data) }
                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { _ ->
            Box(Modifier.fillMaxSize()) {
                SharedTransitionLayout {
                    NavHost(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController,
                        startDestination = Route.Main,
                        enterTransition = { expandEnter() },
                        exitTransition = { expandExit() },
                        popEnterTransition = { expandPopEnter() },
                        popExitTransition = { expandPopExit() }
                    ) {
                        homeNavigation(this@SharedTransitionLayout)
                        bookNavigation()
                        updatesAvailableDialog()
                        addBookToBookshelfDialog()
                        bookManager()
                        storageManager()
                        pluginInstallerDialog()
                        markAllChaptersAsReadDialog()
                        pluginStoreInstallBottomSheet()
                        onBuildNavHost.invoke(this)
                    }
                }

                LnrNavigationBar(
                    showBottomBar = hasBottomBarByRoute && bottomBarVisible,
                    selectedRoute = selectedRoute,
                    navController = navController
                )
            }
        }
    }
}
