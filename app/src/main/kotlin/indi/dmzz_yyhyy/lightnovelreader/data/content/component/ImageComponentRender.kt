package indi.dmzz_yyhyy.lightnovelreader.data.content.component

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.data.image.ImageDimensions
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToImageViewerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ZoomableImage
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.content.component.render.AbstractContentComponentRender
import io.nightfish.lightnovelreader.api.content.component.data.ImageComponentData
import io.nightfish.lightnovelreader.api.image.ImageTransPostProcessingManagerApi
import io.nightfish.lightnovelreader.api.image.ImagePostProcessingPipeline
import io.nightfish.lightnovelreader.api.image.ImageSize
import io.nightfish.lightnovelreader.api.web.WebBookDataSourceManagerApi
import kotlin.math.roundToInt

class ImageComponentRender(
    private val webBookDataSourceManagerApi: WebBookDataSourceManagerApi,
    private val imageTransPostProcessingManagerApi: ImageTransPostProcessingManagerApi
) : AbstractContentComponentRender<ImageComponentRender, ImageComponentData>() {
    override val id = ImageComponentData.id

    override suspend fun measure(
        orientation: WriteMode,
        context: Context,
        density: Density,
        crossSize: Int,
        data: ImageComponentData
    ): Int {
        require(crossSize >= 0) { "Available cross-axis size must not be negative" }
        if (crossSize == 0) return 0
        val placeholderSize = with(density) { 200.dp.roundToPx() }
        val dimensions = ImageDimensions.read(
            context,
            data.uri,
            webBookDataSourceManagerApi.getWebDataSource().imageHeader
        ) ?: return placeholderSize
        val targetSize = when (orientation) {
            WriteMode.Horizontal -> ImageSize(crossSize, null)
            WriteMode.VerticalLTR, WriteMode.VerticalRTL -> ImageSize(null, crossSize)
        }
        val measured = imageTransPostProcessingManagerApi
            .getImageTransformations(ImagePostProcessingPipeline.imageComponent)
            .orEmpty()
            .fold(ImageSize(dimensions.width, dimensions.height)) { input, (_, transformation) ->
                transformation.measure(input, targetSize, data.uri)
            }
        val width = measured.width?.takeIf { it > 0 } ?: return placeholderSize
        val height = measured.height?.takeIf { it > 0 } ?: return placeholderSize
        val mainSize = when (orientation) {
            WriteMode.Horizontal -> crossSize.toDouble() * height / width
            WriteMode.VerticalLTR, WriteMode.VerticalRTL -> crossSize.toDouble() * width / height
        }
        return mainSize
            .roundToInt()
            .coerceAtLeast(placeholderSize)
    }

    @Composable
    override fun Content(
        modifier: Modifier,
        orientation: WriteMode,
        data: ImageComponentData
    ) {
        val imageHeader =
            remember(webBookDataSourceManagerApi.getWebDataSource()) { webBookDataSourceManagerApi.getWebDataSource().imageHeader }
        val navigator = LocalNavigator.current
        ZoomableImage(
            imageUri = data.uri,
            modifier = when (orientation) {
                WriteMode.Horizontal -> modifier.fillMaxWidth()
                WriteMode.VerticalLTR, WriteMode.VerticalRTL -> modifier.fillMaxHeight()
            },
            orientation = orientation,
            onViewImage = {
                navigator.navigateToImageViewerDialog(data.uri)
            },
            header = imageHeader
        )
    }
}
