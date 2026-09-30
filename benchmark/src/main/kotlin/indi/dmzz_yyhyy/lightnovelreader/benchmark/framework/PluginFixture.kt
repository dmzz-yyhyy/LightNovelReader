package indi.dmzz_yyhyy.lightnovelreader.benchmark.framework

import android.content.Intent
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

object PluginFixture {
    private const val AUTHORITY = "indi.dmzz_yyhyy.lightnovelreader.benchmark.files"

    fun openInstaller() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val directory = File(context.cacheDir, "plugin-fixtures").apply { mkdirs() }
        val packageFile = File(directory, "PotatoLib.lnrp")
        context.assets.open("PotatoLib.lnrp").use { input ->
            packageFile.outputStream().use(input::copyTo)
        }
        val uri = FileProvider.getUriForFile(context, AUTHORITY, packageFile)
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                setPackage(BenchmarkTestCase.TARGET_PACKAGE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        )
    }
}
