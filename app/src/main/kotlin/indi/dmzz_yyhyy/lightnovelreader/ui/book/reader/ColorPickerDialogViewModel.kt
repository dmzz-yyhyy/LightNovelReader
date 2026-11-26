package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.userdata.UserData
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorPickerDialogViewModel @Inject constructor(
    val userDataRepository: UserDataRepository
) : ViewModel() {
    private var colorUserData: UserData<Color>? = null

    fun init(colorUserDataPath: UserDataPath<Color>): Flow<Color?> {
        colorUserData = userDataRepository.userData(colorUserDataPath)
        return colorUserData!!.getFlow()
    }

    fun changeBackgroundColor(color: Color) {
        if (colorUserData == null) {
            Log.e("ColorPickerDialogViewModel", "change color user data before init!")
        }
        CoroutineScope(Dispatchers.IO).launch {
            colorUserData!!.set(color)
        }
    }
}
