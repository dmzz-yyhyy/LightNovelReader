package indi.dmzz_yyhyy.lightnovelreader.data.plugin.injector

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginInjectorProvider @Inject constructor() {
    var value: PluginInjector? = null
        private set

    fun setPluginInjector(injector: PluginInjector) {
        value = injector
    }
}