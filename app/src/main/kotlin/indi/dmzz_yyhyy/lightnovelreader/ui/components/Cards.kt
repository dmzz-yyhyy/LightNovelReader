package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
fun FilledCard(
    modifier: Modifier = Modifier,
    shape: Shape,
    onClick: () -> Unit = {},
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color,
        ),
        shape = shape,
        modifier = modifier,
        content = content,
        onClick = onClick
    )
}