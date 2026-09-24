package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
) : ViewModel() {
    val settingState = ThemeSettingState(
        userDataRepository,
        viewModelScope
    )
}
