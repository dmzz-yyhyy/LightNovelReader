package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail;

import com.google.android.material.bottomsheet.BottomSheetBehavior.State;
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData

@State
interface DetailUiState {
    val bookInformation: BookInformation?
    val bookVolumes: BookVolumes?
    val userReadingData: UserReadingData?
    val isLoading: Boolean
}

class MutableDetailUiState: DetailUiState {
    override var bookInformation: BookInformation? = null
    override var bookVolumes: BookVolumes? = null
    override var userReadingData: UserReadingData? = null
    override var isLoading = true
}


