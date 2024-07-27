package indi.dmzz_yyhyy.lightnovelreader.data.userdata

sealed class UserDataPath(
    private val name: String,
    private val parent: UserDataPath? = null,
) {
    val path: String get() = "${parent?.path?.plus(".") ?: ""}$name"
    data object Reader : UserDataPath("reader") {
        data object FontSize : UserDataPath("fontSize", Reader)
        data object FontLineHeight : UserDataPath("fontLineHeight", Reader)
        data object KeepScreenOn : UserDataPath("keepScreenOn", Reader)
    }
    data object ReadingBooks : UserDataPath("reading_books")
}
