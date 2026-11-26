package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.sourcechange

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import indi.dmzz_yyhyy.lightnovelreader.utils.restart
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SourceChangeViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val userDataRepository: UserDataRepository,
    webBookDataSourceManager: WebBookDataSourceManager
) : ViewModel() {

    private val _uiState = MutableSourceChangeUiState().apply {
        currentSourceId = webBookDataSourceProvider.value.id
        webDataSourceItems = webBookDataSourceManager.webDataSourceItems
    }
    val uiState: SourceChangeUiState = _uiState

    fun changeWebSource(newWebDataSourceId: Identifier.DataSource) {
        if (newWebDataSourceId == _uiState.currentSourceId || _uiState.isProcessing) return
        _uiState.isProcessing = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                userDataRepository.userData(UserDataPath.Settings.Data.WebDataSourceId)
                    .set(newWebDataSourceId)
                _uiState.currentSourceId = newWebDataSourceId
                restart(appContext)
            } finally {
                _uiState.isProcessing = false
            }
        }
    }
}
