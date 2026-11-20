package indi.dmzz_yyhyy.lightnovelreader.ui.components

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.image.ImageTransPostProcessingViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.image.imageComponentRequestBuilder
import io.nightfish.lightnovelreader.api.content.component.WriteMode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ZoomableImage(
    imageUri: Uri,
    modifier: Modifier = Modifier,
    onViewImage: () -> Unit,
    placeholderHeight: Dp = 200.dp,
    header: Map<String, String>,
    orientation: WriteMode = WriteMode.Horizontal
) {
    val context = LocalContext.current
    var retryKey by remember { mutableIntStateOf(0) }
    var lastError by remember { mutableStateOf<String?>(null) }
    val imageTransPostProcessingViewModel = hiltViewModel<ImageTransPostProcessingViewModel>()
    val request = remember(imageUri, header) {
        imageComponentRequestBuilder(
            context,
            imageUri,
            header,
            imageTransPostProcessingViewModel.imageTransPostProcessingManager
        ).listener(
            onSuccess = { _, _ -> lastError = null },
            onError = { _, result -> lastError = result.throwable.localizedMessage }
        )
            .build()
    }
    val imageModifier = when (orientation) {
        WriteMode.Horizontal -> Modifier.fillMaxWidth().heightIn(min = placeholderHeight)
        WriteMode.VerticalRTL, WriteMode.VerticalLTR -> Modifier.fillMaxHeight().widthIn(min = placeholderHeight)
    }
    val placeholderModifier = when (orientation) {
        WriteMode.Horizontal -> Modifier.height(placeholderHeight).fillMaxWidth()
        WriteMode.VerticalRTL, WriteMode.VerticalLTR -> Modifier.width(placeholderHeight).fillMaxHeight()
    }
    Box(
        modifier = modifier
            .animateContentSize()
    ) {
        key(retryKey) {
            SubcomposeAsyncImage(
                model = request,
                contentDescription = "",
                contentScale = when (orientation) {
                    WriteMode.Horizontal -> ContentScale.FillWidth
                    WriteMode.VerticalRTL, WriteMode.VerticalLTR -> ContentScale.FillHeight
                },
                modifier = imageModifier.align(Alignment.Center),
                loading = {
                    Box(
                        modifier = placeholderModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Loading()
                    }
                },
                error = {
                    Column(
                        modifier = placeholderModifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            painter = painterResource(R.drawable.release_alert_24px),
                            tint = colorScheme.secondary,
                            contentDescription = null
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("图片加载失败", style = typography.labelLarge)
                        lastError?.let {
                            Text(
                                text = it,
                                style = typography.labelMedium,
                                color = colorScheme.error
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            retryKey++
                            lastError = null
                        }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                },
                success = {
                    SubcomposeAsyncImageContent(
                        modifier = Modifier.pointerInput(onViewImage) {
                            awaitPointerEventScope {
                                val longPressMillis = 380L
                                val twoFingerTapMaxMillis = 280L
                                val slop = viewConfiguration.touchSlop

                                while (true) {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val t0 = down.uptimeMillis

                                    var twoFingerClick = false

                                    val startPos = linkedMapOf(down.id to down.position)
                                    var maxMove = 0f

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val pressedChanges = event.changes.filter { it.pressed }

                                        pressedChanges.forEach { ch ->
                                            if (!startPos.containsKey(ch.id)) startPos[ch.id] =
                                                ch.position
                                            val sp = startPos[ch.id]!!
                                            val dx = ch.position.x - sp.x
                                            val dy = ch.position.y - sp.y
                                            val dist = kotlin.math.hypot(dx, dy)
                                            if (dist > maxMove) maxMove = dist
                                        }

                                        if (!twoFingerClick && pressedChanges.size >= 2) {
                                            twoFingerClick = true
                                        }

                                        /**
                                         * 单指长按且未位移，调用 onViewImage()
                                         * 并消费点击以防止触发阅读器沉浸切换
                                         * */
                                        if (!twoFingerClick) {
                                            val now =
                                                event.changes.firstOrNull { it.id == down.id }?.uptimeMillis
                                                    ?: down.uptimeMillis
                                            val elapsed = now - t0

                                            if (elapsed >= longPressMillis && maxMove <= slop) {
                                                event.changes.forEach { it.consume() }
                                                onViewImage()
                                                break
                                            }

                                        }

                                        /**
                                         * 双指轻点且未位移，调用 onViewImage()
                                         * 并消费点击以防止触发阅读器沉浸切换
                                         * */
                                        if (pressedChanges.isEmpty()) {
                                            if (twoFingerClick) {
                                                val elapsed =
                                                    (event.changes.maxOfOrNull { it.uptimeMillis }
                                                        ?: t0) - t0
                                                if (elapsed <= twoFingerTapMaxMillis && maxMove <= slop) {
                                                    event.changes.forEach { it.consume() }
                                                    onViewImage()
                                                }
                                            }
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            )
        }
    }
}
