package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

val mock = listOf(
    Plugin(isEnabled = true, isUpdatable = false, "io.yukonisen.ehentai4lnr", "E-Hentai Extension", 721, "0721", "yukonisen", "E-Hentai is a extension plugin, designed to provide another source to LightNovelReader. It gives ability to see Hentai online and let you 0721 to our favorite Nightfish ('s app).", "https://github.com/yukonisen/lnr-eh-extension", null),
    Plugin(isEnabled = true, isUpdatable = true, "io.yukonisen.18comic4lnr", "18comic Extension", 1, "1.0.0", "yukonisen", "免費****線上看.", "https://github.com/yukonisen/lnr-18comic-extension", null),
    Plugin(isEnabled = false, isUpdatable = false, "io.yukonisen.exampleplugin", "Test Plugin", 1, "1.0.0", "yukonisen", "test plugin", null, null)
)

@HiltViewModel
class PluginManagerViewModel @Inject constructor(

): ViewModel() {
    val pluginList = mock

    fun onClickEnabledSwitch(id: String) {

    }
}