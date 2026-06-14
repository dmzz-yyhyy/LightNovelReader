package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import coil3.compose.AsyncImage
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.StorePlugin
import io.nightfish.lightnovelreader.api.ApiCompat
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.pluginStoreInstallBottomSheet() {
    dialog<Route.PluginStoreInstall>(
        dialogProperties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) { entry ->
        val navController = LocalNavController.current
        val route = entry.toRoute<Route.PluginStoreInstall>()
        val viewModel = hiltViewModel<PluginStoreInstallViewModel>()

        LaunchedEffect(route.pluginId) {
            viewModel.load(route.pluginId)
        }

        LaunchedEffect(viewModel) {
            viewModel.navigateToInstall.collect { file ->
                navController.popBackStack()
                navController.navigateToPluginInstallerDialog(file.toUri().toString())
            }
        }

        PluginStoreInstallSheet(
            state = viewModel.state,
            onInstall = { plugin -> viewModel.install(plugin) },
            onDismiss = { navController.popBackStack() }
        )
    }
}

fun NavController.navigateToPluginStoreInstall(pluginId: String) {
    popBackStack<Route.PluginStoreInstall>(inclusive = true, saveState = false)
    navigate(Route.PluginStoreInstall(pluginId))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PluginStoreInstallSheet(
    state: StoreInstallState,
    onInstall: (StorePlugin) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.PartiallyExpanded)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surface,
        tonalElevation = 2.dp,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            when (state) {
                is StoreInstallState.Loading -> LoadingContent()
                is StoreInstallState.Error -> ErrorContent(state.message, onDismiss)
                is StoreInstallState.Ready -> PluginContent(
                    plugin = state.plugin,
                    downloading = false,
                    downloadProgress = 0f,
                    onInstall = { onInstall(state.plugin) },
                    onDismiss = onDismiss
                )
                is StoreInstallState.Downloading -> PluginContent(
                    plugin = state.lastPlugin,
                    downloading = true,
                    downloadProgress = state.progress,
                    onInstall = {},
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String, onDismiss: () -> Unit) {
    Text(text = stringResource(R.string.plugin_store_load_failed), style = typography.titleLarge)
    Spacer(Modifier.height(8.dp))

    Text(stringResource(R.string.plugin_store_load_failed_desc), style = typography.bodyMedium, color = colorScheme.error)
    Spacer(Modifier.height(2.dp))
    Text(message, style = typography.bodyMedium, color = colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) { Text(stringResource(R.string.close)) }
}

@Composable
private fun PluginContent(
    plugin: StorePlugin,
    downloading: Boolean,
    downloadProgress: Float,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    var descriptionExpanded by remember { mutableStateOf(false) }

    val targetApi = plugin.compatibility.targetApi
    val isCompatible = targetApi == null || ApiCompat.isSupported(targetApi)
    val progressAnim by animateFloatAsState(
        targetValue = downloadProgress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "",
    )

    Text(stringResource(R.string.plugin_store_install_title), style = typography.titleMedium)

    Spacer(Modifier.height(16.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (plugin.assets.icon != null) {
            AsyncImage(
                model = plugin.assets.icon.url,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = colorScheme.secondaryContainer
            ) {}
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = plugin.name,
                    style = typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (plugin.isNsfw) {
                    Badge(
                        text = "R-18",
                        containerColor = colorScheme.errorContainer,
                        contentColor = colorScheme.onErrorContainer
                    )
                }
            }
            Text(
                text = plugin.author,
                style = typography.bodyMedium,
                color = colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.plugin_version_prefix, plugin.release.versionName),
                style = typography.bodyMedium,
                color = colorScheme.onSurface
            )
        }
    }

    Spacer(Modifier.height(22.dp))

    val hasDescription = plugin.description.isNotBlank() && plugin.description != plugin.summary
    val text = if (descriptionExpanded && hasDescription) plugin.description
    else plugin.summary

    Text(
        text = text,
        style = typography.bodyMedium,
        color = colorScheme.onSurfaceVariant,
        maxLines = if (descriptionExpanded && hasDescription) Int.MAX_VALUE else 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .then(
                if (descriptionExpanded && hasDescription) {
                    Modifier
                        .heightIn(max = 180.dp)
                        .verticalScroll(rememberScrollState())
                } else {
                    Modifier
                }
            )
    )

    if (hasDescription) {
        Box(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = { descriptionExpanded = !descriptionExpanded },
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Text(
                    text = if (descriptionExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                    style = typography.bodyMedium,
                    color = colorScheme.primary
                )
            }
        }
    } else {
        Spacer(Modifier.height(14.dp))
    }

    val changelog = plugin.changelog
    if (changelog.isNotEmpty()){
        Text(
            text = stringResource(R.string.changelog),
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = colorScheme.surfaceContainerHigh
        ) {
            Text(
                text = changelog,
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .heightIn(max = 180.dp)
                    .verticalScroll(rememberScrollState())
            )
        }
        Spacer(Modifier.height(12.dp))
    }

    AnimatedVisibility(visible = downloading, enter = fadeIn(), exit = fadeOut()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { progressAnim },
                modifier = Modifier.fillMaxWidth(),
                trackColor = colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!downloading) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text(stringResource(R.string.cancel)) }
        }

        val sizeLabel = formatSize(
            plugin.download.sizeBytes
                ?: plugin.download.parts.mapNotNull { it.sizeBytes }.takeIf { it.isNotEmpty() }?.sum()
        )
        val buttonText = when {
            !isCompatible -> stringResource(R.string.plugin_disabled)
            downloading -> stringResource(R.string.plugin_store_download_progress, (downloadProgress * 100).toInt())
            sizeLabel != null -> stringResource(R.string.plugin_store_install_with_size, sizeLabel)
            else -> stringResource(R.string.plugin_install_action_install)
        }

        Button(
            onClick = if (downloading) ({}) else onInstall,
            enabled = !downloading && isCompatible,
            modifier = Modifier
                .weight(if (downloading) 2f else 1f)
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = if (!isCompatible) ButtonDefaults.buttonColors(
                disabledContainerColor = colorScheme.error.copy(alpha = 0.38f),
                disabledContentColor = colorScheme.onError
            ) else ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
        ) {
            Text(buttonText)
        }
    }
}

@Composable
private fun Badge(text: String, containerColor: Color, contentColor: Color) {
    Surface(shape = RoundedCornerShape(4.dp), color = containerColor) {
        Text(
            text = text,
            style = typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

private fun formatSize(bytes: Long?): String? {
    if (bytes == null || bytes <= 0) return null
    return when {
        bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1_024 -> "%.0f KB".format(bytes / 1_024.0)
        else -> "$bytes B"
    }
}
