package indi.dmzz_yyhyy.lightnovelreader.data.content.component

import android.graphics.Rect as AndroidRect
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalView
import kotlin.math.ceil
import kotlin.math.floor

internal class VerticalTextSelection {
    var owner by mutableStateOf<Any?>(null)

    fun clear(): Boolean {
        if (owner == null) return false
        owner = null
        return true
    }
}

internal val LocalVerticalTextSelection = compositionLocalOf<VerticalTextSelection?> { null }

@Composable
internal fun VerticalSelectionMenu(
    anchorInWindow: Rect,
    canSelectAll: Boolean,
    copy: () -> Unit,
    selectAll: () -> Unit,
    dismiss: () -> Unit,
) {
    val view = LocalView.current
    val anchor by rememberUpdatedState(anchorInWindow)
    val showSelectAll by rememberUpdatedState(canSelectAll)
    val onCopy by rememberUpdatedState(copy)
    val onSelectAll by rememberUpdatedState(selectAll)
    val onDismiss by rememberUpdatedState(dismiss)
    var mode by remember(view) { mutableStateOf<ActionMode?>(null) }

    DisposableEffect(view) {
        var disposing = false
        val actionMode = view.startActionMode(object : ActionMode.Callback2() {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                menu.add(Menu.NONE, android.R.id.copy, Menu.NONE, android.R.string.copy)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                menu.add(Menu.NONE, android.R.id.selectAll, Menu.NONE, android.R.string.selectAll).apply {
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                    isVisible = showSelectAll
                }
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                val item = menu.findItem(android.R.id.selectAll)
                if (item.isVisible == showSelectAll) return false
                item.isVisible = showSelectAll
                return true
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = when (item.itemId) {
                android.R.id.copy -> { onCopy(); true }
                android.R.id.selectAll -> { onSelectAll(); true }
                else -> false
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                if (!disposing) onDismiss()
            }

            override fun onGetContentRect(mode: ActionMode, view: View, outRect: AndroidRect) {
                val location = IntArray(2)
                view.getLocationInWindow(location)
                outRect.set(
                    floor(anchor.left).toInt() - location[0],
                    floor(anchor.top).toInt() - location[1],
                    ceil(anchor.right).toInt() - location[0],
                    ceil(anchor.bottom).toInt() - location[1],
                )
            }
        }, ActionMode.TYPE_FLOATING)
        mode = actionMode
        onDispose {
            disposing = true
            actionMode?.finish()
        }
    }
    SideEffect {
        mode?.invalidate()
        mode?.invalidateContentRect()
    }
}
