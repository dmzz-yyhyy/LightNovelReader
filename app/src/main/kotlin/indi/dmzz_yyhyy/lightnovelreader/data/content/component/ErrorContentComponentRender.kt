package indi.dmzz_yyhyy.lightnovelreader.data.content.component

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import indi.dmzz_yyhyy.lightnovelreader.utils.ofId
import io.nightfish.lightnovelreader.api.content.component.ComponentDataJsonElementSerializer
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.content.component.render.AbstractContentComponentRender
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.ui.theme.AppTypography
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.dom4j.DocumentHelper
import org.dom4j.Element

class ErrorContentComponentRender :
    AbstractContentComponentRender<ErrorContentComponentRender, ErrorContentComponentData>() {
    override val id = ErrorContentComponentData.id

    override suspend fun measure(
        orientation: WriteMode,
        context: Context,
        density: Density,
        crossSize: Int,
        data: ErrorContentComponentData
    ): Int {
        require(crossSize >= 0) { "Available cross-axis size must not be negative" }
        if (crossSize == 0) return 0
        val measurer = componentTextMeasurer(context, density)
        val constraints = when (orientation) {
            WriteMode.Horizontal -> Constraints(maxWidth = crossSize)
            WriteMode.VerticalRTL, WriteMode.VerticalLTR -> Constraints(maxHeight = crossSize)
        }
        val sizes = listOf("ERROR", data.message).map {
            measurer.measure(it, style = AppTypography.bodyMedium, constraints = constraints).size
        }
        return when (orientation) {
            WriteMode.Horizontal -> sizes.sumOf { it.height }
            WriteMode.VerticalRTL, WriteMode.VerticalLTR -> sizes.maxOf { it.width }
        }
    }

    @Composable
    override fun Content(
        modifier: Modifier,
        orientation: WriteMode,
        data: ErrorContentComponentData
    ) {
        Column(modifier = modifier) {
            Text("ERROR", style = MaterialTheme.typography.bodyMedium)
            Text(data.message, style = MaterialTheme.typography.bodyMedium)
        }
    }

    companion object {
        fun of(message: String) = ErrorContentComponentData(message)
    }
}

@Serializable
data class ErrorContentComponentData(
    val message: String
) : AbstractContentComponentData() {
    override val id = Companion.id
    override fun toJsonElement(): JsonElement = Json.encodeToJsonElement(this)
    override fun toHtmlElement(context: Context): Element = DocumentHelper.createElement("div")

    companion object {
        val id = "error_content_component".ofId()
        val serializer = object : ComponentDataJsonElementSerializer<ErrorContentComponentData> {
            override fun toJsonElement(data: ErrorContentComponentData): JsonElement =
                Json.encodeToJsonElement(data)

            override fun fromJsonElement(json: JsonElement): ErrorContentComponentData =
                Json.decodeFromJsonElement(json)
        }
    }
}
