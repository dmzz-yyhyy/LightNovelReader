package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.InfoItem
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.Plugin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginDetailScreen(
    plugin: Plugin?,
    onClickBack: () -> Unit,
    onClickSwitch: (String) -> Unit
) {
    val enterAlwaysScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            TopBar(
                title = plugin?.name ?: "Plugin",
                onClickBack = onClickBack,
                scrollBehavior = enterAlwaysScrollBehavior
            )
        }
    ) { paddingValues ->
        if (plugin == null) {
            EmptyPage(
                modifier = Modifier.padding(paddingValues),
                icon = painterResource(id = R.drawable.help_center_24px),
                title = "无效插件",
                description = "无法获取插件信息，插件可能无效或损坏",
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            item {
                PluginSwitchBlock(
                    plugin = plugin,
                    onClickSwitch = onClickSwitch
                )
            }
            item {
                PluginInfoBlock(
                    plugin = plugin
                )
            }
        }
    }
}

@Composable
private fun PluginSwitchBlock(
    plugin: Plugin,
    onClickSwitch: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = "启用插件",
                style = AppTypography.titleLarge,
                fontWeight = FontWeight.Normal
            )
            Spacer(Modifier
                .weight(1f)
                .height(80.dp))
            Switch(
                checked = plugin.isEnabled,
                onCheckedChange = {
                    onClickSwitch(plugin.id)
                }
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.info_24px),
                contentDescription = "warning"
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("第三方插件", style = AppTypography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text("该插件由第三方提供", style = AppTypography.bodyMedium)
            }
        }
    }
}

@Composable
private fun PluginInfoBlock(
    plugin: Plugin
) {
    val titleStyle = AppTypography.titleMedium.copy(
        color = colorScheme.onSurface,
        fontWeight = FontWeight.W600
    )
    val contentStyle = AppTypography.labelLarge.copy(
        color = colorScheme.secondary
    )

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 10.dp)
    ) {
        Text(
            modifier = Modifier.padding(vertical = 12.dp),
            text = plugin.description,
            style = AppTypography.labelLarge
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))

        Column {
            InfoItem(
                title = "ID",
                content = plugin.id,
                titleStyle = titleStyle,
                contentStyle = contentStyle,
            )
            InfoItem(
                title = "版本",
                content = plugin.versionName + " [${plugin.version}]",
                titleStyle = titleStyle,
                contentStyle = contentStyle,
            )
            InfoItem(
                title = "作者",
                content = plugin.author,
                titleStyle = titleStyle,
                contentStyle = contentStyle,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit
) {
    MediumTopAppBar(
        title = {
            Text(
                text = title,
                style = AppTypography.titleTopBar,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}