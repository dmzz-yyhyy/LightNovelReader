package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter

open class SingleChoiceFilter(
    private val title: String,
    val dialogTitle: String,
    val description: String,
    private val choices: List<String>,
    private val defaultChoice: String,
    private var onChange: (String) -> Unit
): Filter() {
    var choice: String = defaultChoice
        set(choice) {
            field = choice
            onChange.invoke(choice)
        }

    override fun getType(): FilterTypes = FilterTypes.SINGLE_CHOICE
    override fun getTitle(): String = title
    fun getAllChoices(): List<String> = choices
    fun getDefaultChoice(): String = defaultChoice
}