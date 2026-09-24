package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R

@Composable
fun LnrSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = false,
    shape: Shape = SnackbarDefaults.shape,
    containerColor: Color = SnackbarDefaults.color,
    contentColor: Color = SnackbarDefaults.contentColor,
    actionColor: Color = SnackbarDefaults.actionColor,
    actionContentColor: Color = SnackbarDefaults.actionContentColor,
    dismissActionContentColor: Color = SnackbarDefaults.dismissActionContentColor,
) {
    val actionLabel = snackbarData.visuals.actionLabel
    val actionComposable: (@Composable () -> Unit)? =
        if (actionLabel != null) {
            @Composable {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(contentColor = actionColor),
                    onClick = { snackbarData.performAction() },
                    content = { Text(actionLabel) }
                )
            }
        } else {
            null
        }
    val dismissActionComposable: (@Composable () -> Unit)? =
        if (snackbarData.visuals.withDismissAction) {
            @Composable {
                IconButton(
                    onClick = { snackbarData.dismiss() },
                    content = {
                        Icon(
                            painterResource(R.drawable.close_24px),
                            contentDescription = "dismiss",
                        )
                    }
                )
            }
        } else {
            null
        }
    Snackbar(
        modifier = modifier.padding(12.dp),
        action = actionComposable,
        dismissAction = dismissActionComposable,
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        actionContentColor = actionContentColor,
        dismissActionContentColor = dismissActionContentColor,
        content = {
            Box {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 12.dp),
                    text = snackbarData.visuals.message,
                    style = typography.bodyMedium
                )
            }
        }
    )
}
