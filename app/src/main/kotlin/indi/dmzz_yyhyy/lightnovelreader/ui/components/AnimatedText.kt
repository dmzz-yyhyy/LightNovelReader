package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun AnimatedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    var currentText by remember { mutableStateOf(text) }
    SideEffect{ currentText = text }

    Row(modifier = modifier) {
        for (i in text.indices) {
            val char = text[i]

            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    (slideInVertically(initialOffsetY = { it })).togetherWith(
                        slideOutVertically(targetOffsetY = { -it })
                    )
                }, label = ""
            ) {
                Text(
                    text = it.toString(),
                    style = style,
                    color = color,
                    softWrap = false
                )
            }
        }
    }
}
