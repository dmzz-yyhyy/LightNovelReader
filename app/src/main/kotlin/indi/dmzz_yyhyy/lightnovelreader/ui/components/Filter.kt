package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.Filter
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.SingleChoiceFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.SwitchFilter

@Composable
fun Filter.Component(dialog: (@Composable () -> Unit) -> Unit) {
    when (this) {
        is SwitchFilter -> {
            var enable by remember { mutableStateOf(this.enable) }
            BaseFilter(
                title = this.getTitle(),
                selected = enable,
                onClick = {
                    enable = !enable
                    this.enable = enable
                }
            )
        }
        is SingleChoiceFilter -> {
            var enable by remember { mutableStateOf(this.choice == this.getDefaultChoice()) }
            BaseFilter(
                title = "${this.getTitle()}: ${this.choice}",
                selected = enable,
                onClick = {
                    dialog {

                    }
                }
            )
        }
    }
}

@Composable
fun BaseFilter(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            AnimatedContent(
                targetState = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "FilterTitleColorAnime",
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp,
                    color = it
                )
            }
        },
        modifier = modifier,
        leadingIcon = {
            AnimatedVisibility(
                visible = selected,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.check_24px),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    contentDescription = null
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseDialog(
    enable: Boolean,
    icon: Painter,
    title: String,
    description: String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        visible = enable,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
            BasicAlertDialog(
                onDismissRequest = onDismissRequest,
            ) {
                Column {
                    Column(
                        modifier = Modifier.weight(2f).padding(top = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            painter = icon,
                            tint = MaterialTheme.colorScheme.secondary,
                            contentDescription = null
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.W400,
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.W400,
                        )
                        content.invoke(this)
                    }
                    Box(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(8.dp, 24.dp, 24.dp, 24.dp)
                                .align(Alignment.CenterEnd),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                Modifier
                                    .padding(12.dp, 10.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onDismissRequest
                                    ),
                            ) {
                                Text(
                                    text = "取消",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Box(
                                Modifier
                                    .padding(12.dp, 10.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onConfirmation
                                    ),
                            ) {
                                Text(
                                    text = "设定",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipsDialog(
    selected: String,
    title: String,
    description: String,
    onSelectedChange: (String) -> Unit,
) {
    var enable by remember { mutableStateOf(false) }
    BaseDialog(
        enable = enable,
        icon = painterResource(R.drawable.text_fields_24px),
        title = title,
        description = description,
        onDismissRequest = { enable = false },
        onConfirmation = { enable = false },
    ) {
    }
}