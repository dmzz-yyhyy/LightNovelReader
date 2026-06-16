package indi.dmzz_yyhyy.lightnovelreader.data.content.component

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import indi.dmzz_yyhyy.lightnovelreader.utils.ofId
import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponent
import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponentData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.dom4j.DocumentHelper
import org.dom4j.Element

class ErrorContentComponent(data: ErrorContentComponentData) :
    AbstractContentComponent<ErrorContentComponentData>(data) {
    override val id = ErrorContentComponentData.id

    @Composable
    override fun Content(modifier: Modifier) {
        Column {
            Text("ERROR")
            Text(data.message)
        }
    }

    companion object {
        fun of(message: String) = ErrorContentComponent(
            ErrorContentComponentData(message)
        )
    }
}

@Serializable
data class ErrorContentComponentData(
    val message: String
): AbstractContentComponentData() {
    override val id = Companion.id
    override fun toJsonElement(): JsonElement = Json.encodeToJsonElement(this)
    override fun toHtmlElement(context: Context): Element = DocumentHelper.createElement("div")

    companion object {
        val id = "error_content_component".ofId()
    }
}