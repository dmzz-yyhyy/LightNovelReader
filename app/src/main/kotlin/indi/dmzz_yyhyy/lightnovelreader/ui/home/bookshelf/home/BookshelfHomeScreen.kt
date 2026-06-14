package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import indi.dmzz_yyhyy.lightnovelreader.data.work.SaveBookshelfWork
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfBookMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

data class BookshelfHomeDataSources(
    val getBookInfoFlow: (String) -> StateFlow<BookInformation>,
    val getBookVolumesFlow: (String) -> StateFlow<BookVolumes>,
    val getBookMetadataFlow: (String) -> StateFlow<BookshelfBookMetadata?>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfHomeScreen(
    init: () -> Unit,
    uiState: BookshelfHomeUiState,
    dataSources: BookshelfHomeDataSources,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val backgroundColor by animateColorAsState(
        if (uiState.selectMode) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.surface
    )
    val saveAllBookshelfLauncher = launcher(uiState.saveAllBookshelfJsonData)
    val saveThisBookshelfLauncher = launcher(uiState.saveBookshelfJsonData)
    val importBookshelfLauncher = launcher(uiState.importBookshelf)
    val listState = remember(uiState.selectedBookshelfId) { androidx.compose.foundation.lazy.LazyListState() }

    BackHandler(uiState.selectMode) {
        uiState.onDisableSelectMode()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        init()
    }

    LaunchedEffect(uiState.toast) {
        if (uiState.toast.isEmpty()) return@LaunchedEffect
        Toast.makeText(context, uiState.toast, Toast.LENGTH_SHORT).show()
        uiState.clearToast()
    }

    val shareBookshelf: () -> Unit = remember(
        context,
        workManager,
        coroutineScope,
        uiState.selectedBookshelfId
    ) {
        {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.applicationInfo.processName}.provider",
                File(context.cacheDir, "LightNovelReaderBookshelfData.lnr")
            )
            val workRequest = OneTimeWorkRequestBuilder<SaveBookshelfWork>()
                .setInputData(
                    workDataOf(
                        "bookshelfId" to uiState.selectedBookshelfId,
                        "uri" to uri.toString(),
                    )
                )
                .build()
            workManager.enqueueUniqueWork(
                uri.toString(),
                ExistingWorkPolicy.KEEP,
                workRequest
            )
            coroutineScope.launch(Dispatchers.IO) {
                workManager.getWorkInfoByIdFlow(workRequest.id).collect {
                    when (it?.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            ShareCompat.IntentBuilder(context)
                                .setType("application/zip")
                                .setSubject("分享文件")
                                .addStream(uri)
                                .setChooserTitle("分享书架")
                                .startChooser()
                        }

                        else -> return@collect
                    }
                }
            }
        }
    }

    Column {
        BookshelfHomeTopBar(
            scrollBehavior = scrollBehavior,
            backgroundColor = backgroundColor,
            uiState = uiState,
            onShareBookshelf = shareBookshelf,
            onSaveThisBookshelf = {
                createBookshelfDataFile(
                    uiState.selectedBookshelf.name,
                    saveThisBookshelfLauncher
                )
            },
            onSaveAllBookshelf = {
                createBookshelfDataFile("bookshelves", saveAllBookshelfLauncher)
            },
            onImportBookshelf = {
                selectBookshelfDataFile(importBookshelfLauncher)
            }
        )

        BookshelfHomeContent(
            uiState = uiState,
            dataSources = dataSources,
            listState = listState,
            scrollBehavior = scrollBehavior
        )
    }
}

@Suppress("DuplicatedCode")
fun createBookshelfDataFile(fileName: String, launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Documents")
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/x-lightnovelreader-data"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
        }
        putExtra(Intent.EXTRA_TITLE, "$fileName.lnr")
    }
    launcher.launch(Intent.createChooser(intent, "选择一位置"))
}

@Composable
fun launcher(block: (Uri) -> Unit): ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            activityResult.data?.data?.let(block)
        }
    }
}

@Suppress("DuplicatedCode")
fun selectBookshelfDataFile(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Documents")
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
        }
    }
    launcher.launch(Intent.createChooser(intent, "选择数据文件"))
}
