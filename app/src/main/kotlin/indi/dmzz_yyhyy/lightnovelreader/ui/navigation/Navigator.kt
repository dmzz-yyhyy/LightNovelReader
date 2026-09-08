package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

typealias NavEntryScope = EntryProviderScope<NavKey>

@Stable
class Navigator(
    private val selectedMainIndex: MutableIntState,
    val backStacks: Map<MainDestination, NavBackStack<NavKey>>,
    private val canNavigate: () -> Boolean = { true },
) {
    val mainDestination: MainDestination
        get() = MainDestination.entries[selectedMainIndex.intValue]

    val currentRoute: NavKey
        get() = currentBackStack.last()

    private val currentBackStack: NavBackStack<NavKey>
        get() = checkNotNull(backStacks[mainDestination])

    val isAtMainDestination: Boolean
        get() = currentBackStack.size == 1

    fun navigate(destination: MainDestination) {
        selectedMainIndex.intValue = destination.index
    }

    fun navigate(route: NavKey) {
        if (!canNavigate()) return

        val destination = MainDestination.from(route)
        if (destination != null) {
            navigate(destination)
        } else {
            currentBackStack.add(route)
        }
    }

    fun popBackStack(): Boolean {
        return when {
            currentBackStack.size > 1 -> currentBackStack.removeLastOrNull() != null
            mainDestination != MainDestination.Reading -> {
                selectedMainIndex.intValue = MainDestination.Reading.index
                true
            }
            else -> false
        }
    }

    fun withNavigatePermission(
        canNavigate: () -> Boolean,
    ) = Navigator(selectedMainIndex, backStacks, canNavigate)
}
