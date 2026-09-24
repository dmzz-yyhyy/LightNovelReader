package io.nightfish.lightnovelreader.api.content.builder

import android.net.Uri
import io.nightfish.lightnovelreader.api.content.component.data.ImageComponentData

/**
 * 向[ContentBuilder]中添加一个图片组件
 *
 * @param uri 图片的[Uri]
 *
 * @return 当前构建器实例, 支持链式调用
 *
 * @since Api 2
 */
fun ContentBuilder.image(uri: Uri): ContentBuilder = component(ImageComponentData(uri))