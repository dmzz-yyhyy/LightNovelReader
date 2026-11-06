package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.sourcechange

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalDataManager
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SourceChangeViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val localDataManager: LocalDataManager,
    private val userDataRepository: UserDataRepository,
    private val webBookDataSourceManager: WebBookDataSourceManager,
    webBookDataSourceProvider: WebBookDataSourceProvider
) : ViewModel() {

    private val _uiState = MutableSourceChangeUiState().apply {
        currentSourceId = webBookDataSourceProvider.value.id
        webDataSourceItems = webBookDataSourceManager.webDataSourceItems
    }
    val uiState: SourceChangeUiState = _uiState

    /**
     * 切换主数据源。
     *
     * 多数据源模式下不再清空数据库: 目标数据源的历史数据会被合并进当前数据库,
     * 其他数据源的数据和书架保持不变, 因此无需重启应用。
     */
    fun changeWebSource(newWebDataSourceId: Identifier) {
        if (newWebDataSourceId == _uiState.currentSourceId) return
        if (_uiState.isProcessing) return

        _uiState.isProcessing = true

        CoroutineScope(Dispatchers.IO).launch {
            localDataManager.importLocalDataFile(newWebDataSourceId)
                .onErr { error ->
                    Log.e("SourceChangeViewModel", "Failed to change data source.", error)
                    notifyFailed()
                }
                .onOk {
                    userDataRepository
                        .stringUserData(UserDataPath.Settings.Data.WebDataSourceId.path)
                        .set(newWebDataSourceId.toString())
                    webBookDataSourceManager.onWebDataSourceListChange()
                    withContext(Dispatchers.Main) {
                        _uiState.currentSourceId = newWebDataSourceId
                        _uiState.isProcessing = false
                        Toast.makeText(
                            appContext,
                            appContext.getString(
                                indi.dmzz_yyhyy.lightnovelreader.R.string.settings_data_source_switched
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private suspend fun notifyFailed() = withContext(Dispatchers.Main) {
        _uiState.isProcessing = false
        Toast.makeText(
            appContext,
            "Failed to change data source. Please check the log for more information",
            Toast.LENGTH_LONG
        ).show()
    }
}
