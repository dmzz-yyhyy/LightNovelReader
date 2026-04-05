package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginMetadata
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginSource
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.horizontalPadding
import io.nightfish.lightnovelreader.api.ApiCompat
import kotlinx.coroutines.delay

@Composable
fun PluginCard(
    modifier: Modifier = Modifier,
    enabledPluginList: List<String>,
    isErrorDisabled: Boolean,
    pluginInfo: PluginMetadata,
    onClickDetail: (String) -> Unit,
    onClickSwitch: (PluginMetadata) -> Unit,
    onClickDelete: (id: String, uninstall: Boolean) -> Unit,
    onClickKeyAlert: () -> Unit,
    onClickErrorAlert: () -> Unit,
    onClickIncompatibleAlert: () -> Unit,
    onClickCheckUpdate: (String) -> Unit,
    onClickShowSignatures: (String) -> Unit
) {
    val enabled = pluginInfo.packageName in enabledPluginList
    val disabledByError = isErrorDisabled && !enabled
    val disabledByCompatibility = !ApiCompat.isSupported(pluginInfo.apiVersion)

    var switchEnabled by remember { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }

    val containerColor by animateColorAsState(
        targetValue = when {
            disabledByError -> colorScheme.errorContainer.copy(alpha = 0.22f)
            enabled -> colorScheme.surfaceContainerLow
            else -> colorScheme.surfaceContainer
        },
        label = "cardColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = horizontalPadding)
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = { onClickDetail(pluginInfo.packageName) },
                onLongClick = { menuExpanded = true }
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    color = if (enabled) colorScheme.primaryContainer else colorScheme.surfaceContainerHighest,
                    contentColor = if (enabled) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.extension_24px),
                            contentDescription = "plugin"
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pluginInfo.name,
                            style = typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (pluginInfo.source == PluginSource.InstalledApp) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = colorScheme.primaryContainer,
                                contentColor = colorScheme.onPrimaryContainer,
                                shape = RoundedCornerShape(5.dp)
                            ) {
                                Text(
                                    text = "APP",
                                    style = typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = "${pluginInfo.versionName} · ${stringResource(R.string.plugin_by_author, pluginInfo.author)}",
                        style = typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = pluginInfo.description,
                        style = typography.bodyMedium,
                        color = colorScheme.onSurface,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(4.dp))

                Switch(
                    checked = enabled,
                    enabled = !disabledByError && !disabledByCompatibility && switchEnabled,
                    onCheckedChange = {
                        if (!disabledByError && !disabledByCompatibility && switchEnabled) {
                            onClickSwitch(pluginInfo)
                            switchEnabled = false
                        }
                    }
                )

                LaunchedEffect(switchEnabled) {
                    if (!switchEnabled) {
                        delay(900)
                        switchEnabled = true
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (!pluginInfo.hasSignature) {
                    AssistChip(
                        onClick = onClickKeyAlert,
                        label = { Text("签名无效") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.key_off_24px),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = colorScheme.surfaceContainerHighest,
                            labelColor = colorScheme.onSurfaceVariant,
                            leadingIconContentColor = colorScheme.onSurfaceVariant
                        )
                    )
                }

                if (disabledByError) {
                    AssistChip(
                        onClick = onClickErrorAlert,
                        label = { Text("运行错误") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.release_alert_24px),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = colorScheme.errorContainer.copy(alpha = 0.25f),
                            labelColor = colorScheme.onErrorContainer,
                            leadingIconContentColor = colorScheme.onErrorContainer
                        )
                    )
                }

                if (disabledByCompatibility) {
                    AssistChip(
                        onClick = onClickIncompatibleAlert,
                        label = { Text("不兼容") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.release_alert_24px),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = colorScheme.errorContainer.copy(alpha = 0.25f),
                            labelColor = colorScheme.onErrorContainer,
                            leadingIconContentColor = colorScheme.onErrorContainer
                        )
                    )
                }
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.plugin_signature_info_title),
                            style = typography.bodyLarge
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onClickShowSignatures(pluginInfo.packageName)
                    }
                )
                if (pluginInfo.source == PluginSource.LocalPackage) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "删除插件",
                                style = typography.bodyLarge
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onClickDelete(pluginInfo.packageName, false)
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "卸载并删除",
                                style = typography.bodyLarge
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onClickDelete(pluginInfo.packageName, true)
                        }
                    )
                }
            }
        }
    }
}
