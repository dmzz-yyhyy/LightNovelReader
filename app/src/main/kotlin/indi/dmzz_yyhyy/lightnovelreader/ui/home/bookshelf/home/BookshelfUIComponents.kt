package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.get
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.utils.dateFormatter
import indi.dmzz_yyhyy.lightnovelreader.utils.withHaptic
import io.nightfish.lightnovelreader.api.book.BookInformation

@Composable
fun BookCardContent(
    modifier: Modifier,
    selected: Boolean,
    collected: Boolean,
    bookInformation: BookInformation,
    latestChapterTitle: String? = null,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    titleHeight: Dp?
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(146.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = withHaptic { onLongPress() },
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(94.dp, 144.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val alpha by animateFloatAsState(if (selected) 0.7f else 1f)

                Box(
                    modifier = Modifier.graphicsLayer(alpha = alpha)
                ) {
                    Cover(
                        width = 94.dp,
                        height = 144.dp,
                        uri = bookInformation.coverUri,
                        rounded = 8.dp
                    )

                    if (!latestChapterTitle.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Badge(
                                containerColor = colorScheme.error,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    if (collected) {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .align(Alignment.TopStart)
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colorScheme.secondaryContainer)
                        ) {
                            Icon(
                                modifier = Modifier.scale(0.75f, 0.75f),
                                painter = painterResource(R.drawable.filled_bookmark_24px),
                                contentDescription = "collected_indicator",
                                tint = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (selected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val color = colorScheme.primary
                        Canvas(
                            modifier = Modifier.size(36.dp)
                        ) {
                            drawCircle(
                                color = color,
                                radius = 18.dp.toPx(),
                            )
                        }
                        Icon(
                            modifier = Modifier.size(22.dp),
                            painter = painterResource(R.drawable.check_24px),
                            tint = colorScheme.onPrimary,
                            contentDescription = null
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = if (titleHeight != null) Modifier
                        .height(titleHeight)
                        .wrapContentHeight(Alignment.CenterVertically)
                    else Modifier,
                    text = bookInformation.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.titleMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bookInformation.author,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = typography.bodyMedium.copy(
                            fontWeight = FontWeight.W600,
                            color = colorScheme.primary
                        )
                    )
                    BookStatusIcon(bookInformation.isComplete)
                }

                val dateText = bookInformation.lastUpdated.format(dateFormatter())

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TagChip(painterResource(R.drawable.update_24px))
                    Text(
                        text = dateText,
                        style = typography.labelMedium.copy(color = colorScheme.secondary),
                        maxLines = 1,
                        overflow = TextOverflow.StartEllipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(Modifier.width(2.dp))

                    TagChip(painterResource(R.drawable.article_24px))
                    Text(
                        text = bookInformation.wordCount.get(),
                        style = typography.labelMedium.copy(color = colorScheme.secondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }



                if (latestChapterTitle.isNullOrBlank()) {
                    Text(
                        text = bookInformation.description.trim(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = typography.bodyMedium.copy(
                            color = colorScheme.secondary
                        )
                    )
                } else {
                    Text(
                        text = stringResource(R.string.book_updated_to_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = typography.bodyMedium
                    )
                    Text(
                        text = latestChapterTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = typography.bodyMedium.copy(
                            color = colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TagChip(
    painter: Painter
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(colorScheme.surfaceContainer)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Icon(
            modifier = Modifier.size(15.dp),
            painter = painter,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BookCardContentSkeleton(modifier: Modifier = Modifier) {
    val skeletonColor = colorScheme.surfaceContainerLow
    val skeletonRoundedCorner = RoundedCornerShape(4.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(146.dp)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(94.dp, 144.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(skeletonColor)
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(40.dp)
                    .clip(skeletonRoundedCorner)
                    .background(skeletonColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.43f)
                    .height(20.dp)
                    .clip(skeletonRoundedCorner)
                    .background(skeletonColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(skeletonRoundedCorner)
                    .background(skeletonColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(skeletonRoundedCorner)
                    .background(skeletonColor)
            )
        }
    }
}

@Composable
fun BookStatusIcon(isComplete: Boolean) {
    val painter: Painter = if (isComplete)
        painterResource(R.drawable.done_all_24px)
    else painterResource(R.drawable.hourglass_top_24px)
    Icon(
        modifier = Modifier.size(16.dp),
        painter = painter,
        contentDescription = null,
        tint = colorScheme.outline
    )
}
