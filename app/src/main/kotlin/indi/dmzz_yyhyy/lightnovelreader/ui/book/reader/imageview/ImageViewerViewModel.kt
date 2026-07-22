package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.imageview

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import javax.inject.Inject

@HiltViewModel
class ImageViewerViewModel @Inject constructor(
    val webBookDataSourceProvider: WebBookDataSourceProvider
): ViewModel() {
    val imageHeader get() = webBookDataSourceProvider.value.imageHeader
}