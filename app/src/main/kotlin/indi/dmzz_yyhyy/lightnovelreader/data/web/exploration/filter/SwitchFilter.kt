package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter

abstract class SwitchFilter(
    private var title: String,
    private var onChange: () -> Unit
): Filter() {
    var enable = false
        set(value) {
            field = value
            onChange.invoke()
        }

    override fun getType(): FilterTypes = FilterTypes.SWITCH
    override fun getTitle(): String = title
}