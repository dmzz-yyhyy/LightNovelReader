package indi.dmzz_yyhyy.lightnovelreader.ui.navigation.overlay

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.get
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

class OverlaySceneStrategy : SceneStrategy<NavKey> {

    override fun SceneStrategyScope<NavKey>.calculateScene(
        entries: List<NavEntry<NavKey>>,
    ): Scene<NavKey>? {
        val entry = entries.lastOrNull() ?: return null
        entry.metadata[OverlayMetadataKey] ?: return null

        return OverlayScene(
            key = entry.contentKey,
            entries = listOf(entry),
            previousEntries = entries.dropLast(1),
            overlaidEntries = entries.dropLast(1),
            content = { entry.Content() },
        )
    }
}
