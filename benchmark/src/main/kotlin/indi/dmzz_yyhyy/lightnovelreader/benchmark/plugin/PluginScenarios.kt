package indi.dmzz_yyhyy.lightnovelreader.benchmark.plugin

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.BenchmarkTestCase
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.PluginFixture
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class PluginScenarios : BenchmarkTestCase() {
    @Test
    fun potatoLibInstallsEnablesDisablesAndShowsSignature() {
        launchApp()
        installPotatoLib()
        openRoot("Settings")
        tapText("Plugins")
        visibleText("PotatoLib")
        visibleTextContaining("1.0")

        tapText("PotatoLib")
        visibleText("About this plugin")
        assertCheckable(checked = false)
        tapText("Enable plugin")
        assertCheckable(checked = true)
        tapScrolledText("Signature")
        visibleText("Signature details")
        tapText("OK")

        pressBack()
        tapText("PotatoLib")
        tapText("Enable plugin")
        assertCheckable(checked = false)
    }

    @Test
    fun potatoLibInspectionCanBeCancelled() {
        launchApp()
        openPotatoLibInstaller()
        visibleText("plugin")
        visibleText("io.nightfish.potatolib")
        visibleTextContaining("1.0")
        tapText("Abort")
        assertForegroundPackage(TARGET_PACKAGE)
    }

    @Test
    fun installedPluginDeletionSupportsBothDecisions() {
        launchApp()
        installPotatoLib()
        openRoot("Settings")
        tapText("Plugins")

        longPressText("PotatoLib")
        visibleText("Signature details")
        tapMenuItemBelow("Signature details")
        visibleText("Delete PotatoLib")
        visibleTextContaining("cannot be undone")
        tapText("Cancel")
        visibleText("PotatoLib")

        longPressText("PotatoLib")
        tapMenuItemBelow("Signature details")
        tapText("Delete")
        visibleText("Deletion complete")
        tapText("OK")
        assertTextNotVisible("PotatoLib")
    }

    private fun installPotatoLib() {
        openPotatoLibInstaller()
        visibleText("io.nightfish.potatolib")
        tapText("Install plugin")
        visibleText("Plugin installed")
        tapText("OK")
    }

    private fun openPotatoLibInstaller() {
        PluginFixture.openInstaller()
        assertForegroundPackage(TARGET_PACKAGE)
    }
}
