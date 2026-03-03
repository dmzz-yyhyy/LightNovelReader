package indi.dmzz_yyhyy.lightnovelreader.utils

import java.util.Locale

object LocaleUtil {
    private fun updateResource(language: String, variant: String) {
        val locale = Locale.Builder()
            .setLanguage(language)
            .setRegion(variant)
            .build()

        Locale.setDefault(locale)
    }

    fun set(language: String, variant: String) =
        updateResource(language, variant)
}
