package indi.dmzz_yyhyy.lightnovelreader.utils

import cxhttp.CxHttpHelper
import indi.dmzz_yyhyy.lightnovelreader.utils.network.KotlinSerializationCborConverter
import kotlinx.coroutines.MainScope

object CxHttpInit {
    private var isInit = false

    @Suppress("OPT_IN_USAGE")
    fun init() {
        if (isInit) return
        CxHttpHelper.init(scope=MainScope(), debugLog=false, converter = KotlinSerializationCborConverter())
        isInit = true
    }
}