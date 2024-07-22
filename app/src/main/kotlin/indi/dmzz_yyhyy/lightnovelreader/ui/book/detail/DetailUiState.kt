package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData

@State
interface DetailUiState {
    val bookInformation: BookInformation
    val bookVolumes: BookVolumes
    val userReadingData: UserReadingData
    val isLoading: Boolean
}

class MutableDetailUiState: DetailUiState {
    override var bookInformation: BookInformation by mutableStateOf(BookInformation.empty())
    override var bookVolumes: BookVolumes by mutableStateOf(BookVolumes.empty())
    override var userReadingData: UserReadingData by mutableStateOf(UserReadingData.empty())
    override var isLoading = true
}


