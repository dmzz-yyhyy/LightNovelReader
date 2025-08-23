package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import android.content.ClipData
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BookInfoBottomSheet(
    bookInformation: BookInformation,
    bookVolumes: BookVolumes,
    sheetState: SheetState,
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    @Composable
    fun InfoItem(
        title: String? = "",
        content: String,
        titleStyle: TextStyle,
        contentStyle: TextStyle,
        icon: Painter? = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.weight(3f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                icon?.let {
                    Icon(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title!!,
                    style = titleStyle
                )
            }

            Row(
                modifier = Modifier.weight(7f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = LocalContext.current
                val clipboard = LocalClipboard.current

                Text(
                    text = content,
                    style = contentStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                coroutineScope.launch {
                                    val clipData = ClipData.newPlainText("content", content)
                                    val clipEntry = ClipEntry(clipData = clipData)
                                    clipboard.setClipEntry(clipEntry = clipEntry)
                                    Toast.makeText(context, "内容已复制", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            },
                        )
                )
            }
        }
    }

    AnimatedVisibility(visible = isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState
        ) {
            val titleStyle = AppTypography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.W600
            )
            val contentStyle = AppTypography.labelLarge.copy(
                color = MaterialTheme.colorScheme.secondary
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                InfoItem(
                    title = stringResource(R.string.detail_info_title),
                    content = bookInformation.title,
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                    icon = painterResource(R.drawable.title_24px)
                )

                if (bookInformation.subtitle.isNotEmpty()) {
                    InfoItem(
                        content = bookInformation.subtitle,
                        titleStyle = titleStyle,
                        contentStyle = contentStyle,
                    )
                }

                InfoItem(
                    title = stringResource(R.string.detail_info_id),
                    content = bookInformation.id.toString(),
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                    icon = painterResource(R.drawable.info_24px)
                )

                InfoItem(
                    title = stringResource(R.string.detail_info_author),
                    content = bookInformation.author,
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                    icon = painterResource(R.drawable.person_edit_24px)
                )

                InfoItem(
                    title = stringResource(R.string.detail_info_publishing_house),
                    content = bookInformation.publishingHouse,
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                    icon = painterResource(R.drawable.text_snippet_24px)
                )

                val dateFormat = "yyyy-MM-dd"
                val formatter = DateTimeFormatter.ofPattern(dateFormat)
                InfoItem(
                    title = stringResource(R.string.detail_info_updated_on),
                    content = bookInformation.lastUpdated.format(formatter) + "\n" + if (bookInformation.isComplete) "完结" else "连载中",
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                    icon = painterResource(R.drawable.autorenew_24px)
                )

                InfoItem(
                    title = stringResource(R.string.detail_info_tags),
                    content = bookInformation.tags.joinToString(separator = "，"),
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                    icon = painterResource(R.drawable.tag_24px)
                )

                InfoItem(
                    title = stringResource(R.string.detail_info_stats),
                    content = "${
                        NumberFormat.getInstance().format(bookInformation.wordCount)
                    } 字\n共计 ${bookVolumes.volumes.count()} 卷, ${bookVolumes.volumes.sumOf { volume -> volume.chapters.size }} 章节",
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                    icon = painterResource(R.drawable.text_fields_24px)
                )
            }
        }
    }
}