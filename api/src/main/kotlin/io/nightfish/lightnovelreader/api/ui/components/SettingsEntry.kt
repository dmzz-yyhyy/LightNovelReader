package io.nightfish.lightnovelreader.api.ui.components

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.nightfish.lightnovelreader.api.userdata.BooleanUserData

/**
 * 带开关的设置项控件
 * 通过[BooleanUserData]控制开关状态, 切换时会异步写入用户数据
 *
 * @param modifier Modifier修饰符
 * @param painter 图标
 * @param title 设置项标题
 * @param description 设置项描述文字
 * @param checked 当前开关状态
 * @param booleanUserData 关联的[BooleanUserData]用户数据对象
 * @param disabled 是否禁用此设置项
 *
 * @since Api 2
 */
@Composable
fun SettingsSwitchEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    description: String,
    checked: Boolean,
    booleanUserData: BooleanUserData,
    disabled: Boolean = false
) {
    SettingsSwitchEntry(
        modifier = modifier,
        painter = painter,
        title = title,
        description = description,
        checked = checked,
        onCheckedChange = booleanUserData::asynchronousSet,
        disabled = disabled
    )
}

/**
 * 带开关的设置项控件
 *
 * @param modifier Modifier修饰符
 * @param painter 图标
 * @param title 设置项标题
 * @param description 设置项描述文字
 * @param checked 当前开关状态
 * @param onCheckedChange 开关状态改变时的回调
 * @param disabled 是否禁用此设置项
 *
 * @since Api 2
 */
@Composable
fun SettingsSwitchEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    disabled: Boolean = false
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .then(modifier)
            .fillMaxWidth()
            .clickable(enabled = !disabled) { onCheckedChange(!checked) }
            .padding(horizontal = 22.dp)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        painter?.let {
            Icon(
                modifier = Modifier
                    .padding(end = 22.dp)
                    .size(24.dp),
                painter = it,
                tint = colorScheme.onSurfaceVariant,
                contentDescription = "Icon"
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = colorScheme.onSurface,
                style = typography.headlineSmall
            )
            Text(
                text = description,
                color = colorScheme.onSurfaceVariant,
                style = typography.bodyMedium
            )
        }

        Box(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Switch(
                checked = checked,
                enabled = !disabled,
                onCheckedChange = if (disabled) null else onCheckedChange
            )
        }
    }
}

/**
 * 可点击跳转URL的设置项控件
 *
 * @param modifier Modifier修饰符
 * @param painter 图标
 * @param title 设置项标题
 * @param description 设置项描述文字
 * @param openUrl 点击后用系统浏览器打开的URL
 *
 * @since Api 2
 */
@Composable
fun SettingsClickableEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    description: String,
    openUrl: String
) {
    val context = LocalContext.current
    SettingsClickableEntry(
        modifier = modifier,
        painter = painter,
        title = title,
        description = description,
        onClick = {
            openUrl.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent, null)
            }
        }
    )
}

/**
 * 可点击的设置项控件
 *
 * @param modifier Modifier修饰符
 * @param painter 图标
 * @param title 设置项标题
 * @param option 右侧显示的当前选项值文字(可选)
 * @param trailingContent 右侧自定义Composable内容(可选)
 * @param description 设置项描述文字(可选)
 * @param onClick 点击时的回调
 *
 * @since Api 2
 */
@Composable
fun SettingsClickableEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    option: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    description: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .then(modifier)
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 22.dp)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        painter?.let {
            Icon(
                modifier = Modifier
                    .padding(end = 22.dp)
                    .size(24.dp),
                painter = it,
                tint = colorScheme.onSurfaceVariant,
                contentDescription = "Icon"
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = colorScheme.onSurface,
                style = typography.headlineSmall
            )
            description?.let {
                Text(
                    text = it,
                    color = colorScheme.onSurfaceVariant,
                    style = typography.bodyMedium
                )
            }
            option?.let {
                AnimatedTextLine(
                    text = it,
                    style = typography.bodyMedium,
                    color = colorScheme.primary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        trailingContent?.let { composable ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(55.dp),
                contentAlignment = Alignment.Center
            ) {
                composable()
            }
        }
    }
}
