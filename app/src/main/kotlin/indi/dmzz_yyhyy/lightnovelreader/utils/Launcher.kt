package indi.dmzz_yyhyy.lightnovelreader.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun uriLauncher(block: (Uri) -> Unit): ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            activityResult.data?.data?.let { uri ->
                block(uri)
            }
        }
    }
}

@Composable
fun uriLauncherWithFlag(
    block: (Uri, Boolean) -> Unit
): Pair<ManagedActivityResultLauncher<Intent, ActivityResult>, (Boolean) -> Unit> {
    var isDarkFlag by remember { mutableStateOf(false) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                activityResult.data?.data?.let { uri ->
                    block(uri, isDarkFlag)
                }
            }
        }

    val setFlag: (Boolean) -> Unit = { flag ->
        isDarkFlag = flag
    }

    return launcher to setFlag
}