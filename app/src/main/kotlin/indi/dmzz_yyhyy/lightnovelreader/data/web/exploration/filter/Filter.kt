package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter

abstract class Filter {
    abstract fun getType(): FilterTypes
    abstract fun getTitle(): String
}