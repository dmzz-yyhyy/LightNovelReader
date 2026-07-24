package indi.dmzz_yyhyy.lightnovelreader.data.image

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageTransPostProcessingViewModel @Inject constructor(
    val imageTransPostProcessingManager: ImageTransPostProcessingManager
): ViewModel()