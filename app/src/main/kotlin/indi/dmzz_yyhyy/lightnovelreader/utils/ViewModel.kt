package indi.dmzz_yyhyy.lightnovelreader.utils

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel

/** Graph-shared ViewModels from Nav2 retain one host-level owner after the Nav3 migration. */
@Composable
inline fun <reified VM : ViewModel> activityHiltViewModel(): VM =
    // ponytail: Activity scope is broader than the old graph scope; add a shared-store decorator
    // if these ViewModels ever need early cleanup or multiple host graphs.
    hiltViewModel(LocalActivity.current as ComponentActivity)
