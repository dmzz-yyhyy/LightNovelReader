package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    colors: ListItemColors = ListItemDefaults.colors(),
    supportingText: String,
    trailingContent: @Composable () -> Unit,
) {
    ListItem(
        modifier = modifier,
        colors = colors,
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        trailingContent = trailingContent
    )
}

@Composable
fun CheckBoxListItem(
    modifier: Modifier = Modifier,
    title: String,
    supportingText: String,
    colors: ListItemColors = ListItemDefaults.colors(
        containerColor = Color.Transparent
    ),
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        modifier = modifier.clickable { onCheckedChange(!checked) },
        title = title,
        colors = colors,
        supportingText = supportingText,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}