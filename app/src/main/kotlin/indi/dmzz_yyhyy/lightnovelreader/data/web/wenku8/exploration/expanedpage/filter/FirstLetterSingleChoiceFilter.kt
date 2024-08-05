package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.expanedpage.filter

import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.SingleChoiceFilter

class FirstLetterSingleChoiceFilter(onChange: (String) -> Unit): SingleChoiceFilter(
    title = "首字母",
    dialogTitle = "首字母筛选",
    description = "根据小说标题的拼音首字母筛选。",
    choices = listOf("任意", "0~9", " A ", " B ", " C ", " D ", " E ", " F ", " G ", " H ", " I ", " J ", " K ", " L ", " M ", " N ", " O ", " P ", " Q ", " R ", " S ", " T ", " U ", " V ", " W ", " X ", " Y ", " Z "),
    defaultChoice = "任意",
    onChange = onChange
)