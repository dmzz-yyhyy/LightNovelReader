package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.update.UpdateCheckRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class UpdatesAvailableDialogViewModel @Inject constructor(
    private val updateCheckRepository: UpdateCheckRepository
) : ViewModel() {
    val release = updateCheckRepository.release
    val availableFlow = updateCheckRepository.availableFlow
    val updatePhaseFlow = updateCheckRepository.updatePhase
    val isDownloading: StateFlow<Boolean> = updateCheckRepository.isDownloading
    val downloadProgress: StateFlow<Float> = updateCheckRepository.downloadProgress

    fun downloadUpdate() =
        updateCheckRepository.downloadUpdate()

    fun checkUpdate() =
        updateCheckRepository.check()

    fun resetAvailable() = updateCheckRepository.resetAvailable()
}