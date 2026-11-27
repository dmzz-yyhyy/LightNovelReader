package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.userdata.UserData
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@HiltViewModel
class SliderValueDialogViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {
    private var floatUserData: UserData<Float>? = null

    fun init(floatUserDataPath: UserDataPath<Float>): Flow<Float> {
        floatUserData = userDataRepository.userData(floatUserDataPath)
        val value = floatUserData!!.getFlowWithDefault(Float.NaN)
        return value
    }

    fun setValue(value: Float) {
        if (floatUserData == null) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            floatUserData?.asynchronousSet(value)
        }
    }
}
