package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.readerstyle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import javax.inject.Inject

@HiltViewModel
class ReaderStyleViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
) : ViewModel() {
    val settingState = SettingState(
        userDataRepository,
        viewModelScope
    )
}
