package indi.dmzz_yyhyy.lightnovelreader.utils.network

import android.os.Build

object UserAgentGenerator {
    private const val APPLE_WEBKIT_VERSION = "537.36"
    private const val SAFARI_VERSION = "537.36"
    private const val CHROME_MAJOR_VERSION = 153

    private val cachedUserAgent by lazy {
        buildUserAgent(
            androidVersion = Build.VERSION.RELEASE.orEmpty().ifBlank { "10" },
            model = Build.MODEL.orEmpty().ifBlank { "K" },
            chromeMajorVersion = CHROME_MAJOR_VERSION,
        )
    }

    fun generate(): String = cachedUserAgent

    internal fun buildUserAgent(
        androidVersion: String,
        model: String,
        chromeMajorVersion: Int,
    ): String =
        "Mozilla/5.0 (Linux; Android $androidVersion; $model) " +
                "AppleWebKit/$APPLE_WEBKIT_VERSION (KHTML, like Gecko) " +
                "Chrome/$chromeMajorVersion.0.0.0 Mobile Safari/$SAFARI_VERSION"

}
