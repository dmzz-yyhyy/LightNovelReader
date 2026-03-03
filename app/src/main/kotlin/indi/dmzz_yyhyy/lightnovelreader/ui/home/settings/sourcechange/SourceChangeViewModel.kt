package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.sourcechange

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.runCatching
import com.github.michaelbull.result.unwrapError
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalDataManager
import indi.dmzz_yyhyy.lightnovelreader.data.local.cbor.LocalData
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import indi.dmzz_yyhyy.lightnovelreader.utils.readAppLocalData
import indi.dmzz_yyhyy.lightnovelreader.utils.writeAppLocalData
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltViewModel
class SourceChangeViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val localDataManager: LocalDataManager,
    private val userDataRepository: UserDataRepository,
    webBookDataSourceManager: WebBookDataSourceManager
) : ViewModel() {

    private val _uiState = MutableSourceChangeUiState().apply {
        currentSourceId = webBookDataSourceProvider.default.id
        webDataSourceItems = webBookDataSourceManager.webDataSourceItems
    }
    val uiState: SourceChangeUiState = _uiState
    @Suppress("OPT_IN_USAGE")
    fun changeWebSource(newWebDataSourceId: Int) {
        if (newWebDataSourceId == _uiState.currentSourceId) return
        if (_uiState.isProcessing) return

        _uiState.isProcessing = true

        CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            var isCleanedLocalData = false
            try {
                val result = localDataManager.exportCurrentLocalData()
                    .andThen { data ->
                        runCatching {
                            val webBookDataSourceId = webBookDataSourceProvider.default.id
                            localDataManager.localDataDir
                                .resolve(webBookDataSourceId.toString())
                                .also {
                                    if (it.exists()) it.delete()
                                }
                                .outputStream()
                                .use {
                                    it.writeAppLocalData(Cbor.encodeToByteArray(data))
                                }
                        }
                    }.andThen {
                        isCleanedLocalData = true
                        runCatching {
                            localDataManager.cleanDatabaseWithoutGlobalUserData()
                        }
                    }.andThen out@ {
                        val file = localDataManager.localDataDir.resolve(newWebDataSourceId.toString())
                        if (!file.exists()) return@out Ok(Unit)
                        runCatching {
                            file
                                .inputStream()
                                .use {
                                    Cbor.decodeFromByteArray<LocalData>(it.readAppLocalData())
                                }
                        }.andThen {
                            localDataManager.importLocalDataToDatabase(it)
                        }
                    }.andThen {
                        runCatching {
                            userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path).set(newWebDataSourceId)
                        }
                    }
                if (result.isErr) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(appContext, "Failed to change data source. Please check the log for more information", Toast.LENGTH_LONG).show()
                    }
                    Log.e("SourceChangeViewModel", "Failed to change data source.")
                    result.unwrapError().printStackTrace()
                    if (isCleanedLocalData) rollbackData()
                    return@launch
                } else {
                    restartApp(appContext)
                }
                _uiState.currentSourceId = newWebDataSourceId
            } finally {
                if (isCleanedLocalData) rollbackData()
                _uiState.isProcessing = false
            }
        }
    }

    @Suppress("OPT_IN_USAGE")
    fun rollbackData() {
        CoroutineScope(Dispatchers.IO).launch {
            val webBookDataSourceId = webBookDataSourceProvider.default.id
            val localData =
                localDataManager.localDataDir
                    .resolve(webBookDataSourceId.toString())
                    .inputStream()
                    .use {
                        Cbor.decodeFromByteArray<LocalData>(it.readAppLocalData())
                    }
            localDataManager.importLocalDataToDatabase(localData)
        }
    }

    private fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        exitProcess(0)
    }
}