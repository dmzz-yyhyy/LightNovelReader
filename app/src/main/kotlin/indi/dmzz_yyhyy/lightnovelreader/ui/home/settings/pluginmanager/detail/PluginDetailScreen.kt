package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginMetadata
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginSource
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SectionHeader
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import kotlinx.coroutines.launch

private enum class PluginDetailTab(
    val titleRes: Int
) {
    Overview(R.string.plugin_detail_tab_overview),
    Settings(R.string.plugin_detail_tab_settings)
}

private data class PluginInfoEntry(
    val label: String,
    val value: String,
    val iconRes: Int,
    val monospace: Boolean = false,
    val onClick: () -> Unit = { }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginDetailScreen(
    isEnabled: Boolean,
    pluginInfo: PluginMetadata?,
    onClickBack: () -> Unit,
    onClickSwitch: (PluginMetadata) -> Unit,
    onClickSignature: () -> Unit,
    pluginContent: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val pagerState = rememberPagerState { PluginDetailTab.entries.size }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopBar(
                pluginInfo = pluginInfo,
                onClickBack = onClickBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        if (pluginInfo == null) {
            EmptyPage(
                modifier = Modifier.padding(paddingValues),
                icon = painterResource(id = R.drawable.help_center_24px),
                title = stringResource(R.string.plugin_invalid_title),
                description = stringResource(R.string.plugin_invalid_desc),
            )
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            PluginTabs(
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->

                when (PluginDetailTab.entries[page]) {
                    PluginDetailTab.Overview -> PluginOverviewPage(
                        isEnabled = isEnabled,
                        pluginInfo = pluginInfo,
                        onClickSwitch = onClickSwitch,
                        onClickSignature = onClickSignature
                    )

                    PluginDetailTab.Settings -> PluginSettingsPage(
                        isEnabled = isEnabled,
                        pluginContent = pluginContent
                    )
                }
            }
        }
    }
}

@Composable
private fun PluginTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = PluginDetailTab.entries

    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        divider = {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )
        },
        indicator = {
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(
                        selectedTabIndex = selectedTabIndex,
                        matchContentSize = true
                    )
                    .height(4.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 3.dp,
                            topEnd = 3.dp
                        )
                    )
                    .background(MaterialTheme.colorScheme.primary)
            )
        },
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = stringResource(tab.titleRes),
                        maxLines = 1
                    )
                }
            )
        }
    }
}

@Composable
private fun PluginOverviewPage(
    isEnabled: Boolean,
    pluginInfo: PluginMetadata,
    onClickSwitch: (PluginMetadata) -> Unit,
    onClickSignature: () -> Unit
) {
    val infoEntries = listOf(
        PluginInfoEntry(
            label = stringResource(R.string.plugin_info_id_label),
            value = pluginInfo.packageName,
            iconRes = R.drawable.code_24px,
            monospace = true
        ),
        PluginInfoEntry(
            label = stringResource(R.string.plugin_info_version_label),
            value = "${pluginInfo.versionName} [${pluginInfo.version}]",
            iconRes = R.drawable.deployed_code_update_24px,
            monospace = true
        ),
        PluginInfoEntry(
            label = stringResource(R.string.plugin_info_author_label),
            value = pluginInfo.author,
            iconRes = R.drawable.person_edit_24px
        ),
        PluginInfoEntry(
            label = stringResource(R.string.plugin_detail_api_version),
            value = pluginInfo.apiVersion.toString(),
            iconRes = R.drawable.extension_24px,
            monospace = true
        ),
        PluginInfoEntry(
            label = stringResource(R.string.plugin_detail_signature_label),
            value = stringResource(
                if (pluginInfo.hasSignature) R.string.enabled else R.string.not_applicable
            ),
            iconRes = R.drawable.deployed_code_24px,
            onClick = { onClickSignature() }
        ),
        PluginInfoEntry(
            label = stringResource(R.string.plugin_detail_source_label),
            value = stringResource(
                when (pluginInfo.source) {
                    PluginSource.InstalledApp -> R.string.plugin_detail_source_app
                    PluginSource.LocalPackage -> R.string.plugin_detail_source_local
                }
            ),
            iconRes = R.drawable.info_24px
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            PluginHeroCard(
                enabled = isEnabled,
                onClickSwitch = {
                    onClickSwitch(pluginInfo)
                }
            )
        }
        item {
            PluginTrustNotice()
        }
        item {
            PluginDescriptionCard(description = pluginInfo.description)
        }
        item {
            PluginInfoCard(infoEntries = infoEntries)
        }
        navigationBarSpacer()
    }
}

@Composable
private fun PluginHeroCard(
    enabled: Boolean,
    onClickSwitch: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(onClick = onClickSwitch),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.plugin_enable_label),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            Switch(
                checked = enabled,
                onCheckedChange = { onClickSwitch() }
            )
        }
    }
}

@Composable
private fun PluginTrustNotice() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.info_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.plugin_third_party_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = stringResource(R.string.plugin_third_party_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun PluginDescriptionCard(description: String) {
    SectionHeader(
        text = stringResource(R.string.plugin_detail_summary_title),
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp)

    )
    Text(
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp),
        text = description,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun PluginInfoCard(
    infoEntries: List<PluginInfoEntry>
) {
    SectionHeader(
        text = stringResource(R.string.plugin_detail_info_title),
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp)
    )
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        infoEntries.forEachIndexed { index, item ->
            PluginInfoItem(
                title = item.label,
                content = item.value,
                icon = painterResource(item.iconRes),
                monospace = item.monospace,
                onClick = item.onClick
            )
            if (index != infoEntries.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 64.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                )
            }
        }
    }
}

@Composable
private fun PluginInfoItem(
    title: String,
    content: String,
    icon: Painter,
    monospace: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val copiedText = stringResource(R.string.copied)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    coroutineScope.launch {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(title, content)))
                        Toast.makeText(context, copiedText, Toast.LENGTH_SHORT).show()
                    }
                }
            )
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Spacer(Modifier.width(4.dp))
        Surface(
            modifier = Modifier.size(28.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = content,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default
            )
        }
    }
}

@Composable
private fun PluginSettingsPage(
    isEnabled: Boolean,
    pluginContent: @Composable (PaddingValues) -> Unit
) {
    if (isEnabled) {
        Box(modifier = Modifier.fillMaxSize()) {
            pluginContent(PaddingValues(horizontal = 16.dp, vertical = 8.dp))
        }
    } else {
        EmptyPage(
            modifier = Modifier.fillMaxSize(),
            icon = painterResource(id = R.drawable.toggle_off_24px),
            title = stringResource(R.string.plugin_disabled),
            description = stringResource(R.string.plugin_disabled_desc),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    pluginInfo: PluginMetadata?,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    onClickBack: () -> Unit
) {
    val context = LocalContext.current
    TopAppBar(
        title = {
            Column {
                Text(
                    text = pluginInfo?.name ?: stringResource(R.string.plugin_invalid_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                pluginInfo?.let {
                    Text(
                        text = it.packageName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
        actions = {
            if (pluginInfo?.source == PluginSource.InstalledApp) {
                IconButton(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", pluginInfo.packageName, null)
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.open_in_new_24px),
                        contentDescription = null
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}
