package indi.dmzz_yyhyy.lightnovelreader.data.setting

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.StateFactoryMarker
import io.nightfish.lightnovelreader.api.userdata.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class AbstractSettingState(
    private val coroutineScope: CoroutineScope,
) {

    @StateFactoryMarker
    protected fun <T> UserData<T>.asState(initial: T): State<T> {
        val state = mutableStateOf(initial)
        coroutineScope.launch(Dispatchers.IO) {
            getFlowWithDefault(initial).collect {
                state.value = it
            }
        }
        return state
    }

    @StateFactoryMarker
    protected fun <T> UserData<T>.safeAsState(initial: T): State<T> {
        val state = mutableStateOf(initial)
        coroutineScope.launch(Dispatchers.IO) {
            getFlowWithDefault(initial).collect {
                state.value = it
            }
        }
        return state
    }
}