package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter

interface Filter {
    fun getType(): FilterTypes
    fun getTitle(): String
}