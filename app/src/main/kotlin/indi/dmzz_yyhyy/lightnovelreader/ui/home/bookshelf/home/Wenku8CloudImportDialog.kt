package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.importer.*

@Composable
fun Wenku8CloudImportDialog(
    bookshelfId: Int,
    bookshelfOptions: List<Pair<Int, String>> = emptyList(),
    onSelectBookshelf: (Int) -> Unit = {},
    showLoginDialog: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    viewModel: Wenku8CloudImportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var targetId = remember(bookshelfId) { mutableStateOf(bookshelfId) }

    LaunchedEffect(bookshelfId) {
        if (state is Wenku8CloudImportState.Idle) {
            viewModel.prepare(bookshelfId)
        }
    }

    when (val s = state) {
        is Wenku8CloudImportState.Idle -> {}
        is Wenku8CloudImportState.Prepared -> {
            AlertDialog(
                onDismissRequest = { onDismiss() },
                title = { Text(stringResource(R.string.wenku8_import_title)) },
                text = {
                    Column {
                        if (bookshelfOptions.isNotEmpty()) {
                            Text(stringResource(R.string.wenku8_import_select_bookshelf))
                            Spacer(Modifier.height(4.dp))
                            // 简单列出供选择
                            LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                                items(bookshelfOptions) { (id, name) ->
                                    val selected = id == targetId.value
                                    TextButton(onClick = {
                                        targetId.value = id; onSelectBookshelf(id); viewModel.prepare(id)
                                    }) {
                                        Text((if (selected) "✔ " else "") + name)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Text(stringResource(R.string.wenku8_import_summary_pending, s.needImportIds.size, s.alreadyExists))
                        if (s.needImportIds.isEmpty()) {
                            Text(stringResource(R.string.wenku8_import_nothing_new))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (s.needImportIds.isNotEmpty()) viewModel.execute() else onDismiss()
                    }) { Text(if (s.needImportIds.isEmpty()) stringResource(R.string.wenku8_import_action_close) else stringResource(R.string.wenku8_import_action_start)) }
                },
                dismissButton = {
                    TextButton(onClick = { onDismiss() }) { Text(stringResource(R.string.wenku8_import_action_cancel)) }
                }
            )
        }
        is Wenku8CloudImportState.NeedLogin -> {
            AlertDialog(
                onDismissRequest = { onDismiss() },
                title = { Text(stringResource(R.string.wenku8_import_title)) },
                text = { Text(stringResource(R.string.wenku8_import_error_not_logged_in)) },
                confirmButton = {
                    TextButton(onClick = {
                        showLoginDialog?.invoke()
                    }) { Text(stringResource(R.string.login_wenku8)) }
                },
                dismissButton = {
                    TextButton(onClick = { onDismiss() }) { Text(stringResource(R.string.wenku8_import_action_cancel)) }
                }
            )
        }
        is Wenku8CloudImportState.Running -> {
            AlertDialog(
                onDismissRequest = { viewModel.cancel(); onDismiss() },
                title = { Text(stringResource(R.string.wenku8_import_title)) },
                text = {
                    Column {
                        if (s.total == 0) {
                            Text(stringResource(R.string.wenku8_import_stage_fetch))
                            LinearProgressIndicator()
                        } else {
                            Text(stringResource(R.string.wenku8_import_stage_download, s.current, s.total))
                            LinearProgressIndicator(progress = { (if (s.total == 0) 0f else s.current / s.total.toFloat()).coerceIn(0f,1f) })
                        }
                        s.currentBookId?.let { Text("ID: $it") }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.cancel(); onDismiss() }) { Text(stringResource(R.string.wenku8_import_action_cancel)) }
                }
            )
        }
        is Wenku8CloudImportState.Success -> {
            AlertDialog(
                onDismissRequest = { viewModel.reset(); onDismiss() },
                title = { Text(stringResource(R.string.wenku8_import_title)) },
                text = {
                    Column {
                        Text(stringResource(R.string.wenku8_import_success, s.imported, s.skipped))
                        if (s.successIds.isNotEmpty()) {
                            Text(stringResource(R.string.wenku8_import_log_success, s.successIds.size))
                        }
                        if (s.failedIds.isNotEmpty()) {
                            Text(stringResource(R.string.wenku8_import_log_failed, s.failedIds.size))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.reset(); onDismiss() }) { Text(stringResource(R.string.wenku8_import_action_close)) }
                }
            )
        }
        is Wenku8CloudImportState.Cancelled -> {
            AlertDialog(
                onDismissRequest = { viewModel.reset(); onDismiss() },
                title = { Text(stringResource(R.string.wenku8_import_title)) },
                text = {
                    Column {
                        Text(stringResource(R.string.wenku8_import_cancelled, s.imported))
                        if (s.successIds.isNotEmpty()) Text(stringResource(R.string.wenku8_import_log_success, s.successIds.size))
                        if (s.failedIds.isNotEmpty()) Text(stringResource(R.string.wenku8_import_log_failed, s.failedIds.size))
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.reset(); onDismiss() }) { Text(stringResource(R.string.wenku8_import_action_close)) }
                }
            )
        }
        is Wenku8CloudImportState.Failure -> {
            val msg = when (s.error) {
                Wenku8CloudImportError.NotLoggedIn -> stringResource(R.string.wenku8_import_error_not_logged_in)
                Wenku8CloudImportError.Parse -> stringResource(R.string.wenku8_import_error_parse)
                Wenku8CloudImportError.Network -> stringResource(R.string.wenku8_import_error_network)
                is Wenku8CloudImportError.Unknown -> stringResource(R.string.wenku8_import_error_unknown)
            }
            AlertDialog(
                onDismissRequest = { viewModel.reset(); onDismiss() },
                title = { Text(stringResource(R.string.wenku8_import_title)) },
                text = { Text(msg) },
                confirmButton = {
                    TextButton(onClick = { viewModel.reset(); onDismiss() }) { Text(stringResource(R.string.wenku8_import_action_close)) }
                }
            )
        }
    }
}
