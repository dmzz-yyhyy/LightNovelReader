package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.sourcechange

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import com.github.michaelbull.result.runCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalDataManager
import indi.dmzz_yyhyy.lightnovelreader.data.local.cbor.LocalData
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import indi.dmzz_yyhyy.lightnovelreader.utils.ofId
import indi.dmzz_yyhyy.lightnovelreader.utils.readAppLocalData
import indi.dmzz_yyhyy.lightnovelreader.utils.restart
import indi.dmzz_yyhyy.lightnovelreader.utils.writeAppLocalData
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import javax.inject.Inject

@HiltViewModel
class SourceChangeViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val localDataManager: LocalDataManager,
    private val userDataRepository: UserDataRepository,
    webBookDataSourceManager: WebBookDataSourceManager
) : ViewModel() {

    private val _uiState = MutableSourceChangeUiState().apply {
        currentSourceId = webBookDataSourceProvider.value.id
        webDataSourceItems = webBookDataSourceManager.webDataSourceItems
    }
    val uiState: SourceChangeUiState = _uiState
    @Suppress("OPT_IN_USAGE")
    fun changeWebSource(newWebDataSourceId: Identifier) {
        if (newWebDataSourceId == _uiState.currentSourceId) return
        if (_uiState.isProcessing) return

        _uiState.isProcessing = true

        CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            var isCleanedLocalData = false
            localDataManager.exportCurrentLocalData()
                .andThen { data ->
                    runCatching {
                        val webBookDataSourceId = webBookDataSourceProvider.value.id
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
                    val file = if (newWebDataSourceId == "Wenku8".ofId()) localDataManager.localDataDir.resolve("-791439186")
                    else localDataManager.localDataDir.resolve(newWebDataSourceId.toString())
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
                        userDataRepository.stringUserData(UserDataPath.Settings.Data.WebDataSourceId.path).set(newWebDataSourceId.toString())
                    }
                }.onErr {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(appContext, "Failed to change data source. Please check the log for more information", Toast.LENGTH_LONG).show()
                    }
                    Log.e("SourceChangeViewModel", "Failed to change data source.")
                    it.printStackTrace()
                    if (isCleanedLocalData) rollbackData()
                    return@launch
                }.onOk {
                    restart(appContext)
                }
            _uiState.currentSourceId = newWebDataSourceId
        }
    }

    @Suppress("OPT_IN_USAGE")
    fun rollbackData() {
        CoroutineScope(Dispatchers.IO).launch {
            val webBookDataSourceId = webBookDataSourceProvider.value.id
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
}