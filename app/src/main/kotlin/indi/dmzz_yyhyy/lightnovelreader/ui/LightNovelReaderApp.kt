package indi.dmzz_yyhyy.lightnovelreader.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.UpdatesAvailableDialogViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToPluginInstallerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToPluginStoreInstall
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateUpdatesAvailableDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.LightNovelReaderNavHost
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.MainDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import io.nightfish.lightnovelreader.api.ui.ReaderStyle
import kotlinx.coroutines.flow.Flow

@Composable
fun LightNovelReaderApp(
    onBuildNavHost: NavEntryScope.() -> Unit,
    readerStyle: ReaderStyle,
    imageHeaderGetter: () -> Map<String, String>,
    intentFlow: Flow<Intent>,
    webBookDataSourceFoundedFlow: Flow<Boolean>,
) {

    val navigator = rememberNavigator()
    val updatesAvailableDialogViewModel = hiltViewModel<UpdatesAvailableDialogViewModel>()
    val available by updatesAvailableDialogViewModel.availableFlow.collectAsStateWithLifecycle(false)
    LaunchedEffect(available) {
        if (available) {
            updatesAvailableDialogViewModel.resetAvailable()
            navigator.navigateUpdatesAvailableDialog()
        }
    }
    LaunchedEffect(Unit) {
        intentFlow.collect { intent ->
            if (intent.action == Intent.ACTION_VIEW) {
                val uri = intent.data ?: return@collect
                if (uri.scheme == "lightnovelreader" && uri.host == "install_plugin") {
                    val pluginId = uri.getQueryParameter("id") ?: return@collect
                    navigator.navigateToPluginStoreInstall(pluginId)
                } else {
                    navigator.navigateToPluginInstallerDialog(uri.toString())
                }
            }
        }
    }
    LightNovelReaderNavHost(
        navigator = navigator,
        onBuildNavHost = onBuildNavHost,
        readerStyle = readerStyle,
        imageHeaderGetter = imageHeaderGetter,
        webBookDataSourceFoundedFlow = webBookDataSourceFoundedFlow
    )
}

@Composable
private fun rememberNavigator(): Navigator {
    val selectedMainIndex = rememberSaveable { mutableIntStateOf(MainDestination.Reading.index) }
    val backStacks = buildMap {
        MainDestination.entries.forEach { destination ->
            put(destination, rememberNavBackStack(destination.route))
        }
    }
    return remember(selectedMainIndex, backStacks) {
        Navigator(selectedMainIndex, backStacks)
    }
}
