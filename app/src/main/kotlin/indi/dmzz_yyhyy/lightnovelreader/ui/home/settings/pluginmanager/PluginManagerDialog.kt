package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.utils.ApkSignatureInfo
import indi.dmzz_yyhyy.lightnovelreader.utils.ApkSignatureScheme

@Composable
fun InstallProgressDialog(
    uiState: PluginInstallerDialogUiState,
    onClickClose: () -> Unit,
    onConfirmDecision: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedContent(
                    targetState = uiState.installStep,
                    transitionSpec = {
                        (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                            .togetherWith(fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.8f))
                    },
                ) { step ->
                    when (step) {
                        InstallStepState.AwaitingDecision -> {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = colorScheme.errorContainer,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.info_24px),
                                    contentDescription = null,
                                    tint = colorScheme.onErrorContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        InstallStepState.Completed -> {
                            if (uiState.installCompletedSuccess) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = colorScheme.primaryContainer,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.done_outline_24px),
                                        contentDescription = null,
                                        tint = colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = colorScheme.errorContainer,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.close_24px),
                                        contentDescription = null,
                                        tint = colorScheme.onErrorContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                        InstallStepState.Working -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 3.dp,
                                color = colorScheme.primary,
                                trackColor = colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    val titleText = uiState.installInfo?.name?.takeIf { it.isNotEmpty() }
                        ?: stringResource(R.string.plugin_install_preparing)
                    Text(
                        text = titleText,
                        style = typography.titleLarge,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.installInfo?.let { info ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = info.packageName,
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        if (info.versionName.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.plugin_version_prefix, info.versionName),
                                style = typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (uiState.installStep == InstallStepState.Working) {
                    val progress = uiState.installProgress
                    if (progress != null) {
                        val anim by animateFloatAsState(
                            targetValue = progress.coerceIn(0f, 1f),
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
                        LinearProgressIndicator(
                            progress = { anim },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = colorScheme.primary,
                            trackColor = colorScheme.surfaceVariant,
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = colorScheme.primary,
                            trackColor = colorScheme.surfaceVariant,
                        )
                    }
                }

                val msg = when (uiState.installStep) {
                    InstallStepState.AwaitingDecision ->
                        uiState.installDecision?.message.orEmpty()

                    InstallStepState.Completed -> {
                        val m = uiState.installCompletedMessage
                        if (m.isNotEmpty()) m
                        else if (uiState.installCompletedSuccess) stringResource(R.string.plugin_install_completed)
                        else stringResource(R.string.plugin_install_failed)
                    }

                    InstallStepState.Working ->
                        uiState.installMessage.ifEmpty { stringResource(R.string.plugin_install_preparing) }
                }

                Text(
                    text = msg,
                    style = typography.bodyMedium,
                    color = if (uiState.installStep == InstallStepState.Completed && !uiState.installCompletedSuccess)
                        colorScheme.error
                    else colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            when (uiState.installStep) {
                InstallStepState.Completed -> {
                    FilledTonalButton(onClick = onClickClose) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                }

                InstallStepState.AwaitingDecision -> {
                    val type = uiState.installDecision?.type
                    val label = when (type) {
                        InstallDecisionType.InvalidSignature -> R.string.plugin_install_anyway
                        InstallDecisionType.Reinstall,
                        InstallDecisionType.Downgrade,
                        null -> R.string.next
                    }
                    FilledTonalButton(onClick = { onConfirmDecision(true) }) {
                        Text(text = stringResource(label))
                    }
                }

                InstallStepState.Working -> {
                    FilledTonalButton(onClick = {}, enabled = false) {
                        Text(text = stringResource(R.string.next))
                    }
                }
            }
        },
        dismissButton = {
            val canShowCancel = uiState.installStep != InstallStepState.Completed &&
                    (uiState.installStep == InstallStepState.AwaitingDecision || uiState.installProgress == null)

            if (canShowCancel) {
                OutlinedButton(
                    onClick = {
                        if (uiState.installStep == InstallStepState.AwaitingDecision) onConfirmDecision(false)
                        else onClickClose()
                    }
                ) { Text(text = stringResource(R.string.abort)) }
            }
        }
    )
}

@Composable
fun DeleteProgressDialog(
    uiState: PluginInstallerDialogUiState,
    onClose: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (uiState.uninstallStep == DeleteStepState.Confirming ||
                uiState.uninstallStep == DeleteStepState.Completed) onClose()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedContent(
                    targetState = uiState.uninstallStep,
                    transitionSpec = {
                        (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                            .togetherWith(fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.8f))
                    },
                    label = "delete_icon"
                ) { step ->
                    when (step) {
                        DeleteStepState.Confirming -> {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = colorScheme.errorContainer,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.delete_forever_24px),
                                    contentDescription = null,
                                    tint = colorScheme.onErrorContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        DeleteStepState.Completed -> {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = if (uiState.uninstallCompletedSuccess) colorScheme.primaryContainer
                                        else colorScheme.errorContainer,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (uiState.uninstallCompletedSuccess) R.drawable.done_outline_24px
                                        else R.drawable.close_24px
                                    ),
                                    contentDescription = null,
                                    tint = if (uiState.uninstallCompletedSuccess) colorScheme.onPrimaryContainer
                                    else colorScheme.onErrorContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        DeleteStepState.Working -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 3.dp,
                                color = colorScheme.error,
                                trackColor = colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.plugin_delete_title, uiState.uninstallPluginName),
                        style = typography.titleLarge,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.uninstallStep == DeleteStepState.Working) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = colorScheme.error,
                        trackColor = colorScheme.surfaceVariant,
                    )
                }

                val msg = when (uiState.uninstallStep) {
                    DeleteStepState.Confirming -> uiState.uninstallMessage
                    DeleteStepState.Completed -> uiState.uninstallCompletedMessage
                    DeleteStepState.Working -> uiState.uninstallMessage
                }

                Text(
                    text = msg,
                    style = typography.bodyMedium,
                    color = if (uiState.uninstallStep == DeleteStepState.Completed && !uiState.uninstallCompletedSuccess)
                        colorScheme.error
                    else colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            when (uiState.uninstallStep) {
                DeleteStepState.Confirming -> {
                    FilledTonalButton(onClick = onConfirmDelete) {
                        Text(text = stringResource(R.string.plugin_delete_confirm))
                    }
                }
                DeleteStepState.Completed -> {
                    FilledTonalButton(onClick = onClose) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                }
                DeleteStepState.Working -> {
                    FilledTonalButton(onClick = {}, enabled = false) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                }
            }
        },
        dismissButton = {
            if (uiState.uninstallStep == DeleteStepState.Confirming) {
                OutlinedButton(onClick = onClose) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        }
    )
}

@Composable
fun UpdateCheckDialog(
    uiState: PluginInstallerDialogUiState,
    onClose: () -> Unit,
    onConfirmUpdate: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        icon = {
            AnimatedContent(
                targetState = uiState.updateStep,
                transitionSpec = {
                    (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                        .togetherWith(fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.8f))
                },
                label = "update_icon"
            ) { step ->
                when (step) {
                    UpdateStepState.Latest, UpdateStepState.Completed -> {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(color = colorScheme.primaryContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.done_outline_24px),
                                contentDescription = null,
                                tint = colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    UpdateStepState.Available -> {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(color = colorScheme.tertiaryContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.downloading_24px),
                                contentDescription = null,
                                tint = colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    UpdateStepState.Error -> {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(color = colorScheme.errorContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.close_24px),
                                contentDescription = null,
                                tint = colorScheme.onErrorContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    else -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 3.dp,
                            color = colorScheme.primary,
                            trackColor = colorScheme.surfaceVariant
                        )
                    }
                }
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.plugin_update_check_title),
                    style = typography.headlineSmall,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = uiState.updatePluginName,
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(tween(300)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Download/extract progress bar
                val downloadProgress = uiState.updateDownloadProgress
                AnimatedVisibility(visible = downloadProgress != null) {
                    if (downloadProgress != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val anim by animateFloatAsState(
                                targetValue = downloadProgress.coerceIn(0f, 1f),
                                animationSpec = tween(300, easing = FastOutSlowInEasing),
                                label = "update_progress"
                            )
                            LinearProgressIndicator(
                                progress = { anim },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = colorScheme.primary,
                                trackColor = colorScheme.surfaceVariant,
                            )

                            val percent = if (downloadProgress < 0.75f) {
                                (downloadProgress / 0.75f * 100).toInt().coerceAtMost(100)
                            } else {
                                (((downloadProgress - 0.75f) / 0.25f) * 100).toInt().coerceAtMost(100)
                            }
                            val progressMsg = if (downloadProgress < 0.75f) {
                                stringResource(R.string.plugin_update_downloading_percent, percent)
                            } else {
                                stringResource(R.string.plugin_update_extracting_percent, percent)
                            }
                            Text(
                                text = progressMsg,
                                style = typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Status message (when no progress bar)
                AnimatedVisibility(visible = downloadProgress == null) {
                    val msg = uiState.updateMessage.ifEmpty {
                        when (uiState.updateStep) {
                            UpdateStepState.Checking -> stringResource(R.string.plugin_update_checking)
                            UpdateStepState.Latest -> stringResource(R.string.plugin_update_latest)
                            UpdateStepState.Error -> stringResource(R.string.plugin_error_generic)
                            else -> ""
                        }
                    }
                    Text(
                        text = msg,
                        style = typography.bodyMedium,
                        color = if (uiState.updateStep == UpdateStepState.Error) colorScheme.error
                        else colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val step = uiState.updateStep
                if (uiState.updateDownloadProgress != null) return@AlertDialog

                if (step == UpdateStepState.Available) {
                    FilledTonalButton(onClick = { onConfirmUpdate(uiState.updatePluginId) }) {
                        Text(text = stringResource(R.string.plugin_update_download_install))
                    }
                } else {
                    val enabled = step != UpdateStepState.Checking
                    FilledTonalButton(onClick = onClose, enabled = enabled) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                }
            }
        },
        dismissButton = {
            val step = uiState.updateStep
            if (uiState.updateDownloadProgress != null ||
                step == UpdateStepState.Available ||
                step == UpdateStepState.Checking ||
                step == UpdateStepState.Error
            ) {
                OutlinedButton(onClick = onClose) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        }
    )
}


@Composable
fun PluginNoSignatureDialog(
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = colorScheme.tertiaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.info_24px),
                    contentDescription = null,
                    tint = colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.plugin_signature_about_title),
                style = typography.headlineSmall,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(R.string.plugin_signature_about_body),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Surface(
                    color = colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.plugin_signature_dev_advice_title),
                            style = typography.titleSmall,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.plugin_signature_dev_advice_body),
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = onClose) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    )
}

@Composable
fun PluginErrorDialog(
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = colorScheme.errorContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.close_24px),
                    contentDescription = null,
                    tint = colorScheme.onErrorContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.plugin_disabled_title),
                style = typography.headlineSmall,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = stringResource(R.string.plugin_disabled_body),
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            FilledTonalButton(onClick = onClose) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    )
}

@Composable
fun PluginSignatureDialog(
    signatureInfo: List<ApkSignatureInfo>?,
    onClose: () -> Unit
) {
    val list = signatureInfo.orEmpty()
    val dateFormatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onClose,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = colorScheme.secondaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.key_24px),
                    contentDescription = null,
                    tint = colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.plugin_signature_info_title),
                    style = typography.headlineSmall,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (!list.isEmpty()) {
                    Text(
                        text = stringResource(R.string.plugin_signature_count, list.size),
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        text = {
            if (list.isEmpty()) {
                Text(
                    text = stringResource(R.string.plugin_signature_info_empty),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(list, key = { index, _ -> index }) { index, sig ->
                        Surface(
                            color = colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            style = typography.titleMedium,
                                            color = colorScheme.onSurface
                                        )
                                        Spacer(Modifier.weight(1f))
                                        val schemesText = sig.schemes
                                            .map { it.name }
                                            .sorted()
                                            .joinToString(" · ")
                                            .ifEmpty { ApkSignatureScheme.UNKNOWN.name }
                                        Text(
                                            text = schemesText,
                                            style = typography.labelMedium,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = stringResource(
                                            R.string.plugin_signature_validity,
                                            dateFormatter.format(sig.notBefore),
                                            dateFormatter.format(sig.notAfter)
                                        ),
                                        style = typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }

                                HorizontalDivider(color = colorScheme.outlineVariant)

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = stringResource(R.string.plugin_signature_subject),
                                            style = typography.labelMedium,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        SelectionContainer {
                                            Text(
                                                text = sig.subject,
                                                style = typography.bodyMedium,
                                                color = colorScheme.onSurface,
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = stringResource(R.string.plugin_signature_issuer),
                                            style = typography.labelMedium,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        SelectionContainer {
                                            Text(
                                                text = sig.issuer,
                                                style = typography.bodyMedium,
                                                color = colorScheme.onSurface,
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = stringResource(R.string.plugin_signature_public_key_label),
                                                style = typography.labelMedium,
                                                color = colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = stringResource(
                                                    R.string.plugin_signature_public_key_value,
                                                    sig.publicKeyAlgorithm,
                                                    sig.publicKeyLength
                                                ),
                                                style = typography.bodyMedium,
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = colorScheme.outlineVariant)

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = stringResource(R.string.plugin_signature_sha1_label),
                                            style = typography.labelMedium,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        SelectionContainer {
                                            Text(
                                                text = sig.sha1,
                                                style = typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = onClose) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    )
}
