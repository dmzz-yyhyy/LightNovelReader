package indi.dmzz_yyhyy.lightnovelreader.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.rememberNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.UpdatesAvailableDialogViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateUpdatesAvailableDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.LightNovelReaderNavHost
import io.nightfish.lightnovelreader.api.ui.ReaderStyle

@Composable
fun LightNovelReaderApp(
    onBuildNavHost: NavGraphBuilder.() -> Unit,
    readerStyle: ReaderStyle,
    imageHeaderGetter: () -> Map<String, String>
) {
    val navController = rememberNavController()
    val updatesAvailableDialogViewModel = hiltViewModel<UpdatesAvailableDialogViewModel>()
    val available by updatesAvailableDialogViewModel.availableFlow.collectAsState(false)
    LaunchedEffect(available) {
        if (available) {
            updatesAvailableDialogViewModel.resetAvailable()
            navController.navigateUpdatesAvailableDialog()
        }
    }
    LightNovelReaderNavHost(navController, onBuildNavHost, readerStyle, imageHeaderGetter)
}
