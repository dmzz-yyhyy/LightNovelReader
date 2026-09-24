package io.nightfish.lightnovelreader.api.text.font

import android.net.Uri
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import io.nightfish.lightnovelreader.api.serializer.FontStyleSerializer
import io.nightfish.lightnovelreader.api.serializer.FontWeightSerializer
import io.nightfish.lightnovelreader.api.serializer.UriSerializer
import kotlinx.serialization.Serializable

@Serializable
data class FontConfig(
    @Serializable(UriSerializer::class)
    val uri: Uri,
    @Serializable(FontWeightSerializer::class)
    val weight: FontWeight = FontWeight.Normal,
    @Serializable(FontStyleSerializer::class)
    val style: FontStyle = FontStyle.Normal,
    val variationSettings: FontVariationConfig? = null
)