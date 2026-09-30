package indi.dmzz_yyhyy.lightnovelreader.utils.network

import android.os.Build
import kotlin.random.Random

object UserAgentGenerator {
    private val cachedUserAgent by lazy {
        val androidVersion = Build.VERSION.RELEASE ?: Random.nextInt(10, 17)
        val model = Build.MODEL ?: "Chromium"
        val appleWebKitVersion = versionOf(537..605, 0..99)
        val chromeVersion = versionOf(100..154, 0..9, 0..9999, 0..299)
        val safariVersion = versionOf(537..605, 0..99)

        "Mozilla/5.0 (Linux; Android $androidVersion; $model) " +
                "AppleWebKit/$appleWebKitVersion (KHTML, like Gecko) " +
                "Chrome/$chromeVersion Mobile Safari/$safariVersion"
    }

    fun generate(): String = cachedUserAgent

    private fun versionOf(vararg parts: IntRange): String =
        parts.joinToString(".") { it.random().toString() }

}
