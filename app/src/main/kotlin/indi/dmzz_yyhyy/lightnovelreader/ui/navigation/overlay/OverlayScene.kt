package indi.dmzz_yyhyy.lightnovelreader.ui.navigation.overlay

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.OverlayScene

internal data class OverlayScene(
    override val key: Any,
    override val entries: List<NavEntry<NavKey>>,
    override val previousEntries: List<NavEntry<NavKey>>,
    override val overlaidEntries: List<NavEntry<NavKey>>,
    override val content: @Composable () -> Unit,
) : OverlayScene<NavKey>
