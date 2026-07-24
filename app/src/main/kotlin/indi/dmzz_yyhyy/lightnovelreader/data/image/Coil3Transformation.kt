package indi.dmzz_yyhyy.lightnovelreader.data.image

import android.graphics.Bitmap
import android.net.Uri
import coil3.size.Dimension
import coil3.size.Size
import coil3.transform.Transformation
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.image.ImageSize
import io.nightfish.lightnovelreader.api.image.ImageTransformation

class Coil3Transformation(
    val id: Identifier,
    val uri: Uri,
    val transformation: ImageTransformation
): Transformation() {
    override val cacheKey: String get() = transformation.getCacheKey(uri = uri)

    override suspend fun transform(
        input: Bitmap,
        size: Size
    ): Bitmap = transformation.transform(
        input,
        ImageSize(
            size.width.pxOrNull(),
            size.height.pxOrNull(),
        ),
        uri,
    )

    private fun Dimension.pxOrNull(): Int? {
        return if (this is Dimension.Pixels) px else null
    }
}