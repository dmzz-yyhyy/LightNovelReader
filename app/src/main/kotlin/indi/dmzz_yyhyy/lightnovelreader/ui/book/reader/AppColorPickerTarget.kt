package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import androidx.annotation.StringRes
import indi.dmzz_yyhyy.lightnovelreader.R
import io.nightfish.lightnovelreader.api.Route

sealed class AppColorPickerTarget(
    @get:StringRes
    override val descriptionResId: Int
) : Route.Book.ColorPickerTarget {
    data object Text : AppColorPickerTarget(R.string.dialog_color_picker_text_desc)
    data object Background : AppColorPickerTarget(R.string.dialog_color_picker_background_desc)
}

fun Route.Book.ColorPickerTargetType.toAppTarget(): AppColorPickerTarget = when (this) {
    Route.Book.ColorPickerTargetType.TEXT -> AppColorPickerTarget.Text
    Route.Book.ColorPickerTargetType.BACKGROUND -> AppColorPickerTarget.Background
}
