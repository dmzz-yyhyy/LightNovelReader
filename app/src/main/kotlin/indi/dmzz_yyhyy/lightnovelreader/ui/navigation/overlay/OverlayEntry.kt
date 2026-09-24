package indi.dmzz_yyhyy.lightnovelreader.ui.navigation.overlay

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.metadata
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope

inline fun <reified T : NavKey> NavEntryScope.overlayEntry(
    noinline content: @Composable (T) -> Unit,
) = entry<T>(
    metadata = metadata {
        put(OverlayMetadataKey, Unit)
    },
    content = content,
)

object OverlayMetadataKey : NavMetadataKey<Unit>