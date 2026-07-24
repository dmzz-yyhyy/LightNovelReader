package indi.dmzz_yyhyy.lightnovelreader.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.rememberNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.UpdatesAvailableDialogViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToPluginInstallerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToPluginStoreInstall
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateUpdatesAvailableDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.LightNovelReaderNavHost
import io.nightfish.lightnovelreader.api.ui.ReaderStyle
import kotlinx.coroutines.flow.Flow

@Composable
fun LightNovelReaderApp(
    onBuildNavHost: NavGraphBuilder.() -> Unit,
    readerStyle: ReaderStyle,
    imageHeaderGetter: () -> Map<String, String>,
    intentFlow: Flow<Intent>,
    webBookDataSourceFoundedFlow: Flow<Boolean>,
) {
    val navController = rememberNavController()
    val updatesAvailableDialogViewModel = hiltViewModel<UpdatesAvailableDialogViewModel>()
    val available by updatesAvailableDialogViewModel.availableFlow.collectAsStateWithLifecycle(false)
    LaunchedEffect(available) {
        if (available) {
            updatesAvailableDialogViewModel.resetAvailable()
            navController.navigateUpdatesAvailableDialog()
        }
    }
    LaunchedEffect(Unit) {
        intentFlow.collect { intent ->
            if (intent.action == Intent.ACTION_VIEW) {
                val uri = intent.data ?: return@collect
                if (uri.scheme == "lightnovelreader" && uri.host == "install_plugin") {
                    val pluginId = uri.getQueryParameter("id") ?: return@collect
                    navController.navigateToPluginStoreInstall(pluginId)
                } else {
                    navController.navigateToPluginInstallerDialog(uri.toString())
                }
            }
        }
    }
    LightNovelReaderNavHost(
        navController = navController,
        onBuildNavHost = onBuildNavHost,
        readerStyle = readerStyle,
        imageHeaderGetter = imageHeaderGetter,
        webBookDataSourceFoundedFlow = webBookDataSourceFoundedFlow
    )
}
