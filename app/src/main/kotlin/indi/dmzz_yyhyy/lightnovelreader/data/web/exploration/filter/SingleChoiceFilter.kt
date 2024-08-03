package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter

abstract class SingleChoiceFilter(
    private val title: String,
    private val choices: List<String>,
    private val defaultChoice: String,
    private var onChange: () -> Unit
): Filter() {
    var choice: String = ""
        set(choice) {
            field = choice
            onChange.invoke()
        }

    override fun getType(): FilterTypes = FilterTypes.SINGLE_CHOICE
    override fun getTitle(): String = title
    fun getAllChoices(): List<String> = choices
    fun getDefaultChoice(): String = defaultChoice
}