package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun FilterDropdown(
    title: String,
    selected: Boolean,
    choices: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    selectedOption: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scrollIndicatorState = scrollState.scrollIndicatorState
    val fadeDelayMillis = 2_200
    var showInitialScrollbar by remember(expanded) { mutableStateOf(expanded) }
    val isScrolling = scrollState.isScrollInProgress
    LaunchedEffect(expanded, isScrolling) {
        if (showInitialScrollbar && !isScrolling) {
            delay(fadeDelayMillis.toLong().milliseconds)
            showInitialScrollbar = false
        }
    }
    val modifier = Modifier.heightIn(max = 500.dp)
    Box {
        FilterChip(
            selected = selected,
            onClick = { expanded = true },
            label = { Text(title) },
            trailingIcon = {
                Icon(
                    painterResource(R.drawable.keyboard_arrow_up_24px),
                    contentDescription = null,
                    modifier = Modifier.rotate(180f)
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = if (scrollIndicatorState != null) {
                modifier.padding(horizontal = 4.dp).nonInteractiveScrollbar(
                    state = scrollIndicatorState,
                    orientation = Orientation.Vertical,
                    isFadeEnabled = !showInitialScrollbar,
                    fadeDelayMillis = fadeDelayMillis,
                    mainAxisTrackInset = 8.dp,
                    crossAxisTrackInset = 4.dp
                ).padding(end = 14.dp)
            } else modifier,
            scrollState = scrollState
        ) {
            choices.forEach { (value, label) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            style = if (selectedOption != null) MaterialTheme.typography.bodyLarge
                            else LocalTextStyle.current
                        )
                    },
                    leadingIcon = if (selectedOption != null) {
                        { RadioButton(selected = selectedOption == value, onClick = null) }
                    } else null,
                    onClick = {
                        expanded = false
                        onSelect(value)
                    }
                )
            }
        }
    }
}
