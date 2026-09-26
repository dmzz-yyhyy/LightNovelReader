package indi.dmzz_yyhyy.lightnovelreader.benchmark.performance

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import indi.dmzz_yyhyy.lightnovelreader.benchmark.framework.BenchmarkTestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class StartupScenarios {
    @get:Rule
    val benchmark = MacrobenchmarkRule()

    @Test
    fun coldStartup() = measure(StartupMode.COLD)

    @Test
    fun warmStartup() = measure(StartupMode.WARM)

    private fun measure(mode: StartupMode) {
        benchmark.measureRepeated(
            packageName = BenchmarkTestCase.TARGET_PACKAGE,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            startupMode = mode,
            iterations = 5,
            setupBlock = { pressHome() },
        ) {
            startActivityAndWait()
        }
    }
}
