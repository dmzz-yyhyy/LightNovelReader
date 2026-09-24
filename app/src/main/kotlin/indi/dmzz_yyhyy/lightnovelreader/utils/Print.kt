package indi.dmzz_yyhyy.lightnovelreader.utils

@Deprecated(
    "这是个用于debug的函数，用完记得删！！！",
    ReplaceWith("Log.d()")
)
fun <T> T.debugPrint(tag: String? = null): T =
    this.also { value -> println("${tag?.let { "$it: " } ?: ""}${value.toString()}") }