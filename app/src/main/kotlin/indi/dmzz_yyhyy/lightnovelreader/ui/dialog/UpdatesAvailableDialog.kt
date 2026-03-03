package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import dev.jeziellago.compose.markdowntext.MarkdownText
import indi.dmzz_yyhyy.lightnovelreader.BuildConfig
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.update.Release
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.updatesAvailableDialog() {
    dialog<Route.UpdatesAvailableDialog> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<UpdatesAvailableDialogViewModel>()
        val isDownloading by viewModel.isDownloading.collectAsState()
        val downloadProgress by viewModel.downloadProgress.collectAsState()
        UpdatesAvailableDialog(
            onDismissRequest = { if (!isDownloading) navController.popBackStack() },
            onConfirmation = { viewModel.downloadUpdate() },
            release = viewModel.release,
            isDownloading = isDownloading,
            downloadProgress = downloadProgress
        )
    }
}

@Composable
fun UpdatesAvailableDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    release: Release?,
    isDownloading: Boolean = false,
    downloadProgress: Float = 0f
) {
    println(release?.downloadUrl)
    val context = LocalContext.current
    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(R.drawable.archive_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_updates_available),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                release?.versionName?.let {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = BuildConfig.VERSION_NAME,
                                    style = typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = BuildConfig.VERSION_CODE.toString(),
                                    style = typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Icon(
                                painterResource(R.drawable.keyboard_double_arrow_right_24px),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .size(20.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = release.versionName,
                                    style = typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = release.version.toString(),
                                    style = typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                release?.releaseNotes?.let {
                    if (isDownloading) {
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    } else {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        item {
                            MarkdownText(
                                modifier = Modifier.fillMaxWidth(),
                                markdown = it
                            )
                        }
                    }
                }
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onConfirmation()
                },
                enabled = !isDownloading
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = "${stringResource(R.string.install_update)}... ${(downloadProgress * 100).toInt()}%")
                } else {
                    Icon(
                        painter = painterResource(R.drawable.download_24px),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(text = stringResource(R.string.install_update))
                }
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = onDismissRequest,
                    enabled = !isDownloading
                ) {
                    Text(text = stringResource(R.string.decline))
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        release?.downloadUrl?.let { url ->
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            context.startActivity(intent, null)
                        }
                        onDismissRequest()
                    }
                ) {
                    Text(text = stringResource(R.string.manual_download))
                }
            }
        }
    )
}

fun NavController.navigateUpdatesAvailableDialog() {
    navigate(Route.UpdatesAvailableDialog)
}
