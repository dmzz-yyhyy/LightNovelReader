package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter

abstract class SingleChoiceFilter(private val title: String): Filter {
    var choice: String = ""

    override fun getType(): FilterTypes = FilterTypes.SINGLE_CHOICE
    override fun getTitle(): String = title
    fun getAllChoices(): List<String> = emptyList()
}