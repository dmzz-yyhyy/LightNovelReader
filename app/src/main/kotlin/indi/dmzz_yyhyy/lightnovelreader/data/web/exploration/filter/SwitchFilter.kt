package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter

abstract class SwitchFilter(private val title: String): Filter {
    var enable = false

    override fun getType(): FilterTypes = FilterTypes.SWITCH
    override fun getTitle(): String = title
}