package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction.Companion.Done
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    onValueChange: (String) -> Unit,
    onClickMenuButton: () -> Unit = {},
    onDone: (KeyboardActionScope) -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Box(
        modifier
            .clip(CircleShape)
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .padding(start = 4.dp, end = 4.dp)
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = onClickMenuButton
                ) {
                    Icon(
                        Icons.Outlined.Menu,
                        contentDescription = stringResource(id = R.string.desc_menu)
                    )
                }
            }
            Box(
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                BasicTextField(
                    modifier = Modifier
                        .padding(start = 2.dp, end = 2.dp, top = 2.dp)
                        .fillMaxWidth()
                        .height(MaterialTheme.typography.bodyLarge.lineHeight.value.dp)
                        .align(alignment = Alignment.Center),
                    value = value,
                    onValueChange = onValueChange,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onDone(this)
                            keyboardController?.hide()
                        }
                    ),
                    maxLines = 1,
                    textStyle = style.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
                    decorationBox = {
                        it()
                        if (value == "") {
                            Text(label, style = style.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                    }
                )
            }
            Box(
                Modifier
                    .fillMaxHeight()
                    .padding(start = 4.dp, end = 4.dp)
            ) {
                Box(Modifier.align(Alignment.Center)) {
                    trailingIcon()
                }
            }
        }
    }
}