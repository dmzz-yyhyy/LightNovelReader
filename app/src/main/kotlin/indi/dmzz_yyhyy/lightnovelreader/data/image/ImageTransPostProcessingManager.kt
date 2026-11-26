package indi.dmzz_yyhyy.lightnovelreader.data.image

import android.net.Uri
import coil3.transform.Transformation
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.image.ImagePostProcessingPipeline
import io.nightfish.lightnovelreader.api.image.ImageTransPostProcessingManagerApi
import io.nightfish.lightnovelreader.api.image.ImageTransformation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageTransPostProcessingManager @Inject constructor() : ImageTransPostProcessingManagerApi {
    private val transformationMap =
        mutableMapOf<Identifier.ImagePipeline, MutableMap<Identifier.ImageTransformation, ImageTransformation>>()

    override fun registerImagePostProcessPipeline(identifier: Identifier.ImagePipeline) {
        transformationMap[identifier] = mutableMapOf()
    }

    override fun registerImageTransformation(
        pipeline: Identifier.ImagePipeline,
        transformationIdentifier: Identifier.ImageTransformation,
        transformation: ImageTransformation
    ) {
        transformationMap[pipeline]?.put(transformationIdentifier, transformation)
    }

    override fun getImageTransformations(pipeline: Identifier.ImagePipeline) =
        transformationMap[pipeline]
        ?.map {
            Pair(it.key, it.value)
        }

    override fun getImageTransformation(
        pipeline: Identifier.ImagePipeline,
        transformation: Identifier.ImageTransformation
    ) =
        transformationMap[pipeline]?.get(transformation)

    override fun unregisterTransformations(
        pipeline: Identifier.ImagePipeline,
        transformation: Identifier.ImageTransformation
    ) {
        transformationMap[pipeline]?.remove(transformation)
    }

    fun getCoil3Transformations(
        pipeline: Identifier.ImagePipeline,
        uri: Uri
    ): List<Transformation> {
        val transformations = getImageTransformations(pipeline) ?: return emptyList()
        if (transformations.isEmpty()) return emptyList()
        return transformations.map {
            Coil3Transformation(it.first, uri, it.second)
        }
    }

    init {
        registerImagePostProcessPipeline(ImagePostProcessingPipeline.imageComponent)
        registerImagePostProcessPipeline(ImagePostProcessingPipeline.bookCover)
    }
}